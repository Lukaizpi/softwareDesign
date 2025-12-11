import { useEffect, useState } from 'react';
import { getProducts, getOrders, createOrder } from './api/apiClient';

function App() {
  const [products, setProducts] = useState([]);
  const [orders, setOrders] = useState([]);
  const [tableNumber, setTableNumber] = useState('');
  const [employeeName, setEmployeeName] = useState('');
  const [loading, setLoading] = useState(false);
  const [message, setMessage] = useState('');

  // Cargar datos al entrar
  useEffect(() => {
    loadInitialData();
  }, []);

  async function loadInitialData() {
    try {
      setLoading(true);
      const [prod, ord] = await Promise.all([
        getProducts(),
        getOrders(),
      ]);
      setProducts(prod);
      setOrders(ord);
    } catch (err) {
      console.error(err);
      setMessage('Error cargando datos iniciales');
    } finally {
      setLoading(false);
    }
  }

  async function handleCreateOrder(e) {
    e.preventDefault();
    setMessage('');

    if (!tableNumber || !employeeName) {
      setMessage('Rellena mesa y nombre del camarero');
      return;
    }

    try {
      setLoading(true);

      const newOrder = await createOrder({
        tableNumber,
        employeeName,
      });

      // Añadimos la nueva orden a la lista
      setOrders((prev) => [...prev, newOrder]);

      setMessage(`Orden creada (id: ${newOrder.id ?? 'sin id devuelta'})`);
      setTableNumber('');
      setEmployeeName('');
    } catch (err) {
      console.error(err);
      setMessage('Error creando la orden');
    } finally {
      setLoading(false);
    }
  }

  return (
    <div style={{ padding: '1.5rem', fontFamily: 'system-ui' }}>
      <h1>Gestión de Comandas - Restaurante</h1>

      <section style={{ marginBottom: '2rem' }}>
        <h2>Crear nueva orden</h2>
        <form onSubmit={handleCreateOrder} style={{ display: 'flex', gap: '1rem', flexWrap: 'wrap' }}>
          <label>
            Mesa:
            <input
              type="number"
              value={tableNumber}
              onChange={(e) => setTableNumber(e.target.value)}
              style={{ marginLeft: '0.5rem' }}
            />
          </label>

          <label>
            Camarero:
            <input
              type="text"
              value={employeeName}
              onChange={(e) => setEmployeeName(e.target.value)}
              style={{ marginLeft: '0.5rem' }}
            />
          </label>

          <button type="submit" disabled={loading}>
            {loading ? 'Creando...' : 'Crear orden'}
          </button>
        </form>

        {message && <p style={{ marginTop: '0.5rem' }}>{message}</p>}
      </section>

      <section style={{ marginBottom: '2rem' }}>
        <h2>Productos (menú)</h2>
        {products.length === 0 ? (
          <p>No hay productos definidos todavía.</p>
        ) : (
          <ul>
            {products.map((p) => (
              <li key={p.id}>
                {p.name} - {p.price} €
              </li>
            ))}
          </ul>
        )}
      </section>

      <section>
        <h2>Órdenes abiertas / históricas</h2>
        {orders.length === 0 ? (
          <p>Todavía no hay órdenes.</p>
        ) : (
          <table border="1" cellPadding="6">
            <thead>
              <tr>
                <th>ID</th>
                <th>Mesa</th>
                <th>Camarero</th>
                <th>Estado</th>
              </tr>
            </thead>
            <tbody>
              {orders.map((o) => (
                <tr key={o.id ?? `${o.tableNumber}-${o.employeeName}`}>
                  <td>{o.id ?? '-'}</td>
                  <td>{o.tableNumber}</td>
                  <td>{o.employeeName}</td>
                  <td>{o.status ?? 'OPEN'}</td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </section>
    </div>
  );
}

export default App;