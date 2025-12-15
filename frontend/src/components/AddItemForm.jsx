import { useEffect, useState } from "react";
import { ProductsApi } from "../api/products";
import { OrdersApi } from "../api/orders";

export default function AddItemForm({ orderId, onDone }) {
  const [products, setProducts] = useState([]);
  const [productId, setProductId] = useState("");
  const [quantity, setQuantity] = useState(1);
  const [err, setErr] = useState("");

  useEffect(() => {
    ProductsApi.list().then((p) => {
      setProducts(p);
      if (p[0]) setProductId(String(p[0].id));
    });
  }, []);

  const add = async () => {
    try {
      setErr("");
      await OrdersApi.addItem(orderId, { productId: Number(productId), quantity: Number(quantity) });
      onDone();
    } catch (e) {
      setErr(e.message);
    }
  };

  return (
    <div style={{ marginTop: 12, paddingTop: 12, borderTop: "1px solid #ddd" }}>
      <h4>Añadir item</h4>
      {err && <p style={{ color: "crimson" }}>{err}</p>}
      <select value={productId} onChange={(e) => setProductId(e.target.value)}>
        {products.map(p => <option key={p.id} value={p.id}>{p.name}</option>)}
      </select>
      <input type="number" min="1" value={quantity} onChange={(e) => setQuantity(e.target.value)} style={{ width: 80, marginLeft: 8 }} />
      <button onClick={add} style={{ marginLeft: 8 }}>Añadir</button>
    </div>
  );
}