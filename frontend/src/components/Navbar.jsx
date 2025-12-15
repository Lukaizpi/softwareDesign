// src/components/Navbar.jsx
import { NavLink } from "react-router-dom";

const linkStyle = ({ isActive }) => ({
  marginRight: 12,
  textDecoration: "none",
  fontWeight: isActive ? 700 : 400,
});

export default function Navbar() {
  return (
    <header style={{ padding: "1rem 1.25rem", borderBottom: "1px solid #333" }}>
      <NavLink to="/orders" style={linkStyle}>Órdenes</NavLink>
      <NavLink to="/products" style={linkStyle}>Productos</NavLink>
    </header>
  );
}