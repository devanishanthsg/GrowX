import { Link } from "react-router-dom";

function Navbar() {
  return (
    <header className="navbar">
      <Link to="/" className="brand">
        <span className="brand-icon">🌱</span>
        GrowX
      </Link>

      <nav className="navbar-links">
        <a href="/#home">Home</a>
        <a href="/#features">Features</a>
        <a href="/#about">About</a>

        <Link to="/login" className="login-link">
          Login
        </Link>

        <Link to="/register" className="register-link">
          Get Started
        </Link>
      </nav>
    </header>
  );
}

export default Navbar;