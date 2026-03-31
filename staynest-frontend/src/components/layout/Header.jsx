import { useState, useRef, useEffect } from 'react'
import { Link, useNavigate, useLocation } from 'react-router-dom'
import { FiSearch, FiMenu, FiUser, FiHeart, FiMessageSquare, FiGrid, FiLogOut, FiSettings, FiBriefcase, FiHome } from 'react-icons/fi'
import { useAuth } from '../../context/AuthContext'

export default function Header() {
  const { user, isAuthenticated, isHost, logout } = useAuth()
  const navigate  = useNavigate()
  const location  = useLocation()
  const [menuOpen, setMenuOpen]   = useState(false)
  const [scrolled, setScrolled]   = useState(false)
  const [search,   setSearch]     = useState('')
  const menuRef = useRef(null)

  const isHome = location.pathname === '/'

  useEffect(() => {
    const handleScroll = () => setScrolled(window.scrollY > 10)
    window.addEventListener('scroll', handleScroll)
    return () => window.removeEventListener('scroll', handleScroll)
  }, [])

  useEffect(() => {
    const handler = (e) => { if (menuRef.current && !menuRef.current.contains(e.target)) setMenuOpen(false) }
    document.addEventListener('mousedown', handler)
    return () => document.removeEventListener('mousedown', handler)
  }, [])

  const handleSearch = (e) => {
    e.preventDefault()
    if (search.trim()) navigate(`/properties?city=${encodeURIComponent(search.trim())}`)
    else navigate('/properties')
  }

  return (
    <header className={`sticky top-0 z-50 bg-white transition-shadow duration-200 ${scrolled ? 'shadow-nav' : 'border-b border-staynest-light'}`}>
      <div className="max-w-7xl mx-auto px-4 sm:px-6 h-16 flex items-center justify-between gap-4">

        {/* Logo */}
        <Link to="/" className="flex items-center gap-2 shrink-0">
          <div className="w-8 h-8 bg-primary rounded-lg flex items-center justify-center">
            <FiHome className="text-white text-lg" />
          </div>
          <span className="font-bold text-xl text-primary hidden sm:block">StayNest</span>
        </Link>

        {/* Search bar */}
        <form onSubmit={handleSearch} className="flex-1 max-w-md">
          <div className="flex items-center border border-staynest-light rounded-full px-4 py-2 hover:shadow-card transition-shadow cursor-pointer gap-3">
            <input
              value={search}
              onChange={e => setSearch(e.target.value)}
              placeholder="Search destinations..."
              className="flex-1 text-sm bg-transparent outline-none text-staynest-dark placeholder-staynest-gray min-w-0"
            />
            <button type="submit" className="bg-primary text-white p-1.5 rounded-full shrink-0 hover:bg-primary-hover transition-colors">
              <FiSearch className="text-sm" />
            </button>
          </div>
        </form>

        {/* Right nav */}
        <div className="flex items-center gap-2 shrink-0">
          {isHost && (
            <Link to="/host/properties" className="hidden md:block text-sm font-medium text-staynest-dark hover:bg-staynest-bg px-3 py-2 rounded-full transition-colors whitespace-nowrap">
              Host Dashboard
            </Link>
          )}
          {isAuthenticated && (
            <>
              <Link to="/wishlist" title="Wishlist" className="p-2 rounded-full hover:bg-staynest-bg transition-colors">
                <FiHeart className="text-staynest-dark text-xl" />
              </Link>
              <Link to="/messages" title="Messages" className="p-2 rounded-full hover:bg-staynest-bg transition-colors">
                <FiMessageSquare className="text-staynest-dark text-xl" />
              </Link>
            </>
          )}

          {/* User menu */}
          <div className="relative" ref={menuRef}>
            <button
              onClick={() => setMenuOpen(!menuOpen)}
              className="flex items-center gap-2 border border-staynest-light rounded-full px-3 py-2 hover:shadow-card transition-all duration-200"
            >
              <FiMenu className="text-staynest-dark" />
              <div className="w-7 h-7 bg-staynest-dark rounded-full flex items-center justify-center">
                {user?.firstName ? (
                  <span className="text-white text-xs font-semibold">{user.firstName[0]}{user.lastName?.[0]}</span>
                ) : (
                  <FiUser className="text-white text-sm" />
                )}
              </div>
            </button>

            {menuOpen && (
              <div className="absolute right-0 mt-2 w-56 bg-white rounded-xl shadow-card border border-staynest-light overflow-hidden animate-fade-in z-50">
                {isAuthenticated ? (
                  <>
                    <div className="px-4 py-3 border-b border-staynest-light">
                      <p className="font-semibold text-sm text-staynest-dark">{user?.firstName} {user?.lastName}</p>
                      <p className="text-xs text-staynest-gray">{user?.email}</p>
                      <span className="inline-block mt-1 text-xs bg-primary-light text-primary font-medium px-2 py-0.5 rounded-full">{user?.role}</span>
                    </div>
                    <MenuItem icon={<FiGrid />}         to="/dashboard"    label="Dashboard"   onClick={() => setMenuOpen(false)} />
                    <MenuItem icon={<FiUser />}         to="/profile"      label="Profile"     onClick={() => setMenuOpen(false)} />
                    <MenuItem icon={<FiHeart />}        to="/wishlist"     label="Wishlist"    onClick={() => setMenuOpen(false)} />
                    <MenuItem icon={<FiMessageSquare />} to="/messages"    label="Messages"    onClick={() => setMenuOpen(false)} />
                    {isHost && <MenuItem icon={<FiBriefcase />} to="/host/properties" label="My Properties" onClick={() => setMenuOpen(false)} />}
                    <div className="border-t border-staynest-light">
                      <button onClick={() => { logout(); setMenuOpen(false); navigate('/') }} className="w-full flex items-center gap-3 px-4 py-3 text-sm text-staynest-dark hover:bg-staynest-bg transition-colors">
                        <FiLogOut className="text-staynest-gray" /> Log out
                      </button>
                    </div>
                  </>
                ) : (
                  <>
                    <MenuItem to="/login"    label="Log in"   className="font-semibold" onClick={() => setMenuOpen(false)} />
                    <MenuItem to="/register" label="Sign up"  onClick={() => setMenuOpen(false)} />
                  </>
                )}
              </div>
            )}
          </div>
        </div>
      </div>
    </header>
  )
}

function MenuItem({ to, label, icon, onClick, className = '' }) {
  return (
    <Link to={to} onClick={onClick} className={`flex items-center gap-3 px-4 py-3 text-sm text-staynest-dark hover:bg-staynest-bg transition-colors ${className}`}>
      {icon && <span className="text-staynest-gray">{icon}</span>}
      {label}
    </Link>
  )
}
