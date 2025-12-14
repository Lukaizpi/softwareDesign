import { useState } from "react";
import OrdersPage from "./pages/OrdersPage";
import ProductsPage from "./pages/ProductsPage";

export default function App() {
  const [tab, setTab] = useState("orders");

  return (
    <div style={{ padding: 20, fontFamily: "system-ui" }}>
      <h1>Restaurant POS</h1>

      <nav style={{ display: "flex", gap: 12, marginBottom: 16 }}>
        <button onClick={() => setTab("orders")}>Orders</button>
        <button onClick={() => setTab("products")}>Products</button>
      </nav>

      {tab === "orders" && <OrdersPage />}
      {tab === "products" && <ProductsPage />}
    </div>
  );
}