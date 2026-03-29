import { useState, useEffect } from 'react';
import {
  getProducts,
  createProduct,
  updateProduct,
  getIngredients,
  getIngredientCategories,
  createIngredient,
  updateIngredient,
  createIngredientCategory,
} from '../api/apiClient';
import './ProductManagement.css';

function ProductManagement() {
  const [products, setProducts] = useState([]);
  const [ingredients, setIngredients] = useState([]);
  const [categories, setCategories] = useState([]);
  const [loading, setLoading] = useState(false);
  const [showProductForm, setShowProductForm] = useState(false);
  const [showIngredientForm, setShowIngredientForm] = useState(false);
  const [showCategoryForm, setShowCategoryForm] = useState(false);
  const [selectedProduct, setSelectedProduct] = useState(null);
  const [selectedIngredient, setSelectedIngredient] = useState(null);
  const [formData, setFormData] = useState({
    name: '',
    type: 'FOOD',
    basePrice: 0,
    taxId: null,
    ingredientIds: [],
  });
  const [ingredientFormData, setIngredientFormData] = useState({
    name: '',
    unit: '',
    price: 0,
    categoryId: null,
    available: true,
    stock: null,
  });
  const [categoryFormData, setCategoryFormData] = useState({
    name: '',
    description: '',
  });

  useEffect(() => {
    loadData();
  }, []);

  async function loadData() {
    try {
      setLoading(true);
      const [prods, ingrs, cats] = await Promise.all([
        getProducts(),
        getIngredients(),
        getIngredientCategories(),
      ]);
      setProducts(prods);
      setIngredients(ingrs);
      setCategories(cats);
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
      if (selectedProduct) {
        await updateProduct(selectedProduct.id, formData);
      } else {
        await createProduct(formData);
      }
      setShowProductForm(false);
      setSelectedProduct(null);
      setFormData({
        name: '',
        type: 'FOOD',
        basePrice: 0,
        taxId: null,
        ingredientIds: [],
      });
      loadData();
    } catch (err) {
      console.error(err);
      alert('Error saving product: ' + (err.message || 'Unknown error'));
    } finally {
      setLoading(false);
    }
  }

  async function handleIngredientSubmit(e) {
    e.preventDefault();
    try {
      setLoading(true);
      if (selectedIngredient) {
        await updateIngredient(selectedIngredient.id, ingredientFormData);
      } else {
        await createIngredient(ingredientFormData);
      }
      setShowIngredientForm(false);
      setSelectedIngredient(null);
      setIngredientFormData({
        name: '',
        unit: '',
        price: 0,
        categoryId: null,
        available: true,
        stock: null,
      });
      loadData();
    } catch (err) {
      console.error(err);
      alert('Error saving ingredient: ' + (err.message || 'Unknown error'));
    } finally {
      setLoading(false);
    }
  }

  async function handleCategorySubmit(e) {
    e.preventDefault();
    try {
      setLoading(true);
      await createIngredientCategory(categoryFormData);
      setShowCategoryForm(false);
      setCategoryFormData({
        name: '',
        description: '',
      });
      loadData();
    } catch (err) {
      console.error(err);
      alert('Error creating category: ' + (err.message || 'Unknown error'));
    } finally {
      setLoading(false);
    }
  }

  function formatCurrency(amount) {
    return new Intl.NumberFormat('en-US', {
      style: 'currency',
      currency: 'EUR',
    }).format(amount);
  }

  return (
    <div className="product-management">
      <div className="management-header">
        <h2>Product & Ingredient Management</h2>
        <button onClick={() => setShowProductForm(!showProductForm)} className="btn-primary">
          {showProductForm ? 'Cancel' : '+ New Product'}
        </button>
      </div>

      {showProductForm && (
        <form onSubmit={handleSubmit} className="product-form">
          <h3>{selectedProduct ? 'Edit Product' : 'Create Product'}</h3>
          
          <div className="form-group">
            <label>Product Name</label>
            <input
              type="text"
              value={formData.name}
              onChange={(e) => setFormData({ ...formData, name: e.target.value })}
              required
            />
          </div>

          <div className="form-group">
            <label>Type</label>
            <select
              value={formData.type}
              onChange={(e) => setFormData({ ...formData, type: e.target.value })}
            >
              <option value="FOOD">Food</option>
              <option value="DRINK">Drink</option>
              <option value="SERVICE">Service</option>
            </select>
          </div>

          <div className="form-group">
            <label>Base Price</label>
            <input
              type="number"
              step="0.01"
              value={formData.basePrice}
              onChange={(e) => setFormData({ ...formData, basePrice: parseFloat(e.target.value) || 0 })}
              required
            />
          </div>

          <div className="form-group">
            <label>Ingredients</label>
            <div className="ingredients-checkbox-list">
              {ingredients.map((ing) => (
                <label key={ing.id} className="checkbox-item">
                  <input
                    type="checkbox"
                    checked={formData.ingredientIds.includes(ing.id)}
                    onChange={(e) => {
                      if (e.target.checked) {
                        setFormData({
                          ...formData,
                          ingredientIds: [...formData.ingredientIds, ing.id],
                        });
                      } else {
                        setFormData({
                          ...formData,
                          ingredientIds: formData.ingredientIds.filter((id) => id !== ing.id),
                        });
                      }
                    }}
                  />
                  <span>{ing.name} ({ing.unit}) - {formatCurrency(ing.price)}</span>
                </label>
              ))}
            </div>
          </div>

          <button type="submit" disabled={loading} className="btn-primary">
            {loading ? 'Saving...' : selectedProduct ? 'Update' : 'Create'}
          </button>
        </form>
      )}

      <div className="products-section">
        <h3>Products ({products.length})</h3>
        {loading ? (
          <p>Loading...</p>
        ) : (
          <div className="products-table-container">
            <table className="products-table">
              <thead>
                <tr>
                  <th>Name</th>
                  <th>Type</th>
                  <th>Base Price</th>
                  <th>Ingredients</th>
                  <th>Actions</th>
                </tr>
              </thead>
              <tbody>
                {products.map((product) => (
                  <tr key={product.id}>
                    <td>{product.name}</td>
                    <td>{product.type || product.category}</td>
                    <td>{formatCurrency(product.basePrice || product.price)}</td>
                    <td>{(product.ingredientIds || []).length}</td>
                    <td>
                      <button
                        onClick={() => {
                          setSelectedProduct(product);
                          setFormData({
                            name: product.name,
                            type: product.type || 'FOOD',
                            basePrice: product.basePrice || product.price,
                            taxId: product.taxId,
                            ingredientIds: product.ingredientIds || [],
                          });
                          setShowProductForm(true);
                        }}
                      >
                        Edit
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>

      <div className="ingredients-section">
        <div className="management-header">
          <h3>Ingredients ({ingredients.length})</h3>
          <div>
            <button 
              onClick={() => setShowCategoryForm(!showCategoryForm)} 
              className="btn-secondary"
              style={{ marginRight: '0.5rem' }}
            >
              {showCategoryForm ? 'Cancel Category' : '+ New Category'}
            </button>
            <button 
              onClick={() => setShowIngredientForm(!showIngredientForm)} 
              className="btn-primary"
            >
              {showIngredientForm ? 'Cancel' : '+ New Ingredient'}
            </button>
          </div>
        </div>

        {showCategoryForm && (
          <form onSubmit={handleCategorySubmit} className="product-form" style={{ marginBottom: '1rem' }}>
            <h3>Create Ingredient Category</h3>
            <div className="form-group">
              <label>Category Name</label>
              <input
                type="text"
                value={categoryFormData.name}
                onChange={(e) => setCategoryFormData({ ...categoryFormData, name: e.target.value })}
                required
              />
            </div>
            <div className="form-group">
              <label>Description</label>
              <textarea
                value={categoryFormData.description}
                onChange={(e) => setCategoryFormData({ ...categoryFormData, description: e.target.value })}
                rows="3"
              />
            </div>
            <button type="submit" disabled={loading} className="btn-primary">
              {loading ? 'Creating...' : 'Create Category'}
            </button>
          </form>
        )}

        {showIngredientForm && (
          <form onSubmit={handleIngredientSubmit} className="product-form" style={{ marginBottom: '1rem' }}>
            <h3>{selectedIngredient ? 'Edit Ingredient' : 'Create Ingredient'}</h3>
            <div className="form-group">
              <label>Ingredient Name</label>
              <input
                type="text"
                value={ingredientFormData.name}
                onChange={(e) => setIngredientFormData({ ...ingredientFormData, name: e.target.value })}
                required
              />
            </div>
            <div className="form-row">
              <div className="form-group">
                <label>Unit</label>
                <input
                  type="text"
                  value={ingredientFormData.unit}
                  onChange={(e) => setIngredientFormData({ ...ingredientFormData, unit: e.target.value })}
                  placeholder="e.g., kg, g, ml, piece"
                  required
                />
              </div>
              <div className="form-group">
                <label>Price per Unit</label>
                <input
                  type="number"
                  step="0.01"
                  value={ingredientFormData.price}
                  onChange={(e) => setIngredientFormData({ ...ingredientFormData, price: parseFloat(e.target.value) || 0 })}
                  required
                />
              </div>
            </div>
            <div className="form-group">
              <label>Category</label>
              <select
                value={ingredientFormData.categoryId || ''}
                onChange={(e) => setIngredientFormData({ ...ingredientFormData, categoryId: e.target.value ? parseInt(e.target.value) : null })}
              >
                <option value="">Select category</option>
                {categories.map((cat) => (
                  <option key={cat.id} value={cat.id}>
                    {cat.name}
                  </option>
                ))}
              </select>
            </div>
            <div className="form-row">
              <div className="form-group">
                <label>Stock (optional, leave empty for unlimited)</label>
                <input
                  type="number"
                  step="0.01"
                  value={ingredientFormData.stock !== null ? ingredientFormData.stock : ''}
                  onChange={(e) => setIngredientFormData({ ...ingredientFormData, stock: e.target.value ? parseFloat(e.target.value) : null })}
                />
              </div>
              <div className="form-group">
                <label>Available</label>
                <select
                  value={ingredientFormData.available ? 'true' : 'false'}
                  onChange={(e) => setIngredientFormData({ ...ingredientFormData, available: e.target.value === 'true' })}
                >
                  <option value="true">Yes</option>
                  <option value="false">No</option>
                </select>
              </div>
            </div>
            <button type="submit" disabled={loading} className="btn-primary">
              {loading ? 'Saving...' : selectedIngredient ? 'Update' : 'Create'}
            </button>
          </form>
        )}

        {loading ? (
          <p>Loading...</p>
        ) : ingredients.length === 0 ? (
          <p className="empty-state">No ingredients available</p>
        ) : (
          <div className="products-table-container">
            <table className="products-table">
              <thead>
                <tr>
                  <th>Name</th>
                  <th>Category</th>
                  <th>Unit</th>
                  <th>Price</th>
                  <th>Stock</th>
                  <th>Status</th>
                  <th>Actions</th>
                </tr>
              </thead>
              <tbody>
                {ingredients.map((ing) => (
                  <tr key={ing.id}>
                    <td>{ing.name}</td>
                    <td>{categories.find((c) => c.id === ing.categoryId)?.name || 'N/A'}</td>
                    <td>{ing.unit}</td>
                    <td>{formatCurrency(ing.price)}</td>
                    <td>{ing.stock !== null ? ing.stock : 'Unlimited'}</td>
                    <td>
                      <span className={ing.available ? 'status-active' : 'status-inactive'}>
                        {ing.available ? 'Available' : 'Unavailable'}
                      </span>
                    </td>
                    <td>
                      <button
                        onClick={() => {
                          setSelectedIngredient(ing);
                          setIngredientFormData({
                            name: ing.name,
                            unit: ing.unit,
                            price: ing.price,
                            categoryId: ing.categoryId,
                            available: ing.available,
                            stock: ing.stock,
                          });
                          setShowIngredientForm(true);
                        }}
                      >
                        Edit
                      </button>
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

export default ProductManagement;


