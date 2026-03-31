import { useState, useCallback } from 'react'
import { Link } from 'react-router-dom'
import { FiHeart, FiStar, FiChevronLeft, FiChevronRight } from 'react-icons/fi'
import { wishlistApi } from '../../api'
import { useAuth } from '../../context/AuthContext'
import toast from 'react-hot-toast'

const PLACEHOLDER = 'https://images.unsplash.com/photo-1571896349842-33c89424de2d?w=600&q=80'

const PROPERTY_IMAGES = {
  VILLA:       'https://images.unsplash.com/photo-1613977257363-707ba9348227?w=600&q=80',
  APARTMENT:   'https://images.unsplash.com/photo-1522708323590-d24dbb6b0267?w=600&q=80',
  HOUSE:       'https://images.unsplash.com/photo-1568605114967-8130f3a36994?w=600&q=80',
  HOTEL:       'https://images.unsplash.com/photo-1566073771259-6a8506099945?w=600&q=80',
  RESORT:      'https://images.unsplash.com/photo-1571003123894-1f0594d2b5d9?w=600&q=80',
  COTTAGE:     'https://images.unsplash.com/photo-1449158743715-0a90ebb6d2d8?w=600&q=80',
  GUESTHOUSE:  'https://images.unsplash.com/photo-1630699144867-37acec97df5a?w=600&q=80',
  HOSTEL:      'https://images.unsplash.com/photo-1555854877-bab0e564b8d5?w=600&q=80',
}

export default function PropertyCard({ property, onWishlistChange }) {
  const { isAuthenticated } = useAuth()
  const [saved,    setSaved]    = useState(false)
  const [imgIndex, setImgIndex] = useState(0)

  const images = property.images?.length
    ? property.images.map(img => img.imageUrl)
    : [PROPERTY_IMAGES[property.propertyType] || PLACEHOLDER]

  const toggleWishlist = useCallback(async (e) => {
    e.preventDefault()
    e.stopPropagation()
    if (!isAuthenticated) { toast.error('Please log in to save properties'); return }
    try {
      if (saved) { await wishlistApi.remove(property.propertyId); setSaved(false); toast.success('Removed from wishlist') }
      else        { await wishlistApi.add(property.propertyId);    setSaved(true);  toast.success('Saved to wishlist') }
      onWishlistChange?.()
    } catch { toast.error('Failed to update wishlist') }
  }, [saved, isAuthenticated, property.propertyId, onWishlistChange])

  const prevImg = (e) => { e.preventDefault(); e.stopPropagation(); setImgIndex(i => (i - 1 + images.length) % images.length) }
  const nextImg = (e) => { e.preventDefault(); e.stopPropagation(); setImgIndex(i => (i + 1) % images.length) }

  return (
    <Link to={`/properties/${property.propertyId}`} className="block group card-hover">
      {/* Image */}
      <div className="relative aspect-[4/3] rounded-xl2 overflow-hidden bg-staynest-bg">
        <img
          src={images[imgIndex]}
          alt={property.propertyName}
          className="w-full h-full object-cover transition-transform duration-300 group-hover:scale-105"
          onError={e => { e.target.src = PLACEHOLDER }}
        />

        {/* Nav arrows */}
        {images.length > 1 && (
          <>
            <button onClick={prevImg} className="absolute left-2 top-1/2 -translate-y-1/2 w-7 h-7 bg-white rounded-full shadow flex items-center justify-center opacity-0 group-hover:opacity-100 transition-opacity hover:scale-110 z-10">
              <FiChevronLeft className="text-staynest-dark text-sm" />
            </button>
            <button onClick={nextImg} className="absolute right-2 top-1/2 -translate-y-1/2 w-7 h-7 bg-white rounded-full shadow flex items-center justify-center opacity-0 group-hover:opacity-100 transition-opacity hover:scale-110 z-10">
              <FiChevronRight className="text-staynest-dark text-sm" />
            </button>
            <div className="absolute bottom-2 left-1/2 -translate-x-1/2 flex gap-1">
              {images.map((_, i) => (
                <span key={i} className={`w-1.5 h-1.5 rounded-full transition-colors ${i === imgIndex ? 'bg-white' : 'bg-white/50'}`} />
              ))}
            </div>
          </>
        )}

        {/* Wishlist */}
        <button onClick={toggleWishlist} className="absolute top-3 right-3 z-10 p-1.5 hover:scale-110 transition-transform">
          <FiHeart className={`text-xl drop-shadow ${saved ? 'fill-primary text-primary' : 'text-white fill-black/20'}`} />
        </button>

        {/* Property type badge */}
        <div className="absolute top-3 left-3">
          <span className="bg-white/90 backdrop-blur-sm text-staynest-dark text-xs font-medium px-2 py-1 rounded-full capitalize">
            {property.propertyType?.toLowerCase()}
          </span>
        </div>
      </div>

      {/* Info */}
      <div className="mt-3 px-0.5">
        <div className="flex items-start justify-between gap-2">
          <div className="flex-1 min-w-0">
            <h3 className="font-semibold text-staynest-dark text-sm truncate">{property.propertyName}</h3>
            <p className="text-staynest-gray text-sm mt-0.5">{property.city}, {property.state}</p>
          </div>
          <div className="flex items-center gap-1 shrink-0">
            <FiStar className="text-staynest-dark text-xs fill-staynest-dark" />
            <span className="text-sm font-medium text-staynest-dark">New</span>
          </div>
        </div>
        <p className="text-staynest-gray text-sm mt-1">
          Hosted by <span className="text-staynest-dark">{property.hostName}</span>
        </p>
        <div className="flex items-baseline gap-1 mt-2">
          <span className="font-semibold text-staynest-dark">
            ₹{property.startingPrice?.toLocaleString('en-IN') || (property.units?.[0]?.basePrice?.toLocaleString('en-IN'))}
          </span>
          <span className="text-staynest-gray text-sm">/ night</span>
        </div>
      </div>
    </Link>
  )
}
