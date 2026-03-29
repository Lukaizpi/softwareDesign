import { useEffect, useState } from 'react';
import {
  getProducts,
  getOrders,
  createOrder,
  addItemToOrder,
  cancelOrder,
  payOrder,
  applyDiscount,
  addPayment,
  refundOrder,
  getOrderById,
  getDiscounts,
} from './api/apiClient';
import { isAuthenticated, getCurrentUser, logout } from './api/authClient';
import Login from './components/Login';
import SplitPayment from './components/SplitPayment';
import Reservations from './components/Reservations';
import ProductManagement from './components/ProductManagement';
import DiscountManagement from './components/DiscountManagement';
import UserManagement from './components/UserManagement';
import './App.css';

function App() {
  const [authenticated, setAuthenticated] = useState(isAuthenticated());
  const [user, setUser] = useState(getCurrentUser());
  const [products, setProducts] = useState([]);
  const [orders, setOrders] = useState([]);
  const [selectedOrder, setSelectedOrder] = useState(null);
  const [showSplitPayment, setShowSplitPayment] = useState(false);
  const [tableNumber, setTableNumber] = useState('');
  const [employeeName, setEmployeeName] = useState('');
  const [loading, setLoading] = useState(false);
  const [message, setMessage] = useState('');
  const [activeTab, setActiveTab] = useState('orders');
  const [discounts, setDiscounts] = useState([]);

  useEffect(() => {
    if (authenticated) {
      loadInitialData();
    }
  }, [authenticated]);

  function handleLoginSuccess(loggedInUser) {
    setUser(loggedInUser);
    setAuthenticated(true);
  }

  function handleLogout() {
    logout();
    setAuthenticated(false);
    setUser(null);
  }

  function hasPermission(resource, action) {
    if (!user || !user.role) return false;
    // Simplified permission check - in real app, check user.role.permissions
    if (user.role === 'SuperAdmin') return true;
    if (user.role === 'Manager') {
      // Manager can only read users, not create/update/delete them
      if (resource === 'user' && action === 'read') return true;
      if (resource === 'user' && action !== 'read') return false;
      if (resource === 'role') return false;
      return true; // Manager has access to everything else
    }
    // Employee has limited permissions
    return ['order', 'payment', 'reservation'].includes(resource) && action !== 'delete';
  }

  async function loadInitialData() {
    try {
      setLoading(true);
      const [prod, ord, disc] = await Promise.all([
        getProducts(),
        getOrders(),
        getDiscounts().catch(() => []),
      ]);
      setProducts(prod);
      setOrders(ord);
      setDiscounts(disc);
    } catch (err) {
      console.error(err);
      setMessage('Error loading initial data');
    } finally {
      setLoading(false);
    }
  }

  async function handleCreateOrder(e) {
    e.preventDefault();
    setMessage('');

    if (!tableNumber || !employeeName) {
      setMessage('Please fill in table number and employee name');
      return;
    }

    try {
      setLoading(true);
      const newOrder = await createOrder({ tableNumber, employeeName });
      setOrders((prev) => [...prev, newOrder]);
      setMessage(`✅ Order #${newOrder.id} created successfully`);
      setTableNumber('');
      setEmployeeName('');
      // Open the order detail view to add products
      setSelectedOrder(newOrder);
      setActiveTab('order-detail');
    } catch (err) {
      console.error(err);
      setMessage('❌ Error creating order');
    } finally {
      setLoading(false);
    }
  }

  async function handleAddItem(orderId, productId, quantity = 1) {
    try {
      const updated = await addItemToOrder(orderId, productId, quantity);
      setOrders((prev) => prev.map((o) => (o.id === orderId ? updated : o)));
      if (selectedOrder?.id === orderId) {
        setSelectedOrder(updated);
      }
      setMessage(`✅ Item added to order #${orderId}`);
    } catch (err) {
      setMessage('❌ Error adding item');
    }
  }

  async function handleCancelOrder(orderId) {
    if (!confirm('Cancel this order?')) return;
    try {
      const updated = await cancelOrder(orderId);
      setOrders((prev) => prev.map((o) => (o.id === orderId ? updated : o)));
      if (selectedOrder?.id === orderId) {
        setSelectedOrder(updated);
      }
      setMessage('✅ Order cancelled');
    } catch (err) {
      setMessage('❌ Error cancelling order');
    }
  }

  async function handlePayOrder(orderId) {
    try {
      const updated = await payOrder(orderId);
      setOrders((prev) => prev.map((o) => (o.id === orderId ? updated : o)));
      if (selectedOrder?.id === orderId) {
        setSelectedOrder(updated);
      }
      setMessage('✅ Order marked as paid');
    } catch (err) {
      setMessage('❌ Error marking as paid');
    }
  }

  async function handleApplyDiscount(orderId, discountId) {
    try {
      const updated = await applyDiscount(orderId, discountId);
      setOrders((prev) => prev.map((o) => (o.id === orderId ? updated : o)));
      if (selectedOrder?.id === orderId) {
        setSelectedOrder(updated);
      }
      setMessage('✅ Discount applied');
    } catch (err) {
      setMessage('❌ Error applying discount');
    }
  }

  async function handleAddPayment(orderId) {
    const method = prompt('Payment method (CASH, CARD, GIFT_CARD):');
    if (!['CASH', 'CARD', 'GIFT_CARD'].includes(method?.toUpperCase())) {
      setMessage('❌ Invalid method');
      return;
    }
    const amount = prompt('Amount:');
    if (!amount || isNaN(amount) || parseFloat(amount) <= 0) {
      setMessage('❌ Invalid amount');
      return;
    }
    const tip = prompt('Tip (optional, press Enter for 0):') || '0';
    try {
      const updated = await addPayment(
        orderId,
        method.toUpperCase(),
        parseFloat(amount),
        parseFloat(tip) || 0
      );
      setOrders((prev) => prev.map((o) => (o.id === orderId ? updated : o)));
      if (selectedOrder?.id === orderId) {
        setSelectedOrder(updated);
      }
      setMessage('✅ Payment added');
    } catch (err) {
      setMessage('❌ Error adding payment');
    }
  }

  async function handleRefund(orderId) {
    const amount = prompt('Refund amount (0 for full refund):');
    if (amount === null) return;
    const refundAmount = amount === '' || amount === '0' ? 0 : parseFloat(amount);
    if (isNaN(refundAmount) || refundAmount < 0) {
      setMessage('❌ Invalid amount');
      return;
    }
    const reason = prompt('Reason for refund:') || '';
    try {
      const updated = await refundOrder(orderId, refundAmount, reason);
      setOrders((prev) => prev.map((o) => (o.id === orderId ? updated : o)));
      if (selectedOrder?.id === orderId) {
        setSelectedOrder(updated);
      }
      setMessage('✅ Refund processed');
    } catch (err) {
      setMessage('❌ Error processing refund');
    }
  }

  async function handleViewOrder(orderId) {
    try {
      const order = await getOrderById(orderId);
      setSelectedOrder(order);
      setActiveTab('order-detail');
    } catch (err) {
      setMessage('❌ Error loading order');
    }
  }

  function formatCurrency(amount) {
    return new Intl.NumberFormat('en-US', {
      style: 'currency',
      currency: 'EUR',
    }).format(amount);
  }

  function getStatusColor(status) {
    const colors = {
      OPEN: '#22c55e',
      PAID: '#3b82f6',
      CANCELLED: '#ef4444',
      REFUNDED: '#f59e0b',
    };
    return colors[status] || '#6b7280';
  }

  if (!authenticated) {
    return <Login onLoginSuccess={handleLoginSuccess} />;
  }

  return (
    <div className="app-container">
      <header className="app-header">
        <div className="header-top">
          <h1>🍽️ Restaurant Management System</h1>
          <div className="user-info">
            <span>Welcome, {user?.firstName || user?.username}</span>
            <button onClick={handleLogout} className="btn-logout">Logout</button>
          </div>
        </div>
        <div className="tabs">
          {hasPermission('order', 'read') && (
            <button
              className={activeTab === 'orders' ? 'active' : ''}
              onClick={() => setActiveTab('orders')}
            >
              Orders
            </button>
          )}
          {hasPermission('order', 'create') && (
            <button
              className={activeTab === 'create' ? 'active' : ''}
              onClick={() => setActiveTab('create')}
            >
              New Order
            </button>
          )}
          {hasPermission('reservation', 'read') && (
            <button
              className={activeTab === 'reservations' ? 'active' : ''}
              onClick={() => setActiveTab('reservations')}
            >
              Reservations
            </button>
          )}
          {hasPermission('product', 'read') && (
            <button
              className={activeTab === 'products' ? 'active' : ''}
              onClick={() => setActiveTab('products')}
            >
              Products
            </button>
          )}
          {hasPermission('product', 'create') && (
            <button
              className={activeTab === 'product-management' ? 'active' : ''}
              onClick={() => setActiveTab('product-management')}
            >
              Product Management
            </button>
          )}
          {hasPermission('discount', 'create') && (
            <button
              className={activeTab === 'discounts' ? 'active' : ''}
              onClick={() => setActiveTab('discounts')}
            >
              Discounts
            </button>
          )}
          {hasPermission('user', 'read') && (
            <button
              className={activeTab === 'users' ? 'active' : ''}
              onClick={() => setActiveTab('users')}
            >
              Users
            </button>
          )}
        </div>
      </header>

      {message && (
        <div className={`message ${message.includes('❌') ? 'error' : 'success'}`}>
          {message}
        </div>
      )}

      <main className="app-main">
        {activeTab === 'reservations' && <Reservations />}

        {activeTab === 'product-management' && <ProductManagement />}

        {activeTab === 'discounts' && <DiscountManagement />}

        {activeTab === 'users' && <UserManagement />}

        {activeTab === 'orders' && (
          <section className="orders-section">
            <h2>Orders ({orders.length})</h2>
            {loading ? (
              <p>Loading...</p>
            ) : orders.length === 0 ? (
              <p className="empty-state">No orders yet</p>
            ) : (
              <div className="orders-grid">
                {orders.map((order) => (
                  <div key={order.id} className="order-card">
                    <div className="order-header">
                      <span className="order-id">#{order.id}</span>
                      <span
                        className="order-status"
                        style={{ backgroundColor: getStatusColor(order.status) }}
                      >
                        {order.status}
                      </span>
                    </div>
                    <div className="order-info">
                      <p>
                        <strong>Table:</strong> {order.tableNumber}
                      </p>
                      <p>
                        <strong>Server:</strong> {order.employeeName}
                      </p>
                      <p>
                        <strong>Items:</strong> {order.items?.length || 0}
                      </p>
                      <p>
                        <strong>Total:</strong> {formatCurrency(order.total || 0)}
                      </p>
                      {order.createdAt && (
                        <p className="order-date">
                          {new Date(order.createdAt).toLocaleString('en-US')}
                        </p>
                      )}
                    </div>
                    <div className="order-actions">
                      <button onClick={() => handleViewOrder(order.id)}>View Details</button>
                      {order.status === 'OPEN' && (
                        <>
                          {hasPermission('order', 'update') && (
                            <button onClick={() => handleCancelOrder(order.id)}>Cancel</button>
                          )}
                          {hasPermission('payment', 'create') && (
                            <>
                              <button onClick={() => setShowSplitPayment(true)}>Split Payment</button>
                              <button onClick={() => handlePayOrder(order.id)}>Pay</button>
                            </>
                          )}
                        </>
                      )}
                    </div>
                  </div>
                ))}
              </div>
            )}
          </section>
        )}

        {activeTab === 'create' && (
          <section className="create-section">
            <h2>Create New Order</h2>
            <form onSubmit={handleCreateOrder} className="create-form">
              <div className="form-group">
                <label>Table Number:</label>
                <input
                  type="text"
                  value={tableNumber}
                  onChange={(e) => setTableNumber(e.target.value)}
                  placeholder="e.g., 5"
                  required
                />
              </div>
              <div className="form-group">
                <label>Server Name:</label>
                <input
                  type="text"
                  value={employeeName}
                  onChange={(e) => setEmployeeName(e.target.value)}
                  placeholder="e.g., John Doe"
                  required
                />
              </div>
              <button type="submit" disabled={loading} className="btn-primary">
                {loading ? 'Creating...' : 'Create Order'}
              </button>
            </form>
          </section>
        )}

        {activeTab === 'products' && (
          <section className="products-section">
            <h2>Available Products ({products.length})</h2>
            {loading ? (
              <p>Loading...</p>
            ) : products.length === 0 ? (
              <p className="empty-state">No products available</p>
            ) : (
              <div className="products-grid">
                {products.map((product) => (
                  <div key={product.id} className="product-card">
                    <h3>{product.name}</h3>
                    <p className="product-category">{product.category || product.type}</p>
                    <p className="product-price">{formatCurrency(product.price || product.basePrice)}</p>
                  </div>
                ))}
              </div>
            )}
          </section>
        )}

        {activeTab === 'order-detail' && selectedOrder && (
          <section className="order-detail-section">
            <div className="detail-header">
              <button onClick={() => setActiveTab('orders')} className="btn-back">
                ← Back
              </button>
              <h2>Order #{selectedOrder.id}</h2>
            </div>

            <div className="detail-info">
              <div className="info-row">
                <span>Table:</span>
                <strong>{selectedOrder.tableNumber}</strong>
              </div>
              <div className="info-row">
                <span>Server:</span>
                <strong>{selectedOrder.employeeName}</strong>
              </div>
              <div className="info-row">
                <span>Status:</span>
                <strong style={{ color: getStatusColor(selectedOrder.status) }}>
                  {selectedOrder.status}
                </strong>
              </div>
              {selectedOrder.createdAt && (
                <div className="info-row">
                  <span>Date:</span>
                  <strong>{new Date(selectedOrder.createdAt).toLocaleString('en-US')}</strong>
                </div>
              )}
            </div>

            <div className="detail-items">
              <h3>Items ({selectedOrder.items?.length || 0})</h3>
              {selectedOrder.items && selectedOrder.items.length > 0 ? (
                <table className="items-table">
                  <thead>
                    <tr>
                      <th>Product</th>
                      <th>Quantity</th>
                      <th>Unit Price</th>
                      <th>Tax</th>
                      <th>Subtotal</th>
                    </tr>
                  </thead>
                  <tbody>
                    {selectedOrder.items.map((item, idx) => (
                      <tr key={idx}>
                        <td>{item.productName}</td>
                        <td>{item.quantity}</td>
                        <td>{formatCurrency(item.unitPrice)}</td>
                        <td>{(item.taxRate * 100).toFixed(1)}%</td>
                        <td>
                          {formatCurrency(
                            item.unitPrice * item.quantity * (1 + item.taxRate)
                          )}
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              ) : (
                <p className="empty-state">No items in this order</p>
              )}

              {selectedOrder.status === 'OPEN' && hasPermission('order', 'update') && (
                <div className="add-items-section">
                  <h4>Add Items</h4>
                  <div className="products-quick-add">
                    {products
                      .filter((product) => {
                        // Exclude SERVICE type products from orders
                        const typeStr = String(product.type || '').toUpperCase();
                        return typeStr !== 'SERVICE';
                      })
                      .map((product) => (
                        <button
                          key={product.id}
                          onClick={() => handleAddItem(selectedOrder.id, product.id, 1)}
                          className="btn-add-item"
                        >
                          + {product.name} ({formatCurrency(product.price || product.basePrice)})
                        </button>
                      ))}
                  </div>
                </div>
              )}
            </div>

            <div className="detail-totals">
              <div className="totals-row">
                <span>Subtotal:</span>
                <strong>{formatCurrency(selectedOrder.subtotal || 0)}</strong>
              </div>
              <div className="totals-row">
                <span>Taxes:</span>
                <strong>{formatCurrency(selectedOrder.taxes || 0)}</strong>
              </div>
              {selectedOrder.discountAmount > 0 && (
                <div className="totals-row discount">
                  <span>Discount:</span>
                  <strong>-{formatCurrency(selectedOrder.discountAmount)}</strong>
                </div>
              )}
              {selectedOrder.serviceCharge > 0 && (
                <div className="totals-row">
                  <span>Service Charge:</span>
                  <strong>{formatCurrency(selectedOrder.serviceCharge)}</strong>
                </div>
              )}
              <div className="totals-row total">
                <span>Total:</span>
                <strong>{formatCurrency(selectedOrder.total || 0)}</strong>
              </div>
            </div>

            {selectedOrder.payments && selectedOrder.payments.length > 0 && (
              <div className="detail-payments">
                <h3>Payments ({selectedOrder.payments.length})</h3>
                <table className="payments-table">
                  <thead>
                    <tr>
                      <th>Method</th>
                      <th>Amount</th>
                      <th>Tip</th>
                      <th>Status</th>
                    </tr>
                  </thead>
                  <tbody>
                    {selectedOrder.payments.map((payment) => (
                      <tr key={payment.id}>
                        <td>{payment.method}</td>
                        <td>{formatCurrency(payment.amount)}</td>
                        <td>{formatCurrency(payment.tip || 0)}</td>
                        <td>
                          {payment.refunded ? (
                            <span className="refunded-badge">Refunded</span>
                          ) : (
                            <span className="paid-badge">Paid</span>
                          )}
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )}

            {selectedOrder.status === 'OPEN' && (
              <div className="detail-actions">
                {hasPermission('discount', 'create') && discounts.length > 0 && (
                  <select
                    onChange={(e) => {
                      if (e.target.value) {
                        handleApplyDiscount(selectedOrder.id, e.target.value);
                        e.target.value = '';
                      }
                    }}
                    className="discount-select"
                  >
                    <option value="">Apply Discount</option>
                    {discounts.map((d) => (
                      <option key={d.id} value={d.id}>
                        {d.name} ({d.valueType === 'PERCENTAGE' ? `${d.value}%` : `$${d.value}`})
                      </option>
                    ))}
                  </select>
                )}
                {hasPermission('payment', 'create') && (
                  <>
                    <button onClick={() => handleAddPayment(selectedOrder.id)}>
                      Add Payment
                    </button>
                    <button onClick={() => setShowSplitPayment(true)}>
                      Split Payment
                    </button>
                  </>
                )}
                {hasPermission('order', 'update') && (
                  <button onClick={() => handleCancelOrder(selectedOrder.id)}>
                    Cancel Order
                  </button>
                )}
                {hasPermission('payment', 'create') && (
                  <button onClick={() => handlePayOrder(selectedOrder.id)}>
                    Mark as Paid
                  </button>
                )}
              </div>
            )}

            {selectedOrder.status === 'PAID' && hasPermission('payment', 'refund') && (
              <div className="detail-actions">
                <button onClick={() => handleRefund(selectedOrder.id)}>
                  Refund
                </button>
              </div>
            )}
          </section>
        )}
      </main>

      {showSplitPayment && selectedOrder && (
        <SplitPayment
          order={selectedOrder}
          onUpdate={(updated) => {
            setSelectedOrder(updated);
            setOrders((prev) => prev.map((o) => (o.id === updated.id ? updated : o)));
          }}
          onClose={() => setShowSplitPayment(false)}
        />
      )}
    </div>
  );
}

export default App;
