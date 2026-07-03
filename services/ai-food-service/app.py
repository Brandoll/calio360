import os
import io
import json
from flask import Flask, request, jsonify
from dotenv import load_dotenv
from google import genai
from google.genai import types
from PIL import Image
from pydantic import BaseModel, Field

load_dotenv()

app = Flask(__name__)

# Tu API Key que empieza con AQ.
GEMINI_API_KEY = os.getenv("GEMINI_API_KEY")

# Iniciar cliente oficial de google con la clave de forma nativa
cliente_gemini = genai.Client(api_key=GEMINI_API_KEY)

# DEFINICIÓN DE ESQUEMAS CON PYDANTIC PARA ASEGURAR EL JSON DE RESPUESTA
class FoodItem(BaseModel):
    nombre: str = Field(description="Nombre del ingrediente o plato, priorizando términos de Latinoamérica (ej. quinua, arepa).")
    calorias: int = Field(description="Calorías totales estimadas de esta porción.")
    proteinas: int = Field(description="Gramos de proteína.")
    grasas: int = Field(description="Gramos de grasas.")
    carbohidratos: int = Field(description="Gramos de carbohidratos.")

class AnalysisResult(BaseModel):
    items: list[FoodItem] = Field(description="Lista de ingredientes identificados en la comida.")
    confianza: float = Field(description="Nivel de confianza en la predicción del 0.0 al 1.0.")

# SYSTEM INSTRUCTION (Reglas de personalidad y contexto)
SYSTEM_INSTRUCTION = (
    "Eres un experto nutricionista especializado en gastronomía de Latinoamérica. "
    "Tu tarea es analizar alimentos e identificar ingredientes nativos de la región (ej. quinua, arepa, ceviche, lulo). "
    "Debes calcular con precisión las porciones, calorías, proteínas, grasas y carbohidratos. "
    "Si encuentras platos tradicionales, desglosa los macronutrientes basándote en recetas estándar latinoamericanas."
)

# PROCESAMIENTO DE IMAGEN
@app.route('/analyze', methods=['POST'])
def analizar_imagen():
    if 'imagen' not in request.files:
        return jsonify({"error": "No se subio ninguna imagen"}), 400
    
    archivo = request.files['imagen']
    if archivo.filename == '':
        return jsonify({"error": "Archivo no seleccionado"}), 400

    try:
        bytes_imagen = archivo.read()
        imagen_cargada = Image.open(io.BytesIO(bytes_imagen))

        respuesta = cliente_gemini.models.generate_content(
            model='gemini-2.5-flash',
            contents=[imagen_cargada, "Por favor, analiza esta comida."],
            config=types.GenerateContentConfig(
                response_mime_type="application/json",
                response_schema=AnalysisResult,
                system_instruction=SYSTEM_INSTRUCTION,
                temperature=0.2 # Baja temperatura para que sea más preciso y analítico
            )
        )

        # La respuesta ya es un string JSON válido gracias al response_schema
        return respuesta.text, 200, {'Content-Type': 'application/json'}

    except Exception as e:
        return jsonify({"error": f"Error interno en S-02 con SDK (Imagen): {str(e)}"}), 500


# PROCESAMIENTO DE TEXTO
@app.route('/analyze-text', methods=['POST'])
def analizar_texto():
    data = request.get_json()
    
    if not data or 'texto' not in data:
        return jsonify({"error": "Falta el campo 'texto' en la peticion"}), 400
    
    texto_usuario = data['texto']

    try:
        respuesta = cliente_gemini.models.generate_content(
            model='gemini-2.5-flash',
            contents=[f"Analiza la siguiente comida: '{texto_usuario}'"],
            config=types.GenerateContentConfig(
                response_mime_type="application/json",
                response_schema=AnalysisResult,
                system_instruction=SYSTEM_INSTRUCTION,
                temperature=0.2
            )
        )

        return respuesta.text, 200, {'Content-Type': 'application/json'}

    except Exception as e:
        return jsonify({"error": f"Error interno en S-02 con SDK (Texto): {str(e)}"}), 500


if __name__ == '__main__':
    app.run(port=8082, debug=True)