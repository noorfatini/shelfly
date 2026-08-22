import { useState } from "react";
import { Link, useNavigate, useLocation } from "react-router-dom";
import { useAuth } from "../context/AuthContext";
import ErrorBanner from "../components/ErrorBanner";

export default function Login() {
  const { login } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  async function handleSubmit(e) {
    e.preventDefault();
    setError("");
    setLoading(true);
    try {
      const data = await login(email, password);
      const dest = data.role === "ADMIN" ? "/admin/dashboard" : "/books";
      navigate(location.state?.from || dest, { replace: true });
    } catch (err) {
      setError(err.response?.data?.message || "Could not log in. Check your details and try again.");
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="auth-page">
      <div className="auth-card">
        <p className="eyebrow">Welcome back</p>
        <h1>Log in to Shelfly</h1>
        <p className="auth-sub">Browse the catalogue and manage your borrowings.</p>

        <ErrorBanner message={error} />

        <form onSubmit={handleSubmit} className="form">
          <label>
            Email
            <input type="email" value={email} onChange={(e) => setEmail(e.target.value)} required />
          </label>
          <label>
            Password
            <input type="password" value={password} onChange={(e) => setPassword(e.target.value)} required />
          </label>
          <button className="btn btn-primary btn-block" type="submit" disabled={loading}>
            {loading ? "Logging in…" : "Log in"}
          </button>
        </form>

        <p className="auth-switch">
          New here? <Link to="/register">Create an account</Link>
        </p>

        <div className="demo-hint">
          <strong>Demo accounts</strong>
          <div>Admin — admin@shelfly.com / Admin123!</div>
          <div>Member — member@shelfly.com / Member123!</div>
        </div>
      </div>
    </div>
  );
}
