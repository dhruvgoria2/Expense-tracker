import { useEffect, useState } from "react";
import api from "../../api/axios";
import StatCard from "../StatCard";
import BankAccountsList from "./BankAccountsList";
import { formatINR } from "../../utils/formatcurrency";

const COLORS = [
  "linear-gradient(135deg, #004b93 0%, #0072bc 100%)",
  "linear-gradient(135deg, #0a7e8c 0%, #00aeb3 100%)",
  "linear-gradient(135deg, #b85c00 0%, #e67e22 100%)",
  "linear-gradient(135deg, #5200b8 0%, #7a22e6 100%)",
];

const emptyForm = { bankName: "", accountType: "Savings", accountNumber: "", balance: "" };

export default function BankAccounts() {
  const [accounts, setAccounts] = useState([]);
  const [form, setForm] = useState(emptyForm);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  const load = () =>
    api
      .get("/bank-accounts")
      .then((res) => {
        const list = Array.isArray(res.data) ? res.data : [];
        setAccounts(
          list.map((a, i) => ({
            ...a,
            balance: Number(a.balance) || 0,
            themeColor: COLORS[i % COLORS.length],
          }))
        );
        setError("");
      })
      .catch((err) => {
        console.error("Bank accounts error:", err);
        setError("Failed to load bank accounts");
      })
      .finally(() => setLoading(false));

  useEffect(() => {
    load();
  }, []);

  const handleAdd = async (e) => {
    e.preventDefault();
    try {
      await api.post("/bank-accounts", {
        bankName: form.bankName.trim(),
        accountType: form.accountType,
        accountNumber: form.accountNumber.slice(-4),
        balance: Number(form.balance),
      });
      setForm(emptyForm);
      load();
    } catch (err) {
      console.error(err);
      setError("Failed to add account");
    }
  };

  const handleUpdateBalance = async (a) => {
    const input = window.prompt(`New balance for ${a.bankName}:`, a.balance);
    if (input === null || input.trim() === "" || isNaN(Number(input))) return;
    try {
      await api.put(`/bank-accounts/${a.id}`, {
        bankName: a.bankName,
        accountType: a.accountType,
        accountNumber: a.accountNumber,
        balance: Number(input),
      });
      load();
    } catch (err) {
      console.error(err);
      setError("Failed to update balance");
    }
  };

  const handleDelete = async (id) => {
    if (!window.confirm("Delete this account?")) return;
    try {
      await api.delete(`/bank-accounts/${id}`);
      load();
    } catch (err) {
      console.error(err);
      setError("Failed to delete account");
    }
  };

  if (loading) return <p style={{ padding: 20 }}>Loading...</p>;

  const total = accounts.reduce((s, a) => s + a.balance, 0);

  return (
    <div className="main">
      <h1 className="page-title">Bank Accounts</h1>
      {error && <p style={{ color: "#dc2626" }}>{error}</p>}

      <div className="stats" style={{ gridTemplateColumns: "repeat(2, 1fr)" }}>
        <StatCard title="Total Saved" value={total} bg="#ece6fa" note="Across all banks" />
        <StatCard title="Linked Accounts" value={accounts.length} bg="#e6f4ea" note="Banks added" />
      </div>

      {accounts.length === 0 ? (
        <p style={{ color: "#999", margin: "20px 0" }}>No accounts yet. Add your first one below.</p>
      ) : (
        <BankAccountsList accounts={accounts} />
      )}

      <div className="card" style={{ marginTop: 24 }}>
        <h3 style={{ marginTop: 0 }}>Add Bank Account</h3>
        <form onSubmit={handleAdd} style={{ display: "flex", gap: 10, flexWrap: "wrap" }}>
          <input required placeholder="Bank name" value={form.bankName}
            onChange={(e) => setForm({ ...form, bankName: e.target.value })} />
          <select value={form.accountType}
            onChange={(e) => setForm({ ...form, accountType: e.target.value })}>
            <option>Savings</option>
            <option>Current</option>
            <option>FD</option>
          </select>
          <input required placeholder="Last 4 digits" maxLength={4} value={form.accountNumber}
            onChange={(e) => setForm({ ...form, accountNumber: e.target.value })} />
          <input required type="number" min="0" step="0.01" placeholder="Balance" value={form.balance}
            onChange={(e) => setForm({ ...form, balance: e.target.value })} />
          <button type="submit">Add</button>
        </form>
      </div>

      {accounts.length > 0 && (
        <div className="card" style={{ marginTop: 24 }}>
          <h3 style={{ marginTop: 0 }}>Manage Accounts</h3>
          <div style={{ overflowX: "auto" }}>
            <table>
              <thead>
                <tr>
                  <th style={{ textAlign: "left" }}>Bank</th>
                  <th>Type</th>
                  <th>Account</th>
                  <th style={{ textAlign: "right" }}>Balance</th>
                  <th></th>
                </tr>
              </thead>
              <tbody>
                {accounts.map((a) => (
                  <tr key={a.id}>
                    <td style={{ fontWeight: 600 }}>{a.bankName}</td>
                    <td>{a.accountType}</td>
                    <td>•••• {a.accountNumber}</td>
                    <td style={{ textAlign: "right", fontWeight: 700 }}>{formatINR(a.balance)}</td>
                    <td style={{ whiteSpace: "nowrap" }}>
                      <button onClick={() => handleUpdateBalance(a)}>Edit</button>{" "}
                      <button onClick={() => handleDelete(a.id)}>Delete</button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}
    </div>
  );
}