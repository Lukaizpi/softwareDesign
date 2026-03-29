import { useState, useEffect } from 'react';
import { getDiscounts, createDiscount, updateDiscount } from '../api/apiClient';
import './DiscountManagement.css';

function DiscountManagement() {
  const [discounts, setDiscounts] = useState([]);
  const [loading, setLoading] = useState(false);
  const [showForm, setShowForm] = useState(false);
  const [selectedDiscount, setSelectedDiscount] = useState(null);
  const [formData, setFormData] = useState({
    name: '',
    type: 'ORDER',
    valueType: 'PERCENTAGE',
    value: 0,
    productId: null,
    validFrom: '',
    validTo: '',
    maxUsage: null,
  });

  useEffect(() => {
    loadDiscounts();
  }, []);

  async function loadDiscounts() {
    try {
      setLoading(true);
      const data = await getDiscounts();
      setDiscounts(data);
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  }

  async function handleSubmit(e) {
    e.preventDefault();
    try {
      setLoading(true);
      if (selectedDiscount) {
        await updateDiscount(selectedDiscount.id, {
          name: formData.name,
          status: 'ACTIVE',
          validFrom: formData.validFrom || null,
          validTo: formData.validTo || null,
          maxUsage: formData.maxUsage || null,
        });
      } else {
        await createDiscount({
          name: formData.name,
          type: formData.type,
          valueType: formData.valueType,
          value: formData.value,
          productId: formData.productId || null,
          merchantId: null,
          validFrom: formData.validFrom || null,
          validTo: formData.validTo || null,
          maxUsage: formData.maxUsage || null,
        });
      }
      setShowForm(false);
      setSelectedDiscount(null);
      resetForm();
      loadDiscounts();
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  }

  function resetForm() {
    setFormData({
      name: '',
      type: 'ORDER',
      valueType: 'PERCENTAGE',
      value: 0,
      productId: null,
      validFrom: '',
      validTo: '',
      maxUsage: null,
    });
  }

  function formatDate(dateStr) {
    if (!dateStr) return 'N/A';
    return new Date(dateStr).toLocaleDateString('en-US');
  }

  function isDiscountValid(discount) {
    if (discount.status !== 'ACTIVE') return false;
    const now = new Date();
    if (discount.validFrom && new Date(discount.validFrom) > now) return false;
    if (discount.validTo && new Date(discount.validTo) < now) return false;
    if (discount.maxUsage && discount.currentUsage >= discount.maxUsage) return false;
    return true;
  }

  return (
    <div className="discount-management">
      <div className="management-header">
        <h2>Discount Management</h2>
        <button onClick={() => setShowForm(!showForm)} className="btn-primary">
          {showForm ? 'Cancel' : '+ New Discount'}
        </button>
      </div>

      {showForm && (
        <form onSubmit={handleSubmit} className="discount-form">
          <h3>{selectedDiscount ? 'Edit Discount' : 'Create Discount'}</h3>
          
          <div className="form-group">
            <label>Discount Name</label>
            <input
              type="text"
              value={formData.name}
              onChange={(e) => setFormData({ ...formData, name: e.target.value })}
              required
            />
          </div>

          <div className="form-row">
            <div className="form-group">
              <label>Type</label>
              <select
                value={formData.type}
                onChange={(e) => setFormData({ ...formData, type: e.target.value })}
              >
                <option value="ORDER">Order Level</option>
                <option value="PRODUCT">Product Level</option>
              </select>
            </div>

            <div className="form-group">
              <label>Value Type</label>
              <select
                value={formData.valueType}
                onChange={(e) => setFormData({ ...formData, valueType: e.target.value })}
              >
                <option value="PERCENTAGE">Percentage</option>
                <option value="FIXED">Fixed Amount</option>
              </select>
            </div>
          </div>

          <div className="form-group">
            <label>Value ({formData.valueType === 'PERCENTAGE' ? '%' : '€'})</label>
            <input
              type="number"
              step="0.01"
              value={formData.value}
              onChange={(e) => setFormData({ ...formData, value: parseFloat(e.target.value) || 0 })}
              required
            />
          </div>

          <div className="form-row">
            <div className="form-group">
              <label>Valid From</label>
              <input
                type="datetime-local"
                value={formData.validFrom}
                onChange={(e) => setFormData({ ...formData, validFrom: e.target.value })}
              />
            </div>

            <div className="form-group">
              <label>Valid To</label>
              <input
                type="datetime-local"
                value={formData.validTo}
                onChange={(e) => setFormData({ ...formData, validTo: e.target.value })}
              />
            </div>
          </div>

          <div className="form-group">
            <label>Max Usage (leave empty for unlimited)</label>
            <input
              type="number"
              value={formData.maxUsage || ''}
              onChange={(e) => setFormData({ ...formData, maxUsage: e.target.value ? parseInt(e.target.value) : null })}
            />
          </div>

          <button type="submit" disabled={loading} className="btn-primary">
            {loading ? 'Saving...' : selectedDiscount ? 'Update' : 'Create'}
          </button>
        </form>
      )}

      <div className="discounts-section">
        <h3>Discounts ({discounts.length})</h3>
        {loading ? (
          <p>Loading...</p>
        ) : discounts.length === 0 ? (
          <p className="empty-state">No discounts created yet</p>
        ) : (
          <div className="discounts-grid">
            {discounts.map((discount) => (
              <div key={discount.id} className={`discount-card ${isDiscountValid(discount) ? 'active' : 'inactive'}`}>
                <div className="discount-header">
                  <h4>{discount.name}</h4>
                  <span className={`discount-status ${discount.status.toLowerCase()}`}>
                    {discount.status}
                  </span>
                </div>
                <div className="discount-info">
                  <p><strong>Type:</strong> {discount.type}</p>
                  <p><strong>Value:</strong> {discount.valueType === 'PERCENTAGE' ? `${discount.value}%` : `€${discount.value}`}</p>
                  {discount.validFrom && (
                    <p><strong>Valid From:</strong> {formatDate(discount.validFrom)}</p>
                  )}
                  {discount.validTo && (
                    <p><strong>Valid To:</strong> {formatDate(discount.validTo)}</p>
                  )}
                  {discount.maxUsage && (
                    <p><strong>Usage:</strong> {discount.currentUsage || 0} / {discount.maxUsage}</p>
                  )}
                </div>
                <div className="discount-actions">
                  <button
                    onClick={() => {
                      setSelectedDiscount(discount);
                      setFormData({
                        name: discount.name,
                        type: discount.type,
                        valueType: discount.valueType,
                        value: discount.value,
                        productId: discount.productId,
                        validFrom: discount.validFrom ? new Date(discount.validFrom).toISOString().slice(0, 16) : '',
                        validTo: discount.validTo ? new Date(discount.validTo).toISOString().slice(0, 16) : '',
                        maxUsage: discount.maxUsage,
                      });
                      setShowForm(true);
                    }}
                  >
                    Edit
                  </button>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
}

export default DiscountManagement;
