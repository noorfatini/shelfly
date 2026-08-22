import { Link, NavLink, useNavigate } from "react-router-dom";
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
          <NavLink to="/books">Catalogue</NavLink>
          {user && !isAdmin && <NavLink to="/my-borrowings">My Borrowings</NavLink>}
          {isAdmin && <NavLink to="/admin/books">Manage Books</NavLink>}
          {isAdmin && <NavLink to="/admin/borrowings">All Borrowings</NavLink>}
          {isAdmin && <NavLink to="/admin/dashboard">Dashboard</NavLink>}
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
