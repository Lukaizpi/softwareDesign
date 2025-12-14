import { useState } from "react";
import { OrdersApi } from "../api/orders";

export default function PaymentForm({ orderId, onDone }) {
  const [method, setMethod] = useState("CASH");
  const [amount, setAmount] = useState(0);
  const [tip, setTip] = useState(0);
  const [err, setErr] = useState("");

  const pay = async () => {
    try {
      setErr("");
      await OrdersApi.addPayment(orderId, { method, amount: Number(amount), tip: Number(tip) });
      onDone();
    } catch (e) { setErr(e.message); }
  };

  return (
    <div style={{ marginTop: 12 }}>
      <h4>Pago (split checks)</h4>
      {err && <p style={{ color: "crimson" }}>{err}</p>}

      <select value={method} onChange={(e) => setMethod(e.target.value)}>
        <option value="CASH">CASH</option>
        <option value="CARD">CARD</option>
        <option value="GIFT_CARD">GIFT_CARD</option>
      </select>

      <input type="number" min="0" value={amount} onChange={(e) => setAmount(e.target.value)} placeholder="amount" style={{ width: 110, marginLeft: 8 }} />
      <input type="number" min="0" value={tip} onChange={(e) => setTip(e.target.value)} placeholder="tip" style={{ width: 110, marginLeft: 8 }} />

      <button onClick={pay} style={{ marginLeft: 8 }}>Añadir pago</button>
    </div>
  );
}