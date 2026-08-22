import axios from "axios";

// Vite dev server proxies /api to the backend (see vite.config.js) so this can
// stay relative — no separate frontend .env needed, matching the course setup.
const api = axios.create({
  baseURL: "/api",
});

api.interceptors.request.use((config) => {
  const token = localStorage.getItem("shelfly_token");
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// If the token is invalid/expired, the backend returns 401 — log the user out
// so the UI doesn't sit in a broken authenticated-looking state.
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem("shelfly_token");
      localStorage.removeItem("shelfly_user");
      if (!window.location.pathname.startsWith("/login")) {
        window.location.href = "/login";
      }
    }
    return Promise.reject(error);
  }
);

export default api;
