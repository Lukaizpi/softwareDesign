import { useState } from "react";
import { OrdersApi } from "../api/orders";

export default function DiscountForm({ orderId, onDone }) {
  const [amount, setAmount] = useState(0);
  const [err, setErr] = useState("");

  const apply = async () => {
    try {
      setErr("");
      await OrdersApi.discount(orderId, { amount: Number(amount) });
      onDone();
    } catch (e) { setErr(e.message); }
  };

  return (
    <div style={{ marginTop: 12 }}>
      <h4>Descuento</h4>
      {err && <p style={{ color: "crimson" }}>{err}</p>}
      <input type="number" min="0" value={amount} onChange={(e) => setAmount(e.target.value)} />
      <button onClick={apply} style={{ marginLeft: 8 }}>Aplicar</button>
    </div>
  );
}