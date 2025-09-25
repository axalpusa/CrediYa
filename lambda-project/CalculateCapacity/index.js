import { SESClient, SendEmailCommand } from "@aws-sdk/client-ses";
import { SQSClient, SendMessageCommand } from "@aws-sdk/client-sqs";

const sqsClient = new SQSClient({ region: process.env.AWS_REGION });
const sesClient = new SESClient({ region: process.env.AWS_REGION });

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
  return sqsClient.send(cmd);
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
  return sesClient.send(cmd);
}

export const handler = async (event) => {
  console.log("Event recibido:", JSON.stringify(event));

  const traceId = crypto.randomUUID?.() || `${Date.now()}-${Math.random() * 10000}`;

  try {
    for (const record of event.Records || []) {
      console.log(`[${traceId}] RAW record.body:`, record.body);

      let payload;
      try {
        payload = JSON.parse(record.body);
        // Manejo de doble JSON
        if (typeof payload === "string") {
          console.warn(`[${traceId}] Doble serialización detectada, parseando de nuevo...`);
          payload = JSON.parse(payload);
        }
      } catch (parseErr) {
        console.error(`[${traceId}] Error parseando record.body`, parseErr);
        continue;
      }

      const { order, reportResponses } = payload;
      if (!order) {
        console.warn(`[${traceId}] No order en payload, skipping`);
        continue;
      }

      const baseSalary = Number(reportResponses?.[0]?.baseSalary || 0);
      const maxCapacity = baseSalary * 0.35;

      let deudaMensualActual = 0;
      for (const r of reportResponses || []) {
        const status = (r.statusOrder || "").toUpperCase();
        if (status === "APROBADA" || status === "APPROVED") {
          const monthlyRate = normalizeAnnualToMonthlyRate(Number(r.interestRate || 0));
          deudaMensualActual += monthlyInstallment(
            Number(r.amount),
            monthlyRate,
            Number(r.termMonths)
          );
        }
      }

      const availableCapacity = maxCapacity - deudaMensualActual;

      let newAnnual = order.interestRate ?? null;
      if (newAnnual == null && reportResponses?.length) {
        const arrRates = reportResponses
          .map(r => Number(r.interestRate || 0))
          .filter(x => !isNaN(x));
        newAnnual = arrRates.length
          ? arrRates.reduce((a, b) => a + b, 0) / arrRates.length
          : 0;
      }

      const newMonthlyRate = normalizeAnnualToMonthlyRate(newAnnual);
      const cuotaNuevo = monthlyInstallment(
        Number(order.amount),
        newMonthlyRate,
        Number(order.termMonths)
      );

      let decision = "REJECTED";
      if (cuotaNuevo <= availableCapacity) {
        decision = Number(order.amount) > baseSalary * 5 ? "REVISION" : "APPROVED";
      }

      const { installment: cuotaCalc, plan: paymentPlan } =
        generateAmortizationPlan(Number(order.amount), newMonthlyRate, Number(order.termMonths));

      const resultPayload = {
        orderId: order.idOrder,
        decision
      };
      
      await sendToResultQueue(traceId, resultPayload);
      console.log(`[${traceId}] Sent simplified result to RESULT queue: ${JSON.stringify(resultPayload)}`);

      
      const toEmail = order.emailAddress || reportResponses?.[0]?.email;

      if (toEmail) {
        // Construcción de tabla HTML
        const tableRows = paymentPlan.map(p => `
          <tr>
            <td>${p.month}</td>
            <td>${p.capital}</td>
            <td>${p.interest}</td>
            <td>${p.installment}</td>
            <td>${p.remainingBalance}</td>
          </tr>
        `).join("");

        const htmlBody = `
          <h2>Resultado de tu capacidad de endeudamiento</h2>
          <p><strong>Decision:</strong> ${decision}</p>
          <p><strong>Monthly installment (new loan):</strong> ${cuotaNuevo.toFixed(2)}</p>

          <table border="1" cellspacing="0" cellpadding="5">
            <thead>
              <tr>
                <th>Month</th>
                <th>Capital</th>
                <th>Interest</th>
                <th>Installment</th>
                <th>Remaining</th>
              </tr>
            </thead>
            <tbody>
              ${tableRows}
            </tbody>
          </table>
        `;

        await sendEmailSES(traceId, toEmail, `Result for ${order.idOrder}`, htmlBody);
        console.log(`[${traceId}] Email sent to ${toEmail}`);
      }

      console.log(`[${traceId}] Finished processing record ${record.messageId}`);
    }

    return { statusCode: 200, body: "Processed successfully" };
  } catch (err) {
    console.error(err);
    return { statusCode: 500, body: err.message };
  }
};
