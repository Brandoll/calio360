# 🔐 Calio — S-01 Identity Service

Servicio de autenticación y gestión de usuarios de la plataforma Calio.
Emite tokens JWT que todos los demás servicios usan para validar identidad.

---

## ⚙️ Requisitos

| Herramienta | Versión mínima |
|---|---|
| Docker | 24+ |
| Docker Compose | v2+ |

> No necesitas Java ni Maven instalados.

---

## 🚀 Levantar el servicio

```bash
git clone git@github.com:TU-USUARIO/calio-identity-service.git
cd calio-identity-service
sudo docker compose up --build
```

Cuando veas este mensaje, está listo:
```
Started IdentityServiceApplication in X seconds
```

---

## 🔗 Endpoints

**Base URL:** `http://localhost:8081/api/v1`

### Autenticación (sin token)

| Método | Endpoint | Descripción |
|---|---|---|
| POST | `/auth/register` | Registrar usuario |
| POST | `/auth/login` | Login → devuelve JWT |
| POST | `/auth/refresh` | Renovar access token |
| POST | `/auth/logout` | Cerrar sesión |

### Perfil (requiere `Authorization: Bearer <token>`)

| Método | Endpoint | Descripción |
|---|---|---|
| GET | `/users/me` | Ver perfil |
| PATCH | `/users/me` | Actualizar perfil |
| DELETE | `/users/me` | Eliminar cuenta |
| POST | `/users/me/biometrics` | Registrar peso/talla |
| GET | `/users/me/biometrics` | Historial biométrico |
| POST | `/users/me/goals` | Definir objetivo |
| GET | `/users/me/goals/active` | Ver objetivo activo |
| GET | `/users/me/settings` | Ver configuración |
| PATCH | `/users/me/settings` | Actualizar configuración |

---

## 🧪 Ejemplos de prueba

```bash
# 1. Registrar usuario
curl -X POST http://localhost:8081/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "juan@gmail.com",
    "password": "Pass1234!",
    "firstName": "Juan",
    "lastName": "López",
    "birthDate": "1995-03-15",
    "gender": "MALE"
  }'

# 2. Login (guarda el accessToken)
curl -X POST http://localhost:8081/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"juan@gmail.com","password":"Pass1234!"}'

# 3. Registrar biometría (reemplaza TOKEN)
curl -X POST http://localhost:8081/api/v1/users/me/biometrics \
  -H "Authorization: Bearer TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"weightKg": 75.0, "heightCm": 175.0, "bodyFatPct": 20.0}'

# 4. Definir objetivo (calorías se calculan automáticamente)
curl -X POST http://localhost:8081/api/v1/users/me/goals \
  -H "Authorization: Bearer TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"goalType": "LOSE_WEIGHT", "activityLevel": "MODERATE", "targetWeightKg": 68.0}'

# 5. Ver objetivo con macros calculados
curl http://localhost:8081/api/v1/users/me/goals/active \
  -H "Authorization: Bearer TOKEN"
```

---

## 🧮 Cálculo automático de calorías

El servicio usa la **Fórmula de Harris-Benedict** para calcular calorías y macros:

| Objetivo | Ajuste calórico |
|---|---|
| Perder peso | TDEE − 500 kcal |
| Ganar músculo | TDEE + 300 kcal |
| Mantener / Comer sano | TDEE |

Los macros se distribuyen: **proteínas** según peso y objetivo, **grasas** 25% de calorías, **carbohidratos** el resto.

---

## 🔄 Integración con otros servicios

Este servicio corre en el puerto **8081**.

- El JWT que emite es **válido en todos los demás servicios** (comparten el mismo `JWT_SECRET`)
- Publica eventos a **RabbitMQ** cuando un usuario se registra o actualiza su perfil
- El **S-03 Food Catalog** y demás servicios validan el JWT de este servicio

---

## 🐰 RabbitMQ Management

`http://localhost:15672` — usuario: `calio` / contraseña: `calio123`

**Eventos publicados:**
- `user.registered` → consumido por S-07 Notifications y S-06 Analytics
- `user.profile.updated` → consumido por S-04 Recipe & Meal Plan

---

## 🛑 Detener

```bash
sudo docker compose down       # detener
sudo docker compose down -v    # detener + borrar base de datos
```
