import { useEffect, useRef, useState } from "react";
import { useNavigate } from "react-router-dom";
import {
  BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer,
  PieChart, Pie, Cell,
} from "recharts";
import api from "../api/axios";
import StatCard from "../components/StatCard";
import { formatINR } from "../utils/formatcurrency";

const MONTHS = ["Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"];
const PIE_COLORS = ["#7c3aed", "#06b6d4", "#10b981", "#f59e0b", "#ef4444", "#3b82f6", "#ec4899"];

export default function Dashboard() {
  const navigate = useNavigate();
  const searchRef = useRef(null);

  const [transactions, setTransactions] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [query, setQuery] = useState("");
  const [showBell, setShowBell] = useState(false);

  useEffect(() => {
    api
      .get("/transactions")
      .then((res) => {
        const list = Array.isArray(res.data) ? res.data : res.data?.content ?? [];
        setTransactions(
          list.map((t) => ({
            ...t,
            amount: Number(t.amount) || 0,
            category: t.category?.name ?? t.category ?? "General",
          }))
        );
      })
      .catch((err) => {
        console.error("Transactions error:", err);
        setError("Failed to load transactions");
      })
      .finally(() => setLoading(false));
  }, []);

  if (loading) return <p style={{ padding: "20px" }}>Loading...</p>;
  if (error) return <p style={{ padding: "20px", color: "#dc2626" }}>{error}</p>;

  const income = transactions
    .filter((t) => t.type === "INCOME")
    .reduce((sum, t) => sum + t.amount, 0);

  const expenses = transactions
    .filter((t) => t.type === "EXPENSE")
    .reduce((sum, t) => sum + t.amount, 0);

  const now = new Date();
  const isThisMonth = (t) => {
    const d = new Date(t.date);
    return d.getMonth() === now.getMonth() && d.getFullYear() === now.getFullYear();
  };

  const monthlySavings = transactions
    .filter(isThisMonth)
    .reduce((sum, t) => sum + (t.type === "INCOME" ? t.amount : -t.amount), 0);

  const monthlyData = MONTHS.map((m) => ({ month: m, Income: 0, Expense: 0 }));
  transactions.forEach((t) => {
    const d = new Date(t.date);
    if (d.getFullYear() !== now.getFullYear()) return;
    monthlyData[d.getMonth()][t.type === "INCOME" ? "Income" : "Expense"] += t.amount;
  });

  const categoryData = Object.entries(
    transactions
      .filter((t) => t.type === "EXPENSE")
      .reduce((acc, t) => {
        acc[t.category] = (acc[t.category] || 0) + t.amount;
        return acc;
      }, {})
  ).map(([name, value]) => ({ name, value }));

  const sorted = [...transactions].sort((a, b) => new Date(b.date) - new Date(a.date));

  const q = query.trim().toLowerCase();
  const recent = sorted
    .filter((t) =>
      !q || `${t.title || t.description || ""} ${t.category} ${t.type}`.toLowerCase().includes(q)
    )
    .slice(0, 8);

  const latest3 = sorted.slice(0, 3);

  return (
    <div className="main">
      <div className="topbar">
        <button className="icon-btn primary" title="Add transaction" onClick={() => navigate("/transactions")}>
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round">
            <path d="M12 5v14M5 12h14" />
          </svg>
        </button>

        <button className="icon-btn" title="Search" onClick={() => searchRef.current?.focus()}>
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round">
            <circle cx="11" cy="11" r="7" />
            <path d="M20 20l-3.5-3.5" />
          </svg>
        </button>

        <div style={{ position: "relative" }}>
          <button className="icon-btn" title="Notifications" onClick={() => setShowBell(!showBell)}>
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
              <path d="M18 8a6 6 0 10-12 0c0 7-3 9-3 9h18s-3-2-3-9M13.7 21a2 2 0 01-3.4 0" />
            </svg>
          </button>
          {showBell && (
            <div className="bell-pop">
              <strong style={{ fontSize: 13 }}>Latest activity</strong>
              {latest3.length === 0 ? (
                <p style={{ fontSize: 12, color: "#999", marginTop: 8 }}>Nothing yet.</p>
              ) : (
                latest3.map((t) => (
                  <div key={t.id} className="bell-row">
                    <span>{t.title || t.description || "Untitled"}</span>
                    <span className={t.type === "INCOME" ? "green" : "red"}>
                      {t.type === "INCOME" ? "+" : "-"} {formatINR(t.amount)}
                    </span>
                  </div>
                ))
              )}
            </div>
          )}
        </div>

        <div className="user-chip">
          <div className="avatar">U</div>
          <span>User</span>
        </div>
      </div>

      <h1 className="page-title">Dashboard</h1>

      <div className="stats" style={{ gridTemplateColumns: "repeat(4, 1fr)" }}>
        <StatCard title="Total Balance" value={income - expenses} bg="#ece6fa" note="Available cash" />
        <StatCard title="Total Income" value={income} bg="#e6f4ea" note="All-time earnings" />
        <StatCard title="Total Expenses" value={expenses} bg="#fce8e6" note="All-time spending" />
        <StatCard title="Monthly Savings" value={monthlySavings} bg="#eef1f6" note="Current month net" />
      </div>

      <div className="dash-grid">
        <div className="card">
          <h3 style={{ marginTop: 0 }}>Income vs Expense</h3>
          <div style={{ width: "100%", height: 300 }}>
            <ResponsiveContainer>
              <BarChart data={monthlyData} barGap={4}>
                <CartesianGrid strokeDasharray="3 3" vertical={false} />
                <XAxis dataKey="month" tickLine={false} axisLine={false} />
                <YAxis
                  tickLine={false}
                  axisLine={false}
                  tickFormatter={(v) => `₹${v >= 1000 ? v / 1000 + "k" : v}`}
                />
                <Tooltip formatter={(v) => formatINR(v)} />
                <Legend />
                <Bar dataKey="Income" fill="#7c3aed" radius={[4, 4, 0, 0]} barSize={10} />
                <Bar dataKey="Expense" fill="#06b6d4" radius={[4, 4, 0, 0]} barSize={10} />
              </BarChart>
            </ResponsiveContainer>
          </div>
        </div>

        <aside className="card">
          <h3 style={{ marginTop: 0 }}>Recent Transactions</h3>
          <input
            ref={searchRef}
            className="tx-search"
            placeholder="Search here"
            value={query}
            onChange={(e) => setQuery(e.target.value)}
          />

          {recent.length === 0 ? (
            <p style={{ color: "#999", fontSize: 13 }}>No matching transactions.</p>
          ) : (
            <ul className="tx-list">
              {recent.map((t) => (
                <li key={t.id} className="tx-item">
                  <div className="dot">{t.category.charAt(0).toUpperCase()}</div>
                  <div className="tx-info">
                    <div style={{ fontWeight: 600 }}>{t.title || t.description || "Untitled"}</div>
                    <div style={{ fontSize: 11, color: "#999" }}>
                      {t.category} ·{" "}
                      {new Date(t.date).toLocaleDateString("en-IN", { day: "2-digit", month: "short" })}
                    </div>
                  </div>
                  <div className={`tx-amount ${t.type === "INCOME" ? "green" : "red"}`}>
                    {t.type === "INCOME" ? "+" : "-"} {formatINR(t.amount)}
                  </div>
                </li>
              ))}
            </ul>
          )}
        </aside>
      </div>

      <div className="card">
        <h3 style={{ marginTop: 0 }}>Spending by Category</h3>
        {categoryData.length === 0 ? (
          <p style={{ color: "#999", fontSize: 13 }}>No expenses yet.</p>
        ) : (
          <div style={{ width: "100%", height: 300 }}>
            <ResponsiveContainer>
              <PieChart>
                <Pie
                  data={categoryData}
                  dataKey="value"
                  nameKey="name"
                  innerRadius={70}
                  outerRadius={110}
                  paddingAngle={3}
                >
                  {categoryData.map((_, i) => (
                    <Cell key={i} fill={PIE_COLORS[i % PIE_COLORS.length]} />
                  ))}
                </Pie>
                <Tooltip formatter={(v) => formatINR(v)} />
                <Legend />
              </PieChart>
            </ResponsiveContainer>
          </div>
        )}
      </div>
    </div>
  );
}