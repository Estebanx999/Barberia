const BASE_URL = import.meta.env.VITE_API_URL || "http://localhost:8080/api";

async function request(path, options = {}) {
  const res = await fetch(`${BASE_URL}${path}`, {
    headers: { "Content-Type": "application/json" },
    ...options,
  });
  if (!res.ok) {
    const body = await res.json().catch(() => ({}));
    throw new Error(body.error || `Error ${res.status}`);
  }
  return res.status === 204 ? null : res.json();
}

export const api = {
  login: (correo, password) =>
    request("/auth/login", { method: "POST", body: JSON.stringify({ correo, password }) }),
  listarBarberos: () => request("/barberos"),
  citasDelDia: (barberoId, fecha) =>
    request(`/citas/barbero/${barberoId}?fecha=${fecha}`),
  agendarCita: (payload) =>
    request("/citas", { method: "POST", body: JSON.stringify(payload) }),
  registrarCorte: (payload) =>
    request("/cortes", { method: "POST", body: JSON.stringify(payload) }),
  registrarGasto: (payload) =>
    request("/gastos", { method: "POST", body: JSON.stringify(payload) }),
  reporte: (desde, hasta) =>
    request(`/reportes?desde=${desde}&hasta=${hasta}`),
};
