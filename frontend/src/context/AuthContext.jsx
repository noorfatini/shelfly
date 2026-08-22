import { createContext, useContext, useState, useCallback } from "react";
import api from "../api/axios";

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [user, setUser] = useState(() => {
    const stored = localStorage.getItem("shelfly_user");
    return stored ? JSON.parse(stored) : null;
  });

  const login = useCallback(async (email, password) => {
    const { data } = await api.post("/auth/login", { email, password });
    persistSession(data);
    return data;
  }, []);

  const register = useCallback(async (name, email, password) => {
    const { data } = await api.post("/auth/register", { name, email, password });
    persistSession(data);
    return data;
  }, []);

  const logout = useCallback(() => {
    localStorage.removeItem("shelfly_token");
    localStorage.removeItem("shelfly_user");
    setUser(null);
  }, []);

  function persistSession(data) {
    const sessionUser = { id: data.id, name: data.name, email: data.email, role: data.role };
    localStorage.setItem("shelfly_token", data.token);
    localStorage.setItem("shelfly_user", JSON.stringify(sessionUser));
    setUser(sessionUser);
  }

  const isAdmin = user?.role === "ADMIN";

  return (
    <AuthContext.Provider value={{ user, isAdmin, login, register, logout }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error("useAuth must be used within AuthProvider");
  return ctx;
}
