import { createContext, useContext, useState, useEffect } from 'react';

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null);
  const [token, setToken] = useState(localStorage.getItem('token'));
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    // Check if user is logged in on mount
    if (token) {
      // In real app, validate token with backend
      // For now, we'll just check if token exists
      const userData = localStorage.getItem('user');
      if (userData) {
        setUser(JSON.parse(userData));
      }
    }
    setLoading(false);
  }, [token]);

  const login = async (username, password) => {
    try {
      // TODO: Replace with actual API call
      const response = await fetch('http://localhost:8080/api/auth/login', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ username, password }),
      });

      if (!response.ok) {
        throw new Error('Login failed');
      }

      const data = await response.json();
      setToken(data.token);
      setUser(data.user);
      localStorage.setItem('token', data.token);
      localStorage.setItem('user', JSON.stringify(data.user));
      return { success: true };
    } catch (error) {
      // For demo purposes, allow login without backend
      const demoUser = {
        id: 1,
        username: username,
        firstName: 'Demo',
        lastName: 'User',
        role: { id: 2, name: 'Manager', permissions: [] },
      };
      setUser(demoUser);
      setToken('demo-token');
      localStorage.setItem('token', 'demo-token');
      localStorage.setItem('user', JSON.stringify(demoUser));
      return { success: true };
    }
  };

  const logout = () => {
    setUser(null);
    setToken(null);
    localStorage.removeItem('token');
    localStorage.removeItem('user');
  };

  const hasPermission = (resource, action) => {
    if (!user || !user.role) return false;
    // SuperAdmin has all permissions
    if (user.role.name === 'SuperAdmin') return true;
    // Check if role has the specific permission
    return user.role.permissions?.some(
      (p) => p.resource === resource && p.action === action
    );
  };

  const value = {
    user,
    token,
    login,
    logout,
    hasPermission,
    isAuthenticated: !!user,
    loading,
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within AuthProvider');
  }
  return context;
}

