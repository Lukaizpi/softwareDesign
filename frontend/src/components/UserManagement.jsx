import { useState, useEffect } from 'react';
import { getUsers, createUser, updateUser, deleteUser } from '../api/userClient';
import { getRoles } from '../api/roleClient';
import { getCurrentUser } from '../api/authClient';
import './UserManagement.css';

function UserManagement() {
  const currentUser = getCurrentUser();
  const isManager = currentUser?.role === 'Manager';
  const [users, setUsers] = useState([]);
  const [roles, setRoles] = useState([]);
  const [loading, setLoading] = useState(false);
  const [showForm, setShowForm] = useState(false);
  const [selectedUser, setSelectedUser] = useState(null);
  const [formData, setFormData] = useState({
    username: '',
    email: '',
    password: '',
    firstName: '',
    lastName: '',
    roleId: 3, // Default to Employee
    active: true,
  });

  useEffect(() => {
    loadData();
  }, []);

  async function loadData() {
    try {
      setLoading(true);
      const [usersData, rolesData] = await Promise.all([
        getUsers(),
        getRoles(),
      ]);
      setUsers(usersData);
      setRoles(rolesData);
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
      if (selectedUser) {
        await updateUser(selectedUser.id, formData);
      } else {
        await createUser(formData);
      }
      setShowForm(false);
      setSelectedUser(null);
      resetForm();
      loadData();
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  }

  async function handleDelete(id) {
    if (!confirm('Are you sure you want to deactivate this user?')) return;
    try {
      await deleteUser(id);
      loadData();
    } catch (err) {
      console.error(err);
    }
  }

  function resetForm() {
    setFormData({
      username: '',
      email: '',
      password: '',
      firstName: '',
      lastName: '',
      roleId: 3,
      active: true,
    });
  }

  function getRoleName(roleId) {
    const role = roles.find((r) => r.id === roleId);
    return role ? role.name : 'Unknown';
  }

  return (
    <div className="user-management">
      <div className="management-header">
        <h2>User & Role Management</h2>
        {!isManager && (
          <button onClick={() => setShowForm(!showForm)} className="btn-primary">
            {showForm ? 'Cancel' : '+ New User'}
          </button>
        )}
      </div>

      {showForm && (
        <form onSubmit={handleSubmit} className="user-form">
          <h3>{selectedUser ? 'Edit User' : 'Create User'}</h3>
          
          <div className="form-row">
            <div className="form-group">
              <label>First Name</label>
              <input
                type="text"
                value={formData.firstName}
                onChange={(e) => setFormData({ ...formData, firstName: e.target.value })}
                required
              />
            </div>
            <div className="form-group">
              <label>Last Name</label>
              <input
                type="text"
                value={formData.lastName}
                onChange={(e) => setFormData({ ...formData, lastName: e.target.value })}
                required
              />
            </div>
          </div>

          <div className="form-row">
            <div className="form-group">
              <label>Username</label>
              <input
                type="text"
                value={formData.username}
                onChange={(e) => setFormData({ ...formData, username: e.target.value })}
                required
                disabled={!!selectedUser}
              />
            </div>
            <div className="form-group">
              <label>Email</label>
              <input
                type="email"
                value={formData.email}
                onChange={(e) => setFormData({ ...formData, email: e.target.value })}
                required
              />
            </div>
          </div>

          {!selectedUser && (
            <div className="form-group">
              <label>Password</label>
              <input
                type="password"
                value={formData.password}
                onChange={(e) => setFormData({ ...formData, password: e.target.value })}
                required={!selectedUser}
                minLength="3"
              />
            </div>
          )}

          <div className="form-row">
            <div className="form-group">
              <label>Role</label>
              <select
                value={formData.roleId}
                onChange={(e) => setFormData({ ...formData, roleId: parseInt(e.target.value) })}
              >
                {roles.map((role) => (
                  <option key={role.id} value={role.id}>
                    {role.name}
                  </option>
                ))}
              </select>
            </div>
            {selectedUser && (
              <div className="form-group">
                <label>Status</label>
                <select
                  value={formData.active ? 'true' : 'false'}
                  onChange={(e) => setFormData({ ...formData, active: e.target.value === 'true' })}
                >
                  <option value="true">Active</option>
                  <option value="false">Inactive</option>
                </select>
              </div>
            )}
          </div>

          <button type="submit" disabled={loading} className="btn-primary">
            {loading ? 'Saving...' : selectedUser ? 'Update' : 'Create'}
          </button>
        </form>
      )}

      <div className="users-section">
        <h3>Users ({users.length})</h3>
        {loading ? (
          <p>Loading...</p>
        ) : users.length === 0 ? (
          <p className="empty-state">No users found</p>
        ) : (
          <div className="users-table-container">
            <table className="users-table">
              <thead>
                <tr>
                  <th>Username</th>
                  <th>Name</th>
                  <th>Email</th>
                  <th>Role</th>
                  <th>Status</th>
                  <th>Actions</th>
                </tr>
              </thead>
              <tbody>
                {users.map((user) => (
                  <tr key={user.id}>
                    <td>{user.username}</td>
                    <td>{user.firstName} {user.lastName}</td>
                    <td>{user.email}</td>
                    <td>{getRoleName(user.roleId)}</td>
                    <td>
                      <span className={user.active ? 'status-active' : 'status-inactive'}>
                        {user.active ? 'Active' : 'Inactive'}
                      </span>
                    </td>
                    <td>
                      {!isManager && (
                        <>
                          <button
                            onClick={() => {
                              setSelectedUser(user);
                              setFormData({
                                username: user.username,
                                email: user.email,
                                password: '',
                                firstName: user.firstName,
                                lastName: user.lastName,
                                roleId: user.roleId,
                                active: user.active,
                              });
                              setShowForm(true);
                            }}
                          >
                            Edit
                          </button>
                          <button
                            onClick={() => handleDelete(user.id)}
                            className="btn-delete"
                          >
                            Deactivate
                          </button>
                        </>
                      )}
                      {isManager && <span style={{ color: '#666' }}>View only</span>}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>

    </div>
  );
}

export default UserManagement;

