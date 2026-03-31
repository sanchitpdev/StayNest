import { useState, useEffect } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { FiStar, FiMapPin, FiUsers, FiGrid, FiWifi, FiDroplet, FiTv, FiCoffee, FiHome, FiShare2, FiHeart, FiChevronLeft } from 'react-icons/fi'
import { propertyApi, unitApi, reviewApi, wishlistApi } from '../../api'
import { PageLoader, StarRating, Badge, StatusBadge, Button, Modal } from '../../components/ui'
import BookingWidget from '../../components/booking/BookingWidget'
import Layout from '../../components/layout/Layout'
import { useAuth } from '../../context/AuthContext'
import toast from 'react-hot-toast'
import { format } from 'date-fns'

const PLACEHOLDER = 'https://images.unsplash.com/photo-1571896349842-33c89424de2d?w=800&q=80'

const AMENITY_ICONS = {
  wifi: <FiWifi />, pool: '🏊', parking: '🅿️', kitchen: <FiCoffee />, airConditioning: '❄️',
  gym: '💪', petFriendly: '🐾', beachAccess: '🏖️', tv: <FiTv />, washer: <FiDroplet />,
}

export default function PropertyDetail() {
  const { id }     = useParams()
  const navigate   = useNavigate()
  const { isAuthenticated } = useAuth()

  const [property,  setProperty]  = useState(null)
  const [units,     setUnits]     = useState([])
  const [reviews,   setReviews]   = useState([])
  const [loading,   setLoading]   = useState(true)
  const [gallery,   setGallery]   = useState(false)
  const [activeImg, setActiveImg] = useState(0)
  const [saved,     setSaved]     = useState(false)

  useEffect(() => {
    const load = async () => {
      setLoading(true)
      try {
        const [propRes, unitRes, reviewRes] = await Promise.all([
          propertyApi.getById(id),
          unitApi.getByProperty(id),
          reviewApi.getByProperty(id, { page: 0, size: 6 }),
        ])
        setProperty(propRes.data)
        setUnits(unitRes.data)
        setReviews(reviewRes.data?.content || reviewRes.data || [])

        if (isAuthenticated) {
          try {
            const ws = await wishlistApi.isSaved(id)
            setSaved(ws.data.isSaved)
          } catch {}
        }
      } catch { navigate('/properties') }
      finally  { setLoading(false) }
    }
    load()
  }, [id, isAuthenticated])

  const toggleSave = async () => {
    if (!isAuthenticated) { toast.error('Please log in to save'); return }
    try {
      if (saved) { await wishlistApi.remove(id); setSaved(false); toast.success('Removed from wishlist') }
      else        { await wishlistApi.add(id);    setSaved(true);  toast.success('Saved!') }
    } catch { toast.error('Failed') }
  }

  if (loading) return <Layout><PageLoader /></Layout>
  if (!property) return null

  const images = property.images?.length
    ? property.images.map(i => i.imageUrl)
    : ['https://images.unsplash.com/photo-1613977257363-707ba9348227?w=800&q=80',
       'https://images.unsplash.com/photo-1522708323590-d24dbb6b0267?w=800&q=80',
       'https://images.unsplash.com/photo-1571003123894-1f0594d2b5d9?w=800&q=80',
       'https://images.unsplash.com/photo-1566073771259-6a8506099945?w=800&q=80',
       'https://images.unsplash.com/photo-1449158743715-0a90ebb6d2d8?w=800&q=80']

  const amenities = property.amenities ? Object.entries(property.amenities).filter(([, v]) => v) : []

  return (
    <Layout>
      <div className="max-w-7xl mx-auto px-4 sm:px-6 py-8">

        {/* Back */}
        <button onClick={() => navigate(-1)} className="flex items-center gap-2 text-sm text-staynest-gray hover:text-staynest-dark mb-4 transition-colors">
          <FiChevronLeft /> Back to results
        </button>

        {/* Title row */}
        <div className="flex items-start justify-between gap-4 mb-4">
          <div>
            <h1 className="text-2xl md:text-3xl font-bold text-staynest-dark">{property.propertyName}</h1>
            <div className="flex flex-wrap items-center gap-3 mt-2 text-sm text-staynest-gray">
              <span className="flex items-center gap-1"><FiMapPin className="text-primary" /> {property.city}, {property.state}, {property.country}</span>
              <Badge variant="default">{property.propertyType}</Badge>
              <StatusBadge status={property.propertyStatus || 'ACTIVE'} />
            </div>
          </div>
          <div className="flex gap-2 shrink-0">
            <button onClick={toggleSave} className="flex items-center gap-2 px-3 py-2 rounded-lg border border-staynest-light hover:bg-staynest-bg text-sm font-medium transition-colors">
              <FiHeart className={saved ? 'fill-primary text-primary' : 'text-staynest-dark'} /> Save
            </button>
            <button className="flex items-center gap-2 px-3 py-2 rounded-lg border border-staynest-light hover:bg-staynest-bg text-sm font-medium transition-colors">
              <FiShare2 /> Share
            </button>
          </div>
        </div>

        {/* Image grid */}
        <div className="grid grid-cols-4 grid-rows-2 gap-2 rounded-2xl overflow-hidden h-80 md:h-[420px] mb-10 cursor-pointer" onClick={() => setGallery(true)}>
          <div className="col-span-2 row-span-2">
            <img src={images[0] || PLACEHOLDER} alt="" className="w-full h-full object-cover hover:opacity-95 transition-opacity" onError={e => e.target.src = PLACEHOLDER} />
          </div>
          {images.slice(1, 5).map((img, i) => (
            <div key={i} className="relative overflow-hidden">
              <img src={img || PLACEHOLDER} alt="" className="w-full h-full object-cover hover:opacity-95 transition-opacity" onError={e => e.target.src = PLACEHOLDER} />
              {i === 3 && images.length > 5 && (
                <div className="absolute inset-0 bg-black/40 flex items-center justify-center">
                  <button className="flex items-center gap-2 bg-white text-staynest-dark text-sm font-semibold px-3 py-1.5 rounded-lg">
                    <FiGrid /> +{images.length - 5} more
                  </button>
                </div>
              )}
            </div>
          ))}
        </div>

        {/* Main layout */}
        <div className="grid grid-cols-1 lg:grid-cols-[1fr_380px] gap-10">
          {/* Left content */}
          <div>
            {/* Host info */}
            <div className="flex items-center justify-between pb-6 border-b border-staynest-light">
              <div>
                <h2 className="text-xl font-semibold text-staynest-dark">Hosted by {property.hostName}</h2>
                <p className="text-staynest-gray text-sm mt-1">
                  {units.length} unit{units.length !== 1 ? 's' : ''} · {property.cancellationPolicy?.toLowerCase().replace('_', ' ')} cancellation
                </p>
              </div>
              <div className="w-12 h-12 bg-staynest-dark rounded-full flex items-center justify-center text-white font-bold text-lg">
                {property.hostName?.[0]}
              </div>
            </div>

            {/* Description */}
            <div className="py-6 border-b border-staynest-light">
              <p className="text-staynest-dark leading-relaxed">{property.description}</p>
            </div>

            {/* Amenities */}
            {amenities.length > 0 && (
              <div className="py-6 border-b border-staynest-light">
                <h3 className="text-xl font-semibold text-staynest-dark mb-4">What this place offers</h3>
                <div className="grid grid-cols-2 md:grid-cols-3 gap-3">
                  {amenities.map(([key]) => (
                    <div key={key} className="flex items-center gap-3 text-staynest-dark">
                      <span className="text-xl">{AMENITY_ICONS[key] || '✓'}</span>
                      <span className="text-sm capitalize">{key.replace(/([A-Z])/g, ' $1').trim()}</span>
                    </div>
                  ))}
                </div>
              </div>
            )}

            {/* Units */}
            {units.length > 0 && (
              <div className="py-6 border-b border-staynest-light">
                <h3 className="text-xl font-semibold text-staynest-dark mb-4">Available units</h3>
                <div className="space-y-3">
                  {units.map(unit => (
                    <div key={unit.unitId} className="border border-staynest-light rounded-xl p-4 hover:border-staynest-gray transition-colors">
                      <div className="flex items-start justify-between">
                        <div>
                          <h4 className="font-semibold text-staynest-dark">{unit.unitName}</h4>
                          <p className="text-staynest-gray text-sm mt-1">
                            Unit {unit.unitNumber} · {unit.bedrooms} bed · {unit.bathrooms} bath · {unit.maxGuests} guests max
                            {unit.squareFeet && ` · ${unit.squareFeet} sq ft`}
                          </p>
                        </div>
                        <div className="text-right shrink-0">
                          <p className="font-bold text-staynest-dark">₹{unit.basePrice?.toLocaleString('en-IN')}</p>
                          <p className="text-staynest-gray text-xs">per night</p>
                          {unit.cleaningFee > 0 && <p className="text-staynest-gray text-xs">+₹{unit.cleaningFee?.toLocaleString('en-IN')} cleaning</p>}
                        </div>
                      </div>
                      <div className="flex items-center gap-2 mt-3">
                        <div className={`w-2 h-2 rounded-full ${unit.isAvailable ? 'bg-green-500' : 'bg-red-400'}`} />
                        <span className="text-xs text-staynest-gray">{unit.isAvailable ? 'Available' : 'Unavailable'}</span>
                      </div>
                    </div>
                  ))}
                </div>
              </div>
            )}

            {/* Location */}
            <div className="py-6 border-b border-staynest-light">
              <h3 className="text-xl font-semibold text-staynest-dark mb-3">Location</h3>
              <p className="text-staynest-gray text-sm">{property.address}, {property.city}, {property.state}, {property.country} {property.postalCode}</p>
              {property.latitude && property.longitude && (
                <a href={`https://maps.google.com/?q=${property.latitude},${property.longitude}`} target="_blank" rel="noreferrer"
                  className="inline-flex items-center gap-2 mt-3 text-sm font-medium text-staynest-dark underline hover:text-primary">
                  <FiMapPin /> View on Google Maps
                </a>
              )}
            </div>

            {/* Reviews */}
            <div className="py-6">
              <div className="flex items-center gap-3 mb-6">
                <FiStar className="text-staynest-dark fill-staynest-dark text-xl" />
                <h3 className="text-xl font-semibold text-staynest-dark">
                  {reviews.length > 0 ? `${reviews.length} review${reviews.length > 1 ? 's' : ''}` : 'No reviews yet'}
                </h3>
              </div>
              {reviews.length > 0 && (
                <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                  {reviews.map(r => (
                    <div key={r.reviewId} className="border border-staynest-light rounded-xl p-4">
                      <div className="flex items-start gap-3 mb-3">
                        <div className="w-9 h-9 bg-staynest-dark rounded-full flex items-center justify-center text-white text-sm font-semibold shrink-0">
                          {r.reviewerName?.[0]}
                        </div>
                        <div>
                          <p className="font-semibold text-sm text-staynest-dark">{r.reviewerName}</p>
                          <p className="text-xs text-staynest-gray">{r.createdAt && format(new Date(r.createdAt), 'MMMM yyyy')}</p>
                        </div>
                        <div className="ml-auto flex items-center gap-1">
                          <FiStar className="text-staynest-dark fill-staynest-dark text-xs" />
                          <span className="text-sm font-medium">{r.rating}</span>
                        </div>
                      </div>
                      <p className="text-sm text-staynest-dark leading-relaxed">{r.comment}</p>
                      {r.hostResponse && (
                        <div className="mt-3 bg-staynest-bg rounded-lg p-3">
                          <p className="text-xs font-semibold text-staynest-dark mb-1">Response from host</p>
                          <p className="text-xs text-staynest-gray">{r.hostResponse}</p>
                        </div>
                      )}
                    </div>
                  ))}
                </div>
              )}
            </div>
          </div>

          {/* Booking widget */}
          <div><BookingWidget property={property} units={units} /></div>
        </div>
      </div>

      {/* Gallery modal */}
      <Modal isOpen={gallery} onClose={() => setGallery(false)} title={`Photos (${images.length})`} size="xl">
        <div className="space-y-3">
          {images.map((img, i) => (
            <img key={i} src={img || PLACEHOLDER} alt={`Photo ${i + 1}`} className="w-full rounded-xl object-cover max-h-96" onError={e => e.target.src = PLACEHOLDER} />
          ))}
        </div>
      </Modal>
    </Layout>
  )
}
