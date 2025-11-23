// src/components/providers/ProviderDetailsModal.jsx
import React from "react";

export default function ProviderDetailsModal({ data, close }) {
  return (
    <div className="fixed inset-0 bg-black/40 flex justify-center items-center p-4 z-50">
      <div className="bg-white w-full max-w-lg rounded-xl shadow-xl p-6">
        <h2 className="text-lg font-semibold text-slate-900 mb-4">
          Provider Details
        </h2>

        <div className="space-y-2 text-sm">
          <div className="flex justify-between">
            <span className="text-slate-600">ID:</span>
            <span className="font-medium">{data.id}</span>
          </div>

          <div className="flex justify-between">
            <span className="text-slate-600">Business Name:</span>
            <span className="font-medium">{data.businessName}</span>
          </div>

          <div className="flex justify-between">
            <span className="text-slate-600">Phone:</span>
            <span className="font-medium">{data.phoneNumber}</span>
          </div>

          <div className="flex justify-between">
            <span className="text-slate-600">Approved:</span>
            <span className="font-medium">
              {data.isApproved ? "Yes" : "No"}
            </span>
          </div>

          <div className="mt-4 p-3 bg-slate-50 rounded-lg border">
            <h3 className="font-medium text-slate-800 mb-1">
              Description
            </h3>
            <p className="text-slate-600 text-sm">
              {data.description || "No description available."}
            </p>
          </div>
        </div>

        <div className="flex justify-end mt-6">
          <button
            onClick={close}
            className="px-4 py-2 text-sm bg-primary-600 text-white rounded-md hover:bg-primary-700"
          >
            Close
          </button>
        </div>
      </div>
    </div>
  );
}
