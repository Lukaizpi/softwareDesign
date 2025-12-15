import { useState, useEffect } from 'react';
import {
  getReservations,
  createReservation,
  updateReservation,
  cancelReservation,
  getAvailableSlots,
  getCustomers,
  createCustomer,
} from '../api/reservationClient';
import { getProducts } from '../api/apiClient';
import './Reservations.css';

function Reservations() {
  const [reservations, setReservations] = useState([]);
  const [customers, setCustomers] = useState([]);
  const [services, setServices] = useState([]); // Products of type SERVICE
  const [loading, setLoading] = useState(false);
  const [showForm, setShowForm] = useState(false);
  const [selectedDate, setSelectedDate] = useState('');
  const [availableSlots, setAvailableSlots] = useState([]);
  const [formData, setFormData] = useState({
    customerName: '',
    customerEmail: '',
    customerPhone: '',
    serviceId: '',
    employeeId: '',
    startTime: '',
    notes: '',
  });

  useEffect(() => {
    loadData();
  }, []);

  async function loadData() {
    try {
      setLoading(true);
      const [res, cust, products] = await Promise.all([
        getReservations(),
        getCustomers(),
        getProducts(),
      ]);
      setReservations(res);
      setCustomers(cust);
      
      // Filter products to only show SERVICE type (exclude "Test Service Product")
      const serviceProducts = (products || []).filter(p => {
        if (!p) return false;
        // Exclude "Test Service Product"
        if (p.name && p.name.toLowerCase().includes('test')) {
          return false;
        }
        // Check type enum (string comparison) - case insensitive
        const typeStr = String(p.type || '').toUpperCase();
        const isServiceType = typeStr === 'SERVICE';
        // Check category (legacy support)
        const isServiceCategory = p.category && String(p.category).toLowerCase() === 'service';
        // Check name contains "service" (fallback)
        const nameHasService = p.name && String(p.name).toLowerCase().includes('service');
        
        const isService = isServiceType || isServiceCategory || nameHasService;
        return isService;
      });
      
      setServices(serviceProducts);
      
      console.log('=== SERVICE LOADING DEBUG ===');
      console.log('All products:', products);
      console.log('Service products filtered:', serviceProducts);
      console.log('Services count:', serviceProducts.length);
      
      if (serviceProducts.length === 0) {
        console.warn('No services found. Create products with type "SERVICE" in Product Management.');
      }
    } catch (err) {
      console.error('Error loading data:', err);
    } finally {
      setLoading(false);
    }
  }

  async function handleServiceChange(serviceId) {
    if (!serviceId || !selectedDate) {
      setAvailableSlots([]);
      return;
    }
    try {
      setLoading(true);
      const employeeId = formData.employeeId ? parseInt(formData.employeeId) : null;
      console.log('Loading slots for service:', serviceId, 'date:', selectedDate, 'employee:', employeeId);
      const slots = await getAvailableSlots(parseInt(serviceId), employeeId, selectedDate);
      console.log('Received slots:', slots);
      setAvailableSlots(slots);
      if (slots.length === 0) {
        console.warn('No available slots found');
      }
    } catch (err) {
      console.error('Error loading slots:', err);
      setAvailableSlots([]);
      alert('Error loading available slots: ' + (err.message || 'Unknown error'));
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    if (formData.serviceId && selectedDate) {
      handleServiceChange(formData.serviceId);
    }
  }, [formData.serviceId, selectedDate, formData.employeeId]);

  async function handleSubmit(e) {
    e.preventDefault();
    try {
      setLoading(true);
      // First create or find customer
      let customerId;
      if (formData.customerName) {
        const nameParts = formData.customerName.trim().split(' ');
        const firstName = nameParts[0] || formData.customerName;
        const lastName = nameParts.slice(1).join(' ') || '';
        
        try {
          const customer = await createCustomer({
            firstName,
            lastName,
            email: formData.customerEmail || '',
            phone: formData.customerPhone || '',
            notes: '',
          });
          customerId = customer.id;
          console.log('Customer created with ID:', customerId);
        } catch (err) {
          console.error('Error creating customer:', err);
          // Try to find existing customer by name
          const existing = customers.find(c => {
            const fullName = ((c.firstName || '') + ' ' + (c.lastName || '')).trim().toLowerCase();
            return fullName === formData.customerName.toLowerCase();
          });
          if (existing) {
            customerId = existing.id;
            console.log('Using existing customer ID:', customerId);
          } else {
            throw new Error('Could not create or find customer: ' + (err.message || 'Unknown error'));
          }
        }
      } else {
        throw new Error('Customer name is required');
      }

      // Validate required fields
      if (!formData.serviceId) {
        throw new Error('Service is required');
      }
      if (!selectedDate) {
        throw new Error('Date is required');
      }
      if (!formData.startTime) {
        throw new Error('Time slot is required');
      }

      // Validate date is not in the past
      const selectedDateTime = new Date(`${selectedDate}T${formData.startTime}`);
      if (selectedDateTime < new Date()) {
        throw new Error('Cannot create reservation in the past. Please select a future date and time.');
      }

      // Format date-time properly (ISO 8601)
      const dateTimeStr = `${selectedDate}T${formData.startTime}:00`;
      
      // For now, we'll use the product ID as serviceId
      // The backend ReservationService expects a serviceId, but we're using products
      // We need to create a service entry or use the product directly
      // For simplicity, we'll use the product ID as serviceId
      const selectedService = services.find(s => s.id === parseInt(formData.serviceId));
      if (!selectedService) {
        throw new Error('Selected service not found');
      }
      
      // Use product ID as serviceId (backend will need to handle this)
      // For now, we'll create a temporary service or use product directly
      const reservationData = {
        customerId: customerId,
        serviceId: parseInt(formData.serviceId), // This is actually a product ID now
        employeeId: formData.employeeId ? parseInt(formData.employeeId) : null,
        branchId: null,
        startTime: dateTimeStr,
        notes: formData.notes || '',
        depositAmount: 0,
      };
      
      console.log('Creating reservation with data:', reservationData);
      await createReservation(reservationData);
      setShowForm(false);
      setSelectedDate('');
      setAvailableSlots([]);
      setFormData({
        customerName: '',
        customerEmail: '',
        customerPhone: '',
        serviceId: '',
        employeeId: '',
        startTime: '',
        notes: '',
      });
      loadData();
    } catch (err) {
      console.error('Error creating reservation:', err);
      const errorMessage = err.message || 'Unknown error';
      alert(`Error creating reservation: ${errorMessage}\n\nPlease check:\n- Customer name is filled\n- Service is selected\n- Date is selected\n- Time slot is selected`);
    } finally {
      setLoading(false);
    }
  }

  function formatDate(dateStr) {
    return new Date(dateStr).toLocaleString('en-US');
  }

  function getStatusColor(status) {
    const colors = {
      PENDING: '#f59e0b',
      CONFIRMED: '#3b82f6',
      COMPLETED: '#10b981',
      CANCELLED: '#ef4444',
      NO_SHOW: '#6b7280',
    };
    return colors[status] || '#6b7280';
  }

  return (
    <div className="reservations-container">
      <div className="reservations-header">
        <h2>Reservations ({reservations.length})</h2>
        <button onClick={() => setShowForm(!showForm)} className="btn-primary">
          {showForm ? 'Cancel' : '+ New Reservation'}
        </button>
      </div>

      {showForm && (
        <form onSubmit={handleSubmit} className="reservation-form">
          <h3>Create Reservation</h3>
          
          <div className="form-group">
            <label>Customer Name</label>
            <input
              type="text"
              value={formData.customerName}
              onChange={(e) => setFormData({ ...formData, customerName: e.target.value })}
              placeholder="Enter customer name"
              required
            />
          </div>
          <div className="form-row">
            <div className="form-group">
              <label>Email (optional)</label>
              <input
                type="email"
                value={formData.customerEmail}
                onChange={(e) => setFormData({ ...formData, customerEmail: e.target.value })}
                placeholder="customer@example.com"
              />
            </div>
            <div className="form-group">
              <label>Phone (optional)</label>
              <input
                type="tel"
                value={formData.customerPhone}
                onChange={(e) => setFormData({ ...formData, customerPhone: e.target.value })}
                placeholder="123456789"
              />
            </div>
          </div>

          <div className="form-group">
            <label>Service</label>
            <select
              value={formData.serviceId}
              onChange={(e) => {
                setFormData({ ...formData, serviceId: e.target.value });
                handleServiceChange(e.target.value);
              }}
              required
            >
              <option value="">Select service</option>
              {services.length === 0 ? (
                <option disabled>No services available. Please create SERVICE type products first.</option>
              ) : (
                services.map((s) => (
                  <option key={s.id} value={s.id}>
                    {s.name} - ${(s.basePrice || s.price || 0).toFixed(2)}
                  </option>
                ))
              )}
            </select>
            {services.length === 0 && (
              <p className="error-message" style={{ marginTop: '0.5rem', fontSize: '0.875rem' }}>
                No services found. Create products with type "SERVICE" in Product Management.
              </p>
            )}
          </div>

          <div className="form-group">
            <label>Date</label>
            <input
              type="date"
              value={selectedDate}
              min={new Date().toISOString().split('T')[0]}
              onChange={(e) => {
                setSelectedDate(e.target.value);
                if (formData.serviceId) {
                  handleServiceChange(formData.serviceId);
                }
              }}
              required
            />
          </div>

          {formData.serviceId && selectedDate ? (
            <div className="form-group">
              <label>Available Time Slots</label>
              {loading ? (
                <p>Loading available slots...</p>
              ) : availableSlots.length > 0 ? (
                <select
                  value={formData.startTime}
                  onChange={(e) => setFormData({ ...formData, startTime: e.target.value })}
                  required
                >
                  <option value="">Select time</option>
                  {availableSlots.map((slot) => (
                    <option key={slot} value={slot}>
                      {slot}
                    </option>
                  ))}
                </select>
              ) : (
                <p className="error-message">No available slots for this date. Please select another date.</p>
              )}
            </div>
          ) : null}

          <div className="form-group">
            <label>Notes (optional)</label>
            <textarea
              value={formData.notes}
              onChange={(e) => setFormData({ ...formData, notes: e.target.value })}
              rows="3"
            />
          </div>

          <button type="submit" disabled={loading} className="btn-primary">
            {loading ? 'Creating...' : 'Create Reservation'}
          </button>
        </form>
      )}

      {loading && !showForm ? (
        <p>Loading...</p>
      ) : (
        <div className="reservations-grid">
          {reservations.map((res) => (
            <div key={res.id} className="reservation-card">
              <div className="reservation-header">
                <span className="reservation-id">#{res.id}</span>
                <span
                  className="reservation-status"
                  style={{ backgroundColor: getStatusColor(res.status) }}
                >
                  {res.status}
                </span>
              </div>
              <div className="reservation-info">
                <p><strong>Customer:</strong> {res.customer?.firstName} {res.customer?.lastName}</p>
                <p><strong>Service:</strong> {res.service?.name}</p>
                <p><strong>Date:</strong> {formatDate(res.startTime)}</p>
                {res.employeeId && <p><strong>Employee:</strong> {res.employeeId}</p>}
                {res.notes && <p><strong>Notes:</strong> {res.notes}</p>}
              </div>
              {res.status !== 'CANCELLED' && res.status !== 'COMPLETED' && (
                <div className="reservation-actions">
                  <button onClick={() => cancelReservation(res.id).then(loadData)}>
                    Cancel
                  </button>
                </div>
              )}
            </div>
          ))}
        </div>
      )}
    </div>
  );
}

export default Reservations;

