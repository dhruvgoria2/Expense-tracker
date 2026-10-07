import { useEffect, useState } from "react";
import api from "../api/axios";

export default function Categories() {
  const [categories, setCategories] = useState([]);
  const [form, setForm] = useState({ name: "", type: "EXPENSE" });

  const load = () =>
    api.get("/categories").then((r) => setCategories(r.data)).catch(console.error);

  useEffect(() => {
    load();
  }, []);

  const handleChange = (e) => setForm({ ...form, [e.target.name]: e.target.value });

  const addCategory = async () => {
    if (!form.name.trim()) return;
    try {
      await api.post("/categories", { name: form.name.trim(), type: form.type });
      setForm({ name: "", type: "EXPENSE" });
      load();
    } catch (e) {
      console.error(e);
    }
  };

  return (
    <>
      <h1 style={{ marginBottom: 20 }}>Categories</h1>

      <div className="card">
        <h3>Add Category</h3>
        <div className="form-row">
          <input name="name" placeholder="Name (e.g. Food)" value={form.name} onChange={handleChange} />
          <select name="type" value={form.type} onChange={handleChange}>
            <option value="EXPENSE">Expense</option>
            <option value="INCOME">Income</option>
          </select>
          <button onClick={addCategory}>Add</button>
        </div>
      </div>

      <div className="card">
        <table>
          <thead>
            <tr><th>Name</th><th>Type</th></tr>
          </thead>
          <tbody>
            {categories.map((c) => (
              <tr key={c.id}>
                <td>{c.name}</td>
                <td className={c.type === "INCOME" ? "green" : "red"}>{c.type}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </>
  );
}