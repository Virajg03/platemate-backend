// // src/pages/providers/ProvidersPage.jsx
// import React, { useEffect, useState } from "react";
// import {
//   getProviders,
//   approveProvider,
//   rejectProvider,
//   deleteProvider,
//   getProviderById,
// } from "../../api/providersApi";

// export default function ProvidersPage() {
//   const [providers, setProviders] = useState([]);
//   const [loading, setLoading] = useState(true);

//   const fetchData = async () => {
//     setLoading(true);
//     try {
//       const res = await getProviders();
//       setProviders(res.data);
//     } catch (err) {
//       console.error("Failed to fetch providers", err);
//     } finally {
//       setLoading(false);
//     }
//   };

//   useEffect(() => {
//     fetchData();
//   }, []);

//   const handleApprove = async (id) => {
//     await approveProvider(id);
//     fetchData();
//   };

//   const handleReject = async (id) => {
//     await rejectProvider(id);
//     fetchData();
//   };

//   const handleDelete = async (id) => {
//     await deleteProvider(id);
//     fetchData();
//   };

//   return (
//     <div className="p-6">
//       <h2 className="text-xl font-semibold mb-4">All Providers</h2>

//       <div className="bg-white border rounded-xl shadow">
//         <table className="min-w-full">
//           <thead className="bg-slate-50 border-b">
//             <tr>
//               <th className="p-3 text-left text-sm font-semibold">ID</th>
//               <th className="p-3 text-left text-sm font-semibold">Business</th>
//               <th className="p-3 text-left text-sm font-semibold">Owner</th>
//               <th className="p-3 text-left text-sm font-semibold">Status</th>
//               <th className="p-3 text-right text-sm font-semibold">Actions</th>
//             </tr>
//           </thead>

//           <tbody>
//             {loading ? (
//               <tr>
//                 <td colSpan={5} className="p-6 text-center text-slate-500">
//                   Loading providers...
//                 </td>
//               </tr>
//             ) : providers.length === 0 ? (
//               <tr>
//                 <td colSpan={5} className="p-6 text-center text-slate-500">
//                   No providers found.
//                 </td>
//               </tr>
//             ) : (
//               providers.map((p) => (
//                 <tr key={p.id} className="border-b hover:bg-slate-50">
//                   <td className="p-3 text-sm">{p.id}</td>
//                   <td className="p-3 text-sm">{p.businessName}</td>
//                   <td className="p-3 text-sm">{p.user?.username}</td>

//                   <td className="p-3 text-sm">
//                     {p.isVerified ? (
//                       <span className="text-green-600 font-medium">
//                         Verified
//                       </span>
//                     ) : (
//                       <span className="text-yellow-600 font-medium">
//                         Pending
//                       </span>
//                     )}
//                   </td>

//                   <td className="p-3 text-right space-x-2">
//                     {!p.isVerified && (
//                       <>
//                         <button
//                           onClick={() => handleApprove(p.id)}
//                           className="px-3 py-1 bg-green-600 text-white text-xs rounded"
//                         >
//                           Approve
//                         </button>
//                         <button
//                           onClick={() => handleReject(p.id)}
//                           className="px-3 py-1 bg-yellow-600 text-white text-xs rounded"
//                         >
//                           Reject
//                         </button>
//                       </>
//                     )}

//                     <button
//                       onClick={() => handleDelete(p.id)}
//                       className="px-3 py-1 bg-red-600 text-white text-xs rounded"
//                     >
//                       Delete
//                     </button>
//                   </td>
//                 </tr>
//               ))
//             )}
//           </tbody>
//         </table>
//       </div>
//     </div>
//   );
// }

// src/pages/providers/ProvidersPage.jsx
import React, { useEffect, useState } from "react";
import {
  getProviders,
  approveProvider,
  rejectProvider,
  deleteProvider,
} from "../../api/providersApi";

export default function ProvidersPage() {
  const [providers, setProviders] = useState([]);
  const [loading, setLoading] = useState(true);

  const fetchData = async () => {
    setLoading(true);
    try {
      const res = await getProviders();
      setProviders(res.data);
    } catch (err) {
      console.error("Failed to fetch providers", err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchData();
  }, []);

  const handleApprove = async (id) => {
    await approveProvider(id);
    fetchData();
  };

  const handleReject = async (id) => {
    await rejectProvider(id);
    fetchData();
  };

  const handleDelete = async (id) => {
    await deleteProvider(id);
    fetchData();
  };

  return (
    <div className="p-4 md:p-6">
      {/* HEADER */}
      <div className="mb-6">
        <h2 className="text-xl font-semibold text-slate-900">Providers</h2>
        <p className="text-sm text-slate-600">
          Manage all tiffin providers registered on the platform.
        </p>
      </div>

      <div className="bg-white border rounded-xl shadow-sm overflow-hidden transition-all">

        {/* DESKTOP TABLE */}
        <div className="hidden md:block overflow-x-auto">
          <table className="min-w-full">
            <thead className="bg-slate-100 border-b">
              <tr>
                <th className="p-3 text-left text-sm font-semibold">ID</th>
                <th className="p-3 text-left text-sm font-semibold">Business</th>
                <th className="p-3 text-left text-sm font-semibold">Owner</th>
                <th className="p-3 text-left text-sm font-semibold">Status</th>
                <th className="p-3 text-right text-sm font-semibold">
                  Actions
                </th>
              </tr>
            </thead>

            <tbody className="animate-fadeIn">
              {loading ? (
                <tr>
                  <td colSpan={5} className="p-6 text-center text-slate-500">
                    Loading providers...
                  </td>
                </tr>
              ) : providers.length === 0 ? (
                <tr>
                  <td colSpan={5} className="p-6 text-center text-slate-500">
                    No providers found.
                  </td>
                </tr>
              ) : (
                providers.map((p) => (
                  <tr
                    key={p.id}
                    className="border-b hover:bg-slate-50 transition-all"
                  >
                    <td className="p-3 text-sm">{p.id}</td>
                    <td className="p-3 text-sm">{p.businessName}</td>
                    <td className="p-3 text-sm">{p.user?.username}</td>

                    <td className="p-3 text-sm">
                      {p.isVerified ? (
                        <span className="text-green-600 font-medium">
                          Verified
                        </span>
                      ) : (
                        <span className="text-yellow-600 font-medium">
                          Pending
                        </span>
                      )}
                    </td>

                    <td className="p-3 text-right space-x-2">
                      {!p.isVerified && (
                        <>
                          <button
                            onClick={() => handleApprove(p.id)}
                            className="px-3 py-1 bg-green-600 text-white text-xs rounded hover:bg-green-700 transition active:scale-95"
                          >
                            Approve
                          </button>

                          <button
                            onClick={() => handleReject(p.id)}
                            className="px-3 py-1 bg-yellow-500 text-white text-xs rounded hover:bg-yellow-600 transition active:scale-95"
                          >
                            Reject
                          </button>
                        </>
                      )}

                      <button
                        onClick={() => handleDelete(p.id)}
                        className="px-3 py-1 bg-red-600 text-white text-xs rounded hover:bg-red-700 transition active:scale-95"
                      >
                        Delete
                      </button>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>

        {/* MOBILE CARD VIEW */}
        <div className="md:hidden p-3 space-y-4 animate-fadeIn">
          {loading ? (
            <p className="text-center text-slate-500">Loading providers...</p>
          ) : providers.length === 0 ? (
            <p className="text-center text-slate-500">No providers found.</p>
          ) : (
            providers.map((p) => (
              <div
                key={p.id}
                className="bg-white border rounded-xl p-4 shadow-sm hover:shadow-md transition-all"
              >
                <div className="flex justify-between">
                  <h3 className="text-sm font-semibold text-slate-900">
                    {p.businessName}
                  </h3>

                  <span
                    className={`text-xs px-2 py-1 rounded ${
                      p.isVerified
                        ? "bg-green-100 text-green-700"
                        : "bg-yellow-100 text-yellow-700"
                    }`}
                  >
                    {p.isVerified ? "Verified" : "Pending"}
                  </span>
                </div>

                <p className="text-sm text-slate-600 mt-1">
                  Owner:{" "}
                  <span className="font-medium">{p.user?.username}</span>
                </p>

                <div className="flex justify-end gap-3 mt-4">
                  {!p.isVerified && (
                    <>
                      <button
                        onClick={() => handleApprove(p.id)}
                        className="text-green-600 text-sm hover:underline active:scale-95"
                      >
                        Approve
                      </button>

                      <button
                        onClick={() => handleReject(p.id)}
                        className="text-yellow-600 text-sm hover:underline active:scale-95"
                      >
                        Reject
                      </button>
                    </>
                  )}

                  <button
                    onClick={() => handleDelete(p.id)}
                    className="text-red-600 text-sm hover:underline active:scale-95"
                  >
                    Delete
                  </button>
                </div>
              </div>
            ))
          )}
        </div>
      </div>
    </div>
  );
}
