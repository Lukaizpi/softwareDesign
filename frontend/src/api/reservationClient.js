const BASE_URL = 'http://localhost:8080/api';

export async function getReservations() {
  const res = await fetch(`${BASE_URL}/reservations`, {
    headers: getAuthHeaders(),
  });
  if (!res.ok) throw new Error('Error loading reservations');
  return res.json();
}

export async function getReservationById(id) {
  const res = await fetch(`${BASE_URL}/reservations/${id}`, {
    headers: getAuthHeaders(),
  });
  if (!res.ok) throw new Error('Error loading reservation');
  return res.json();
}

export async function createReservation(data) {
  const res = await fetch(`${BASE_URL}/reservations`, {
    method: 'POST',
    headers: getAuthHeaders(),
    body: JSON.stringify(data),
  });
  if (!res.ok) {
    const errorData = await res.json().catch(() => ({ error: 'Error creating reservation' }));
    throw new Error(errorData.error || 'Error creating reservation');
  }
  return res.json();
}

export async function updateReservation(id, data) {
  const res = await fetch(`${BASE_URL}/reservations/${id}`, {
    method: 'PUT',
    headers: getAuthHeaders(),
    body: JSON.stringify(data),
  });
  if (!res.ok) throw new Error('Error updating reservation');
  return res.json();
}

export async function cancelReservation(id) {
  const res = await fetch(`${BASE_URL}/reservations/${id}/cancel`, {
    method: 'POST',
    headers: getAuthHeaders(),
  });
  if (!res.ok) throw new Error('Error cancelling reservation');
  return res.json();
}

export async function getAvailableSlots(serviceId, employeeId, date) {
  // Format date as ISO DateTime string (backend expects LocalDateTime)
  // If date is just a string like "2025-12-15", convert it to a full datetime
  let dateTimeStr;
  if (date instanceof Date) {
    dateTimeStr = date.toISOString();
  } else if (typeof date === 'string') {
    // If it's just a date (YYYY-MM-DD), add time to make it a full datetime
    if (date.match(/^\d{4}-\d{2}-\d{2}$/)) {
      dateTimeStr = `${date}T00:00:00`;
    } else {
      dateTimeStr = date;
    }
  } else {
    dateTimeStr = new Date().toISOString();
  }
  
  const url = `${BASE_URL}/reservations/availability?serviceId=${serviceId}${employeeId ? `&employeeId=${employeeId}` : ''}&date=${encodeURIComponent(dateTimeStr)}`;
  console.log('Fetching availability:', url);
  const res = await fetch(url, { headers: getAuthHeaders() });
  if (!res.ok) {
    const errorText = await res.text();
    console.error('Availability error:', errorText);
    throw new Error('Error loading availability: ' + errorText);
  }
  const slots = await res.json();
  console.log('Available slots:', slots);
  return slots;
}

export async function getCustomers() {
  const res = await fetch(`${BASE_URL}/reservations/customers`, {
    headers: getAuthHeaders(),
  });
  if (!res.ok) throw new Error('Error loading customers');
  return res.json();
}

export async function createCustomer(data) {
  const res = await fetch(`${BASE_URL}/reservations/customers`, {
    method: 'POST',
    headers: getAuthHeaders(),
    body: JSON.stringify(data),
  });
  if (!res.ok) throw new Error('Error creating customer');
  return res.json();
}

export async function getServices() {
  const res = await fetch(`${BASE_URL}/reservations/services`, {
    headers: getAuthHeaders(),
  });
  if (!res.ok) throw new Error('Error loading services');
  return res.json();
}

function getAuthHeaders() {
  const token = localStorage.getItem('token');
  return {
    'Content-Type': 'application/json',
    ...(token && { Authorization: `Bearer ${token}` }),
  };
}

