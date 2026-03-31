import { Routes, Route, Navigate } from 'react-router-dom'
import { useAuth } from './context/AuthContext'
import { Spinner } from './components/ui'

// Pages
import Home             from './pages/Home'
import { Login, Register } from './pages/auth/AuthPages'
import PropertyList     from './pages/property/PropertyList'
import PropertyDetail   from './pages/property/PropertyDetail'
import CreateProperty   from './pages/property/CreateProperty'
import HostDashboard    from './pages/property/HostDashboard'
import { MyBookings, BookingDetail } from './pages/booking/BookingPages'
import HostBookings     from './pages/booking/HostBookings'
import Dashboard        from './pages/dashboard/Dashboard'
import Messages         from './pages/messages/Messages'
import { Wishlist }     from './pages/wishlist/Wishlist'
import Profile          from './pages/user/Profile'

// Route guards
function PrivateRoute({ children }) {
  const { isAuthenticated, loading } = useAuth()
  if (loading) return <div className="min-h-screen flex items-center justify-center"><Spinner size="lg" className="text-primary" /></div>
  return isAuthenticated ? children : <Navigate to="/login" replace />
}

function HostRoute({ children }) {
  const { isAuthenticated, isHost, isAdmin, loading } = useAuth()
  if (loading) return <div className="min-h-screen flex items-center justify-center"><Spinner size="lg" className="text-primary" /></div>
  if (!isAuthenticated)    return <Navigate to="/login"      replace />
  if (!isHost && !isAdmin) return <Navigate to="/dashboard"  replace />
  return children
}

function GuestRoute({ children }) {
  const { isAuthenticated, loading } = useAuth()
  if (loading) return null
  return isAuthenticated ? <Navigate to="/" replace /> : children
}

export default function App() {
  return (
    <Routes>
      {/* Public */}
      <Route path="/"            element={<Home />} />
      <Route path="/properties"  element={<PropertyList />} />
      <Route path="/properties/:id" element={<PropertyDetail />} />

      {/* Auth (guest-only) */}
      <Route path="/login"    element={<GuestRoute><Login /></GuestRoute>} />
      <Route path="/register" element={<GuestRoute><Register /></GuestRoute>} />

      {/* Authenticated */}
      <Route path="/dashboard"  element={<PrivateRoute><Dashboard /></PrivateRoute>} />
      <Route path="/bookings"   element={<PrivateRoute><MyBookings /></PrivateRoute>} />
      <Route path="/bookings/:id" element={<PrivateRoute><BookingDetail /></PrivateRoute>} />
      <Route path="/messages"   element={<PrivateRoute><Messages /></PrivateRoute>} />
      <Route path="/wishlist"   element={<PrivateRoute><Wishlist /></PrivateRoute>} />
      <Route path="/profile"    element={<PrivateRoute><Profile /></PrivateRoute>} />

      {/* Host-only */}
      <Route path="/host/properties"            element={<HostRoute><HostDashboard /></HostRoute>} />
      <Route path="/host/properties/create"     element={<HostRoute><CreateProperty /></HostRoute>} />
      <Route path="/host/properties/edit/:id"   element={<HostRoute><CreateProperty /></HostRoute>} />
      <Route path="/host/bookings"              element={<HostRoute><HostBookings /></HostRoute>} />

      {/* 404 */}
      <Route path="*" element={
        <div className="min-h-screen flex flex-col items-center justify-center gap-4 text-center px-4">
          <p className="text-8xl">🏚️</p>
          <h1 className="text-3xl font-bold text-staynest-dark">Page not found</h1>
          <p className="text-staynest-gray">The page you're looking for doesn't exist.</p>
          <a href="/" className="btn-primary mt-2">Go home</a>
        </div>
      } />
    </Routes>
  )
}
