import React, { useState } from "react";
import { updateCategory } from "../../api/categoriesApi";

export default function EditCategoryModal({ close, data, refresh }) {
  const [form, setForm] = useState({
    categoryName: data.categoryName,
    description: data.description,
  });

  const handleChange = (e) =>
    setForm({ ...form, [e.target.name]: e.target.value });

  const submit = async (e) => {
    e.preventDefault();
    await updateCategory(data.id, form);
    refresh();
    close();
  };

  return (
    <div className="fixed inset-0 bg-black/40 flex items-center justify-center">
      <div className="bg-white p-6 w-full max-w-md rounded-xl">
        <h3 className="text-lg font-semibold mb-4">Edit Category</h3>

        <form className="space-y-4" onSubmit={submit}>
          <input
            name="categoryName"
            className="w-full p-2 border rounded"
            placeholder="Category Name"
            value={form.categoryName}
            onChange={handleChange}
          />

          <textarea
            name="description"
            className="w-full p-2 border rounded"
            rows="3"
            value={form.description}
            onChange={handleChange}
          />

          <div className="flex justify-end space-x-3">
            <button onClick={close} type="button" className="px-4 py-2 bg-slate-200 rounded">
              Cancel
            </button>

            <button type="submit" className="px-4 py-2 bg-primary-600 text-white rounded">
              Save Changes
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}

