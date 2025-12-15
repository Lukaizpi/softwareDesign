import { useState } from 'react';
import { login, register } from '../api/authClient';
import './Login.css';

function Login({ onLoginSuccess }) {
  const [isRegister, setIsRegister] = useState(false);
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [email, setEmail] = useState('');
  const [firstName, setFirstName] = useState('');
  const [lastName, setLastName] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  async function handleLogin(e) {
    e.preventDefault();
    setError('');
    setSuccess('');
    setLoading(true);

    try {
      const result = await login(username, password);
      if (onLoginSuccess) {
        onLoginSuccess(result.user);
      }
    } catch (err) {
      setError(err.message || 'Invalid username or password');
    } finally {
      setLoading(false);
    }
  }

  async function handleRegister(e) {
    e.preventDefault();
    setError('');
    setSuccess('');
    setLoading(true);

    if (!username || !password || !email || !firstName || !lastName) {
      setError('All fields are required');
      setLoading(false);
      return;
    }

    try {
      await register({
        username,
        email,
        password,
        firstName,
        lastName,
        roleId: 3, // Default to Employee role
      });
      setSuccess('Registration successful! You can now login.');
      setIsRegister(false);
      setUsername('');
      setPassword('');
      setEmail('');
      setFirstName('');
      setLastName('');
    } catch (err) {
      setError(err.message || 'Registration failed');
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="login-container">
      <div className="login-box">
        <h1>🍽️ Restaurant Management</h1>
        <h2>{isRegister ? 'Sign Up' : 'Sign In'}</h2>
        
        <div className="auth-tabs">
          <button
            className={!isRegister ? 'active' : ''}
            onClick={() => {
              setIsRegister(false);
              setError('');
              setSuccess('');
            }}
          >
            Login
          </button>
          <button
            className={isRegister ? 'active' : ''}
            onClick={() => {
              setIsRegister(true);
              setError('');
              setSuccess('');
            }}
          >
            Register
          </button>
        </div>

        {isRegister ? (
          <form onSubmit={handleRegister}>
            <div className="form-group">
              <label>First Name</label>
              <input
                type="text"
                value={firstName}
                onChange={(e) => setFirstName(e.target.value)}
                required
                autoFocus
              />
            </div>
            <div className="form-group">
              <label>Last Name</label>
              <input
                type="text"
                value={lastName}
                onChange={(e) => setLastName(e.target.value)}
                required
              />
            </div>
            <div className="form-group">
              <label>Email</label>
              <input
                type="email"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                required
              />
            </div>
            <div className="form-group">
              <label>Username</label>
              <input
                type="text"
                value={username}
                onChange={(e) => setUsername(e.target.value)}
                required
              />
            </div>
            <div className="form-group">
              <label>Password</label>
              <input
                type="password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                required
                minLength="3"
              />
            </div>
            {error && <div className="error-message">{error}</div>}
            {success && <div className="success-message">{success}</div>}
            <button type="submit" disabled={loading} className="btn-primary">
              {loading ? 'Registering...' : 'Register'}
            </button>
          </form>
        ) : (
          <form onSubmit={handleLogin}>
            <div className="form-group">
              <label>Username</label>
              <input
                type="text"
                value={username}
                onChange={(e) => setUsername(e.target.value)}
                required
                autoFocus
              />
            </div>
            <div className="form-group">
              <label>Password</label>
              <input
                type="password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                required
              />
            </div>
            {error && <div className="error-message">{error}</div>}
            {success && <div className="success-message">{success}</div>}
            <button type="submit" disabled={loading} className="btn-primary">
              {loading ? 'Signing in...' : 'Sign In'}
            </button>
          </form>
        )}

        <div className="login-note">
          <p><strong>Default Users:</strong></p>
          <ul>
            <li><strong>admin</strong> / <strong>admin</strong> (SuperAdmin)</li>
            <li><strong>manager</strong> / <strong>manager</strong> (Manager)</li>
            <li><strong>employee</strong> / <strong>employee</strong> (Employee)</li>
          </ul>
        </div>
      </div>
    </div>
  );
}

export default Login;
