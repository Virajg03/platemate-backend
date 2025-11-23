// // src/pages/Dashboard.jsx
// import React from "react";

// export default function Dashboard() {
//   return (
//     <div className="p-6 space-y-4">
//       <h1 className="text-xl font-semibold text-slate-900">
//         Dashboard Overview
//       </h1>
//       <p className="text-sm text-slate-600">
//         Here you’ll see total users, providers, orders, payouts & pending approvals.
//       </p>

//       <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
//         <div className="bg-white border rounded-xl p-4 shadow-sm">
//           <p className="text-xs text-slate-500">Total Users</p>
//           <p className="text-2xl font-semibold mt-1">—</p>
//         </div>

//         <div className="bg-white border rounded-xl p-4 shadow-sm">
//           <p className="text-xs text-slate-500">Providers</p>
//           <p className="text-2xl font-semibold mt-1">—</p>
//         </div>

//         <div className="bg-white border rounded-xl p-4 shadow-sm">
//           <p className="text-xs text-slate-500">Orders</p>
//           <p className="text-2xl font-semibold mt-1">—</p>
//         </div>

//         <div className="bg-white border rounded-xl p-4 shadow-sm">
//           <p className="text-xs text-slate-500">Pending Providers</p>
//           <p className="text-2xl font-semibold mt-1">—</p>
//         </div>
//       </div>
//     </div>
//   );
// }


// src/pages/Dashboard.jsx
import React, { useEffect, useState } from "react";

export default function Dashboard() {
  const [loading, setLoading] = useState(true);

  const [stats, setStats] = useState({
    totalUsers: 0,
    totalProviders: 0,
    pendingProviders: 0,
    totalOrders: 0,
  });

  const token = localStorage.getItem("admin_token"); // assuming you stored admin token here

  const fetchDashboardStats = async () => {
    try {
      const headers = {
        Authorization: `Bearer ${token}`,
      };

      // Fetch all data in parallel (faster)
      const [usersRes, providersRes, pendingRes, ordersRes] = await Promise.all([
        fetch(`${import.meta.env.VITE_API_URL}/api/admin/users`, { headers }),
        fetch(`${import.meta.env.VITE_API_URL}/api/admin/providers`, { headers }),
        fetch(`${import.meta.env.VITE_API_URL}/api/admin/providers/pending`, { headers }),
        fetch(`${import.meta.env.VITE_API_URL}/api/admin/orders`, { headers }),
      ]);

      const users = await usersRes.json();
      const providers = await providersRes.json();
      const pending = await pendingRes.json();
      const orders = await ordersRes.json();

      setStats({
        totalUsers: users.length,
        totalProviders: providers.length,
        pendingProviders: pending.length,
        totalOrders: orders.length,
      });

    } catch (err) {
      console.error("Failed to load dashboard stats", err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchDashboardStats();
  }, []);

  if (loading) {
    return (
      <div className="p-6 text-slate-600 animate-pulse">
        Loading dashboard data...
      </div>
    );
  }

  return (
    <div className="p-6 space-y-4">
      <h1 className="text-xl font-semibold text-slate-900">
        Dashboard Overview
      </h1>
      <p className="text-sm text-slate-600">
        Live stats from PlateMate admin system.
      </p>

      <div className="grid grid-cols-1 md:grid-cols-4 gap-4">

        {/* Users */}
        <div className="bg-white border rounded-xl p-5 shadow-sm hover:shadow-md transition">
          <p className="text-xs text-slate-500">Total Users</p>
          <p className="text-3xl font-bold mt-2 text-slate-900">
            {stats.totalUsers}
          </p>
        </div>

        {/* Providers */}
        <div className="bg-white border rounded-xl p-5 shadow-sm hover:shadow-md transition">
          <p className="text-xs text-slate-500">Providers</p>
          <p className="text-3xl font-bold mt-2 text-slate-900">
            {stats.totalProviders}
          </p>
        </div>

        {/* Orders */}
        <div className="bg-white border rounded-xl p-5 shadow-sm hover:shadow-md transition">
          <p className="text-xs text-slate-500">Orders</p>
          <p className="text-3xl font-bold mt-2 text-slate-900">
            {stats.totalOrders}
          </p>
        </div>

        {/* Pending Providers */}
        <div className="bg-white border rounded-xl p-5 shadow-sm hover:shadow-md transition">
          <p className="text-xs text-slate-500">Pending Providers</p>
          <p className="text-3xl font-bold mt-2 text-slate-900">
            {stats.pendingProviders}
          </p>
        </div>

      </div>
    </div>
  );
}
