import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";
import ErrorBanner from "../components/ErrorBanner";

export default function Register() {
  const { register } = useAuth();
  const navigate = useNavigate();
  const [name, setName] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  async function handleSubmit(e) {
    e.preventDefault();
    setError("");
    setLoading(true);
    try {
      await register(name, email, password);
      navigate("/books", { replace: true });
    } catch (err) {
      setError(err.response?.data?.message || "Could not create your account. Please try again.");
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="auth-page">
      <div className="auth-card">
        <p className="eyebrow">Join the library</p>
        <h1>Create your membership</h1>
        <p className="auth-sub">Sign up to start borrowing books from Shelfly.</p>

        <ErrorBanner message={error} />

        <form onSubmit={handleSubmit} className="form">
          <label>
            Full name
            <input type="text" value={name} onChange={(e) => setName(e.target.value)} required />
          </label>
          <label>
            Email
            <input type="email" value={email} onChange={(e) => setEmail(e.target.value)} required />
          </label>
          <label>
            Password
            <input
              type="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              required
              minLength={6}
            />
            <span className="hint">At least 6 characters</span>
          </label>
          <button className="btn btn-primary btn-block" type="submit" disabled={loading}>
            {loading ? "Creating account…" : "Create account"}
          </button>
        </form>

        <p className="auth-switch">
          Already a member? <Link to="/login">Log in</Link>
        </p>
      </div>
    </div>
  );
}
