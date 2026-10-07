import { useEffect, useState } from "react";
import api from "../api/axios";
import { formatINR } from "../utils/formatCurrency";

export default function Transactions() {
  const [txns, setTxns] = useState([]);
  const [categories, setCategories] = useState([]);
  const [search, setSearch] = useState("");
  const [typeFilter, setTypeFilter] = useState("ALL");
  const [form, setForm] = useState({ amount: "", description: "", date: "", type: "EXPENSE", categoryId: "" });

  const load = () =>
    api.get("/transactions").then((r) => setTxns(r.data)).catch(console.error);

  useEffect(() => {
    load();
    api.get("/categories").then((r) => setCategories(r.data)).catch(console.error);
  }, []);

  const handleChange = (e) => setForm({ ...form, [e.target.name]: e.target.value });

  const addTxn = async () => {
    try {
      await api.post("/transactions", {
        amount: Number(form.amount),
        description: form.description,
        date: form.date,
        type: form.type,
        category: form.categoryId ? { id: Number(form.categoryId) } : null,
      });
      setForm({ amount: "", description: "", date: "", type: "EXPENSE", categoryId: "" });
      load();
    } catch (e) {
      console.error(e);
    }
  };

  const deleteTxn = async (id) => {
    await api.delete(`/transactions/${id}`);
    load();
  };

  const filtered = txns
    .filter((t) => typeFilter === "ALL" || t.type === typeFilter)
    .filter((t) => (t.description || "").toLowerCase().includes(search.toLowerCase()))
    .sort((a, b) => new Date(b.date) - new Date(a.date));

  const downloadCsv = () => {
    const rows = [
      ["Description", "Date", "Amount", "Type"],
      ...filtered.map((t) => [t.description, t.date, t.amount, t.type]),
    ];
    const csv = rows.map((r) => r.join(",")).join("\n");
    const a = document.createElement("a");
    a.href = URL.createObjectURL(new Blob([csv], { type: "text/csv" }));
    a.download = "transactions.csv";
    a.click();
  };

  return (
    <>
      <h1 style={{ marginBottom: 20 }}>Transactions</h1>

      <div className="card">
        <h3>Add Transaction</h3>
        <div className="form-row">
          <input name="description" placeholder="Description" value={form.description} onChange={handleChange} />
          <input name="amount" type="number" placeholder="Amount (₹)" value={form.amount} onChange={handleChange} />
          <input name="date" type="date" value={form.date} onChange={handleChange} />
          <select name="type" value={form.type} onChange={handleChange}>
            <option value="EXPENSE">Expense</option>
            <option value="INCOME">Income</option>
          </select>
          <select name="categoryId" value={form.categoryId} onChange={handleChange}>
            <option value="">Category</option>
            {categories.map((c) => (
              <option key={c.id} value={c.id}>{c.name}</option>
            ))}
          </select>
          <button onClick={addTxn}>Add</button>
        </div>
      </div>

      <div className="card">
        <div className="form-row">
          <input placeholder="Search here" value={search} onChange={(e) => setSearch(e.target.value)} />
          <select value={typeFilter} onChange={(e) => setTypeFilter(e.target.value)}>
            <option value="ALL">All</option>
            <option value="INCOME">Income</option>
            <option value="EXPENSE">Expense</option>
          </select>
          <button onClick={downloadCsv}>Download</button>
        </div>

        <table>
          <thead>
            <tr>
              <th>Name</th><th>Category</th><th>Date</th><th>Amount</th><th>Type</th><th></th>
            </tr>
          </thead>
          <tbody>
            {filtered.map((t) => (
              <tr key={t.id}>
                <td>{t.description}</td>
                <td>{t.category?.name || "-"}</td>
                <td>{t.date}</td>
                <td className={t.type === "INCOME" ? "green" : "red"}>{formatINR(t.amount)}</td>
                <td>{t.type}</td>
                <td><button onClick={() => deleteTxn(t.id)}>Delete</button></td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </>
  );
}