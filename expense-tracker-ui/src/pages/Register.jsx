import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import api from "../api/axios";

export default function Register() {
  const [form, setForm] = useState({ name: "", email: "", password: "" });
  const [error, setError] = useState("");
  const navigate = useNavigate();

  const handleChange = (e) =>
    setForm({ ...form, [e.target.name]: e.target.value });

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      await api.post("/auth/register", form);
      navigate("/login");
    } catch {
      setError("Registration failed (email may already exist)");
    }
  };

  return (
    <form className="auth-box" onSubmit={handleSubmit}>
      <h2>Create account</h2>
      <input name="name" value={form.name} onChange={handleChange} placeholder="Name" required />
      <input name="email" type="email" value={form.email} onChange={handleChange} placeholder="Email" required />
      <input name="password" type="password" value={form.password} onChange={handleChange} placeholder="Password" required />
      <button type="submit">Register</button>
      {error && <p className="red">{error}</p>}
      <p>Already have an account? <Link to="/login">Login</Link></p>
    </form>
  );
}