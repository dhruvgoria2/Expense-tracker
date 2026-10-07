import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";

function getEmailFromToken() {
  try {
    const token = localStorage.getItem("token"); // use the key your Login.jsx saves
    return JSON.parse(atob(token.split(".")[1])).sub || "";
  } catch {
    return "";
  }
}

export default function Settings() {
  const navigate = useNavigate();
  const email = getEmailFromToken();
  const [dark, setDark] = useState(localStorage.getItem("theme") === "dark");

  useEffect(() => {
    document.body.classList.toggle("dark", dark);
    localStorage.setItem("theme", dark ? "dark" : "light");
  }, [dark]);

  const handleLogout = () => {
    localStorage.removeItem("token");
    navigate("/login");
  };

  return (
    <div className="main">
      <h1 className="page-title">Settings</h1>

      <div className="card" style={{ marginBottom: 20 }}>
        <h3 style={{ marginTop: 0 }}>Profile</h3>
        <div style={{ display: "flex", alignItems: "center", gap: 14 }}>
          <div className="avatar">{(email[0] || "U").toUpperCase()}</div>
          <div>
            <div style={{ fontWeight: 600 }}>{email || "Unknown user"}</div>
            <div style={{ fontSize: 12, color: "#999" }}>Signed in</div>
          </div>
        </div>
      </div>

      <div className="card" style={{ marginBottom: 20 }}>
        <h3 style={{ marginTop: 0 }}>Appearance</h3>
        <label style={{ display: "flex", justifyContent: "space-between", alignItems: "center" }}>
          <span>Dark mode</span>
          <input type="checkbox" checked={dark} onChange={(e) => setDark(e.target.checked)} />
        </label>
      </div>

      <div className="card">
        <h3 style={{ marginTop: 0 }}>Account</h3>
        <button onClick={handleLogout}>Log out</button>
      </div>
    </div>
  );
}
