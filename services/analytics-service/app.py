import os
import json
import threading
import io
from datetime import datetime
from flask import Flask, jsonify, send_file
from dotenv import load_dotenv
from pymongo import MongoClient
import pika
from reportlab.pdfgen import canvas
from reportlab.lib.pagesizes import letter

load_dotenv()

app = Flask(__name__)

# ==========================================
# CONFIGURACIÓN (Variables CALIO_*)
# ==========================================
MONGO_URI = os.getenv("CALIO_ANALYTICS_MONGO_URI", "mongodb://calio-mongodb:27017/calio_analytics_db")
cliente_mongo = MongoClient(MONGO_URI)
base_datos = cliente_mongo['calio_analytics_db']
coleccion_analiticas = base_datos['user_stats']

# ==========================================
# HEALTH CHECK
# ==========================================
@app.route('/health', methods=['GET'])
def health():
    return jsonify({"status": "ok", "service": "calio-analytics-service"}), 200

# ==========================================
# RUTAS REST (GRÁFICAS)
# ==========================================

@app.route('/stats/week/<int:id_usuario>', methods=['GET'])
def obtener_estadisticas_semanales(id_usuario):
    try:
        estadisticas_usuario = coleccion_analiticas.find_one({"user_id": id_usuario}, {"_id": 0})
        
        if not estadisticas_usuario:
            fecha_hoy = datetime.now().strftime("%Y-%m-%d")
            datos_iniciales = {
                "user_id": id_usuario,
                "weight_progress": [
                    {"date": fecha_hoy, "weight": 70.0}
                ],
                "streak_days": 0,
                "logros": ["Perfil inicializado"],
                "historial_calorias": [
                    {"date": fecha_hoy, "calories_consumed": 0, "calories_burned": 0}
                ],
                "weekly_macros": {
                    "calories": 0, "proteins": 0, "carbs": 0, "fats": 0
                }
            }
            coleccion_analiticas.insert_one(datos_iniciales.copy())
            if "_id" in datos_iniciales: del datos_iniciales["_id"]
            return jsonify(datos_iniciales), 200
            
        return jsonify(estadisticas_usuario), 200

    except Exception as e:
        return jsonify({"error": f"Error en calio-analytics-service (GET Stats): {str(e)}"}), 500


@app.route('/analytics/chart/weight/<int:id_usuario>', methods=['GET'])
def datos_grafica_peso(id_usuario):
    try:
        usuario = coleccion_analiticas.find_one({"user_id": id_usuario}, {"weight_progress": 1, "_id": 0})
        if not usuario: return jsonify({"error": "Usuario no encontrado"}), 404
        return jsonify(usuario.get("weight_progress", [])), 200
    except Exception as e:
        return jsonify({"error": str(e)}), 500


@app.route('/analytics/chart/calories/<int:id_usuario>', methods=['GET'])
def datos_grafica_calorias(id_usuario):
    try:
        usuario = coleccion_analiticas.find_one({"user_id": id_usuario}, {"historial_calorias": 1, "_id": 0})
        if not usuario: return jsonify({"error": "Usuario no encontrado"}), 404
        return jsonify(usuario.get("historial_calorias", [])), 200
    except Exception as e:
        return jsonify({"error": str(e)}), 500


# ==========================================
# REPORTES PDF
# ==========================================
@app.route('/report/pdf/<int:id_usuario>', methods=['GET'])
def generar_reporte_pdf(id_usuario):
    try:
        usuario = coleccion_analiticas.find_one({"user_id": id_usuario})
        if not usuario: return jsonify({"error": "No hay datos para generar reporte"}), 404

        buffer = io.BytesIO()
        c = canvas.Canvas(buffer, pagesize=letter)
        c.setFont("Helvetica-Bold", 16)
        c.drawString(100, 750, f"Reporte Semanal de Salud - Usuario {id_usuario}")
        
        c.setFont("Helvetica", 12)
        macros = usuario.get("weekly_macros", {})
        c.drawString(100, 700, f"Calorias Consumidas: {macros.get('calories', 0)} kcal")
        c.drawString(100, 680, f"Proteinas: {macros.get('proteins', 0)} g")
        c.drawString(100, 660, f"Carbohidratos: {macros.get('carbs', 0)} g")
        c.drawString(100, 640, f"Grasas: {macros.get('fats', 0)} g")
        
        c.drawString(100, 600, f"Dias seguidos en racha (Streak): {usuario.get('streak_days', 0)}")
        
        c.showPage()
        c.save()

        buffer.seek(0)
        return send_file(buffer, as_attachment=True, download_name=f"reporte_usuario_{id_usuario}.pdf", mimetype='application/pdf')

    except Exception as e:
        return jsonify({"error": f"Error generando PDF: {str(e)}"}), 500


# ==========================================
# RABBITMQ CONSUMER ASINCRONO
# ==========================================
def rabbitmq_consumer():
    host = os.getenv("CALIO_RABBITMQ_HOST", "calio-rabbitmq")
    port = int(os.getenv("CALIO_RABBITMQ_PORT", "5672"))
    user = os.getenv("CALIO_RABBITMQ_USER", "calio_admin")
    password = os.getenv("CALIO_RABBITMQ_PASSWORD", "calio_admin")

    credentials = pika.PlainCredentials(user, password)
    parameters = pika.ConnectionParameters(host, port, '/', credentials)
    
    try:
        connection = pika.BlockingConnection(parameters)
        channel = connection.channel()

        # El exchange 'calio.events' es usado por toda la plataforma
        channel.exchange_declare(exchange='calio.events', exchange_type='topic', durable=True)

        # Cola persistente (durable) para analíticas
        channel.queue_declare(queue='calio.analytics.food', durable=True)
        channel.queue_declare(queue='calio.analytics.exercise', durable=True)

        # Bindings
        channel.queue_bind(exchange='calio.events', queue='calio.analytics.food', routing_key='food.registered')
        channel.queue_bind(exchange='calio.events', queue='calio.analytics.exercise', routing_key='activity.updated')

        def callback(ch, method, properties, body):
            print(f" [Analytics] Recibido evento {method.routing_key}")
            try:
                data = json.loads(body)
                user_id = data.get("userId")
                fecha = data.get("fecha", datetime.now().strftime("%Y-%m-%d"))

                # Crear perfil si no existe
                coleccion_analiticas.update_one(
                    {"user_id": user_id},
                    {"$setOnInsert": {
                        "user_id": user_id,
                        "weekly_macros": {"calories": 0, "proteins": 0, "carbs": 0, "fats": 0},
                        "historial_calorias": [{"date": fecha, "calories_consumed": 0, "calories_burned": 0}]
                    }},
                    upsert=True
                )

                if method.routing_key == 'food.registered':
                    calorias = data.get("caloriasConsumidas", 0)
                    coleccion_analiticas.update_one(
                        {"user_id": user_id, "historial_calorias.date": fecha},
                        {"$inc": {"historial_calorias.$.calories_consumed": calorias, "weekly_macros.calories": calorias}}
                    )

                elif method.routing_key == 'activity.updated':
                    calorias = data.get("caloriasQuemadas", 0)
                    coleccion_analiticas.update_one(
                        {"user_id": user_id, "historial_calorias.date": fecha},
                        {"$inc": {"historial_calorias.$.calories_burned": calorias}}
                    )

            except Exception as e:
                print(f"Error procesando mensaje RabbitMQ: {e}")

        print(' [*] Analytics Service esperando eventos de RabbitMQ...')
        channel.basic_consume(queue='calio.analytics.food', on_message_callback=callback, auto_ack=True)
        channel.basic_consume(queue='calio.analytics.exercise', on_message_callback=callback, auto_ack=True)
        channel.start_consuming()

    except Exception as e:
        print(f"No se pudo conectar a RabbitMQ: {e}")

# ==========================================
# ENTRYPOINT (solo para desarrollo local)
# ==========================================
if __name__ == '__main__':
    consumer_thread = threading.Thread(target=rabbitmq_consumer, daemon=True)
    consumer_thread.start()

    app.run(host='0.0.0.0', port=8086, debug=False, use_reloader=False)