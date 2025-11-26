// src/components/providers/ProviderDetailsModal.jsx
import React, { useEffect, useState } from "react";
import { getProviderById } from "../../api/providersApi";

export default function ProviderDetailsModal({ providerId, close }) {
  const [provider, setProvider] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchProviderDetails = async () => {
      try {
        setLoading(true);
        const res = await getProviderById(providerId);
        setProvider(res.data);
      } catch (error) {
        console.error("Error fetching provider details:", error);
      } finally {
        setLoading(false);
      }
    };

    if (providerId) {
      fetchProviderDetails();
    }
  }, [providerId]);

  if (loading) {
    return (
      <div className="fixed inset-0 bg-black/40 flex justify-center items-center p-4 z-50">
        <div className="bg-white w-full max-w-3xl rounded-xl shadow-xl p-6">
          <div className="text-center text-slate-500">
            Loading provider details...
          </div>
        </div>
      </div>
    );
  }

  if (!provider) {
    return (
      <div className="fixed inset-0 bg-black/40 flex justify-center items-center p-4 z-50">
        <div className="bg-white w-full max-w-3xl rounded-xl shadow-xl p-6">
          <div className="text-center text-red-500">Provider not found</div>
          <button
            onClick={close}
            className="mt-4 px-4 py-2 bg-slate-600 text-white rounded-lg hover:bg-slate-700"
          >
            Close
          </button>
        </div>
      </div>
    );
  }

  return (
    <div
      className="fixed inset-0 bg-black/40 flex justify-center items-center p-4 z-50"
      onClick={close}
    >
      <div
        className="bg-white w-full max-w-3xl rounded-xl shadow-xl p-6 max-h-[90vh] overflow-y-auto"
        onClick={(e) => e.stopPropagation()}
      >
        {/* Header */}
        <div className="flex justify-between items-center mb-6">
          <h2 className="text-xl font-semibold text-slate-900">
            Provider Details
          </h2>
          <button
            onClick={close}
            className="text-slate-400 hover:text-slate-600 text-2xl leading-none"
          >
            ×
          </button>
        </div>

        {/* Provider Information */}
        <div className="space-y-4">
          {/* Basic Information */}
          <div className="bg-slate-50 rounded-lg p-4 space-y-3">
            <h3 className="font-semibold text-slate-900 mb-3">
              Basic Information
            </h3>

            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div>
                <label className="text-xs text-slate-500 uppercase">
                  Provider ID
                </label>
                <p className="text-sm font-medium text-slate-900 mt-1">
                  {provider.id}
                </p>
              </div>

              <div>
                <label className="text-xs text-slate-500 uppercase">
                  Business Name
                </label>
                <p className="text-sm font-medium text-slate-900 mt-1">
                  {provider.businessName || "N/A"}
                </p>
              </div>

              {provider.user && (
                <>
                  <div>
                    <label className="text-xs text-slate-500 uppercase">
                      Owner Username
                    </label>
                    <p className="text-sm font-medium text-slate-900 mt-1">
                      {provider.user.username || "N/A"}
                    </p>
                  </div>

                  <div>
                    <label className="text-xs text-slate-500 uppercase">
                      Owner Email
                    </label>
                    <p className="text-sm font-medium text-slate-900 mt-1">
                      {provider.user.email || "N/A"}
                    </p>
                  </div>

                  {provider.user.phoneNumber && (
                    <div>
                      <label className="text-xs text-slate-500 uppercase">
                        Phone Number
                      </label>
                      <p className="text-sm font-medium text-slate-900 mt-1">
                        {provider.user.phoneNumber}
                      </p>
                    </div>
                  )}
                </>
              )}

              <div>
                <label className="text-xs text-slate-500 uppercase">
                  Verification Status
                </label>
                <p className="text-sm font-medium text-slate-900 mt-1">
                  <span
                    className={`inline-block px-2 py-1 rounded text-xs ${
                      provider.isVerified
                        ? "bg-green-100 text-green-700"
                        : "bg-yellow-100 text-yellow-700"
                    }`}
                  >
                    {provider.isVerified ? "Verified" : "Pending"}
                  </span>
                </p>
              </div>
            </div>
          </div>

          {/* Business Details */}
          <div className="bg-slate-50 rounded-lg p-4 space-y-3">
            <h3 className="font-semibold text-slate-900 mb-3">
              Business Details
            </h3>

            {provider.description && (
              <div>
                <label className="text-xs text-slate-500 uppercase">
                  Description
                </label>
                <p className="text-sm text-slate-900 mt-1">
                  {provider.description}
                </p>
              </div>
            )}

            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div>
                <label className="text-xs text-slate-500 uppercase">
                  Commission Rate
                </label>
                <p className="text-sm font-medium text-slate-900 mt-1">
                  {provider.commissionRate !== null &&
                  provider.commissionRate !== undefined
                    ? `${provider.commissionRate}%`
                    : "N/A"}
                </p>
              </div>

              <div>
                <label className="text-xs text-slate-500 uppercase">
                  Provides Delivery
                </label>
                <p className="text-sm font-medium text-slate-900 mt-1">
                  <span
                    className={`inline-block px-2 py-1 rounded text-xs ${
                      provider.providesDelivery
                        ? "bg-green-100 text-green-700"
                        : "bg-slate-100 text-slate-700"
                    }`}
                  >
                    {provider.providesDelivery ? "Yes" : "No"}
                  </span>
                </p>
              </div>

              {provider.deliveryRadius && (
                <div>
                  <label className="text-xs text-slate-500 uppercase">
                    Delivery Radius
                  </label>
                  <p className="text-sm font-medium text-slate-900 mt-1">
                    {provider.deliveryRadius} km
                  </p>
                </div>
              )}
            </div>
          </div>

          {/* Delivery Zone Information */}
          {provider.zone && (
            <div className="bg-slate-50 rounded-lg p-4 space-y-3">
              <h3 className="font-semibold text-slate-900 mb-3">
                Delivery Zone
              </h3>

              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                <div>
                  <label className="text-xs text-slate-500 uppercase">
                    Zone Name
                  </label>
                  <p className="text-sm font-medium text-slate-900 mt-1">
                    {provider.zone.zoneName || "N/A"}
                  </p>
                </div>

                <div>
                  <label className="text-xs text-slate-500 uppercase">
                    City
                  </label>
                  <p className="text-sm font-medium text-slate-900 mt-1">
                    {provider.zone.city || "N/A"}
                  </p>
                </div>

                {provider.zone.pincodeRanges && (
                  <div className="md:col-span-2">
                    <label className="text-xs text-slate-500 uppercase">
                      Pincode Ranges
                    </label>
                    <p className="text-sm font-medium text-slate-900 mt-1">
                      {provider.zone.pincodeRanges}
                    </p>
                  </div>
                )}
              </div>
            </div>
          )}

          {/* Address Information */}
          {provider.user && provider.user.address && (
            <div className="bg-slate-50 rounded-lg p-4 space-y-3">
              <h3 className="font-semibold text-slate-900 mb-3">
                Address Information
              </h3>

              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                {provider.user.address.street && (
                  <div className="md:col-span-2">
                    <label className="text-xs text-slate-500 uppercase">
                      Street
                    </label>
                    <p className="text-sm font-medium text-slate-900 mt-1">
                      {provider.user.address.street}
                    </p>
                  </div>
                )}

                {provider.user.address.city && (
                  <div>
                    <label className="text-xs text-slate-500 uppercase">
                      City
                    </label>
                    <p className="text-sm font-medium text-slate-900 mt-1">
                      {provider.user.address.city}
                    </p>
                  </div>
                )}

                {provider.user.address.state && (
                  <div>
                    <label className="text-xs text-slate-500 uppercase">
                      State
                    </label>
                    <p className="text-sm font-medium text-slate-900 mt-1">
                      {provider.user.address.state}
                    </p>
                  </div>
                )}

                {provider.user.address.pincode && (
                  <div>
                    <label className="text-xs text-slate-500 uppercase">
                      Pincode
                    </label>
                    <p className="text-sm font-medium text-slate-900 mt-1">
                      {provider.user.address.pincode}
                    </p>
                  </div>
                )}
              </div>
            </div>
          )}
        </div>

        {/* Close Button */}
        <div className="flex justify-end mt-6">
          <button
            onClick={close}
            className="px-5 py-2 bg-sky-600 text-white rounded-lg hover:bg-sky-700 transition"
          >
            Close
          </button>
        </div>
      </div>
    </div>
  );
}
