import { useState, useEffect } from 'react'
import { useParams, useNavigate, Link } from 'react-router-dom'
import { FiCalendar, FiUsers, FiMapPin, FiCreditCard, FiMessageSquare, FiStar, FiCheck } from 'react-icons/fi'
import { bookingApi, paymentApi, reviewApi, couponApi } from '../../api'
import { PageLoader, Button, StatusBadge, Badge, Modal, EmptyState, Input, Textarea } from '../../components/ui'
import Layout from '../../components/layout/Layout'
import { useAuth } from '../../context/AuthContext'
import toast from 'react-hot-toast'
import { format } from 'date-fns'

export function MyBookings() {
  const navigate = useNavigate()
  const [bookings, setBookings] = useState([])
  const [loading, setLoading]   = useState(true)
  const [tab, setTab]           = useState('all')

  useEffect(() => {
    bookingApi.getMyBookings().then(r => setBookings(r.data)).catch(() => setBookings([])).finally(() => setLoading(false))
  }, [])

  const filtered = bookings.filter(b => {
    if (tab === 'upcoming')  return ['CONFIRMED','PENDING'].includes(b.bookingStatus)
    if (tab === 'completed') return b.bookingStatus === 'COMPLETED'
    if (tab === 'cancelled') return b.bookingStatus === 'CANCELLED'
    return true
  })

  return (
    <Layout>
      <div className="max-w-4xl mx-auto px-4 sm:px-6 py-10">
        <h1 className="text-3xl font-bold text-staynest-dark mb-6">My Trips</h1>
        <div className="flex gap-1 border-b border-staynest-light mb-8">
          {['all','upcoming','completed','cancelled'].map(t => (
            <button key={t} onClick={() => setTab(t)}
              className={`px-4 py-3 text-sm font-medium capitalize border-b-2 transition-colors -mb-px ${tab === t ? 'border-staynest-dark text-staynest-dark' : 'border-transparent text-staynest-gray hover:text-staynest-dark'}`}>
              {t}
            </button>
          ))}
        </div>
        {loading ? <PageLoader /> : filtered.length === 0 ? (
          <EmptyState icon="✈️" title="No trips found" description="Your bookings will appear here."
            action={<Button onClick={() => navigate('/properties')}>Explore stays</Button>} />
        ) : (
          <div className="space-y-4">
            {filtered.map(b => (
              <Link key={b.bookingId} to={`/bookings/${b.bookingId}`}
                className="flex flex-col sm:flex-row gap-4 bg-white border border-staynest-light rounded-2xl overflow-hidden hover:shadow-card transition-shadow p-4 group">
                <div className="w-full sm:w-36 h-28 rounded-xl shrink-0 bg-gradient-to-br from-primary/10 to-secondary/10 flex items-center justify-center text-4xl">🏠</div>
                <div className="flex-1 min-w-0">
                  <div className="flex items-start justify-between gap-2">
                    <h3 className="font-semibold text-staynest-dark truncate group-hover:text-primary transition-colors">{b.propertyName}</h3>
                    <StatusBadge status={b.bookingStatus} />
                  </div>
                  <p className="text-staynest-gray text-sm mt-1">{b.propertyCity}</p>
                  <div className="flex flex-wrap items-center gap-4 mt-3 text-sm text-staynest-gray">
                    <span className="flex items-center gap-1.5">
                      <FiCalendar className="text-xs" />
                      {b.checkInDate && format(new Date(b.checkInDate), 'MMM d')} – {b.checkOutDate && format(new Date(b.checkOutDate), 'MMM d, yyyy')}
                    </span>
                    <span className="flex items-center gap-1.5"><FiUsers className="text-xs" />{b.numGuests} guest{b.numGuests > 1 ? 's' : ''}</span>
                    <span className="font-semibold text-staynest-dark">₹{(b.finalPrice || b.totalPrice)?.toLocaleString('en-IN')}</span>
                  </div>
                </div>
              </Link>
            ))}
          </div>
        )}
      </div>
    </Layout>
  )
}

export function BookingDetail() {
  const { id }   = useParams()
  const navigate = useNavigate()
  const { isHost, user } = useAuth()

  const [booking, setBooking]   = useState(null)
  const [loading, setLoading]   = useState(true)
  const [payModal, setPayModal] = useState(false)
  const [revModal, setRevModal] = useState(false)
  const [couponModal, setCouponModal] = useState(false)
  const [actL, setActL]         = useState(false)

  const [payForm, setPayForm]   = useState({ amount: '', paymentMethod: 'UPI' })
  const [revForm, setRevForm]   = useState({ rating: 5, comment: '', cleanlinessRating: 5, accuracyRating: 5, communicationRating: 5, locationRating: 5, valueRating: 5 })
  const [couponCode, setCouponCode] = useState('')

  const load = () => bookingApi.getById(id).then(r => setBooking(r.data)).catch(() => navigate('/bookings')).finally(() => setLoading(false))
  useEffect(() => { load() }, [id])

  const isGuest = booking?.guestId === user?.userId

  const cancelBooking  = async () => { setActL(true); try { await bookingApi.cancel(id);    toast.success('Cancelled');      load() } catch (e) { toast.error(e.response?.data?.message || 'Error') } finally { setActL(false) } }
  const confirmBooking = async () => { setActL(true); try { await bookingApi.confirm(id);   toast.success('Confirmed!');     load() } catch (e) { toast.error(e.response?.data?.message || 'Error') } finally { setActL(false) } }
  const completeBooking= async () => { setActL(true); try { await bookingApi.complete(id);  toast.success('Completed!');    load() } catch (e) { toast.error(e.response?.data?.message || 'Error') } finally { setActL(false) } }

  const makePayment = async () => {
    setActL(true)
    try { await paymentApi.create({ bookingId: id, amount: Number(payForm.amount), paymentMethod: payForm.paymentMethod }); toast.success('Payment recorded!'); setPayModal(false); load() }
    catch (e) { toast.error(e.response?.data?.message || 'Payment failed') } finally { setActL(false) }
  }

  const submitReview = async () => {
    setActL(true)
    try { await reviewApi.create({ ...revForm, bookingId: id }); toast.success('Review submitted!'); setRevModal(false); load() }
    catch (e) { toast.error(e.response?.data?.message || 'Failed') } finally { setActL(false) }
  }

  const applyCoupon = async () => {
    setActL(true)
    try { await couponApi.apply({ bookingId: id, couponCode }); toast.success('Coupon applied!'); setCouponModal(false); load() }
    catch (e) { toast.error(e.response?.data?.message || 'Invalid coupon') } finally { setActL(false) }
  }

  if (loading) return <Layout><PageLoader /></Layout>
  if (!booking) return null

  return (
    <Layout>
      <div className="max-w-4xl mx-auto px-4 sm:px-6 py-10">
        <button onClick={() => navigate(-1)} className="text-sm text-staynest-gray hover:text-staynest-dark mb-6 block">← Back</button>
        <div className="grid grid-cols-1 lg:grid-cols-[1fr_300px] gap-8">
          {/* Left */}
          <div>
            <div className="flex items-center justify-between mb-1">
              <h1 className="text-2xl font-bold text-staynest-dark">Booking Details</h1>
              <StatusBadge status={booking.bookingStatus} />
            </div>
            <p className="text-staynest-gray text-xs mb-7">ID: {booking.bookingId?.slice(0, 8).toUpperCase()}</p>

            {/* Property */}
            <div className="bg-staynest-bg rounded-2xl p-5 mb-6 flex items-start gap-4">
              <div className="w-16 h-16 rounded-xl bg-gradient-to-br from-primary/20 to-secondary/20 flex items-center justify-center text-3xl shrink-0">🏠</div>
              <div>
                <h3 className="font-semibold text-staynest-dark">{booking.propertyName}</h3>
                <p className="text-staynest-gray text-sm">{booking.unitName} · Unit {booking.unitNumber}</p>
                <p className="text-staynest-gray text-sm flex items-center gap-1 mt-0.5"><FiMapPin className="text-xs" />{booking.propertyCity}</p>
                <Link to={`/properties/${booking.propertyId}`} className="text-xs text-primary hover:underline mt-1 inline-block">View property →</Link>
              </div>
            </div>

            {/* Dates grid */}
            <div className="grid grid-cols-2 gap-3 mb-6">
              {[
                { label: 'Check-in',  value: booking.checkInDate  && format(new Date(booking.checkInDate),  'EEE, MMM d yyyy') },
                { label: 'Check-out', value: booking.checkOutDate && format(new Date(booking.checkOutDate), 'EEE, MMM d yyyy') },
                { label: 'Duration',  value: `${booking.numberOfNights} night${booking.numberOfNights > 1 ? 's' : ''}` },
                { label: 'Guests',    value: `${booking.numGuests} guest${booking.numGuests > 1 ? 's' : ''}` },
              ].map(item => (
                <div key={item.label} className="border border-staynest-light rounded-xl p-4">
                  <p className="text-xs text-staynest-gray uppercase tracking-wide mb-1">{item.label}</p>
                  <p className="font-semibold text-staynest-dark text-sm">{item.value}</p>
                </div>
              ))}
            </div>

            {/* Special request */}
            {booking.specialRequests && (
              <div className="mb-6 p-4 bg-blue-50 rounded-xl">
                <p className="text-xs font-semibold text-blue-700 mb-1">Special request</p>
                <p className="text-sm text-blue-700">{booking.specialRequests}</p>
              </div>
            )}

            {/* Payments */}
            {booking.payments?.length > 0 && (
              <div className="mb-6">
                <h3 className="font-semibold text-staynest-dark mb-3">Payments</h3>
                {booking.payments.map(p => (
                  <div key={p.paymentId} className="flex items-center justify-between border border-staynest-light rounded-xl px-4 py-3 text-sm mb-2">
                    <div className="flex items-center gap-2">
                      <FiCreditCard className="text-staynest-gray" />
                      <span className="capitalize">{p.paymentMethod?.replace('_',' ').toLowerCase()}</span>
                      <Badge variant={p.paymentStatus === 'COMPLETED' ? 'success' : 'warning'}>{p.paymentStatus}</Badge>
                    </div>
                    <span className="font-semibold">₹{p.amount?.toLocaleString('en-IN')}</span>
                  </div>
                ))}
              </div>
            )}

            {/* Review */}
            {booking.review && (
              <div className="border border-staynest-light rounded-xl p-4">
                <h3 className="font-semibold text-staynest-dark mb-3">Your review</h3>
                <div className="flex items-center gap-1 mb-2">
                  {Array.from({ length: 5 }, (_, i) => (
                    <FiStar key={i} className={`text-sm ${i < booking.review.rating ? 'fill-yellow-400 text-yellow-400' : 'text-staynest-light'}`} />
                  ))}
                </div>
                <p className="text-sm text-staynest-dark">{booking.review.comment}</p>
              </div>
            )}
          </div>

          {/* Right - price + actions */}
          <div>
            <div className="bg-white border border-staynest-light rounded-2xl p-5 shadow-widget sticky top-24">
              <h3 className="font-semibold text-staynest-dark mb-4">Price summary</h3>
              <div className="space-y-2 text-sm">
                <div className="flex justify-between"><span className="text-staynest-gray">Subtotal</span><span>₹{booking.totalPrice?.toLocaleString('en-IN')}</span></div>
                {booking.discountAmount > 0 && (
                  <div className="flex justify-between text-green-600"><span>Discount</span><span>-₹{booking.discountAmount?.toLocaleString('en-IN')}</span></div>
                )}
                <div className="border-t border-staynest-light pt-2 flex justify-between font-bold">
                  <span>Total</span><span>₹{(booking.finalPrice || booking.totalPrice)?.toLocaleString('en-IN')}</span>
                </div>
                {booking.totalPaid > 0 && <div className="flex justify-between text-green-600 text-xs"><span>Paid</span><span>₹{booking.totalPaid?.toLocaleString('en-IN')}</span></div>}
                {booking.remainingAmount > 0 && <div className="flex justify-between text-red-500 text-xs"><span>Remaining</span><span>₹{booking.remainingAmount?.toLocaleString('en-IN')}</span></div>}
              </div>

              {booking.appliedCouponCode && (
                <div className="mt-3 bg-green-50 text-green-700 text-xs px-3 py-2 rounded-lg">Coupon <strong>{booking.appliedCouponCode}</strong> applied ✓</div>
              )}

              <div className="mt-5 space-y-2">
                {isGuest && booking.bookingStatus === 'PENDING' && (
                  <>
                    {!booking.appliedCouponCode && <Button variant="secondary" className="w-full" onClick={() => setCouponModal(true)}>Apply coupon</Button>}
                    <Button className="w-full" onClick={() => setPayModal(true)}><FiCreditCard /> Pay now</Button>
                    <Button variant="danger" className="w-full" loading={actL} onClick={cancelBooking}>Cancel</Button>
                  </>
                )}
                {isGuest && booking.bookingStatus === 'CONFIRMED' && (
                  <Button className="w-full" onClick={() => setPayModal(true)}><FiCreditCard /> Pay now</Button>
                )}
                {isGuest && booking.bookingStatus === 'COMPLETED' && !booking.review && (
                  <Button className="w-full" onClick={() => setRevModal(true)}><FiStar /> Write review</Button>
                )}
                {isHost && booking.bookingStatus === 'PENDING' && (
                  <Button className="w-full" loading={actL} onClick={confirmBooking}><FiCheck /> Confirm</Button>
                )}
                {isHost && booking.bookingStatus === 'CONFIRMED' && (
                  <Button className="w-full" loading={actL} onClick={completeBooking}><FiCheck /> Mark completed</Button>
                )}
                <Button variant="secondary" className="w-full" onClick={() => navigate('/messages')}>
                  <FiMessageSquare /> Message {isGuest ? 'host' : 'guest'}
                </Button>
              </div>
            </div>
          </div>
        </div>
      </div>

      <Modal isOpen={payModal} onClose={() => setPayModal(false)} title="Record payment" size="sm">
        <div className="space-y-4">
          <Input label="Amount (₹)" type="number" value={payForm.amount} onChange={e => setPayForm(f => ({ ...f, amount: e.target.value }))} placeholder="Enter amount" />
          <div><label className="block text-sm font-medium text-staynest-dark mb-1.5">Payment method</label>
            <select value={payForm.paymentMethod} onChange={e => setPayForm(f => ({ ...f, paymentMethod: e.target.value }))} className="input-base">
              {['UPI','CREDIT_CARD','DEBIT_CARD','NET_BANKING','WALLET','CASH'].map(m => <option key={m} value={m}>{m.replace('_',' ')}</option>)}
            </select>
          </div>
          <Button className="w-full" loading={actL} onClick={makePayment}>Confirm payment</Button>
        </div>
      </Modal>

      <Modal isOpen={revModal} onClose={() => setRevModal(false)} title="Write a review" size="sm">
        <div className="space-y-4">
          <div>
            <label className="block text-sm font-medium text-staynest-dark mb-2">Overall rating</label>
            <div className="flex gap-2">{[1,2,3,4,5].map(n => (
              <button key={n} onClick={() => setRevForm(f => ({ ...f, rating: n }))} className={`text-2xl transition-transform hover:scale-110 ${n <= revForm.rating ? 'opacity-100' : 'opacity-30'}`}>⭐</button>
            ))}</div>
          </div>
          <Textarea label="Your review" value={revForm.comment} onChange={e => setRevForm(f => ({ ...f, comment: e.target.value }))} rows={3} placeholder="Share your experience..." />
          <div className="grid grid-cols-2 gap-3">
            {['cleanlinessRating','accuracyRating','communicationRating','locationRating','valueRating'].map(field => (
              <div key={field}>
                <label className="block text-xs text-staynest-gray mb-1 capitalize">{field.replace('Rating','').replace(/([A-Z])/g,' $1').trim()}</label>
                <select value={revForm[field]} onChange={e => setRevForm(f => ({ ...f, [field]: Number(e.target.value) }))} className="input-base text-sm py-2">
                  {[1,2,3,4,5].map(n => <option key={n} value={n}>{n} ⭐</option>)}
                </select>
              </div>
            ))}
          </div>
          <Button className="w-full" loading={actL} onClick={submitReview}>Submit review</Button>
        </div>
      </Modal>

      <Modal isOpen={couponModal} onClose={() => setCouponModal(false)} title="Apply coupon" size="sm">
        <div className="space-y-4">
          <Input label="Coupon code" value={couponCode} onChange={e => setCouponCode(e.target.value.toUpperCase())} placeholder="e.g. SUMMER20" />
          <Button className="w-full" loading={actL} onClick={applyCoupon}>Apply</Button>
        </div>
      </Modal>
    </Layout>
  )
}
