import mongoose from 'mongoose';

const NotificationLogSchema = new mongoose.Schema({
  userId: {
    type: Number,
    required: true,
  },
  tipo: {
    type: String,
    enum: ['motivacional', 'alerta', 'recordatorio'],
    required: true,
  },
  mensaje: {
    type: String,
    required: true,
  },
  fecha: {
    type: Date,
    default: Date.now,
  },
  entregado: {
    type: Boolean,
    default: true,
  }
});

export const NotificationLog = mongoose.model('NotificationLog', NotificationLogSchema);
