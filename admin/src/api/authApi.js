// src/api/authApi.js
import axiosClient from "./axiosClient";

export const loginApi = (data) => axiosClient.post("/api/auth/login", data);

// For later if needed
export const signupAdminApi = (data) =>
  axiosClient.post("/api/auth/signup", data);
