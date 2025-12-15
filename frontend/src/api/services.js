import { apiFetch } from "./http";

/**
 * ServiceCatalog API (Beauty salon services)
 * Backend endpoints:
 *  - GET  /api/services
 *  - POST /api/services
 */
export const ServicesApi = {
  list: () => apiFetch("/api/services"),

  create: ({ name, durationMinutes, price }) => {
    // Validación mínima cliente (evita peticiones basura)
    if (!name || String(name).trim().length < 2) {
      return Promise.reject(new Error("El nombre del servicio es obligatorio"));
    }
    const dur = Number(durationMinutes);
    const pr = Number(price);
    if (!Number.isFinite(dur) || dur <= 0) {
      return Promise.reject(new Error("durationMinutes debe ser > 0"));
    }
    if (!Number.isFinite(pr) || pr < 0) {
      return Promise.reject(new Error("price debe ser >= 0"));
    }

    return apiFetch("/api/services", {
      method: "POST",
      body: JSON.stringify({
        name: String(name).trim(),
        durationMinutes: dur,
        price: pr,
      }),
    });
  },
};