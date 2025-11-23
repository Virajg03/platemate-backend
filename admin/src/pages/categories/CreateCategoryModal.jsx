import React, { useState } from "react";
import { createCategory } from "../../api/categoriesApi";

export default function CreateCategoryModal({ close, refresh }) {
  const [form, setForm] = useState({
    categoryName: "",
    description: "",
  });

  const handleChange = (e) =>
    setForm({ ...form, [e.target.name]: e.target.value });

  const handleSubmit = async (e) => {
    e.preventDefault();
    await createCategory(form);
    refresh();
    close();
  };

  return (
    <div className="fixed inset-0 flex items-center justify-center bg-black/40">
      <div className="bg-white p-6 w-full max-w-md rounded-xl shadow-lg">
        <h3 className="text-lg font-semibold mb-4">Create Category</h3>

        <form onSubmit={handleSubmit} className="space-y-4">
          <input
            name="categoryName"
            placeholder="Category Name"
            className="w-full p-2 border rounded"
            value={form.categoryName}
            onChange={handleChange}
            required
          />

          <textarea
            name="description"
            placeholder="Description"
            className="w-full p-2 border rounded"
            rows="3"
            value={form.description}
            onChange={handleChange}
          />

          <div className="flex justify-end space-x-3">
            <button
              type="button"
              onClick={close}
              className="px-4 py-2 bg-slate-200 rounded"
            >
              Cancel
            </button>
            <button
              type="submit"
              className="px-4 py-2 bg-primary-600 text-white rounded"
            >
              Create
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
