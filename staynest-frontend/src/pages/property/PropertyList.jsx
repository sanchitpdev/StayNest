import { useState, useEffect } from 'react'
import { useSearchParams } from 'react-router-dom'
import { FiFilter, FiSearch, FiX } from 'react-icons/fi'
import { propertyApi } from '../../api'
import PropertyCard from '../../components/property/PropertyCard'
import { PageLoader, Button, Select, EmptyState } from '../../components/ui'
import Layout from '../../components/layout/Layout'

const PROPERTY_TYPES = ['APARTMENT','HOUSE','VILLA','HOTEL','RESORT','COTTAGE','GUESTHOUSE','HOSTEL']

export default function PropertyList() {
  const [searchParams, setSearchParams] = useSearchParams()
  const [properties, setProps]   = useState([])
  const [loading, setLoading]    = useState(true)
  const [showFilter, setShowFilter] = useState(false)
  const [filters, setFilters]    = useState({
    city:         searchParams.get('city') || '',
    type:         searchParams.get('type') || '',
    minPrice:     '',
    maxPrice:     '',
    minGuests:    '',
    sortBy:       'createdAt',
    sortDirection:'desc',
    page: 0, size: 12,
  })
  const [total, setTotal] = useState(0)

  useEffect(() => {
    const city = searchParams.get('city')
    if (city) setFilters(f => ({ ...f, city }))
  }, [searchParams])

  useEffect(() => { fetchProperties() }, [filters.page, filters.type, filters.city])

  const fetchProperties = async () => {
    setLoading(true)
    try {
      let res
      if (filters.city) {
        res = await propertyApi.searchCity({ city: filters.city, page: filters.page, size: filters.size })
        setProps(res.data.content || res.data)
        setTotal(res.data.totalElements || res.data.length)
      } else if (filters.type) {
        res = await propertyApi.search({ type: filters.type })
        setProps(res.data)
        setTotal(res.data.length)
      } else {
        res = await propertyApi.getPaginated({ page: filters.page, size: filters.size, sortBy: filters.sortBy, sortDirection: filters.sortDirection })
        setProps(res.data.content || [])
        setTotal(res.data.totalElements || 0)
      }
    } catch { setProps([]); setTotal(0) }
    finally { setLoading(false) }
  }

  const handleSearch = (e) => {
    e.preventDefault()
    setSearchParams(filters.city ? { city: filters.city } : {})
    setFilters(f => ({ ...f, page: 0 }))
    fetchProperties()
  }

  const clearFilter = (key) => {
    setFilters(f => ({ ...f, [key]: '', page: 0 }))
    if (key === 'city') setSearchParams({})
  }

  return (
    <Layout>
      <div className="max-w-7xl mx-auto px-4 sm:px-6 py-8">

        {/* Search + Filter bar */}
        <div className="flex flex-col sm:flex-row gap-3 mb-8">
          <form onSubmit={handleSearch} className="flex-1 flex gap-2">
            <div className="relative flex-1">
              <FiSearch className="absolute left-3 top-1/2 -translate-y-1/2 text-staynest-gray" />
              <input
                value={filters.city}
                onChange={e => setFilters(f => ({ ...f, city: e.target.value }))}
                placeholder="Search by city..."
                className="input-base pl-9"
              />
              {filters.city && (
                <button type="button" onClick={() => clearFilter('city')} className="absolute right-3 top-1/2 -translate-y-1/2 text-staynest-gray hover:text-staynest-dark">
                  <FiX className="text-sm" />
                </button>
              )}
            </div>
            <Button type="submit">Search</Button>
          </form>

          <div className="flex gap-2">
            <Select value={filters.type} onChange={e => setFilters(f => ({ ...f, type: e.target.value, page: 0 }))} className="min-w-32">
              <option value="">All types</option>
              {PROPERTY_TYPES.map(t => <option key={t} value={t}>{t.charAt(0) + t.slice(1).toLowerCase()}</option>)}
            </Select>
            <Select value={filters.sortBy} onChange={e => setFilters(f => ({ ...f, sortBy: e.target.value }))} className="min-w-32">
              <option value="createdAt">Newest</option>
              <option value="propertyName">Name A-Z</option>
            </Select>
          </div>
        </div>

        {/* Active filters */}
        <div className="flex flex-wrap gap-2 mb-6">
          {filters.city && (
            <span className="flex items-center gap-1 bg-staynest-dark text-white text-sm px-3 py-1 rounded-full">
              {filters.city}
              <button onClick={() => clearFilter('city')}><FiX className="text-xs" /></button>
            </span>
          )}
          {filters.type && (
            <span className="flex items-center gap-1 bg-staynest-dark text-white text-sm px-3 py-1 rounded-full">
              {filters.type}
              <button onClick={() => clearFilter('type')}><FiX className="text-xs" /></button>
            </span>
          )}
        </div>

        {/* Results */}
        {loading ? (
          <PageLoader />
        ) : properties.length === 0 ? (
          <EmptyState
            icon="🏡"
            title="No properties found"
            description="Try adjusting your search filters or explore a different destination."
            action={<Button onClick={() => { setFilters(f => ({ ...f, city: '', type: '', page: 0 })); setSearchParams({}) }}>Clear all filters</Button>}
          />
        ) : (
          <>
            <p className="text-staynest-gray text-sm mb-6">
              {total} {total === 1 ? 'property' : 'properties'} found
              {filters.city && ` in ${filters.city}`}
            </p>
            <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-x-5 gap-y-8">
              {properties.map(p => <PropertyCard key={p.propertyId} property={p} />)}
            </div>

            {/* Pagination */}
            {total > filters.size && (
              <div className="flex justify-center gap-3 mt-12">
                <Button variant="secondary" disabled={filters.page === 0} onClick={() => setFilters(f => ({ ...f, page: f.page - 1 }))}>← Previous</Button>
                <span className="flex items-center text-sm text-staynest-gray px-4">Page {filters.page + 1}</span>
                <Button variant="secondary" disabled={(filters.page + 1) * filters.size >= total} onClick={() => setFilters(f => ({ ...f, page: f.page + 1 }))}>Next →</Button>
              </div>
            )}
          </>
        )}
      </div>
    </Layout>
  )
}
