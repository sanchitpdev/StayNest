import { useState, useEffect } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { FiPlus, FiEdit2, FiTrash2, FiEye, FiEyeOff, FiCheck, FiSlash, FiHome } from 'react-icons/fi'
import { propertyApi } from '../../api'
import { PageLoader, Button, StatusBadge, Modal, EmptyState } from '../../components/ui'
import Layout from '../../components/layout/Layout'
import toast from 'react-hot-toast'

export default function HostDashboard() {
  const navigate = useNavigate()
  const [properties, setProps]  = useState([])
  const [loading,    setLoading] = useState(true)
  const [confirm,    setConfirm] = useState(null) // { propertyId, action }

  const load = async () => {
    setLoading(true)
    try { const r = await propertyApi.getMyProperties(); setProps(r.data) }
    catch { setProps([]) }
    finally { setLoading(false) }
  }

  useEffect(() => { load() }, [])

  const handleAction = async (propertyId, action) => {
    try {
      if (action === 'activate')   await propertyApi.activate(propertyId)
      if (action === 'deactivate') await propertyApi.deactivate(propertyId)
      if (action === 'delete')     await propertyApi.delete(propertyId)
      toast.success(`Property ${action}d successfully`)
      setConfirm(null)
      load()
    } catch (err) { toast.error(err.response?.data?.message || 'Action failed') }
  }

  const PLACEHOLDER = 'https://images.unsplash.com/photo-1571896349842-33c89424de2d?w=400&q=80'

  return (
    <Layout>
      <div className="max-w-6xl mx-auto px-4 sm:px-6 py-10">
        <div className="flex items-center justify-between mb-8">
          <div>
            <h1 className="text-3xl font-bold text-staynest-dark">My Properties</h1>
            <p className="text-staynest-gray mt-1">{properties.length} listing{properties.length !== 1 ? 's' : ''}</p>
          </div>
          <Button onClick={() => navigate('/host/properties/create')}><FiPlus /> New listing</Button>
        </div>

        {loading ? (
          <PageLoader />
        ) : properties.length === 0 ? (
          <EmptyState
            icon={<FiHome />}
            title="No properties yet"
            description="Start earning by listing your first property on StayNest."
            action={<Button onClick={() => navigate('/host/properties/create')}><FiPlus /> Create your first listing</Button>}
          />
        ) : (
          <div className="grid grid-cols-1 gap-4">
            {properties.map(p => (
              <div key={p.propertyId} className="bg-white border border-staynest-light rounded-2xl overflow-hidden hover:shadow-card transition-shadow">
                <div className="flex flex-col sm:flex-row">
                  {/* Image */}
                  <div className="w-full sm:w-48 h-36 sm:h-auto shrink-0">
                    <img
                      src={p.images?.[0]?.imageUrl || PLACEHOLDER}
                      alt={p.propertyName}
                      className="w-full h-full object-cover"
                      onError={e => e.target.src = PLACEHOLDER}
                    />
                  </div>

                  {/* Content */}
                  <div className="flex-1 p-5">
                    <div className="flex items-start justify-between gap-3">
                      <div>
                        <div className="flex items-center gap-2 flex-wrap">
                          <h3 className="font-semibold text-staynest-dark">{p.propertyName}</h3>
                          <StatusBadge status={p.propertyStatus || 'ACTIVE'} />
                        </div>
                        <p className="text-staynest-gray text-sm mt-1">{p.city}, {p.state} · {p.propertyType}</p>
                        <div className="flex items-center gap-4 mt-3 text-sm text-staynest-gray">
                          <span>{p.totalUnits || 0} unit{(p.totalUnits || 0) !== 1 ? 's' : ''}</span>
                          <span className="text-staynest-dark font-medium">
                            From ₹{p.startingPrice?.toLocaleString('en-IN') || '—'}/night
                          </span>
                        </div>
                      </div>

                      {/* Actions */}
                      <div className="flex items-center gap-2 shrink-0 flex-wrap justify-end">
                        <Link to={`/properties/${p.propertyId}`}>
                          <Button variant="outline" size="sm"><FiEye /> View</Button>
                        </Link>
                        <Link to={`/host/properties/edit/${p.propertyId}`}>
                          <Button variant="outline" size="sm"><FiEdit2 /> Edit</Button>
                        </Link>
                        {p.propertyStatus === 'ACTIVE' ? (
                          <Button variant="outline" size="sm" onClick={() => setConfirm({ propertyId: p.propertyId, action: 'deactivate' })}>
                            <FiEyeOff /> Deactivate
                          </Button>
                        ) : p.propertyStatus === 'DRAFT' || p.propertyStatus === 'INACTIVE' ? (
                          <Button variant="outline" size="sm" onClick={() => handleAction(p.propertyId, 'activate')}>
                            <FiCheck /> Activate
                          </Button>
                        ) : null}
                        <Button variant="danger" size="sm" onClick={() => setConfirm({ propertyId: p.propertyId, action: 'delete' })}>
                          <FiTrash2 />
                        </Button>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>

      {/* Confirm modal */}
      <Modal isOpen={!!confirm} onClose={() => setConfirm(null)} title={confirm?.action === 'delete' ? 'Delete property?' : 'Deactivate property?'} size="sm">
        <p className="text-staynest-gray mb-6 text-sm">
          {confirm?.action === 'delete'
            ? 'This will permanently delete the property and all its units. This cannot be undone.'
            : 'This will hide your property from guests. You can reactivate it anytime.'}
        </p>
        <div className="flex gap-3">
          <Button variant="secondary" className="flex-1" onClick={() => setConfirm(null)}>Cancel</Button>
          <Button variant={confirm?.action === 'delete' ? 'danger' : 'primary'} className="flex-1"
            onClick={() => handleAction(confirm.propertyId, confirm.action)}>
            {confirm?.action === 'delete' ? 'Delete' : 'Deactivate'}
          </Button>
        </div>
      </Modal>
    </Layout>
  )
}
