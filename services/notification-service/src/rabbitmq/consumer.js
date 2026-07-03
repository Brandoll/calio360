import amqp from 'amqplib';
import { NotificationLog } from '../models/NotificationLog.js';

export async function startRabbitMQConsumer() {
  const host = process.env.CALIO_RABBITMQ_HOST || 'calio-rabbitmq';
  const port = process.env.CALIO_RABBITMQ_PORT || 5672;
  const user = process.env.CALIO_RABBITMQ_USER || 'calio_admin';
  const password = process.env.CALIO_RABBITMQ_PASSWORD || 'calio_admin';
  
  const rabbitUrl = `amqp://${user}:${password}@${host}:${port}`;

  try {
    const connection = await amqp.connect(rabbitUrl);
    const channel = await connection.createChannel();
    
    const exchange = 'calio.events';
    await channel.assertExchange(exchange, 'topic', { durable: true });
    
    // Colas persistentes (durable) — no exclusive
    await channel.assertQueue('calio.notifications.water', { durable: true });
    await channel.assertQueue('calio.notifications.exercise', { durable: true });
    await channel.assertQueue('calio.notifications.goal', { durable: true });
    await channel.assertQueue('calio.notifications.send', { durable: true });

    // Bindings según la arquitectura
    channel.bindQueue('calio.notifications.water', exchange, 'meal.updated');
    channel.bindQueue('calio.notifications.exercise', exchange, 'activity.updated');
    channel.bindQueue('calio.notifications.goal', exchange, 'goal.completed');
    channel.bindQueue('calio.notifications.send', exchange, 'notification.send');

    console.log('[calio-notification-service] Esperando eventos en RabbitMQ...');

    const handleMessage = async (msg) => {
      if (msg !== null) {
        const routingKey = msg.fields.routingKey;
        const content = JSON.parse(msg.content.toString());
        
        console.log(`[Notification] Recibido evento: ${routingKey}`);

        let mensajePush = '';
        let tipo = 'motivacional';

        if (routingKey === 'meal.updated') {
          tipo = 'recordatorio';
          mensajePush = `¡Excelente! Has registrado tu comida. ¡Mantente en el camino!`;
        } else if (routingKey === 'activity.updated') {
          tipo = 'motivacional';
          mensajePush = `¡Gran trabajo en tu entrenamiento, quemaste ${content.caloriasQuemadas || 0} calorías! Sigue así.`;
        } else if (routingKey === 'goal.completed') {
          tipo = 'motivacional';
          mensajePush = `¡Felicidades! Has completado tu meta: ${content.meta || 'Meta personalizada'}`;
        } else if (routingKey === 'notification.send') {
          tipo = content.tipo || 'alerta';
          mensajePush = content.mensaje || 'Tienes una nueva notificación.';
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
    };

    channel.consume('calio.notifications.water', handleMessage);
    channel.consume('calio.notifications.exercise', handleMessage);
    channel.consume('calio.notifications.goal', handleMessage);
    channel.consume('calio.notifications.send', handleMessage);

  } catch (error) {
    console.error('[calio-notification-service] No se pudo conectar a RabbitMQ:', error);
    // Retry after 10 seconds
    setTimeout(startRabbitMQConsumer, 10000);
  }
}
