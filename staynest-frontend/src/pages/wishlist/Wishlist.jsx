import { useState, useEffect } from 'react'
import { Link } from 'react-router-dom'
import { FiHeart, FiTrash2, FiStar, FiUsers, FiMapPin } from 'react-icons/fi'
import { wishlistApi } from '../../api'
import { PageLoader, Button, EmptyState } from '../../components/ui'
import Layout from '../../components/layout/Layout'
import toast from 'react-hot-toast'

const PLACEHOLDER = 'https://images.unsplash.com/photo-1571896349842-33c89424de2d?w=400&q=80'

export function Wishlist() {
  const [items,   setItems]   = useState([])
  const [loading, setLoading] = useState(true)
  const [page,    setPage]    = useState(0)
  const [total,   setTotal]   = useState(0)

  const load = async () => {
    setLoading(true)
    try {
      const r = await wishlistApi.getMyWishlist({ page, size: 12 })
      setItems(r.data.content || r.data)
      setTotal(r.data.totalElements || r.data.length)
    } catch { setItems([]) }
    finally { setLoading(false) }
  }

  useEffect(() => { load() }, [page])

  const remove = async (propertyId, e) => {
    e.preventDefault()
    try {
      await wishlistApi.remove(propertyId)
      toast.success('Removed from wishlist')
      load()
    } catch { toast.error('Failed to remove') }
  }

  return (
    <Layout>
      <div className="max-w-6xl mx-auto px-4 sm:px-6 py-10">
        <div className="flex items-center gap-3 mb-8">
          <FiHeart className="text-primary text-2xl fill-primary" />
          <div>
            <h1 className="text-3xl font-bold text-staynest-dark">Saved stays</h1>
            <p className="text-staynest-gray text-sm mt-0.5">{total} saved {total === 1 ? 'property' : 'properties'}</p>
          </div>
        </div>

        {loading ? <PageLoader /> : items.length === 0 ? (
          <EmptyState
            icon={<FiHeart />}
            title="No saved stays yet"
            description="Tap the heart icon on any property to save it here for later."
            action={<Link to="/properties"><Button>Explore stays</Button></Link>}
          />
        ) : (
          <>
            <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-x-5 gap-y-8">
              {items.map(item => (
                <Link key={item.wishlistId} to={`/properties/${item.propertyId}`} className="block group card-hover relative">
                  <div className="aspect-[4/3] rounded-xl2 overflow-hidden bg-staynest-bg">
                    <div className="w-full h-full bg-gradient-to-br from-primary/10 to-secondary/10 flex items-center justify-center text-5xl">
                      🏠
                    </div>
                  </div>
                  <button
                    onClick={e => remove(item.propertyId, e)}
                    className="absolute top-3 right-3 w-8 h-8 bg-white rounded-full shadow flex items-center justify-center hover:bg-red-50 transition-colors z-10">
                    <FiTrash2 className="text-red-400 text-sm" />
                  </button>
                  <div className="mt-3">
                    <div className="flex items-start justify-between gap-2">
                      <h3 className="font-semibold text-staynest-dark text-sm truncate">{item.propertyName}</h3>
                      {item.averageRating && (
                        <div className="flex items-center gap-1 shrink-0">
                          <FiStar className="text-staynest-dark fill-staynest-dark text-xs" />
                          <span className="text-xs font-medium">{Number(item.averageRating).toFixed(1)}</span>
                        </div>
                      )}
                    </div>
                    <p className="text-staynest-gray text-sm mt-0.5 flex items-center gap-1">
                      <FiMapPin className="text-xs" />{item.city}, {item.state}
                    </p>
                    {item.startingPrice && (
                      <p className="mt-1.5 text-sm">
                        <span className="font-semibold text-staynest-dark">₹{item.startingPrice?.toLocaleString('en-IN')}</span>
                        <span className="text-staynest-gray"> / night</span>
                      </p>
                    )}
                  </div>
                </Link>
              ))}
            </div>
            {total > 12 && (
              <div className="flex justify-center gap-3 mt-10">
                <Button variant="secondary" disabled={page === 0} onClick={() => setPage(p => p - 1)}>← Previous</Button>
                <Button variant="secondary" disabled={(page + 1) * 12 >= total} onClick={() => setPage(p => p + 1)}>Next →</Button>
              </div>
            )}
          </>
        )}
      </div>
    </Layout>
  )
}
