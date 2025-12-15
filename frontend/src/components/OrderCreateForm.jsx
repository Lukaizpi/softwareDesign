import { useState } from "react";

export default function OrderCreateForm({ onCreate }) {
  const [tableNumber, setTableNumber] = useState("1");
  const [employeeName, setEmployeeName] = useState("Juan");

  return (
    <div style={{ display: "flex", gap: 8, alignItems: "end", marginBottom: 12 }}>
      <div>
        <label>Mesa</label><br />
        <input value={tableNumber} onChange={(e) => setTableNumber(e.target.value)} />
      </div>
      <div>
        <label>Empleado</label><br />
        <input value={employeeName} onChange={(e) => setEmployeeName(e.target.value)} />
      </div>
      <button onClick={() => onCreate({ tableNumber, employeeName })}>Crear orden</button>
    </div>
  );
}