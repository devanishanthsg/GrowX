import { useState } from "react";
import {
  Link,
  useNavigate,
} from "react-router-dom";

function Register() {
  const navigate = useNavigate();

  const [formData, setFormData] = useState({
    name: "",
    email: "",
    location: "",
    password: "",
  });

  const [error, setError] = useState("");

  function handleChange(event) {
    const { name, value } = event.target;

    setFormData((previousData) => ({
      ...previousData,
      [name]: value,
    }));
  }

  function handleSubmit(event) {
  event.preventDefault();
  setError("");

  const name = formData.name.trim();
  const email = formData.email.trim();
  const location = formData.location.trim();
  const password = formData.password.trim();

  if (!name || !email || !location || !password) {
    setError("Please complete all fields.");
    return;
  }

  if (password.length < 6) {
    setError(
      "Password must contain at least 6 characters."
    );
    return;
  }

  const user = {
    name,
    email,
    location,
    farmName: `${name}'s Farm`,
  };

  localStorage.setItem(
    "growx-user",
    JSON.stringify(user)
  );

  navigate("/dashboard");
}
  return (
    <main className="auth-page">
      <section className="auth-presentation">
        <Link to="/" className="auth-logo">
          🌱 GrowX
        </Link>

        <div>
          <p className="auth-label">
            JOIN GROWX
          </p>

          <h1>
            Begin your smart farming journey.
          </h1>

          <p>
            Create your account and start using
            AI-powered tools for modern agriculture.
          </p>
        </div>
      </section>

      <section className="auth-form-section">
        <form
          className="auth-form"
          onSubmit={handleSubmit}
        >
          <div className="auth-form-heading">
            <h2>Create account</h2>
            <p>Register as a farmer on GrowX.</p>
          </div>

          {error && (
            <p className="form-error">{error}</p>
          )}

          <label>
            Full name

            <input
              name="name"
              value={formData.name}
              onChange={handleChange}
              placeholder="Enter your name"
            />
          </label>

          <label>
            Email address

            <input
              type="email"
              name="email"
              value={formData.email}
              onChange={handleChange}
              placeholder="farmer@example.com"
            />
          </label>

          <label>
            Farm location

            <input
              name="location"
              value={formData.location}
              onChange={handleChange}
              placeholder="Example: Coimbatore"
            />
          </label>

          <label>
            Password

            <input
              type="password"
              name="password"
              value={formData.password}
              onChange={handleChange}
              placeholder="Create a strong password"
            />
          </label>

          <button
            type="submit"
            className="full-primary-button"
          >
            Create Account
          </button>

          <p className="auth-switch">
            Already have an account?{" "}
            <Link to="/login">Login</Link>
          </p>
        </form>
      </section>
    </main>
  );
}

export default Register;