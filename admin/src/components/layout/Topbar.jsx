// src/components/layout/Topbar.jsx
import React from "react";
import { useAuthStore } from "../../store/authStore";

export default function Topbar({ title }) {
  const { username, logout } = useAuthStore();

  const handleLogout = () => {
    logout();
  };

  return (
    <header className="h-14 border-b bg-white flex items-center justify-between px-4">
      <h2 className="text-base font-semibold text-slate-800">{title}</h2>

      <div className="flex items-center gap-3">
        <span className="text-sm text-slate-600">
          Logged in as <span className="font-medium">{username}</span>
        </span>
        <button
          onClick={handleLogout}
          className="px-3 py-1.5 text-xs rounded-lg border text-slate-700 hover:bg-slate-100"
        >
          Logout
        </button>
      </div>
    </header>
  );
}
