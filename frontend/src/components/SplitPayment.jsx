import { useState, useEffect } from 'react';
import { startSplitPayment, addPaymentSplit, completeSplitPayment, getOrderById } from '../api/apiClient';
import './SplitPayment.css';

function SplitPayment({ order, onUpdate, onClose }) {
  const [currentOrder, setCurrentOrder] = useState(order);
  const [splits, setSplits] = useState(order.paymentSplits || []);
  const [currentSplit, setCurrentSplit] = useState({
    customerName: '',
    method: 'CASH',
    tip: 0,
    itemIds: [],
  });
  const [loading, setLoading] = useState(false);
  const [message, setMessage] = useState('');

  useEffect(() => {
    // Refresh order data
    if (order.id) {
      getOrderById(order.id).then(setCurrentOrder).catch(console.error);
    }
  }, [order.id]);

  async function handleStartSplit() {
    try {
      setLoading(true);
      const updated = await startSplitPayment(order.id);
      setCurrentOrder(updated);
      onUpdate(updated);
      setMessage('✅ Split payment started. Select items for each customer.');
    } catch (err) {
      setMessage('❌ Error starting split payment: ' + err.message);
    } finally {
      setLoading(false);
    }
  }

  function toggleItem(itemId) {
    setCurrentSplit((prev) => {
      const newItemIds = prev.itemIds.includes(itemId)
        ? prev.itemIds.filter((id) => id !== itemId)
        : [...prev.itemIds, itemId];
      return {
        ...prev,
        itemIds: newItemIds,
      };
    });
  }

  // Calculate amount based on selected items
  function calculateAmountForItems(itemIds) {
    if (!itemIds || itemIds.length === 0) return 0;
    let total = 0;
    (currentOrder.items || []).forEach((item) => {
      if (itemIds.includes(item.id)) {
        const lineTotal = (item.unitPrice || 0) * (item.quantity || 1);
        const taxAmount = lineTotal * (item.taxRate || 0);
        total += lineTotal + taxAmount;
      }
    });
    return total;
  }

  async function handleAddSplit() {
    if (!currentSplit.customerName || currentSplit.itemIds.length === 0) {
      setMessage('Please fill customer name and select items');
      return;
    }

    try {
      setLoading(true);
      // Calculate amount automatically based on selected items
      const calculatedAmount = calculateAmountForItems(currentSplit.itemIds);
      
      const splitData = {
        customerName: currentSplit.customerName,
        method: currentSplit.method,
        amount: calculatedAmount,
        tip: currentSplit.tip,
        itemIds: currentSplit.itemIds,
      };
      const updated = await addPaymentSplit(order.id, splitData);
      setCurrentOrder(updated);
      setSplits(updated.paymentSplits || []);
      setCurrentSplit({
        customerName: '',
        method: 'CASH',
        tip: 0,
        itemIds: [],
      });
      onUpdate(updated);
      setMessage('✅ Split added successfully');
    } catch (err) {
      console.error('Error adding split:', err);
      setMessage('❌ Error adding split: ' + (err.message || 'Unknown error'));
    } finally {
      setLoading(false);
    }
  }

  async function handleComplete() {
    try {
      setLoading(true);
      const updated = await completeSplitPayment(order.id);
      onUpdate(updated);
      onClose();
    } catch (err) {
      setMessage('Error completing split payment');
    } finally {
      setLoading(false);
    }
  }

  const selectedItemIds = (currentOrder.paymentSplits || []).flatMap((s) => s.orderItemIds || []);
  const availableItems = (currentOrder.items || []).filter((item) => {
    const itemId = item.id;
    return itemId && !selectedItemIds.includes(itemId);
  });
  const totalSplit = (currentOrder.paymentSplits || []).reduce((sum, s) => sum + (s.amount || 0) + (s.tip || 0), 0);
  const remaining = (currentOrder.total || 0) - totalSplit;

  return (
    <div className="split-payment-modal">
      <div className="split-payment-content">
        <div className="split-header">
          <h2>Split Payment - Order #{order.id}</h2>
          <button onClick={onClose} className="btn-close">×</button>
        </div>

        {message && (
          <div className={`split-message ${message.includes('❌') ? 'error' : 'success'}`}>
            {message}
          </div>
        )}

        {!currentOrder.itemsLocked && (
          <button onClick={handleStartSplit} className="btn-start-split" disabled={loading}>
            Start Split Payment
          </button>
        )}

        {currentOrder.itemsLocked && (
          <>
            <div className="split-summary">
              <p><strong>Total:</strong> ${(currentOrder.total || 0).toFixed(2)}</p>
              <p><strong>Split Total:</strong> ${totalSplit.toFixed(2)}</p>
              <p><strong>Remaining:</strong> ${remaining.toFixed(2)}</p>
            </div>

            <div className="split-form">
              <h3>Add Payment Split</h3>
              <div className="form-row">
                <label>Customer Name</label>
                <input
                  type="text"
                  value={currentSplit.customerName}
                  onChange={(e) => setCurrentSplit({ ...currentSplit, customerName: e.target.value })}
                  placeholder="Customer name"
                />
              </div>

              <div className="form-row">
                <label>Select Items</label>
                <div className="items-grid">
                  {availableItems.length === 0 ? (
                    <p className="empty-state">All items have been assigned</p>
                  ) : (
                    availableItems.map((item) => {
                      const itemId = item.id;
                      if (!itemId) return null;
                      const lineTotal = (item.unitPrice || 0) * (item.quantity || 1);
                      const taxAmount = lineTotal * (item.taxRate || 0);
                      const itemTotal = lineTotal + taxAmount;
                      return (
                        <label key={itemId} className="item-checkbox">
                          <input
                            type="checkbox"
                            checked={currentSplit.itemIds.includes(itemId)}
                            onChange={() => toggleItem(itemId)}
                          />
                          <span>{item.productName} - ${itemTotal.toFixed(2)}</span>
                        </label>
                      );
                    })
                  )}
                </div>
                {currentSplit.itemIds.length > 0 && (
                  <div className="calculated-amount" style={{ marginTop: '1rem', padding: '0.75rem', background: '#f0f9ff', borderRadius: '6px' }}>
                    <strong>Selected Items Total: ${calculateAmountForItems(currentSplit.itemIds).toFixed(2)}</strong>
                  </div>
                )}
              </div>

              <div className="form-row">
                <label>Payment Method</label>
                <select
                  value={currentSplit.method}
                  onChange={(e) => setCurrentSplit({ ...currentSplit, method: e.target.value })}
                >
                  <option value="CASH">Cash</option>
                  <option value="CARD">Card</option>
                  <option value="GIFT_CARD">Gift Card</option>
                </select>
              </div>

              <div className="form-row">
                <label>Tip (optional)</label>
                <input
                  type="number"
                  step="0.01"
                  value={currentSplit.tip}
                  onChange={(e) => setCurrentSplit({ ...currentSplit, tip: parseFloat(e.target.value) || 0 })}
                />
              </div>

              <button onClick={handleAddSplit} className="btn-add-split" disabled={loading}>
                Add Split
              </button>
            </div>

            <div className="splits-list">
              <h3>Payment Splits ({(currentOrder.paymentSplits || []).length})</h3>
              {(currentOrder.paymentSplits || []).length === 0 ? (
                <p className="empty-state">No splits added yet</p>
              ) : (
                (currentOrder.paymentSplits || []).map((split, idx) => (
                  <div key={idx} className="split-item">
                    <p><strong>{split.customerName || 'Customer ' + (idx + 1)}</strong> - {split.method}</p>
                    <p>Amount: ${(split.amount || 0).toFixed(2)} + Tip: ${(split.tip || 0).toFixed(2)}</p>
                    {split.orderItemIds && split.orderItemIds.length > 0 && (
                      <p className="split-items-info">Items: {split.orderItemIds.length}</p>
                    )}
                  </div>
                ))
              )}
            </div>

            {remaining <= 0.01 && (
              <button onClick={handleComplete} className="btn-complete" disabled={loading}>
                Complete Payment
              </button>
            )}
          </>
        )}
      </div>
    </div>
  );
}

export default SplitPayment;

