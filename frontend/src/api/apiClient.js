const BASE_URL = 'http://localhost:8080/api';

// GET /api/products
export async function getProducts() {
  const res = await fetch(`${BASE_URL}/products`);
  if (!res.ok) {
    throw new Error('Error cargando productos');
  }
  return res.json();
}

// GET /api/orders
export async function getOrders() {
  const res = await fetch(`${BASE_URL}/orders`);
  if (!res.ok) {
    throw new Error('Error cargando órdenes');
  }
  return res.json();
}

// POST /api/orders
// body esperado ahora mismo: { "tableNumber": "5", "employeeName": "Juan" }
export async function createOrder(data) {
  const res = await fetch(`${BASE_URL}/orders`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(data),
  });

  if (!res.ok) {
    throw new Error('Error creando la orden');
  }
  return res.json();
}