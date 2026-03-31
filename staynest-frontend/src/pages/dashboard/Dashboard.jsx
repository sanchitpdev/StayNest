import { useState, useEffect } from 'react'
import { Link } from 'react-router-dom'
import { FiHome, FiCalendar, FiStar, FiDollarSign, FiTrendingUp, FiUsers, FiHeart, FiArrowRight } from 'react-icons/fi'
import { dashboardApi } from '../../api'
import { PageLoader, StatusBadge, Button } from '../../components/ui'
import Layout from '../../components/layout/Layout'
import { useAuth } from '../../context/AuthContext'
import { format } from 'date-fns'

export default function Dashboard() {
  const { user, isHost } = useAuth()
  const [stats,   setStats]   = useState(null)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    dashboardApi.getStats().then(r => setStats(r.data)).catch(() => setStats(null)).finally(() => setLoading(false))
  }, [])

  if (loading) return <Layout><PageLoader /></Layout>

  const StatCard = ({ icon, label, value, sub, color = 'primary' }) => {
    const colors = { primary: 'bg-primary/10 text-primary', teal: 'bg-secondary/10 text-secondary', yellow: 'bg-yellow-100 text-yellow-600', blue: 'bg-blue-100 text-blue-600' }
    return (
      <div className="bg-white border border-staynest-light rounded-2xl p-5 hover:shadow-card transition-shadow">
        <div className={`w-10 h-10 rounded-xl flex items-center justify-center text-xl mb-3 ${colors[color]}`}>{icon}</div>
        <p className="text-2xl font-bold text-staynest-dark">{value ?? '—'}</p>
        <p className="text-sm font-medium text-staynest-dark mt-0.5">{label}</p>
        {sub && <p className="text-xs text-staynest-gray mt-1">{sub}</p>}
      </div>
    )
  }

  return (
    <Layout>
      <div className="max-w-6xl mx-auto px-4 sm:px-6 py-10">
        {/* Header */}
        <div className="mb-8">
          <h1 className="text-3xl font-bold text-staynest-dark">
            Welcome back, {user?.firstName}! 👋
          </h1>
          <p className="text-staynest-gray mt-1">Here's what's happening with your account</p>
        </div>

        {/* Stats grid */}
        {isHost ? (
          <>
            <div className="grid grid-cols-2 lg:grid-cols-4 gap-4 mb-8">
              <StatCard icon={<FiHome />}      label="Total Properties"  value={stats?.totalProperties}  color="primary" />
              <StatCard icon={<FiCalendar />}  label="Total Bookings"    value={stats?.totalBookings}    color="teal" />
              <StatCard icon={<FiDollarSign />} label="Total Revenue"   value={stats?.totalEarnings ? `₹${Number(stats.totalEarnings).toLocaleString('en-IN')}` : '₹0'} color="yellow" />
              <StatCard icon={<FiStar />}      label="Average Rating"    value={stats?.averageRating ? Number(stats.averageRating).toFixed(1) : 'New'} color="blue" />
            </div>
            <div className="grid grid-cols-2 lg:grid-cols-4 gap-4 mb-10">
              <StatCard icon={<FiTrendingUp />} label="Pending Bookings"   value={stats?.pendingBookings}   color="primary" />
              <StatCard icon={<FiCalendar />}   label="Confirmed Bookings" value={stats?.confirmedBookings} color="teal" />
              <StatCard icon={<FiUsers />}      label="Total Guests"       value={stats?.totalGuests}        color="blue" />
              <StatCard icon={<FiStar />}       label="Total Reviews"      value={stats?.totalReviews}       color="yellow" />
            </div>
          </>
        ) : (
          <div className="grid grid-cols-2 lg:grid-cols-4 gap-4 mb-10">
            <StatCard icon={<FiCalendar />}   label="Total Bookings"    value={stats?.totalBookings}    color="primary" />
            <StatCard icon={<FiCalendar />}   label="Upcoming Trips"    value={stats?.upcomingBookings} color="teal" />
            <StatCard icon={<FiHeart />}      label="Saved Properties"  value={stats?.wishlistCount}    color="yellow" />
            <StatCard icon={<FiStar />}       label="Reviews Written"   value={stats?.totalReviews}     color="blue" />
          </div>
        )}

        {/* Quick actions */}
        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
          {/* Recent bookings */}
          <div className="bg-white border border-staynest-light rounded-2xl p-6">
            <div className="flex items-center justify-between mb-4">
              <h3 className="font-semibold text-staynest-dark">Recent Bookings</h3>
              <Link to="/bookings" className="text-sm text-primary hover:underline flex items-center gap-1">View all <FiArrowRight className="text-xs" /></Link>
            </div>
            {stats?.recentBookings?.length > 0 ? (
              <div className="space-y-3">
                {stats.recentBookings.slice(0, 4).map(b => (
                  <Link key={b.bookingId} to={`/bookings/${b.bookingId}`}
                    className="flex items-center justify-between p-3 rounded-xl hover:bg-staynest-bg transition-colors">
                    <div>
                      <p className="text-sm font-medium text-staynest-dark">{b.propertyName}</p>
                      <p className="text-xs text-staynest-gray mt-0.5">
                        {b.checkInDate && format(new Date(b.checkInDate), 'MMM d')} – {b.checkOutDate && format(new Date(b.checkOutDate), 'MMM d')}
                      </p>
                    </div>
                    <StatusBadge status={b.bookingStatus} />
                  </Link>
                ))}
              </div>
            ) : (
              <div className="text-center py-8">
                <p className="text-staynest-gray text-sm">No bookings yet</p>
                <Link to="/properties"><Button variant="secondary" size="sm" className="mt-3">Find a stay</Button></Link>
              </div>
            )}
          </div>

          {/* Quick links */}
          <div className="bg-white border border-staynest-light rounded-2xl p-6">
            <h3 className="font-semibold text-staynest-dark mb-4">Quick Actions</h3>
            <div className="space-y-2">
              {[
                { icon: <FiHome />,     to: '/properties',           label: 'Browse properties',    desc: 'Find your next stay' },
                { icon: <FiCalendar />, to: '/bookings',             label: 'My trips',             desc: 'View all bookings' },
                { icon: <FiHeart />,    to: '/wishlist',             label: 'Saved stays',          desc: 'Your wishlist' },
                isHost && { icon: <FiTrendingUp />, to: '/host/properties', label: 'Manage listings', desc: 'Your properties' },
                isHost && { icon: <FiCalendar />, to: '/host/bookings', label: 'Guest bookings', desc: 'Manage guest reservations' },
              ].filter(Boolean).map(item => (
                <Link key={item.to} to={item.to}
                  className="flex items-center gap-3 p-3 rounded-xl hover:bg-staynest-bg transition-colors group">
                  <div className="w-9 h-9 bg-primary/10 text-primary rounded-xl flex items-center justify-center text-sm">{item.icon}</div>
                  <div className="flex-1">
                    <p className="text-sm font-medium text-staynest-dark">{item.label}</p>
                    <p className="text-xs text-staynest-gray">{item.desc}</p>
                  </div>
                  <FiArrowRight className="text-staynest-gray group-hover:text-staynest-dark transition-colors text-sm" />
                </Link>
              ))}
            </div>
          </div>
        </div>
      </div>
    </Layout>
  )
}
