import { Link } from "react-router-dom";

export default function NotFound() {
  return (
    <div className="page-container">
      <div className="state-block empty-state">
        <h3>Page not found</h3>
        <p>This shelf is empty. The page you're looking for doesn't exist.</p>
        <Link className="btn btn-primary" to="/books">Back to catalogue</Link>
      </div>
    </div>
  );
}
