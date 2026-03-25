import { createContext, useContext, useState } from 'react'

const AuthContext = createContext(null)

function normalizeStoredValue(value) {
  if (!value || value === 'null' || value === 'undefined') return null
  return value
}

export function AuthProvider({ children }) {
  const [token, setToken] = useState(normalizeStoredValue(localStorage.getItem('token')))
  const [username, setUsername] = useState(normalizeStoredValue(localStorage.getItem('username')))
  const [role, setRole] = useState(normalizeStoredValue(localStorage.getItem('role')))

  const login = (newToken, newUsername, newRole) => {
    localStorage.setItem('token', newToken)
    localStorage.setItem('username', newUsername)
    localStorage.setItem('role', newRole || 'USER')
    setToken(newToken)
    setUsername(newUsername)
    setRole(newRole || 'USER')
  }

  const logout = () => {
    localStorage.removeItem('token')
    localStorage.removeItem('username')
    localStorage.removeItem('role')
    setToken(null)
    setUsername(null)
    setRole(null)
  }

  return (
    <AuthContext.Provider value={{
      token, username, role,
      login, logout,
      isAuthenticated: !!token,
      isAdmin: role === 'ADMIN',
    }}>
      {children}
    </AuthContext.Provider>
  )
}

export const useAuth = () => useContext(AuthContext)
