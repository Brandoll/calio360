import amqp from 'amqplib';
import { NotificationLog } from '../models/NotificationLog.js';

export async function startRabbitMQConsumer() {
  const host = process.env.RABBITMQ_HOST || 'localhost';
  const port = process.env.RABBITMQ_PORT || 5672;
  const user = process.env.RABBITMQ_USER || 'guest';
  const password = process.env.RABBITMQ_PASSWORD || 'guest';
  
  const rabbitUrl = `amqp://${user}:${password}@${host}:${port}`;

  try {
    const connection = await amqp.connect(rabbitUrl);
    const channel = await connection.createChannel();
    
    const exchange = 'calio.events';
    await channel.assertExchange(exchange, 'topic', { durable: true });
    
    const q = await channel.assertQueue('', { exclusive: true });
    
    // Bindings según la arquitectura
    channel.bindQueue(q.queue, exchange, 'agua.actualizada');
    channel.bindQueue(q.queue, exchange, 'rutina.completada');

    console.log(' [*] Notification Service esperando eventos en RabbitMQ...');

    channel.consume(q.queue, async (msg) => {
      if (msg !== null) {
        const routingKey = msg.fields.routingKey;
        const content = JSON.parse(msg.content.toString());
        
        console.log(` [Notification] Recibido evento: ${routingKey}`);

        let mensajePush = '';
        let tipo = 'motivacional';

        if (routingKey === 'agua.actualizada') {
          tipo = 'recordatorio';
          mensajePush = `¡Excelente! Llevas ${content.vasosAgua} vasos de agua hoy. ¡Mantente hidratado!`;
        } else if (routingKey === 'rutina.completada') {
          tipo = 'motivacional';
          mensajePush = `¡Gran trabajo en tu entrenamiento, quemaste ${content.caloriasQuemadas} calorías! Sigue así.`;
        }

        if (mensajePush) {
          console.log(` ---> Enviando Push a User ${content.userId}: "${mensajePush}"`);
          
          // Guardar registro en MongoDB
          try {
            await NotificationLog.create({
              userId: content.userId,
              tipo: tipo,
              mensaje: mensajePush
            });
          } catch (dbError) {
            console.error('Error guardando log en MongoDB:', dbError);
          }
        }

        channel.ack(msg);
      }
    });

  } catch (error) {
    console.error('No se pudo conectar a RabbitMQ:', error);
  }
}
