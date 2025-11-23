// src/api/categoriesApi.js
import api from "./axiosClient";

// GET ALL
export const getCategories = () => api.get("/api/categories");

// GET ONE
export const getCategoryById = (id) =>
  api.get(`/api/categories/${id}`);

// CREATE
export const createCategory = (data) =>
  api.post("/api/categories", data);

// UPDATE
export const updateCategory = (id, data) =>
  api.put(`/api/categories/${id}`, data);

// DELETE
export const deleteCategory = (id) =>
  api.delete(`/api/categories/${id}`);
