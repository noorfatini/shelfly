import { Link, useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";

export default function Navbar() {
  const { user, isAdmin, logout } = useAuth();
  const navigate = useNavigate();

  function handleLogout() {
    logout();
    navigate("/login");
  }

  return (
    <header className="navbar">
      <div className="navbar-inner">
        <Link to="/books" className="brand">
          <span className="brand-mark">Sh</span>
          <span className="brand-name">Shelfly</span>
        </Link>

        <nav className="nav-links">
          <Link to="/books">Catalogue</Link>
          {user && !isAdmin && <Link to="/my-borrowings">My Borrowings</Link>}
          {isAdmin && <Link to="/admin/books">Manage Books</Link>}
          {isAdmin && <Link to="/admin/borrowings">All Borrowings</Link>}
          {isAdmin && <Link to="/admin/dashboard">Dashboard</Link>}
        </nav>

        <div className="nav-auth">
          {user ? (
            <>
              <span className="nav-user">{user.name} · {user.role === "ADMIN" ? "Librarian" : "Member"}</span>
              <button className="btn btn-ghost" onClick={handleLogout}>Log out</button>
            </>
          ) : (
            <>
              <Link className="btn btn-ghost" to="/login">Log in</Link>
              <Link className="btn btn-primary" to="/register">Join Shelfly</Link>
            </>
          )}
        </div>
      </div>
    </header>
  );
}
