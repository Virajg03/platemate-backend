// src/components/orders/OrderDetailsModal.jsx
import React from "react";

export default function OrderDetailsModal({ order, close }) {
  return (
    <div className="fixed inset-0 bg-black/40 flex justify-center items-center p-4 z-50">
      <div className="bg-white w-full max-w-lg rounded-xl p-6 shadow-xl">
        <h2 className="text-lg font-semibold mb-4">
          Order Details (#{order.id})
        </h2>

        <div className="space-y-3 text-sm text-slate-700">
          <div>
            <strong>Customer:</strong> Customer #{order.customerId}
          </div>
          <div>
            <strong>Provider:</strong> {order.providerName}
          </div>
          <div>
            <strong>Total Amount:</strong> ₹{order.totalAmount}
          </div>
          <div>
            <strong>Status:</strong> {order.orderStatus}
          </div>

          {order.cartItems && order.cartItems.length > 0 && (
            <div className="mt-4">
              <h3 className="font-medium mb-2">Items</h3>
              <ul className="space-y-1">
                {order.cartItems.map((item) => (
                  <li
                    key={item.cartItemId}
                    className="flex justify-between bg-slate-50 p-2 rounded-lg border"
                  >
                    <span>
                      {item.itemName} × {item.quantity}
                    </span>
                    <span className="font-medium">
                      ₹{item.itemTotal}
                    </span>
                  </li>
                ))}
              </ul>
            </div>
          )}

          {order.deliveryAddress && (
            <div className="mt-4">
              <h3 className="font-medium mb-1">Delivery Address</h3>
              <p className="text-slate-600 text-sm">
                {order.deliveryAddress}
              </p>
            </div>
          )}
        </div>

        <div className="flex justify-end mt-6">
          <button
            onClick={close}
            className="px-4 py-2 bg-primary-600 text-white rounded-md hover:bg-primary-700 text-sm"
          >
            Close
          </button>
        </div>
      </div>
    </div>
  );
}
