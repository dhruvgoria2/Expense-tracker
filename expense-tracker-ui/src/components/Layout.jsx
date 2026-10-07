import { useEffect } from "react";
import { NavLink, Outlet, useNavigate } from "react-router-dom";

export default function Layout() {
  const links = ["dashboard", "transactions", "categories", "budgets", "bank-accounts", "settings"];
  const navigate = useNavigate();

  // apply the saved dark/light theme on every page
  useEffect(() => {
    document.body.classList.toggle("dark", localStorage.getItem("theme") === "dark");
  }, []);

  const logout = () => {
    localStorage.removeItem("token");
    navigate("/login");
  };

  return (
    <div className="app">
      <aside className="sidebar">
        <h2 style={{ marginBottom: 32 }}>EXTRACK</h2>
        {links.map((l) => (
          <NavLink key={l} to={`/${l}`} className={({ isActive }) => (isActive ? "active" : "")}>
            {l.replace("-", " ").toUpperCase()}
          </NavLink>
        ))}
        <button className="logout" onClick={logout}>LOGOUT</button>
      </aside>
      <main className="main"><Outlet /></main>
    </div>
  );
}