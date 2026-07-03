import { serve } from '@hono/node-server';
import { Hono } from 'hono';
import mongoose from 'mongoose';
import dotenv from 'dotenv';
import { startRabbitMQConsumer } from './rabbitmq/consumer.js';
import { NotificationLog } from './models/NotificationLog.js';

dotenv.config();

const app = new Hono();

// Endpoint manual de pruebas /notify
app.post('/notify', async (c) => {
  try {
    const body = await c.req.json();
    
    if (!body.userId || !body.mensaje) {
      return c.json({ error: 'Faltan campos requeridos (userId, mensaje)' }, 400);
    }

    // Mock del envío push (Firebase/APNs iría aquí)
    console.log(`[API] Push enviado manualmente a User ${body.userId}: "${body.mensaje}"`);

    // Guardar en log
    await NotificationLog.create({
      userId: body.userId,
      tipo: body.tipo || 'alerta',
      mensaje: body.mensaje
    });

    return c.json({ status: 'sent', message: 'Notificación enviada exitosamente' }, 200);

  } catch (error) {
    console.error(error);
    return c.json({ error: 'Error procesando la solicitud' }, 500);
  }
});

app.get('/health', (c) => c.json({ status: 'Notification Service is running' }));

// Iniciar base de datos y servidor
const PORT = process.env.PORT || 8087;
const MONGO_URI = process.env.MONGO_URI || 'mongodb://localhost:27017/calio_notifications';

async function bootstrap() {
  try {
    await mongoose.connect(MONGO_URI);
    console.log(' Conectado a MongoDB (Notifications)');

    // Iniciar consumidor de RabbitMQ en background
    startRabbitMQConsumer();

    serve({
      fetch: app.fetch,
      port: PORT,
    }, (info) => {
      console.log(` Notification Service (Hono) levantado en http://localhost:${info.port}`);
    });

  } catch (error) {
    console.error('Error arrancando el servicio:', error);
    process.exit(1);
  }
}

bootstrap();
