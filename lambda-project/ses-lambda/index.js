const { SESClient, SendEmailCommand } = require("@aws-sdk/client-ses");

const ses = new SESClient({ region: process.env.AWS_REGION });

exports.handler = async (event) => {
    console.log('Event recibido:', JSON.stringify(event));

    for (const record of event.Records) {
        const body = JSON.parse(record.body);

        const emailParams = {
            Source: 'axalpusa1125@gmail.com',
            Destination: { ToAddresses: [body.emailAddress] },
            Message: {
                Subject: { Data: `Estado de tu pedido ${body.status}` },
                Body: { Text: { Data: `Hola, tu pedido con ID ${body.idOrder} tiene el estado: ${body.status}` } }
            }
        };

        try {
            const command = new SendEmailCommand(emailParams);
            const result = await ses.send(command);
            console.log('Correo enviado con MessageId:', result.MessageId);
        } catch (error) {
            console.error('Error enviando correo:', error);
        }
    }

    return { statusCode: 200, body: 'Mensajes procesados' };
};
