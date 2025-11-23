// // src/pages/orders/OrdersPage.jsx
// import React, { useEffect, useState } from "react";
// import { getAllOrders } from "../../api/ordersApi";
// import AssignPartnerModal from "./AssignPartnerModal";

// export default function OrdersPage() {
//   const [orders, setOrders] = useState([]);
//   const [loading, setLoading] = useState(true);

//   const [assignModal, setAssignModal] = useState({
//     show: false,
//     orderId: null,
//   });

//   const loadOrders = async () => {
//     try {
//       const res = await getAllOrders();
//       setOrders(res.data);
//     } catch (err) {
//       console.error("Failed to load orders", err);
//     } finally {
//       setLoading(false);
//     }
//   };

//   useEffect(() => {
//     loadOrders();
//   }, []);

//   const getStatusBadge = (status) => {
//     const styles = {
//       READY: "bg-blue-100 text-blue-700",
//       OUT_FOR_DELIVERY: "bg-yellow-100 text-yellow-700",
//       DELIVERED: "bg-green-100 text-green-700",
//       CANCELLED: "bg-red-100 text-red-700",
//     };

//     return (
//       <span
//         className={`px-2 py-1 rounded text-xs font-medium ${
//           styles[status] || "bg-gray-100 text-gray-700"
//         }`}
//       >
//         {status}
//       </span>
//     );
//   };

//   return (
//     <div className="p-6">
//       <h2 className="text-xl font-semibold text-slate-900 mb-4">
//         Orders Management
//       </h2>

//       <div className="bg-white border shadow-sm rounded-xl overflow-hidden">
//         <table className="min-w-full">
//           <thead className="bg-slate-50 border-b">
//             <tr>
//               <th className="p-3 text-left text-sm font-semibold">Order ID</th>
//               <th className="p-3 text-left text-sm font-semibold">Customer</th>
//               <th className="p-3 text-left text-sm font-semibold">Provider</th>
//               <th className="p-3 text-left text-sm font-semibold">Amount</th>
//               <th className="p-3 text-left text-sm font-semibold">Status</th>
//               <th className="p-3 text-right text-sm font-semibold">Actions</th>
//             </tr>
//           </thead>

//           <tbody>
//             {loading ? (
//               <tr>
//                 <td colSpan={6} className="p-5 text-center text-slate-500">
//                   Loading orders...
//                 </td>
//               </tr>
//             ) : orders.length === 0 ? (
//               <tr>
//                 <td colSpan={6} className="p-5 text-center text-slate-500">
//                   No orders found.
//                 </td>
//               </tr>
//             ) : (
//               orders.map((o) => (
//                 <tr key={o.id} className="border-b hover:bg-slate-50">
//                   <td className="p-3 text-sm">{o.id}</td>
//                   <td className="p-3 text-sm">{o.customerName}</td>
//                   <td className="p-3 text-sm">{o.providerName}</td>
//                   <td className="p-3 text-sm font-medium">₹{o.totalAmount}</td>
//                   <td className="p-3 text-sm">{getStatusBadge(o.orderStatus)}</td>

//                   <td className="p-3 text-right">
//                     <button className="px-3 py-1 text-sm text-primary-600 hover:text-primary-800">
//                       View
//                     </button>

//                     {o.orderStatus === "READY" && (
//                       <button
//                         onClick={() =>
//                           setAssignModal({ show: true, orderId: o.id })
//                         }
//                         className="ml-2 px-3 py-1 text-sm text-green-600 hover:text-green-800"
//                       >
//                         Assign Delivery
//                       </button>
//                     )}
//                   </td>
//                 </tr>
//               ))
//             )}
//           </tbody>
//         </table>
//       </div>

//       {/* 📌 Assign Partner Modal */}
//       {assignModal.show && (
//         <AssignPartnerModal
//           orderId={assignModal.orderId}
//           close={() => setAssignModal({ show: false, orderId: null })}
//           refresh={loadOrders}
//         />
//       )}
//     </div>
//   );
// }


// src/pages/orders/OrdersPage.jsx
import React, { useEffect, useState } from "react";
import { getAllOrders } from "../../api/ordersApi";
import AssignPartnerModal from "./AssignPartnerModal";

export default function OrdersPage() {
  const [orders, setOrders] = useState([]);
  const [loading, setLoading] = useState(true);

  const [assignModal, setAssignModal] = useState({
    show: false,
    orderId: null,
  });

  const loadOrders = async () => {
    try {
      const res = await getAllOrders();
      setOrders(res.data);
    } catch (err) {
      console.error("Failed to load orders", err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadOrders();
  }, []);

  const getStatusBadge = (status) => {
    const styles = {
      READY: "bg-blue-100 text-blue-700",
      OUT_FOR_DELIVERY: "bg-yellow-100 text-yellow-700",
      DELIVERED: "bg-green-100 text-green-700",
      CANCELLED: "bg-red-100 text-red-700",
    };

    return (
      <span
        className={`px-2 py-1 rounded text-xs font-medium ${
          styles[status] || "bg-gray-100 text-gray-700"
        }`}
      >
        {status}
      </span>
    );
  };

  return (
    <div className="p-6 animate-fadeIn">
      <h2 className="text-xl font-semibold text-slate-900 mb-4">
        Orders Management
      </h2>

      {/* Wrapper */}
      <div className="bg-white border shadow-sm rounded-xl overflow-hidden transition-all">

        {/* Desktop Table */}
        <div className="hidden md:block">
          <table className="min-w-full">
            <thead className="bg-slate-50 border-b">
              <tr>
                <th className="p-3 text-left text-sm font-semibold">Order ID</th>
                <th className="p-3 text-left text-sm font-semibold">Customer</th>
                <th className="p-3 text-left text-sm font-semibold">Provider</th>
                <th className="p-3 text-left text-sm font-semibold">Amount</th>
                <th className="p-3 text-left text-sm font-semibold">Status</th>
                <th className="p-3 text-right text-sm font-semibold">Actions</th>
              </tr>
            </thead>

            <tbody>
              {loading ? (
                <tr>
                  <td colSpan={6} className="p-5 text-center text-slate-500">
                    Loading orders...
                  </td>
                </tr>
              ) : orders.length === 0 ? (
                <tr>
                  <td colSpan={6} className="p-5 text-center text-slate-500">
                    No orders found.
                  </td>
                </tr>
              ) : (
                orders.map((o, index) => (
                  <tr
                    key={o.id}
                    className="border-b hover:bg-slate-50 transition-all animate-slideUp"
                    style={{ animationDelay: `${index * 90}ms` }}
                  >
                    <td className="p-3 text-sm">{o.id}</td>
                    <td className="p-3 text-sm">{o.customerName}</td>
                    <td className="p-3 text-sm">{o.providerName}</td>
                    <td className="p-3 text-sm font-medium">₹{o.totalAmount}</td>
                    <td className="p-3 text-sm">{getStatusBadge(o.orderStatus)}</td>

                    <td className="p-3 text-right">
                      <button className="px-3 py-1 text-sm text-primary-600 hover:text-primary-800 transition">
                        View
                      </button>

                      {o.orderStatus === "READY" && (
                        <button
                          onClick={() =>
                            setAssignModal({ show: true, orderId: o.id })
                          }
                          className="ml-2 px-3 py-1 text-sm text-green-600 hover:text-green-800 transition"
                        >
                          Assign Delivery
                        </button>
                      )}
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>

        {/* Mobile Cards */}
        <div className="md:hidden p-3 space-y-4">
          {loading ? (
            <p className="text-center text-slate-500">Loading orders...</p>
          ) : orders.length === 0 ? (
            <p className="text-center text-slate-500">No orders found.</p>
          ) : (
            orders.map((o, index) => (
              <div
                key={o.id}
                className="border rounded-lg p-4 shadow-sm hover:shadow-md transition-all animate-slideUp"
                style={{ animationDelay: `${index * 100}ms` }}
              >
                <div className="flex justify-between">
                  <span className="text-sm font-semibold text-slate-700">
                    #{o.id}
                  </span>
                  {getStatusBadge(o.orderStatus)}
                </div>

                <div className="mt-2 text-sm">
                  <p>
                    <strong>Customer:</strong> {o.customerName}
                  </p>
                  <p>
                    <strong>Provider:</strong> {o.providerName}
                  </p>
                  <p>
                    <strong>Amount:</strong> ₹{o.totalAmount}
                  </p>
                </div>

                <div className="mt-3 flex justify-between">
                  <button className="text-primary-600 text-sm">View</button>

                  {o.orderStatus === "READY" && (
                    <button
                      onClick={() =>
                        setAssignModal({ show: true, orderId: o.id })
                      }
                      className="text-green-600 text-sm"
                    >
                      Assign Delivery
                    </button>
                  )}
                </div>
              </div>
            ))
          )}
        </div>
      </div>

      {/* Modals */}
      {assignModal.show && (
        <div className="animate-fadeIn">
          <AssignPartnerModal
            orderId={assignModal.orderId}
            close={() => setAssignModal({ show: false, orderId: null })}
            refresh={loadOrders}
          />
        </div>
      )}
    </div>
  );
}
