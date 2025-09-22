import { SESClient, SendEmailCommand } from "@aws-sdk/client-ses";
import { SQSClient, SendMessageCommand } from "@aws-sdk/client-sqs";

const ses = new SESClient({ region: "us-east-2" });
const sqs = new SQSClient({ region: "us-east-2" });

const RESULT_QUEUE_URL = process.env.RESULT_QUEUE_URL;
const SENDER_EMAIL = process.env.SENDER_EMAIL;

function normalizeAnnualToMonthlyRate(rateRaw) {
  if (rateRaw == null || isNaN(rateRaw)) return 0;
  let annual = Number(rateRaw);
  if (annual > 1) annual = annual / 100;
  return annual / 12;
}

function monthlyInstallment(P, monthlyRate, n) {
  P = Number(P);
  n = Number(n);
  if (n <= 0) return 0;
  if (!monthlyRate || monthlyRate === 0) return P / n;
  const factor = Math.pow(1 + monthlyRate, n);
  return (P * monthlyRate * factor) / (factor - 1);
}

function generateAmortizationPlan(P, monthlyRate, n) {
  const installment = monthlyInstallment(P, monthlyRate, n);
  let balance = Number(P);
  const plan = [];
  for (let m = 1; m <= n; m++) {
    const interest = balance * monthlyRate;
    const capital = installment - interest;
    balance -= capital;
    plan.push({
      month: m,
      installment: Number(installment.toFixed(2)),
      capital: Number(capital.toFixed(2)),
      interest: Number(interest.toFixed(2)),
      remainingBalance: Number(Math.max(balance, 0).toFixed(2)),
    });
  }
  return { installment: Number(installment.toFixed(2)), plan };
}

async function sendToResultQueue(traceId, payload) {
  if (!RESULT_QUEUE_URL) {
    console.warn(`[${traceId}] RESULT_QUEUE_URL not set — skipping queue send`);
    return { ok: false, reason: "NO_RESULT_QUEUE" };
  }
  const cmd = new SendMessageCommand({
    QueueUrl: RESULT_QUEUE_URL,
    MessageBody: JSON.stringify(payload),
  });
  return sqs.send(cmd);
}

async function sendEmailSES(traceId, toEmail, subject, htmlBody) {
  if (!SENDER_EMAIL) {
    console.warn(`[${traceId}] SENDER_EMAIL not set — skipping SES send`);
    return { ok: false, reason: "NO_SENDER" };
  }
  const cmd = new SendEmailCommand({
    Source: SENDER_EMAIL,
    Destination: { ToAddresses: [toEmail] },
    Message: {
      Subject: { Data: subject },
      Body: { Html: { Data: htmlBody } },
    },
  });
  return ses.send(cmd);
}

// 🔑 Export correcto en ESM (no uses exports.handler)
export const handler = async (event) => {
  for (const record of event.Records || []) {
    const traceId =
      (globalThis.crypto && crypto.randomUUID)
        ? crypto.randomUUID()
        : `${Date.now()}-${Math.floor(Math.random() * 10000)}`;

    try {
      console.log(`[${traceId}] Received record ${record.messageId}`);
      const payload = JSON.parse(record.body);
      const { order, reportResponses } = payload;

      if (!order) {
        console.warn(`[${traceId}] no order in payload; skipping`);
        continue;
      }

      const baseSalary =
        reportResponses?.[0]?.baseSalary ? Number(reportResponses[0].baseSalary) : 0;
      const maxCapacity = baseSalary * 0.35;

      let deudaMensualActual = 0;
      if (Array.isArray(reportResponses)) {
        for (const r of reportResponses) {
          const status = (r.statusOrder || "").toString().toUpperCase();
          if (status === "APROBADA" || status === "APPROVED") {
            const monthlyRate = normalizeAnnualToMonthlyRate(Number(r.interestRate || 0));
            deudaMensualActual += monthlyInstallment(
              Number(r.amount),
              monthlyRate,
              Number(r.termMonths)
            );
          }
        }
      }

      const availableCapacity = maxCapacity - deudaMensualActual;

      let newAnnual = order.interestRate ?? null;
      if (newAnnual == null && Array.isArray(reportResponses) && reportResponses.length > 0) {
        const arrRates = reportResponses
          .map((r) => Number(r.interestRate || 0))
          .filter((x) => !isNaN(x));
        newAnnual = arrRates.length ? arrRates.reduce((a, b) => a + b, 0) / arrRates.length : 0;
      }
      const newMonthlyRate = normalizeAnnualToMonthlyRate(Number(newAnnual || 0));
      const cuotaNuevo = monthlyInstallment(
        Number(order.amount),
        newMonthlyRate,
        Number(order.termMonths)
      );

      let decision = "RECHAZADO";
      if (cuotaNuevo <= availableCapacity) {
        if (baseSalary && Number(order.amount) > baseSalary * 5) {
          decision = "REVISION_MANUAL";
        } else {
          decision = "APROBADO";
        }
      }

      const { installment: cuotaCalc, plan: paymentPlan } =
        generateAmortizationPlan(Number(order.amount), newMonthlyRate, Number(order.termMonths));

      const result = {
        orderId: order.idOrder,
        decision,
        maxCapacity: Number(maxCapacity.toFixed(2)),
        debtMonthly: Number(deudaMensualActual.toFixed(2)),
        availableCapacity: Number(availableCapacity.toFixed(2)),
        cuotaNuevo: Number(cuotaNuevo.toFixed(2)),
        cuotaCalculada: Number(cuotaCalc.toFixed(2)),
        paymentPlan,
      };

      console.log(`[${traceId}] Calculation result: ${JSON.stringify(result)}`);

      try {
        await sendToResultQueue(traceId, { traceId, result, originalOrder: order });
        console.log(`[${traceId}] Sent result to RESULT queue.`);
      } catch (qErr) {
        console.error(`[${traceId}] Failed to send to result queue:`, qErr);
      }

      try {
        const toEmail = order.emailAddress || reportResponses?.[0]?.email;
        if (toEmail) {
          const subject = `Result for your loan request ${order.idOrder ?? "(no-id)"}`;
          let html = `<p>Decision: <b>${decision}</b></p>`;
          html += `<p>Monthly installment (new loan): <b>${result.cuotaNuevo.toFixed(2)}</b></p>`;
          html += `<table border="1" cellpadding="4">
            <thead><tr><th>Month</th><th>Capital</th><th>Interest</th><th>Installment</th><th>Remaining</th></tr></thead><tbody>`;
          for (const p of paymentPlan) {
            html += `<tr><td>${p.month}</td><td>${p.capital}</td><td>${p.interest}</td><td>${p.installment}</td><td>${p.remainingBalance}</td></tr>`;
          }
          html += `</tbody></table>`;
          await sendEmailSES(traceId, toEmail, subject, html);
          console.log(`[${traceId}] Email sent to ${toEmail}`);
        } else {
          console.warn(`[${traceId}] No recipient email found, skipping SES send`);
        }
      } catch (sesErr) {
        console.error(`[${traceId}] SES send error:`, sesErr);
      }

      console.log(`[${traceId}] Finished processing record ${record.messageId}`);
    } catch (err) {
      console.error(`[${traceId}] Unhandled error processing record:`, err);
      // throw err; // si quieres que SQS reprocesse
    }
  }
  return { statusCode: 200 };
};
