import { apiFetch } from "./http";

export const ProductsApi = {
  list: () => apiFetch("/api/products"),
  get: (id) => apiFetch(`/api/products/${id}`),
};