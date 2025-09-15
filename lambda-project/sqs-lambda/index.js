const { SQSClient, SendMessageCommand } = require("@aws-sdk/client-sqs");

const client = new SQSClient({ region: process.env.AWS_REGION });

exports.handler = async (event) => {
    console.log("Event recibido:", event);

    const queueUrl = process.env.QUEUE_URL;

    try {
        const command = new SendMessageCommand({
            QueueUrl: queueUrl,
            MessageBody: JSON.stringify(event)
        });
        const result = await client.send(command);
        console.log("Mensaje enviado con ID:", result.MessageId);
        return { statusCode: 200, body: result.MessageId };
    } catch (err) {
        console.error(err);
        return { statusCode: 500, body: err.message };
    }
};
