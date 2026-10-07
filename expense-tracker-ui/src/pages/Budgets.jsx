import { useEffect, useState } from "react";
import api from "../api/axios";
import { formatINR } from "../utils/formatCurrency";

export default function Budgets() {
  const [budgets, setBudgets] = useState([]);
  const [categories, setCategories] = useState([]);
  const [form, setForm] = useState({ categoryId: "", amountLimit: "", startDate: "", endDate: "" });

  const load = () =>
    api.get("/budgets").then((r) => setBudgets(r.data)).catch(console.error);

  useEffect(() => {
    load();
    api.get("/categories").then((r) => setCategories(r.data)).catch(console.error);
  }, []);

  const handleChange = (e) => setForm({ ...form, [e.target.name]: e.target.value });

  const saveBudget = async () => {
    if (!form.categoryId || !form.amountLimit || !form.startDate || !form.endDate) return;
    try {
      await api.post("/budgets", {
        categoryId: Number(form.categoryId),
        amountLimit: Number(form.amountLimit),
        startDate: form.startDate,
        endDate: form.endDate,
      });
      setForm({ categoryId: "", amountLimit: "", startDate: "", endDate: "" });
      load();
    } catch (e) {
      console.error(e);
    }
  };

  const deleteBudget = async (id) => {
    await api.delete(`/budgets/${id}`);
    load();
  };

  return (
    <>
      <h1 style={{ marginBottom: 20 }}>Budgets</h1>

      <div className="card">
        <h3>Set Budget</h3>
        <div className="form-row">
          <select name="categoryId" value={form.categoryId} onChange={handleChange}>
            <option value="">Category</option>
            {categories
              .filter((c) => c.type === "EXPENSE")
              .map((c) => (
                <option key={c.id} value={c.id}>{c.name}</option>
              ))}
          </select>
          <input name="amountLimit" type="number" placeholder="Limit (₹)" value={form.amountLimit} onChange={handleChange} />
          <input name="startDate" type="date" value={form.startDate} onChange={handleChange} />
          <input name="endDate" type="date" value={form.endDate} onChange={handleChange} />
          <button onClick={saveBudget}>Save</button>
        </div>
      </div>

      <div className="card">
        <table>
          <thead>
            <tr><th>Category</th><th>Period</th><th>Spent / Limit</th><th>Progress</th><th></th></tr>
          </thead>
          <tbody>
            {budgets.map((b) => {
              const pct = Math.min((b.spent / b.amountLimit) * 100, 100);
              return (
                <tr key={b.id}>
                  <td>{b.categoryName}</td>
                  <td>{b.startDate} → {b.endDate}</td>
                  <td className={b.overBudget ? "red" : ""}>
                    {formatINR(b.spent)} / {formatINR(b.amountLimit)}
                  </td>
                  <td style={{ width: 160 }}>
                    <div style={{ background: "#e9e5f5", borderRadius: 6, height: 8 }}>
                      <div style={{
                        width: `${pct}%`, height: 8, borderRadius: 6,
                        background: b.overBudget ? "#dc2626" : "#7c3aed",
                      }} />
                    </div>
                  </td>
                  <td><button onClick={() => deleteBudget(b.id)}>Delete</button></td>
                </tr>
              );
            })}
          </tbody>
        </table>
      </div>
    </>
  );
}