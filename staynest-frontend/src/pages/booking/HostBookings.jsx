import { useState, useEffect } from 'react'
import { Link } from 'react-router-dom'
import { FiCalendar, FiUsers, FiCheck, FiX } from 'react-icons/fi'
import { propertyApi, bookingApi } from '../../api'
import { PageLoader, Button, StatusBadge, EmptyState } from '../../components/ui'
import Layout from '../../components/layout/Layout'
import toast from 'react-hot-toast'
import { format } from 'date-fns'

export default function HostBookings() {
  const [properties, setProperties] = useState([])
  const [bookings,   setBookings]   = useState([])
  const [loading,    setLoading]    = useState(true)
  const [selectedProp, setSelectedProp] = useState('all')
  const [tab, setTab] = useState('all')

  useEffect(() => {
    const load = async () => {
      try {
        const propsRes = await propertyApi.getMyProperties()
        setProperties(propsRes.data)
        const allBookings = []
        for (const prop of propsRes.data) {
          try {
            const bRes = await bookingApi.getByProperty(prop.propertyId)
            allBookings.push(...bRes.data)
          } catch {}
        }
        setBookings(allBookings)
      } catch {}
      finally { setLoading(false) }
    }
    load()
  }, [])

  const filtered = bookings.filter(b => {
    const propMatch = selectedProp === 'all' || b.propertyId === selectedProp
    const tabMatch  = tab === 'all'
      || (tab === 'pending'   && b.bookingStatus === 'PENDING')
      || (tab === 'confirmed' && b.bookingStatus === 'CONFIRMED')
      || (tab === 'completed' && b.bookingStatus === 'COMPLETED')
    return propMatch && tabMatch
  })

  const handleAction = async (bookingId, action) => {
    try {
      if (action === 'confirm')  await bookingApi.confirm(bookingId)
      if (action === 'complete') await bookingApi.complete(bookingId)
      toast.success(`Booking ${action}d!`)
      setBookings(bs => bs.map(b => b.bookingId === bookingId ? { ...b, bookingStatus: action === 'confirm' ? 'CONFIRMED' : 'COMPLETED' } : b))
    } catch (err) { toast.error(err.response?.data?.message || 'Action failed') }
  }

  return (
    <Layout>
      <div className="max-w-6xl mx-auto px-4 sm:px-6 py-10">
        <h1 className="text-3xl font-bold text-staynest-dark mb-6">Guest Bookings</h1>

        {/* Filters */}
        <div className="flex flex-col sm:flex-row gap-3 mb-6">
          <select value={selectedProp} onChange={e => setSelectedProp(e.target.value)} className="input-base max-w-xs text-sm">
            <option value="all">All properties</option>
            {properties.map(p => <option key={p.propertyId} value={p.propertyId}>{p.propertyName}</option>)}
          </select>

          <div className="flex gap-1">
            {['all','pending','confirmed','completed'].map(t => (
              <button key={t} onClick={() => setTab(t)}
                className={`px-4 py-2 text-sm rounded-full capitalize transition-colors ${tab === t ? 'bg-staynest-dark text-white' : 'bg-staynest-bg text-staynest-gray hover:text-staynest-dark'}`}>
                {t}
              </button>
            ))}
          </div>
        </div>

        {loading ? <PageLoader /> : filtered.length === 0 ? (
          <EmptyState icon={<FiCalendar />} title="No bookings" description="Bookings for your properties will appear here." />
        ) : (
          <div className="space-y-3">
            {filtered.map(b => (
              <div key={b.bookingId} className="bg-white border border-staynest-light rounded-2xl p-5 hover:shadow-card transition-shadow">
                <div className="flex flex-col sm:flex-row items-start gap-4">
                  <div className="flex-1 min-w-0">
                    <div className="flex items-center gap-2 flex-wrap mb-2">
                      <h3 className="font-semibold text-staynest-dark">{b.guestName}</h3>
                      <span className="text-staynest-gray text-sm">·</span>
                      <span className="text-staynest-gray text-sm">{b.propertyName}</span>
                      <StatusBadge status={b.bookingStatus} />
                    </div>
                    <div className="flex flex-wrap gap-4 text-sm text-staynest-gray">
                      <span className="flex items-center gap-1.5"><FiCalendar className="text-xs" />
                        {b.checkInDate && format(new Date(b.checkInDate), 'MMM d')} – {b.checkOutDate && format(new Date(b.checkOutDate), 'MMM d, yyyy')}
                      </span>
                      <span className="flex items-center gap-1.5"><FiUsers className="text-xs" />{b.numGuests} guests</span>
                      <span className="font-semibold text-staynest-dark">₹{(b.finalPrice || b.totalPrice)?.toLocaleString('en-IN')}</span>
                    </div>
                  </div>

                  <div className="flex items-center gap-2 shrink-0">
                    <Link to={`/bookings/${b.bookingId}`}>
                      <Button variant="outline" size="sm">View</Button>
                    </Link>
                    {b.bookingStatus === 'PENDING' && (
                      <Button size="sm" onClick={() => handleAction(b.bookingId, 'confirm')}>
                        <FiCheck /> Confirm
                      </Button>
                    )}
                    {b.bookingStatus === 'CONFIRMED' && (
                      <Button size="sm" onClick={() => handleAction(b.bookingId, 'complete')}>
                        <FiCheck /> Complete
                      </Button>
                    )}
                  </div>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>
    </Layout>
  )
}
