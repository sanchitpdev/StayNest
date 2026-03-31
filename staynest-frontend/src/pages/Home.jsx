import { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import { FiSearch, FiMapPin, FiStar, FiShield, FiHome, FiHeart } from 'react-icons/fi'
import { propertyApi } from '../api'
import PropertyCard from '../components/property/PropertyCard'
import { PageLoader, Button } from '../components/ui'
import Layout from '../components/layout/Layout'

const CATEGORIES = [
  { label: 'Beach',     icon: '🏖️', type: 'RESORT'    },
  { label: 'Mountain',  icon: '⛰️', type: 'COTTAGE'   },
  { label: 'City',      icon: '🏙️', type: 'APARTMENT' },
  { label: 'Villa',     icon: '🏡', type: 'VILLA'     },
  { label: 'Hotel',     icon: '🏨', type: 'HOTEL'     },
  { label: 'Hostel',    icon: '🛏️', type: 'HOSTEL'    },
]

const HERO_DESTINATIONS = [
  { city: 'Goa',      img: 'https://images.unsplash.com/photo-1512343879784-a960bf40e7f2?w=400&q=80' },
  { city: 'Mumbai',   img: 'https://images.unsplash.com/photo-1595658658481-d53d3f999875?w=400&q=80' },
  { city: 'Jaipur',   img: 'https://images.unsplash.com/photo-1477587458883-47145ed94f57?w=400&q=80' },
  { city: 'Manali',   img: 'https://images.unsplash.com/photo-1597077144787-93df7e98fd53?w=400&q=80' },
  { city: 'Kerala',   img: 'https://images.unsplash.com/photo-1602216056096-3b40cc0c9944?w=400&q=80' },
  { city: 'Udaipur',  img: 'https://images.unsplash.com/photo-1599661046289-e31897846e41?w=400&q=80' },
]

export default function Home() {
  const navigate   = useNavigate()
  const [search,   setSearch]    = useState('')
  const [category, setCategory]  = useState(null)
  const [properties, setProps]   = useState([])
  const [loading,    setLoading] = useState(true)

  useEffect(() => {
    const fetchProps = async () => {
      setLoading(true)
      try {
        const params = category ? { type: category } : {}
        const res = await propertyApi.getAll(params)
        setProps(res.data)
      } catch { setProps([]) } finally { setLoading(false) }
    }
    fetchProps()
  }, [category])

  const handleSearch = (e) => {
    e.preventDefault()
    if (search.trim()) navigate(`/properties?city=${encodeURIComponent(search.trim())}`)
    else navigate('/properties')
  }

  return (
    <Layout>
      {/* Hero */}
      <section className="relative bg-gradient-to-br from-primary/5 via-white to-secondary/5 py-20 px-4">
        <div className="max-w-4xl mx-auto text-center">
          <h1 className="text-5xl md:text-6xl font-bold text-staynest-dark leading-tight mb-4">
            Find your perfect<br />
            <span className="text-primary">stay anywhere</span>
          </h1>
          <p className="text-lg text-staynest-gray mb-10 max-w-xl mx-auto">
            Discover unique homes, villas, and experiences hosted by locals across India and beyond.
          </p>

          {/* Search bar */}
          <form onSubmit={handleSearch} className="bg-white rounded-full shadow-widget p-2 flex items-center gap-2 max-w-2xl mx-auto">
            <FiMapPin className="ml-4 text-primary text-xl shrink-0" />
            <input
              value={search}
              onChange={e => setSearch(e.target.value)}
              placeholder="Where do you want to go?"
              className="flex-1 text-staynest-dark placeholder-staynest-gray outline-none text-base py-2 bg-transparent"
            />
            <button type="submit" className="bg-primary text-white px-6 py-3 rounded-full font-semibold hover:bg-primary-hover transition-colors flex items-center gap-2 shrink-0">
              <FiSearch /> Search
            </button>
          </form>
        </div>
      </section>

      {/* Destinations */}
      <section className="max-w-7xl mx-auto px-4 sm:px-6 py-12">
        <h2 className="text-2xl font-bold text-staynest-dark mb-6">Popular destinations</h2>
        <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-6 gap-3">
          {HERO_DESTINATIONS.map(d => (
            <button key={d.city} onClick={() => navigate(`/properties?city=${d.city}`)}
              className="relative rounded-xl overflow-hidden aspect-square group cursor-pointer"
            >
              <img src={d.img} alt={d.city} className="w-full h-full object-cover group-hover:scale-110 transition-transform duration-300" />
              <div className="absolute inset-0 bg-black/30 group-hover:bg-black/20 transition-colors" />
              <span className="absolute inset-0 flex items-end p-3 text-white font-semibold text-sm">{d.city}</span>
            </button>
          ))}
        </div>
      </section>

      {/* Categories */}
      <section className="border-y border-staynest-light">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 py-4 flex gap-6 overflow-x-auto no-scrollbar">
          <button
            onClick={() => setCategory(null)}
            className={`flex flex-col items-center gap-1 pb-2 shrink-0 border-b-2 transition-colors ${!category ? 'border-staynest-dark text-staynest-dark' : 'border-transparent text-staynest-gray hover:text-staynest-dark'}`}
          >
            <span className="text-2xl">🏠</span>
            <span className="text-xs font-medium whitespace-nowrap">All</span>
          </button>
          {CATEGORIES.map(c => (
            <button
              key={c.label}
              onClick={() => setCategory(c.type === category ? null : c.type)}
              className={`flex flex-col items-center gap-1 pb-2 shrink-0 border-b-2 transition-colors ${category === c.type ? 'border-staynest-dark text-staynest-dark' : 'border-transparent text-staynest-gray hover:text-staynest-dark'}`}
            >
              <span className="text-2xl">{c.icon}</span>
              <span className="text-xs font-medium whitespace-nowrap">{c.label}</span>
            </button>
          ))}
        </div>
      </section>

      {/* Listings */}
      <section className="max-w-7xl mx-auto px-4 sm:px-6 py-10">
        {loading ? (
          <PageLoader />
        ) : properties.length === 0 ? (
          <div className="text-center py-20">
            <p className="text-4xl mb-4">🏡</p>
            <h3 className="text-xl font-semibold text-staynest-dark mb-2">No properties found</h3>
            <p className="text-staynest-gray">Try a different category or search term</p>
          </div>
        ) : (
          <>
            <h2 className="text-2xl font-bold text-staynest-dark mb-6">
              {category ? `${CATEGORIES.find(c => c.type === category)?.label}s` : 'All stays'}
              <span className="text-staynest-gray text-base font-normal ml-2">({properties.length})</span>
            </h2>
            <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-x-5 gap-y-8">
              {properties.map(p => <PropertyCard key={p.propertyId} property={p} />)}
            </div>
          </>
        )}
      </section>

      {/* Why StayNest */}
      <section className="bg-staynest-bg py-16">
        <div className="max-w-7xl mx-auto px-4 sm:px-6">
          <h2 className="text-3xl font-bold text-staynest-dark text-center mb-12">Why choose StayNest?</h2>
          <div className="grid grid-cols-1 md:grid-cols-3 gap-8">
            {[
              { icon: <FiShield className="text-3xl text-primary" />, title: 'Secure Bookings', desc: 'Every booking is protected with our secure payment system and transparent cancellation policies.' },
              { icon: <FiHeart   className="text-3xl text-primary" />, title: 'Handpicked Stays', desc: 'Each property is verified by our team to ensure quality, comfort, and accurate descriptions.' },
              { icon: <FiStar    className="text-3xl text-primary" />, title: 'Real Reviews',    desc: 'Read genuine reviews from verified guests who have actually stayed at each property.' },
            ].map(f => (
              <div key={f.title} className="bg-white rounded-2xl p-8 shadow-sm hover:shadow-card transition-shadow">
                <div className="mb-4">{f.icon}</div>
                <h3 className="text-xl font-bold text-staynest-dark mb-3">{f.title}</h3>
                <p className="text-staynest-gray leading-relaxed">{f.desc}</p>
              </div>
            ))}
          </div>
        </div>
      </section>

      {/* Host CTA */}
      <section className="py-16 px-4">
        <div className="max-w-4xl mx-auto bg-gradient-to-r from-primary to-primary-hover rounded-3xl p-10 text-center text-white">
          <FiHome className="text-5xl mx-auto mb-4 opacity-80" />
          <h2 className="text-3xl font-bold mb-4">Become a StayNest Host</h2>
          <p className="text-white/80 text-lg mb-8 max-w-xl mx-auto">
            Share your space, earn extra income, and meet travellers from all over the world.
          </p>
          <Button variant="secondary" className="bg-white text-primary border-white hover:bg-white/90 hover:text-primary-hover px-8 py-4 text-base" onClick={() => navigate('/register')}>
            Start hosting today
          </Button>
        </div>
      </section>
    </Layout>
  )
}
