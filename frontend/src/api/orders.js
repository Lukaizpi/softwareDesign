import { apiFetch } from "./http";

export const OrdersApi = {
  list: () => apiFetch("/api/orders"),
  get: (id) => apiFetch(`/api/orders/${id}`),

  create: ({ tableNumber, employeeName }) =>
    apiFetch("/api/orders", {
      method: "POST",
      body: JSON.stringify({ tableNumber, employeeName }),
    }),

  addItem: (orderId, { productId, quantity }) =>
    apiFetch(`/api/orders/${orderId}/items`, {
      method: "POST",
      body: JSON.stringify({ productId, quantity }),
    }),

  pay: (orderId) =>
    apiFetch(`/api/orders/${orderId}/pay`, { method: "POST" }),

  addPayment: (orderId, { method, amount, tip }) =>
    apiFetch(`/api/orders/${orderId}/payments`, {
      method: "POST",
      body: JSON.stringify({ method, amount, tip }),
    }),

  discount: (orderId, { amount }) =>
    apiFetch(`/api/orders/${orderId}/discount`, {
      method: "POST",
      body: JSON.stringify({ amount }),
    }),

  refund: (orderId, { amount }) =>
    apiFetch(`/api/orders/${orderId}/refund`, {
      method: "POST",
      body: JSON.stringify({ amount }),
    }),
};