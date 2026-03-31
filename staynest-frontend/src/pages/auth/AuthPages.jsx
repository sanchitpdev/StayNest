import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { FiEye, FiEyeOff, FiHome } from 'react-icons/fi'
import { useAuth } from '../../context/AuthContext'
import { Input, Button, ErrorMessage } from '../../components/ui'
import toast from 'react-hot-toast'

// ── LOGIN ─────────────────────────────────────────────────
export function Login() {
  const { login }   = useAuth()
  const navigate    = useNavigate()
  const [form, setF] = useState({ email: '', password: '' })
  const [showPw, setSP] = useState(false)
  const [loading, setL]  = useState(false)
  const [error, setE]    = useState('')

  const handle = e => setF(p => ({ ...p, [e.target.name]: e.target.value }))

  const submit = async (e) => {
    e.preventDefault(); setE(''); setL(true)
    try {
      const user = await login(form.email, form.password)
      toast.success(`Welcome back, ${user.firstName}!`)
      navigate('/')
    } catch (err) {
      setE(err.response?.data?.message || 'Invalid email or password')
    } finally { setL(false) }
  }

  return (
    <div className="min-h-screen flex">
      {/* Left image panel */}
      <div className="hidden lg:block w-1/2 relative">
        <img src="https://images.unsplash.com/photo-1571003123894-1f0594d2b5d9?w=900&q=80" alt="" className="w-full h-full object-cover" />
        <div className="absolute inset-0 bg-gradient-to-t from-black/60 to-transparent flex items-end p-10">
          <div className="text-white">
            <h2 className="text-3xl font-bold mb-2">Find your next adventure</h2>
            <p className="text-white/80">Thousands of unique stays across India and beyond.</p>
          </div>
        </div>
      </div>

      {/* Right form panel */}
      <div className="flex-1 flex items-center justify-center p-6">
        <div className="w-full max-w-md">
          <Link to="/" className="flex items-center gap-2 mb-8">
            <div className="w-8 h-8 bg-primary rounded-lg flex items-center justify-center"><FiHome className="text-white" /></div>
            <span className="font-bold text-xl text-primary">StayNest</span>
          </Link>

          <h1 className="text-3xl font-bold text-staynest-dark mb-2">Welcome back</h1>
          <p className="text-staynest-gray mb-8">Log in to your StayNest account</p>

          <form onSubmit={submit} className="space-y-4">
            <ErrorMessage message={error} />
            <Input label="Email address" name="email" type="email" value={form.email} onChange={handle} placeholder="you@example.com" required />
            <div className="relative">
              <Input label="Password" name="password" type={showPw ? 'text' : 'password'} value={form.password} onChange={handle} placeholder="Your password" required />
              <button type="button" onClick={() => setSP(!showPw)} className="absolute right-3 top-9 text-staynest-gray hover:text-staynest-dark">
                {showPw ? <FiEyeOff /> : <FiEye />}
              </button>
            </div>
            <Button loading={loading} className="w-full py-4 text-base mt-2">Log in</Button>
          </form>

          <p className="text-center text-sm text-staynest-gray mt-6">
            Don't have an account?{' '}
            <Link to="/register" className="text-primary font-semibold hover:underline">Sign up</Link>
          </p>
        </div>
      </div>
    </div>
  )
}

// ── REGISTER ──────────────────────────────────────────────
export function Register() {
  const { register } = useAuth()
  const navigate     = useNavigate()
  const [form, setF]  = useState({ firstName: '', lastName: '', email: '', password: '', phoneNumber: '', role: 'GUEST' })
  const [showPw, setSP] = useState(false)
  const [loading, setL]  = useState(false)
  const [error, setE]    = useState('')

  const handle = e => setF(p => ({ ...p, [e.target.name]: e.target.value }))

  const submit = async (e) => {
    e.preventDefault(); setE(''); setL(true)
    try {
      const user = await register(form)
      toast.success(`Welcome to StayNest, ${user.firstName}!`)
      navigate('/')
    } catch (err) {
      setE(err.response?.data?.message || 'Registration failed. Please try again.')
    } finally { setL(false) }
  }

  return (
    <div className="min-h-screen flex">
      <div className="hidden lg:block w-1/2 relative">
        <img src="https://images.unsplash.com/photo-1613977257363-707ba9348227?w=900&q=80" alt="" className="w-full h-full object-cover" />
        <div className="absolute inset-0 bg-gradient-to-t from-black/60 to-transparent flex items-end p-10">
          <div className="text-white">
            <h2 className="text-3xl font-bold mb-2">Join StayNest today</h2>
            <p className="text-white/80">Book unique stays or become a host and earn.</p>
          </div>
        </div>
      </div>

      <div className="flex-1 flex items-center justify-center p-6 overflow-y-auto">
        <div className="w-full max-w-md py-8">
          <Link to="/" className="flex items-center gap-2 mb-8">
            <div className="w-8 h-8 bg-primary rounded-lg flex items-center justify-center"><FiHome className="text-white" /></div>
            <span className="font-bold text-xl text-primary">StayNest</span>
          </Link>

          <h1 className="text-3xl font-bold text-staynest-dark mb-2">Create your account</h1>
          <p className="text-staynest-gray mb-8">Start your journey with StayNest</p>

          <form onSubmit={submit} className="space-y-4">
            <ErrorMessage message={error} />
            <div className="grid grid-cols-2 gap-3">
              <Input label="First name" name="firstName" value={form.firstName} onChange={handle} placeholder="John" required />
              <Input label="Last name"  name="lastName"  value={form.lastName}  onChange={handle} placeholder="Doe"  required />
            </div>
            <Input label="Email address" name="email" type="email" value={form.email} onChange={handle} placeholder="you@example.com" required />
            <Input label="Phone number"  name="phoneNumber" type="tel" value={form.phoneNumber} onChange={handle} placeholder="+91 98765 43210" />
            <div className="relative">
              <Input label="Password" name="password" type={showPw ? 'text' : 'password'} value={form.password} onChange={handle} placeholder="Min. 8 characters" required />
              <button type="button" onClick={() => setSP(!showPw)} className="absolute right-3 top-9 text-staynest-gray hover:text-staynest-dark">
                {showPw ? <FiEyeOff /> : <FiEye />}
              </button>
            </div>

            {/* Role selection */}
            <div>
              <label className="block text-sm font-medium text-staynest-dark mb-2">I want to</label>
              <div className="grid grid-cols-2 gap-3">
                {[
                  { value: 'GUEST', label: '🧳 Book stays', desc: 'Find and book properties' },
                  { value: 'HOST',  label: '🏠 Host guests', desc: 'List and manage properties' },
                ].map(r => (
                  <label key={r.value} className={`border-2 rounded-xl p-4 cursor-pointer transition-all ${form.role === r.value ? 'border-primary bg-primary-light' : 'border-staynest-light hover:border-staynest-gray'}`}>
                    <input type="radio" name="role" value={r.value} checked={form.role === r.value} onChange={handle} className="hidden" />
                    <span className="block text-sm font-semibold text-staynest-dark">{r.label}</span>
                    <span className="block text-xs text-staynest-gray mt-0.5">{r.desc}</span>
                  </label>
                ))}
              </div>
            </div>

            <Button loading={loading} className="w-full py-4 text-base">Create account</Button>

            <p className="text-xs text-staynest-gray text-center leading-relaxed">
              By signing up, you agree to our <a href="#" className="underline">Terms of Service</a> and <a href="#" className="underline">Privacy Policy</a>.
            </p>
          </form>

          <p className="text-center text-sm text-staynest-gray mt-6">
            Already have an account?{' '}
            <Link to="/login" className="text-primary font-semibold hover:underline">Log in</Link>
          </p>
        </div>
      </div>
    </div>
  )
}
