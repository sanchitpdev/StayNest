import { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import DatePicker from 'react-datepicker'
import 'react-datepicker/dist/react-datepicker.css'
import { FiCalendar, FiUsers, FiChevronDown } from 'react-icons/fi'
import { Button, Spinner } from '../ui'
import { bookingApi, pricingApi, couponApi } from '../../api'
import { useAuth } from '../../context/AuthContext'
import toast from 'react-hot-toast'
import { format, differenceInDays } from 'date-fns'

export default function BookingWidget({ property, units }) {
  const { isAuthenticated } = useAuth()
  const navigate = useNavigate()

  const [selectedUnit,  setSelectedUnit]  = useState(units?.[0] || null)
  const [checkIn,       setCheckIn]       = useState(null)
  const [checkOut,      setCheckOut]      = useState(null)
  const [guests,        setGuests]        = useState(1)
  const [couponCode,    setCouponCode]    = useState('')
  const [pricing,       setPricing]       = useState(null)
  const [available,     setAvailable]     = useState(null)
  const [loading,       setLoading]       = useState(false)
  const [priceLoading,  setPriceLoading]  = useState(false)
  const [couponApplied, setCouponApplied] = useState(null)

  const nights = checkIn && checkOut ? differenceInDays(checkOut, checkIn) : 0

  // Auto-select first unit
  useEffect(() => { if (units?.length) setSelectedUnit(units[0]) }, [units])

  // Fetch price when dates selected
  useEffect(() => {
    if (!selectedUnit || !checkIn || !checkOut || nights <= 0) { setPricing(null); return }
    const fetchPrice = async () => {
      setPriceLoading(true)
      try {
        const res = await pricingApi.calculate(selectedUnit.unitId, {
          checkIn:  format(checkIn,  'yyyy-MM-dd'),
          checkOut: format(checkOut, 'yyyy-MM-dd'),
        })
        setPricing(res.data)
      } catch { setPricing(null) } finally { setPriceLoading(false) }
    }
    fetchPrice()
  }, [selectedUnit, checkIn, checkOut, nights])

  // Check availability
  useEffect(() => {
    if (!selectedUnit || !checkIn || !checkOut || nights <= 0) { setAvailable(null); return }
    const check = async () => {
      try {
        const res = await bookingApi.checkAvailability(selectedUnit.unitId, {
          checkIn:  format(checkIn,  'yyyy-MM-dd'),
          checkOut: format(checkOut, 'yyyy-MM-dd'),
        })
        setAvailable(res.data.available)
      } catch { setAvailable(null) }
    }
    check()
  }, [selectedUnit, checkIn, checkOut, nights])

  const handleBook = async () => {
    if (!isAuthenticated) { navigate('/login'); return }
    if (!checkIn || !checkOut) { toast.error('Please select dates'); return }
    if (!selectedUnit) { toast.error('Please select a unit'); return }
    if (!guests) { toast.error('Please select number of guests'); return }

    setLoading(true)
    try {
      const res = await bookingApi.create({
        unitId:       selectedUnit.unitId,
        checkInDate:  format(checkIn,  'yyyy-MM-dd'),
        checkOutDate: format(checkOut, 'yyyy-MM-dd'),
        numGuests:    guests,
      })
      toast.success('Booking created successfully!')
      navigate(`/bookings/${res.data.bookingId}`)
    } catch (err) {
      toast.error(err.response?.data?.message || 'Booking failed')
    } finally {
      setLoading(false)
    }
  }

  const basePrice  = pricing?.totalPrice || (selectedUnit?.basePrice * nights) || 0
  const cleaningFee = selectedUnit?.cleaningFee || 0
  const total       = basePrice

  return (
    <div className="bg-white rounded-2xl shadow-widget border border-staynest-light p-6 sticky top-24">
      {/* Price header */}
      <div className="flex items-baseline gap-1 mb-5">
        <span className="text-2xl font-bold text-staynest-dark">
          ₹{selectedUnit?.basePrice?.toLocaleString('en-IN') || '—'}
        </span>
        <span className="text-staynest-gray text-sm">/ night</span>
      </div>

      {/* Unit selector */}
      {units?.length > 1 && (
        <div className="mb-4">
          <label className="block text-xs font-semibold text-staynest-dark uppercase tracking-wide mb-2">Select Unit</label>
          <div className="relative">
            <select
              value={selectedUnit?.unitId || ''}
              onChange={e => setSelectedUnit(units.find(u => u.unitId === e.target.value))}
              className="input-base appearance-none pr-8 text-sm"
            >
              {units.map(u => (
                <option key={u.unitId} value={u.unitId}>
                  {u.unitName} — ₹{u.basePrice?.toLocaleString('en-IN')}/night ({u.maxGuests} guests)
                </option>
              ))}
            </select>
            <FiChevronDown className="absolute right-3 top-1/2 -translate-y-1/2 text-staynest-gray pointer-events-none" />
          </div>
        </div>
      )}

      {/* Date pickers */}
      <div className="border border-staynest-light rounded-xl overflow-hidden mb-3">
        <div className="grid grid-cols-2 divide-x divide-staynest-light">
          <div className="p-3">
            <p className="text-xs font-bold text-staynest-dark uppercase tracking-wide mb-1">Check-in</p>
            <DatePicker
              selected={checkIn}
              onChange={date => { setCheckIn(date); if (checkOut && date >= checkOut) setCheckOut(null) }}
              selectsStart startDate={checkIn} endDate={checkOut}
              minDate={new Date()}
              placeholderText="Add date"
              className="text-sm text-staynest-dark w-full outline-none cursor-pointer"
              dateFormat="MMM d"
            />
          </div>
          <div className="p-3">
            <p className="text-xs font-bold text-staynest-dark uppercase tracking-wide mb-1">Check-out</p>
            <DatePicker
              selected={checkOut}
              onChange={setCheckOut}
              selectsEnd startDate={checkIn} endDate={checkOut}
              minDate={checkIn || new Date()}
              placeholderText="Add date"
              className="text-sm text-staynest-dark w-full outline-none cursor-pointer"
              dateFormat="MMM d"
            />
          </div>
        </div>
        <div className="border-t border-staynest-light p-3">
          <p className="text-xs font-bold text-staynest-dark uppercase tracking-wide mb-1">Guests</p>
          <div className="flex items-center gap-2">
            <FiUsers className="text-staynest-gray text-sm" />
            <input
              type="number" min={1} max={selectedUnit?.maxGuests || 10}
              value={guests} onChange={e => setGuests(Number(e.target.value))}
              className="text-sm text-staynest-dark outline-none w-16"
            />
            <span className="text-staynest-gray text-sm">guest{guests > 1 ? 's' : ''}</span>
          </div>
        </div>
      </div>

      {/* Availability */}
      {available === false && (
        <p className="text-sm text-red-500 text-center mb-3">Not available for selected dates</p>
      )}
      {available === true && (
        <p className="text-sm text-green-600 text-center mb-3">✓ Available for your dates</p>
      )}

      {/* Price breakdown */}
      {nights > 0 && (
        <div className="mb-4 space-y-2">
          {priceLoading ? (
            <div className="flex justify-center py-2"><Spinner size="sm" className="text-primary" /></div>
          ) : (
            <>
              <div className="flex justify-between text-sm text-staynest-dark">
                <span>₹{selectedUnit?.basePrice?.toLocaleString('en-IN')} × {nights} night{nights > 1 ? 's' : ''}</span>
                <span>₹{(selectedUnit?.basePrice * nights)?.toLocaleString('en-IN')}</span>
              </div>
              {cleaningFee > 0 && (
                <div className="flex justify-between text-sm text-staynest-dark">
                  <span>Cleaning fee</span><span>₹{cleaningFee?.toLocaleString('en-IN')}</span>
                </div>
              )}
              <div className="border-t border-staynest-light pt-2 flex justify-between font-semibold text-staynest-dark">
                <span>Total</span><span>₹{total?.toLocaleString('en-IN')}</span>
              </div>
            </>
          )}
        </div>
      )}

      {/* Book button */}
      <Button
        onClick={handleBook}
        loading={loading}
        disabled={available === false || !selectedUnit}
        className="w-full text-base py-4"
      >
        {!isAuthenticated ? 'Log in to book' : nights > 0 ? `Reserve · ₹${total?.toLocaleString('en-IN')}` : 'Check availability'}
      </Button>

      <p className="text-xs text-staynest-gray text-center mt-3">You won't be charged yet</p>
    </div>
  )
}
