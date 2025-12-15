# 🚀 Cómo Probar el Proyecto

## Estado Actual: **88% Completado** ✅

### ✅ Funcionalidades Implementadas

1. **Autenticación y Usuarios**
   - Login/Registro
   - Gestión de usuarios y roles
   - RBAC (Role-Based Access Control)

2. **Gestión de Órdenes**
   - Crear, editar, cancelar órdenes
   - Split payments (pagos divididos)
   - Aplicar descuentos
   - Procesar pagos y reembolsos

3. **Gestión de Reservas**
   - Crear, editar, cancelar reservas
   - Verificación de disponibilidad
   - Gestión de clientes y servicios

4. **Gestión de Productos**
   - CRUD de productos
   - Gestión de ingredientes y categorías
   - Cálculo de precios en tiempo real

5. **Gestión de Descuentos**
   - Crear descuentos con períodos de validez
   - Descuentos a nivel de producto u orden
   - Límites de uso

## 📋 Cómo Iniciar el Proyecto

### 1. Iniciar el Backend (Spring Boot)

```bash
cd backend
./gradlew bootRun
```

El backend estará disponible en: `http://localhost:8080`

**Endpoints principales:**
- Health check: `http://localhost:8080/health`
- API: `http://localhost:8080/api/*`

### 2. Iniciar el Frontend (React + Vite)

En otra terminal:

```bash
cd frontend
npm install  # Solo la primera vez
npm run dev
```

El frontend estará disponible en: `http://localhost:5173`

## 🔐 Credenciales de Prueba

El sistema viene con 3 usuarios predefinidos:

| Usuario | Contraseña | Rol | Permisos |
|---------|------------|-----|----------|
| `admin` | `admin` | SuperAdmin | Acceso completo a todo |
| `manager` | `manager` | Manager | Gestión de órdenes, productos, descuentos, reservas |
| `employee` | `employee` | Employee | Crear órdenes y reservas básicas |

## 🧪 Probar Funcionalidades

### 1. Autenticación
- Ve a `http://localhost:5173`
- Inicia sesión con cualquiera de los usuarios de arriba
- Prueba el registro de nuevos usuarios

### 2. Gestión de Órdenes
- Crea una nueva orden
- Agrega productos
- Prueba el split payment (dividir pago entre varios clientes)
- Aplica descuentos
- Procesa el pago

### 3. Gestión de Reservas
- Crea una reserva
- Selecciona un servicio
- Verifica disponibilidad
- Asigna empleado

### 4. Gestión de Productos
- Ve a "Product Management"
- Crea productos nuevos
- Agrega ingredientes
- Asigna categorías

### 5. Gestión de Descuentos
- Ve a "Discounts"
- Crea un descuento
- Define período de validez
- Establece límites de uso

### 6. Gestión de Usuarios
- Ve a "Users" (solo SuperAdmin y Manager)
- Crea nuevos usuarios
- Asigna roles
- Activa/desactiva usuarios

## 📊 Estructura del Proyecto

```
softwareDesign/
├── backend/              # Spring Boot
│   ├── src/main/java/com/restaurant/backend/
│   │   ├── Service/      # Lógica de negocio
│   │   ├── controller/  # Endpoints REST
│   │   ├── model/        # Entidades
│   │   └── config/       # Configuración
│   └── build.gradle
│
└── frontend/             # React + Vite
    ├── src/
    │   ├── components/   # Componentes React
    │   ├── api/          # Clientes API
    │   └── App.jsx        # Componente principal
    └── package.json
```

## 🔍 Verificar que Todo Funciona

### Backend
```bash
# Verificar que el backend está corriendo
curl http://localhost:8080/health

# Debería responder: "Backend OK"
```

### Frontend
- Abre `http://localhost:5173` en tu navegador
- Deberías ver la pantalla de login
- Inicia sesión con `admin` / `admin`

## 📝 Notas Importantes

1. **Datos en Memoria**: Actualmente los datos se guardan en memoria (Maps). Al reiniciar el backend, se pierden los datos (excepto los usuarios por defecto).

2. **Contraseñas**: Por ahora las contraseñas están en texto plano (para demo). En producción se debe usar BCrypt.

3. **Tokens**: Se usan tokens simples. En producción se debe implementar JWT completo.

4. **CORS**: Está configurado para permitir todas las solicitudes desde el frontend.

## 🐛 Solución de Problemas

### El backend no inicia
```bash
cd backend
./gradlew clean build
./gradlew bootRun
```

### El frontend no inicia
```bash
cd frontend
rm -rf node_modules package-lock.json
npm install
npm run dev
```

### Error "Failed to fetch"
- Verifica que el backend esté corriendo en el puerto 8080
- Verifica que no haya errores en la consola del backend
- Revisa la configuración de CORS

## 📈 Próximos Pasos

Para llegar al 100%:
1. Implementar BCrypt para contraseñas
2. Completar JWT authentication
3. Migrar a base de datos (JPA)
4. UI de configuración de impuestos
5. Vista de calendario avanzada para reservas

