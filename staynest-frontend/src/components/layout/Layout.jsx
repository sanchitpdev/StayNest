import { Link } from 'react-router-dom'
import { FiHome, FiGlobe, FiFacebook, FiTwitter, FiInstagram } from 'react-icons/fi'

export function Footer() {
  return (
    <footer className="border-t border-staynest-light bg-white mt-16">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 py-10">
        <div className="grid grid-cols-1 md:grid-cols-3 gap-8 mb-8">
          <div>
            <div className="flex items-center gap-2 mb-4">
              <div className="w-7 h-7 bg-primary rounded-lg flex items-center justify-center">
                <FiHome className="text-white text-sm" />
              </div>
              <span className="font-bold text-lg text-primary">StayNest</span>
            </div>
            <p className="text-sm text-staynest-gray leading-relaxed">Find unique places to stay with local hosts. Book experiences unlike any hotel.</p>
          </div>
          <div>
            <h4 className="font-semibold text-sm text-staynest-dark mb-3">Support</h4>
            <ul className="space-y-2 text-sm text-staynest-gray">
              <li><Link to="/properties" className="hover:text-staynest-dark transition-colors">Find a stay</Link></li>
              <li><Link to="/register"   className="hover:text-staynest-dark transition-colors">Become a Host</Link></li>
              <li><a href="#" className="hover:text-staynest-dark transition-colors">Help Center</a></li>
            </ul>
          </div>
          <div>
            <h4 className="font-semibold text-sm text-staynest-dark mb-3">Follow us</h4>
            <div className="flex gap-3">
              {[FiFacebook, FiTwitter, FiInstagram].map((Icon, i) => (
                <a key={i} href="#" className="w-9 h-9 rounded-full border border-staynest-light flex items-center justify-center text-staynest-gray hover:border-staynest-dark hover:text-staynest-dark transition-colors">
                  <Icon />
                </a>
              ))}
            </div>
          </div>
        </div>
        <div className="border-t border-staynest-light pt-6 flex flex-col sm:flex-row items-center justify-between gap-3">
          <p className="text-xs text-staynest-gray">© 2026 StayNest. All rights reserved.</p>
          <div className="flex items-center gap-1 text-xs text-staynest-gray">
            <FiGlobe className="text-sm" /> English (IN)
          </div>
        </div>
      </div>
    </footer>
  )
}

import Header from './Header'

export default function Layout({ children }) {
  return (
    <div className="min-h-screen flex flex-col">
      <Header />
      <main className="flex-1">{children}</main>
      <Footer />
    </div>
  )
}
