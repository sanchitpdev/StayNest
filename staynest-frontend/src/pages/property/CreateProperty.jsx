import { useState, useEffect } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import { FiPlus, FiMinus } from 'react-icons/fi'
import { propertyApi, unitApi } from '../../api'
import { Input, Select, Textarea, Button, ErrorMessage } from '../../components/ui'
import Layout from '../../components/layout/Layout'
import toast from 'react-hot-toast'

const TYPES = ['APARTMENT','HOUSE','VILLA','HOTEL','RESORT','COTTAGE','GUESTHOUSE','HOSTEL']
const AMENITIES_LIST = ['wifi','pool','parking','kitchen','airConditioning','gym','petFriendly','beachAccess','tv','washer']

export default function CreateProperty() {
  const navigate = useNavigate()
  const { id }   = useParams()
  const isEdit   = !!id

  const [loading, setLoading] = useState(false)
  const [error,   setError]   = useState('')
  const [step,    setStep]    = useState(1)

  const [form, setForm] = useState({
    propertyName: '', description: '', propertyType: 'APARTMENT',
    address: '', city: '', state: '', country: 'India', postalCode: '',
    latitude: '', longitude: '',
    amenities: {},
  })

  const [units, setUnits] = useState([{
    unitName: '', unitNumber: '101', bedrooms: 1, bathrooms: 1,
    maxGuests: 2, squareFeet: '', basePrice: '', cleaningFee: 0, isAvailable: true,
  }])

  useEffect(() => {
    if (isEdit) {
      propertyApi.getById(id).then(res => {
        const p = res.data
        setForm({ propertyName: p.propertyName, description: p.description, propertyType: p.propertyType,
          address: p.address || p.streetAddress, city: p.city, state: p.state, country: p.country,
          postalCode: p.postalCode || '', latitude: p.latitude || '', longitude: p.longitude || '',
          amenities: p.amenities || {} })
      }).catch(() => navigate('/host/properties'))
    }
  }, [id, isEdit])

  const handleChange = e => setForm(f => ({ ...f, [e.target.name]: e.target.value }))
  const toggleAmenity = key => setForm(f => ({ ...f, amenities: { ...f.amenities, [key]: !f.amenities[key] } }))
  const updateUnit = (i, field, value) => setUnits(u => u.map((unit, idx) => idx === i ? { ...unit, [field]: value } : unit))
  const addUnit    = () => setUnits(u => [...u, { unitName: '', unitNumber: `10${u.length + 1}`, bedrooms: 1, bathrooms: 1, maxGuests: 2, squareFeet: '', basePrice: '', cleaningFee: 0, isAvailable: true }])
  const removeUnit = i  => setUnits(u => u.filter((_, idx) => idx !== i))

  const handleSubmit = async () => {
    setError(''); setLoading(true)
    try {
      let propId = id
      if (isEdit) {
        await propertyApi.update(id, form)
        toast.success('Property updated!')
      } else {
        const res = await propertyApi.create(form)
        propId = res.data.propertyId
        // Create units
        for (const unit of units) {
          if (unit.unitName && unit.basePrice) {
            await unitApi.create({ ...unit, propertyId: propId, bedrooms: Number(unit.bedrooms), bathrooms: Number(unit.bathrooms), maxGuests: Number(unit.maxGuests), basePrice: Number(unit.basePrice), cleaningFee: Number(unit.cleaningFee) || 0 })
          }
        }
        // Auto-activate
        await propertyApi.activate(propId)
        toast.success('Property created and listed!')
      }
      navigate(`/properties/${propId}`)
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to save property')
    } finally { setLoading(false) }
  }

  const steps = [
    { n: 1, label: 'Basic Info' },
    { n: 2, label: 'Location'   },
    { n: 3, label: 'Amenities'  },
    { n: !isEdit ? 4 : null, label: 'Units' },
  ].filter(Boolean)

  return (
    <Layout>
      <div className="max-w-3xl mx-auto px-4 sm:px-6 py-10">
        <h1 className="text-3xl font-bold text-staynest-dark mb-2">{isEdit ? 'Edit Property' : 'List your property'}</h1>
        <p className="text-staynest-gray mb-8">Share your space with travellers around the world</p>

        {/* Steps */}
        <div className="flex items-center gap-0 mb-10">
          {steps.filter(s => s.n).map((s, i) => (
            <div key={s.n} className="flex items-center flex-1">
              <div className="flex flex-col items-center">
                <div className={`w-8 h-8 rounded-full flex items-center justify-center text-sm font-bold transition-colors ${step >= s.n ? 'bg-primary text-white' : 'bg-staynest-light text-staynest-gray'}`}>
                  {s.n}
                </div>
                <span className="text-xs text-staynest-gray mt-1 whitespace-nowrap">{s.label}</span>
              </div>
              {i < steps.filter(s => s.n).length - 1 && <div className={`flex-1 h-0.5 mx-2 mb-4 ${step > s.n ? 'bg-primary' : 'bg-staynest-light'}`} />}
            </div>
          ))}
        </div>

        <ErrorMessage message={error} />

        {/* Step 1: Basic */}
        {step === 1 && (
          <div className="space-y-5">
            <Input label="Property name" name="propertyName" value={form.propertyName} onChange={handleChange} placeholder="e.g. Sunset Paradise Villa" required />
            <Textarea label="Description" name="description" value={form.description} onChange={handleChange} rows={4} placeholder="Describe what makes your property special..." />
            <Select label="Property type" name="propertyType" value={form.propertyType} onChange={handleChange}>
              {TYPES.map(t => <option key={t} value={t}>{t.charAt(0) + t.slice(1).toLowerCase()}</option>)}
            </Select>
          </div>
        )}

        {/* Step 2: Location */}
        {step === 2 && (
          <div className="space-y-5">
            <Input label="Street address" name="address" value={form.address} onChange={handleChange} placeholder="e.g. 123 Beach Road, Candolim" required />
            <div className="grid grid-cols-2 gap-4">
              <Input label="City"  name="city"  value={form.city}  onChange={handleChange} placeholder="Goa"       required />
              <Input label="State" name="state" value={form.state} onChange={handleChange} placeholder="Goa"       required />
            </div>
            <div className="grid grid-cols-2 gap-4">
              <Input label="Country"     name="country"    value={form.country}    onChange={handleChange} placeholder="India" required />
              <Input label="Postal code" name="postalCode" value={form.postalCode} onChange={handleChange} placeholder="403515" />
            </div>
            <div className="grid grid-cols-2 gap-4">
              <Input label="Latitude (optional)"  name="latitude"  type="number" value={form.latitude}  onChange={handleChange} placeholder="15.5183" />
              <Input label="Longitude (optional)" name="longitude" type="number" value={form.longitude} onChange={handleChange} placeholder="73.7630" />
            </div>
          </div>
        )}

        {/* Step 3: Amenities */}
        {step === 3 && (
          <div>
            <h3 className="text-lg font-semibold text-staynest-dark mb-4">What does your place offer?</h3>
            <div className="grid grid-cols-2 md:grid-cols-3 gap-3">
              {AMENITIES_LIST.map(a => (
                <label key={a} className={`flex items-center gap-3 p-4 rounded-xl border-2 cursor-pointer transition-all ${form.amenities[a] ? 'border-primary bg-primary-light' : 'border-staynest-light hover:border-staynest-gray'}`}>
                  <input type="checkbox" checked={!!form.amenities[a]} onChange={() => toggleAmenity(a)} className="hidden" />
                  <span className="text-xl">{{ wifi:'📶', pool:'🏊', parking:'🅿️', kitchen:'🍳', airConditioning:'❄️', gym:'💪', petFriendly:'🐾', beachAccess:'🏖️', tv:'📺', washer:'🫧' }[a]}</span>
                  <span className="text-sm font-medium text-staynest-dark capitalize">{a.replace(/([A-Z])/g, ' $1').trim()}</span>
                </label>
              ))}
            </div>
          </div>
        )}

        {/* Step 4: Units (new property only) */}
        {step === 4 && !isEdit && (
          <div>
            <div className="flex items-center justify-between mb-4">
              <h3 className="text-lg font-semibold text-staynest-dark">Add units / rooms</h3>
              <Button variant="secondary" size="sm" onClick={addUnit}><FiPlus /> Add unit</Button>
            </div>
            <div className="space-y-6">
              {units.map((unit, i) => (
                <div key={i} className="border border-staynest-light rounded-xl p-5">
                  <div className="flex items-center justify-between mb-4">
                    <h4 className="font-semibold text-staynest-dark">Unit {i + 1}</h4>
                    {units.length > 1 && <button onClick={() => removeUnit(i)} className="text-red-400 hover:text-red-600 text-sm"><FiMinus /></button>}
                  </div>
                  <div className="grid grid-cols-2 gap-4">
                    <Input label="Unit name" value={unit.unitName} onChange={e => updateUnit(i, 'unitName', e.target.value)} placeholder="Ocean View Suite" />
                    <Input label="Unit number" value={unit.unitNumber} onChange={e => updateUnit(i, 'unitNumber', e.target.value)} placeholder="101" />
                    <Input label="Bedrooms" type="number" min={0} value={unit.bedrooms} onChange={e => updateUnit(i, 'bedrooms', e.target.value)} />
                    <Input label="Bathrooms" type="number" min={1} step={0.5} value={unit.bathrooms} onChange={e => updateUnit(i, 'bathrooms', e.target.value)} />
                    <Input label="Max guests" type="number" min={1} value={unit.maxGuests} onChange={e => updateUnit(i, 'maxGuests', e.target.value)} />
                    <Input label="Square feet (optional)" type="number" value={unit.squareFeet} onChange={e => updateUnit(i, 'squareFeet', e.target.value)} />
                    <Input label="Base price / night (₹)" type="number" value={unit.basePrice} onChange={e => updateUnit(i, 'basePrice', e.target.value)} placeholder="5000" />
                    <Input label="Cleaning fee (₹)" type="number" value={unit.cleaningFee} onChange={e => updateUnit(i, 'cleaningFee', e.target.value)} placeholder="0" />
                  </div>
                </div>
              ))}
            </div>
          </div>
        )}

        {/* Navigation */}
        <div className="flex justify-between mt-10 pt-6 border-t border-staynest-light">
          <Button variant="secondary" onClick={() => step > 1 ? setStep(s => s - 1) : navigate('/host/properties')} disabled={loading}>
            {step === 1 ? 'Cancel' : '← Back'}
          </Button>
          {step < (isEdit ? 3 : 4) ? (
            <Button onClick={() => setStep(s => s + 1)}>Continue →</Button>
          ) : (
            <Button loading={loading} onClick={handleSubmit}>{isEdit ? 'Save changes' : 'Publish listing'}</Button>
          )}
        </div>
      </div>
    </Layout>
  )
}
