const BASE_URL = 'http://localhost:8080/api';

function getAuthHeaders() {
  const token = localStorage.getItem('token');
  return {
    'Content-Type': 'application/json',
    ...(token && { Authorization: `Bearer ${token}` }),
  };
}

// ========== PRODUCTS ==========
export async function getProducts() {
  const res = await fetch(`${BASE_URL}/products`, {
    headers: getAuthHeaders(),
  });
  if (!res.ok) throw new Error('Error loading products');
  return res.json();
}

export async function createProduct(data) {
  const res = await fetch(`${BASE_URL}/products`, {
    method: 'POST',
    headers: getAuthHeaders(),
    body: JSON.stringify(data),
  });
  if (!res.ok) throw new Error('Error creating product');
  return res.json();
}

export async function updateProduct(id, data) {
  const res = await fetch(`${BASE_URL}/products/${id}`, {
    method: 'PUT',
    headers: getAuthHeaders(),
    body: JSON.stringify(data),
  });
  if (!res.ok) throw new Error('Error updating product');
  return res.json();
}

export async function getIngredients() {
  const res = await fetch(`${BASE_URL}/ingredients`, {
    headers: getAuthHeaders(),
  });
  if (!res.ok) throw new Error('Error loading ingredients');
  return res.json();
}


export async function createIngredient(data) {
  const res = await fetch(`${BASE_URL}/ingredients`, {
    method: 'POST',
    headers: getAuthHeaders(),
    body: JSON.stringify(data),
  });
  if (!res.ok) throw new Error('Error creating ingredient');
  return res.json();
}

export async function updateIngredient(id, data) {
  const res = await fetch(`${BASE_URL}/ingredients/${id}`, {
    method: 'PUT',
    headers: getAuthHeaders(),
    body: JSON.stringify(data),
  });
  if (!res.ok) throw new Error('Error updating ingredient');
  return res.json();
}

export async function createIngredientCategory(data) {
  const res = await fetch(`${BASE_URL}/ingredient-categories`, {
    method: 'POST',
    headers: getAuthHeaders(),
    body: JSON.stringify(data),
  });
  if (!res.ok) throw new Error('Error creating category');
  return res.json();
}

// ========== ORDERS ==========
export async function getOrders() {
  const res = await fetch(`${BASE_URL}/orders`, {
    headers: getAuthHeaders(),
  });
  if (!res.ok) throw new Error('Error loading orders');
  return res.json();
}

export async function getOrderById(id) {
  const res = await fetch(`${BASE_URL}/orders/${id}`, {
    headers: getAuthHeaders(),
  });
  if (!res.ok) throw new Error('Error loading order');
  return res.json();
}

export async function createOrder(data) {
  const res = await fetch(`${BASE_URL}/orders`, {
    method: 'POST',
    headers: getAuthHeaders(),
    body: JSON.stringify(data),
  });
  if (!res.ok) throw new Error('Error creating order');
  return res.json();
}

export async function addItemToOrder(orderId, productId, quantity) {
  const res = await fetch(`${BASE_URL}/orders/${orderId}/items`, {
    method: 'POST',
    headers: getAuthHeaders(),
    body: JSON.stringify({ productId, quantity }),
  });
  if (!res.ok) throw new Error('Error adding item');
  return res.json();
}

export async function cancelOrder(orderId) {
  const res = await fetch(`${BASE_URL}/orders/${orderId}/cancel`, {
    method: 'POST',
    headers: getAuthHeaders(),
  });
  if (!res.ok) throw new Error('Error cancelling order');
  return res.json();
}

export async function payOrder(orderId) {
  const res = await fetch(`${BASE_URL}/orders/${orderId}/pay`, {
    method: 'POST',
    headers: getAuthHeaders(),
  });
  if (!res.ok) throw new Error('Error marking as paid');
  return res.json();
}

export async function updateOrderStatus(orderId, status) {
  const res = await fetch(`${BASE_URL}/orders/${orderId}/status`, {
    method: 'PUT',
    headers: getAuthHeaders(),
    body: JSON.stringify({ status }),
  });
  if (!res.ok) throw new Error('Error updating status');
  return res.json();
}

export async function applyDiscount(orderId, discountId) {
  // Get discount and order to calculate correct amount
  const [discount, order] = await Promise.all([
    getDiscountById(discountId),
    getOrderById(orderId),
  ]);
  
  let discountAmount = 0;
  if (discount.valueType === 'PERCENTAGE') {
    // Calculate percentage of subtotal + taxes
    const totalBeforeDiscount = (order.subtotal || 0) + (order.taxes || 0);
    discountAmount = totalBeforeDiscount * (discount.value / 100.0);
  } else {
    // Fixed amount
    discountAmount = discount.value;
  }
  
  const res = await fetch(`${BASE_URL}/orders/${orderId}/discount`, {
    method: 'POST',
    headers: getAuthHeaders(),
    body: JSON.stringify({ amount: discountAmount }),
  });
  if (!res.ok) throw new Error('Error applying discount');
  return res.json();
}

export async function getDiscounts() {
  const res = await fetch(`${BASE_URL}/discounts`, {
    headers: getAuthHeaders(),
  });
  if (!res.ok) throw new Error('Error loading discounts');
  return res.json();
}

export async function getDiscountById(id) {
  const res = await fetch(`${BASE_URL}/discounts/${id}`, {
    headers: getAuthHeaders(),
  });
  if (!res.ok) throw new Error('Error loading discount');
  return res.json();
}

export async function createDiscount(data) {
  const res = await fetch(`${BASE_URL}/discounts`, {
    method: 'POST',
    headers: getAuthHeaders(),
    body: JSON.stringify(data),
  });
  if (!res.ok) throw new Error('Error creating discount');
  return res.json();
}

export async function updateDiscount(id, data) {
  const res = await fetch(`${BASE_URL}/discounts/${id}`, {
    method: 'PUT',
    headers: getAuthHeaders(),
    body: JSON.stringify(data),
  });
  if (!res.ok) throw new Error('Error updating discount');
  return res.json();
}

export async function getActiveDiscounts() {
  const res = await fetch(`${BASE_URL}/discounts/active`, {
    headers: getAuthHeaders(),
  });
  if (!res.ok) throw new Error('Error loading active discounts');
  return res.json();
}

export async function addPayment(orderId, method, amount, tip = 0) {
  const res = await fetch(`${BASE_URL}/orders/${orderId}/payments`, {
    method: 'POST',
    headers: getAuthHeaders(),
    body: JSON.stringify({ method, amount, tip }),
  });
  if (!res.ok) throw new Error('Error adding payment');
  return res.json();
}

// Split payment
export async function startSplitPayment(orderId) {
  const res = await fetch(`${BASE_URL}/orders/${orderId}/split/start`, {
    method: 'POST',
    headers: getAuthHeaders(),
  });
  if (!res.ok) throw new Error('Error starting split payment');
  return res.json();
}

export async function addPaymentSplit(orderId, splitData) {
  const res = await fetch(`${BASE_URL}/orders/${orderId}/split`, {
    method: 'POST',
    headers: getAuthHeaders(),
    body: JSON.stringify(splitData),
  });
  if (!res.ok) throw new Error('Error adding payment split');
  return res.json();
}

export async function completeSplitPayment(orderId) {
  const res = await fetch(`${BASE_URL}/orders/${orderId}/split/complete`, {
    method: 'POST',
    headers: getAuthHeaders(),
  });
  if (!res.ok) throw new Error('Error completing split payment');
  return res.json();
}

export async function refundOrder(orderId, amount = 0, reason = '') {
  const res = await fetch(`${BASE_URL}/orders/${orderId}/refund`, {
    method: 'POST',
    headers: getAuthHeaders(),
    body: JSON.stringify({ amount, reason }),
  });
  if (!res.ok) throw new Error('Error processing refund');
  return res.json();
}

export async function getIngredientCategories() {
  const res = await fetch(`${BASE_URL}/ingredient-categories`, {
    headers: getAuthHeaders(),
  });
  if (!res.ok) throw new Error('Error loading ingredient categories');
  return res.json();
}

// ========== TAXES ==========
export async function getTaxes() {
  const res = await fetch(`${BASE_URL}/taxes`, {
    headers: getAuthHeaders(),
  });
  if (!res.ok) throw new Error('Error loading taxes');
  return res.json();
}

export async function createTax(data) {
  const res = await fetch(`${BASE_URL}/taxes`, {
    method: 'POST',
    headers: getAuthHeaders(),
    body: JSON.stringify(data),
  });
  if (!res.ok) throw new Error('Error creating tax');
  return res.json();
}
