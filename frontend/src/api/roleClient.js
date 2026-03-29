const BASE_URL = 'http://localhost:8080/api';

function getAuthHeaders() {
  const token = localStorage.getItem('token');
  return {
    'Content-Type': 'application/json',
    ...(token && { Authorization: `Bearer ${token}` }),
  };
}

export async function getRoles() {
  const res = await fetch(`${BASE_URL}/roles`, {
    headers: getAuthHeaders(),
  });
  if (!res.ok) throw new Error('Error loading roles');
  return res.json();
}

export async function getRoleById(id) {
  const res = await fetch(`${BASE_URL}/roles/${id}`, {
    headers: getAuthHeaders(),
  });
  if (!res.ok) throw new Error('Error loading role');
  return res.json();
}

export async function getRolePermissions(id) {
  const res = await fetch(`${BASE_URL}/roles/${id}/permissions`, {
    headers: getAuthHeaders(),
  });
  if (!res.ok) throw new Error('Error loading permissions');
  return res.json();
}


