import api from './axios'

// ─── AUTH ────────────────────────────────────────────────
export const authApi = {
  register: (data)  => api.post('/auth/register', data),
  login:    (data)  => api.post('/auth/login', data),
  testAuth: ()      => api.get('/auth/test'),
}

// ─── PROPERTIES ──────────────────────────────────────────
export const propertyApi = {
  getAll:          (params) => api.get('/properties', { params }),
  getPaginated:    (params) => api.get('/properties/paginated', { params }),
  getById:         (id)     => api.get(`/properties/${id}`),
  getByHost:       (hostId) => api.get(`/properties/host/${hostId}`),
  getMyProperties: ()       => api.get('/properties/my-properties'),
  search:          (params) => api.get('/properties/search', { params }),
  searchCity:      (params) => api.get('/properties/search/city', { params }),
  advancedSearch:  (data)   => api.post('/properties/search/advanced', data),
  create:          (data)   => api.post('/properties', data),
  update:          (id, d)  => api.put(`/properties/${id}`, d),
  delete:          (id)     => api.delete(`/properties/${id}`),
  activate:        (id)     => api.patch(`/properties/${id}/activate`),
  deactivate:      (id)     => api.patch(`/properties/${id}/deactivate`),
  suspend:         (id)     => api.patch(`/properties/${id}/suspend`),
}

// ─── UNITS ───────────────────────────────────────────────
export const unitApi = {
  getById:              (id)         => api.get(`/units/${id}`),
  getByProperty:        (propertyId) => api.get(`/units/property/${propertyId}`),
  getAvailableByProperty: (propertyId) => api.get(`/units/property/${propertyId}/available`),
  create:               (data)       => api.post('/units', data),
  update:               (id, data)   => api.put(`/units/${id}`, data),
  delete:               (id)         => api.delete(`/units/${id}`),
}

// ─── BOOKINGS ────────────────────────────────────────────
export const bookingApi = {
  create:            (data)          => api.post('/bookings', data),
  getById:           (id)            => api.get(`/bookings/${id}`),
  getMyBookings:     ()              => api.get('/bookings/my-bookings'),
  getUpcoming:       ()              => api.get('/bookings/upcoming'),
  getByProperty:     (propertyId)   => api.get(`/bookings/property/${propertyId}`),
  checkAvailability: (unitId, p)    => api.get(`/bookings/availability/${unitId}`, { params: p }),
  cancel:            (id)            => api.post(`/bookings/${id}/cancel`),
  confirm:           (id)            => api.post(`/bookings/${id}/confirm`),
  complete:          (id)            => api.post(`/bookings/${id}/complete`),
}

// ─── PAYMENTS ────────────────────────────────────────────
export const paymentApi = {
  create:         (data)      => api.post('/payments', data),
  getById:        (id)        => api.get(`/payments/${id}`),
  getByBooking:   (bookingId) => api.get(`/payments/booking/${bookingId}`),
  getMyPayments:  ()          => api.get('/payments/my-payments'),
}

// ─── REVIEWS ─────────────────────────────────────────────
export const reviewApi = {
  create:              (data)        => api.post('/reviews', data),
  getById:             (id)          => api.get(`/reviews/${id}`),
  getByProperty:       (propertyId, params) => api.get(`/reviews/property/${propertyId}`, { params }),
  getByUser:           (userId)      => api.get(`/reviews/user/${userId}`),
  getMyReviews:        ()            => api.get('/reviews/my-reviews'),
  getReviewableBookings: ()          => api.get('/reviews/reviewable-bookings'),
  addHostResponse:     (id, data)    => api.post(`/reviews/${id}/host-response`, data),
}

// ─── WISHLIST ────────────────────────────────────────────
export const wishlistApi = {
  getMyWishlist:    (params)     => api.get('/wishlists', { params }),
  add:              (propertyId) => api.post(`/wishlists/${propertyId}`),
  remove:           (propertyId) => api.delete(`/wishlists/${propertyId}`),
  isSaved:          (propertyId) => api.get(`/wishlists/is-saved/${propertyId}`),
  getCount:         ()           => api.get('/wishlists/count'),
}

// ─── MESSAGES ────────────────────────────────────────────
export const messageApi = {
  startConversation: (propertyId)      => api.post(`/messages/conversations/property/${propertyId}`),
  getConversations:  ()                => api.get('/messages/conversations'),
  getConversation:   (id)              => api.get(`/messages/conversations/${id}`),
  sendMessage:       (convId, data)    => api.post(`/messages/conversations/${convId}/send`, data),
  getMessages:       (convId)          => api.get(`/messages/conversations/${convId}/messages`),
}

// ─── COUPONS ─────────────────────────────────────────────
export const couponApi = {
  getActive:    ()          => api.get('/coupons'),
  getAll:       ()          => api.get('/coupons/all'),
  create:       (data)      => api.post('/coupons', data),
  apply:        (data)      => api.post('/coupons/apply', data),
  deactivate:   (id)        => api.patch(`/coupons/${id}/deactivate`),
}

// ─── USERS ───────────────────────────────────────────────
export const userApi = {
  getMyProfile:   ()          => api.get('/users/me'),
  getById:        (id)        => api.get(`/users/${id}`),
  updateProfile:  (data)      => api.put('/users/me', data),
  changePassword: (data)      => api.post('/users/me/change-password', data),
  getMyStats:     ()          => api.get('/users/me/stats'),
}

// ─── PRICING ─────────────────────────────────────────────
export const pricingApi = {
  calculate: (unitId, params) => api.get(`/pricing/units/${unitId}`, { params }),
}

// ─── DASHBOARD ───────────────────────────────────────────
export const dashboardApi = {
  getStats: () => api.get('/dashboard/stats'),
}

// ─── IMAGES ──────────────────────────────────────────────
export const imageApi = {
  addToProperty: (propertyId, data) => api.post(`/images/properties/${propertyId}`, data),
  addToUnit:     (unitId, data)     => api.post(`/images/units/${unitId}`, data),
  delete:        (imageId)          => api.delete(`/images/${imageId}`),
  getByProperty: (propertyId)       => api.get(`/images/properties/${propertyId}`),
  getByUnit:     (unitId)           => api.get(`/images/units/${unitId}`),
}
