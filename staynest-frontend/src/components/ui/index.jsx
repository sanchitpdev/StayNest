import { FiX, FiStar } from 'react-icons/fi'
import { useEffect } from 'react'

// ── Button ────────────────────────────────────────────────
export function Button({ children, variant = 'primary', size = 'md', loading, className = '', ...props }) {
  const base   = 'inline-flex items-center justify-center gap-2 font-semibold rounded-lg transition-all duration-200 disabled:opacity-50 disabled:cursor-not-allowed'
  const sizes  = { sm: 'px-4 py-2 text-sm', md: 'px-6 py-3 text-sm', lg: 'px-8 py-4 text-base' }
  const variants = {
    primary:   'bg-primary text-white hover:bg-primary-hover',
    secondary: 'border border-staynest-dark text-staynest-dark hover:bg-staynest-bg',
    ghost:     'text-staynest-dark hover:bg-staynest-bg',
    danger:    'bg-red-500 text-white hover:bg-red-600',
    outline:   'border border-staynest-light text-staynest-dark hover:border-staynest-dark',
  }
  return (
    <button className={`${base} ${sizes[size]} ${variants[variant]} ${className}`} disabled={loading || props.disabled} {...props}>
      {loading && <Spinner size="sm" className="text-current" />}
      {children}
    </button>
  )
}

// ── Input ─────────────────────────────────────────────────
export function Input({ label, error, className = '', ...props }) {
  return (
    <div className="w-full">
      {label && <label className="block text-sm font-medium text-staynest-dark mb-1.5">{label}</label>}
      <input className={`input-base ${error ? 'border-red-400 ring-1 ring-red-400' : ''} ${className}`} {...props} />
      {error && <p className="mt-1 text-xs text-red-500">{error}</p>}
    </div>
  )
}

// ── Select ────────────────────────────────────────────────
export function Select({ label, error, children, className = '', ...props }) {
  return (
    <div className="w-full">
      {label && <label className="block text-sm font-medium text-staynest-dark mb-1.5">{label}</label>}
      <select className={`input-base bg-white ${error ? 'border-red-400' : ''} ${className}`} {...props}>
        {children}
      </select>
      {error && <p className="mt-1 text-xs text-red-500">{error}</p>}
    </div>
  )
}

// ── Textarea ──────────────────────────────────────────────
export function Textarea({ label, error, className = '', ...props }) {
  return (
    <div className="w-full">
      {label && <label className="block text-sm font-medium text-staynest-dark mb-1.5">{label}</label>}
      <textarea className={`input-base resize-none ${error ? 'border-red-400' : ''} ${className}`} {...props} />
      {error && <p className="mt-1 text-xs text-red-500">{error}</p>}
    </div>
  )
}

// ── Modal ─────────────────────────────────────────────────
export function Modal({ isOpen, onClose, title, children, size = 'md' }) {
  useEffect(() => {
    document.body.style.overflow = isOpen ? 'hidden' : ''
    return () => { document.body.style.overflow = '' }
  }, [isOpen])

  if (!isOpen) return null
  const sizes = { sm: 'max-w-md', md: 'max-w-lg', lg: 'max-w-2xl', xl: 'max-w-4xl' }
  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4">
      <div className="absolute inset-0 bg-black/50 backdrop-blur-sm" onClick={onClose} />
      <div className={`relative bg-white rounded-2xl shadow-widget w-full ${sizes[size]} max-h-[90vh] overflow-y-auto animate-fade-in`}>
        <div className="flex items-center justify-between px-6 py-4 border-b border-staynest-light sticky top-0 bg-white z-10">
          <h2 className="font-semibold text-lg text-staynest-dark">{title}</h2>
          <button onClick={onClose} className="p-2 rounded-full hover:bg-staynest-bg transition-colors"><FiX /></button>
        </div>
        <div className="p-6">{children}</div>
      </div>
    </div>
  )
}

// ── Spinner ───────────────────────────────────────────────
export function Spinner({ size = 'md', className = '' }) {
  const sizes = { sm: 'w-4 h-4', md: 'w-8 h-8', lg: 'w-12 h-12' }
  return (
    <div className={`${sizes[size]} border-2 border-current border-t-transparent rounded-full animate-spin ${className}`} />
  )
}

// ── PageLoader ────────────────────────────────────────────
export function PageLoader() {
  return (
    <div className="min-h-[60vh] flex items-center justify-center">
      <div className="flex flex-col items-center gap-3">
        <Spinner size="lg" className="text-primary" />
        <p className="text-sm text-staynest-gray">Loading...</p>
      </div>
    </div>
  )
}

// ── StarRating ────────────────────────────────────────────
export function StarRating({ rating, max = 5, size = 'sm', showValue = true }) {
  const sizes = { sm: 'text-xs', md: 'text-sm', lg: 'text-base' }
  return (
    <div className={`flex items-center gap-1 ${sizes[size]}`}>
      <FiStar className="text-staynest-dark fill-staynest-dark" />
      <span className="font-semibold text-staynest-dark">{rating ? Number(rating).toFixed(1) : 'New'}</span>
    </div>
  )
}

// ── Badge ─────────────────────────────────────────────────
export function Badge({ children, variant = 'default', className = '' }) {
  const variants = {
    default: 'bg-staynest-bg text-staynest-dark',
    primary: 'bg-primary-light text-primary',
    success: 'bg-green-50 text-green-700',
    warning: 'bg-yellow-50 text-yellow-700',
    danger:  'bg-red-50 text-red-600',
    info:    'bg-blue-50 text-blue-700',
  }
  return (
    <span className={`inline-block px-2.5 py-1 rounded-full text-xs font-medium ${variants[variant]} ${className}`}>
      {children}
    </span>
  )
}

// ── EmptyState ────────────────────────────────────────────
export function EmptyState({ icon, title, description, action }) {
  return (
    <div className="flex flex-col items-center justify-center py-20 text-center px-4">
      {icon && <div className="text-5xl text-staynest-light mb-4">{icon}</div>}
      <h3 className="text-xl font-semibold text-staynest-dark mb-2">{title}</h3>
      {description && <p className="text-staynest-gray text-sm max-w-sm mb-6">{description}</p>}
      {action}
    </div>
  )
}

// ── ErrorMessage ──────────────────────────────────────────
export function ErrorMessage({ message }) {
  if (!message) return null
  return (
    <div className="bg-red-50 border border-red-200 text-red-600 text-sm px-4 py-3 rounded-lg">{message}</div>
  )
}

// ── StatusBadge (booking/property status) ─────────────────
export function StatusBadge({ status }) {
  const map = {
    PENDING:     { label: 'Pending',     variant: 'warning' },
    CONFIRMED:   { label: 'Confirmed',   variant: 'success' },
    COMPLETED:   { label: 'Completed',   variant: 'info'    },
    CANCELLED:   { label: 'Cancelled',   variant: 'danger'  },
    REJECTED:    { label: 'Rejected',    variant: 'danger'  },
    CHECKED_IN:  { label: 'Checked In',  variant: 'primary' },
    CHECKED_OUT: { label: 'Checked Out', variant: 'default' },
    ACTIVE:      { label: 'Active',      variant: 'success' },
    DRAFT:       { label: 'Draft',       variant: 'default' },
    INACTIVE:    { label: 'Inactive',    variant: 'warning' },
    SUSPENDED:   { label: 'Suspended',   variant: 'danger'  },
  }
  const { label, variant } = map[status] || { label: status, variant: 'default' }
  return <Badge variant={variant}>{label}</Badge>
}
