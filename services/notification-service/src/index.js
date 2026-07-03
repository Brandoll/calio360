import { serve } from '@hono/node-server';
import { Hono } from 'hono';
import mongoose from 'mongoose';
import dotenv from 'dotenv';
import { startRabbitMQConsumer } from './rabbitmq/consumer.js';
import { NotificationLog } from './models/NotificationLog.js';

dotenv.config();

const app = new Hono();

// ==========================================
// HEALTH CHECK
// ==========================================
app.get('/health', (c) => c.json({ status: 'ok', service: 'calio-notification-service' }));

// ==========================================
// ENDPOINT: Envío manual de notificación
// ==========================================
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

// ==========================================
// BOOTSTRAP
// ==========================================
const PORT = process.env.CALIO_NOTIFICATION_PORT || 8087;
const MONGO_URI = process.env.CALIO_NOTIFICATION_MONGO_URI || 'mongodb://calio-mongodb:27017/calio_notification_db';

async function bootstrap() {
  try {
    await mongoose.connect(MONGO_URI);
    console.log('[calio-notification-service] Conectado a MongoDB');

    // Iniciar consumidor de RabbitMQ en background
    startRabbitMQConsumer();

    serve({
      fetch: app.fetch,
      port: PORT,
    }, (info) => {
      console.log(`[calio-notification-service] Levantado en puerto ${info.port}`);
    });

  } catch (error) {
    console.error('[calio-notification-service] Error arrancando:', error);
    process.exit(1);
  }
}

bootstrap();
