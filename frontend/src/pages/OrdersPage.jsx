import { useEffect, useState } from "react";
import { OrdersApi } from "../api/orders";
import OrderCreateForm from "../components/OrderCreateForm";
import AddItemForm from "../components/AddItemForm";
import PaymentForm from "../components/PaymentForm";
import DiscountForm from "../components/DiscountForm";

export default function OrdersPage() {
  const [orders, setOrders] = useState([]);
  const [selectedId, setSelectedId] = useState(null);
  const [err, setErr] = useState("");

  const refresh = async () => {
    try {
      setErr("");
      const data = await OrdersApi.list();
      setOrders(data);
      if (selectedId && !data.find(o => o.id === selectedId)) setSelectedId(null);
    } catch (e) {
      setErr(e.message);
    }
  };

  useEffect(() => { refresh(); }, []);

  const selected = orders.find(o => o.id === selectedId);

  const onCreate = async (payload) => {
    try {
      const created = await OrdersApi.create(payload);
      await refresh();
      setSelectedId(created.id);
    } catch (e) {
      setErr(e.message);
    }
  };

  return (
    <div>
      <h2>Orders</h2>
      {err && <p style={{ color: "crimson" }}>{err}</p>}

      <OrderCreateForm onCreate={onCreate} />

      <div style={{ display: "grid", gridTemplateColumns: "1fr 1.2fr", gap: 16 }}>
        <div>
          <h3>Listado</h3>
          <ul>
            {orders.map(o => (
              <li key={o.id}>
                <button
                  onClick={() => OrdersApi.refund(selected.id, { amount: 0 }).then(refresh)}
                >
                  Refund total
                </button>
              </li>
            ))}
          </ul>
        </div>

        <div>
          <h3>Detalle</h3>
          {!selected ? (
            <p>Selecciona una orden.</p>
          ) : (
            <div>
              <p>
                <b>ID:</b> {selected.id} | <b>Status:</b> {selected.status}{" "}
                {selected.refunded ? <span style={{ color: "crimson" }}> (REFUNDED)</span> : null}
              </p>

              <p>
                <b>Created:</b> {selected.createdAt ? new Date(selected.createdAt).toLocaleString() : "-"}
              </p>

              <p>
                <b>Subtotal:</b> {selected.subtotal} € | <b>Taxes:</b> {selected.taxes} € |{" "}
                <b>Discount:</b> {selected.discountAmount ?? 0} € |{" "}
                <b>Service:</b> {selected.serviceCharge ?? 0} € | <b>Total:</b> {selected.total} €
              </p>

              <h4>Items</h4>
              {(!selected.items || selected.items.length === 0) ? (
                <p>Sin items.</p>
              ) : (
                <ul>
                  {selected.items.map((it, idx) => (
                    <li key={idx}>
                      {it.productName} x{it.quantity} — {it.unitPrice}€ (tax {it.taxRate})
                    </li>
                  ))}
                </ul>
              )}

              <h4>Pagos</h4>
              <ul>
                {selected.payments?.map(p => (
                  <li key={p.id}>
                    {p.method} — {p.amount} € + tip {p.tip} €
                    {p.refunded && " (REFUNDED)"}
                  </li>
                ))}
              </ul>

              {selected.refunded && (
                <p style={{ color: "orange" }}>⚠ Orden refundada</p>
              )}


              <AddItemForm orderId={selected.id} onDone={refresh} />
              <DiscountForm orderId={selected.id} onDone={refresh} />
              <PaymentForm orderId={selected.id} onDone={refresh} />
            </div>
          )}
        </div>
      </div>
    </div>
  );
}