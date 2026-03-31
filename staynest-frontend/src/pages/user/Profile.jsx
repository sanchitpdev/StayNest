import { useState, useEffect } from 'react'
import { FiUser, FiMail, FiPhone, FiEdit2, FiLock, FiSave } from 'react-icons/fi'
import { userApi } from '../../api'
import { Input, Button, Textarea, ErrorMessage, Badge } from '../../components/ui'
import Layout from '../../components/layout/Layout'
import { useAuth } from '../../context/AuthContext'
import toast from 'react-hot-toast'

export default function Profile() {
  const { user, refreshUser } = useAuth()
  const [profile, setProfile]   = useState(null)
  const [loading, setLoading]   = useState(true)
  const [editMode, setEditMode] = useState(false)
  const [pwMode,   setPwMode]   = useState(false)
  const [saving,   setSaving]   = useState(false)
  const [error,    setError]    = useState('')

  const [form, setForm] = useState({ firstName: '', lastName: '', phoneNumber: '', bio: '' })
  const [pwForm, setPwForm] = useState({ currentPassword: '', newPassword: '', confirmPassword: '' })

  useEffect(() => {
    userApi.getMyProfile()
      .then(r => {
        setProfile(r.data)
        setForm({ firstName: r.data.firstName || '', lastName: r.data.lastName || '', phoneNumber: r.data.phoneNumber || '', bio: r.data.bio || '' })
      })
      .catch(() => {})
      .finally(() => setLoading(false))
  }, [])

  const saveProfile = async () => {
    setSaving(true); setError('')
    try {
      await userApi.updateProfile(form)
      await refreshUser()
      setEditMode(false)
      toast.success('Profile updated!')
    } catch (err) { setError(err.response?.data?.message || 'Update failed') }
    finally { setSaving(false) }
  }

  const changePassword = async () => {
    if (pwForm.newPassword !== pwForm.confirmPassword) { setError('Passwords do not match'); return }
    setSaving(true); setError('')
    try {
      await userApi.changePassword({ currentPassword: pwForm.currentPassword, newPassword: pwForm.newPassword })
      setPwMode(false)
      setPwForm({ currentPassword: '', newPassword: '', confirmPassword: '' })
      toast.success('Password changed!')
    } catch (err) { setError(err.response?.data?.message || 'Failed to change password') }
    finally { setSaving(false) }
  }

  const data = profile || user
  if (loading) return <Layout><div className="flex justify-center py-20"><div className="w-8 h-8 border-2 border-primary border-t-transparent rounded-full animate-spin" /></div></Layout>

  return (
    <Layout>
      <div className="max-w-3xl mx-auto px-4 sm:px-6 py-10">
        <h1 className="text-3xl font-bold text-staynest-dark mb-8">My Profile</h1>

        {/* Avatar + name */}
        <div className="flex items-center gap-5 mb-8 bg-white border border-staynest-light rounded-2xl p-6">
          <div className="w-20 h-20 bg-gradient-to-br from-primary to-primary-hover rounded-full flex items-center justify-center text-white text-3xl font-bold shrink-0">
            {data?.firstName?.[0]}{data?.lastName?.[0]}
          </div>
          <div>
            <h2 className="text-xl font-bold text-staynest-dark">{data?.firstName} {data?.lastName}</h2>
            <p className="text-staynest-gray text-sm mt-0.5">{data?.email}</p>
            <div className="flex items-center gap-2 mt-2">
              <Badge variant={data?.role === 'HOST' ? 'primary' : 'default'}>{data?.role}</Badge>
              {data?.isVerified && <Badge variant="success">Verified ✓</Badge>}
            </div>
          </div>
        </div>

        <ErrorMessage message={error} />

        {/* Profile form */}
        <div className="bg-white border border-staynest-light rounded-2xl p-6 mb-6">
          <div className="flex items-center justify-between mb-5">
            <h3 className="font-semibold text-staynest-dark">Personal information</h3>
            {!editMode && (
              <Button variant="ghost" size="sm" onClick={() => { setEditMode(true); setError('') }}>
                <FiEdit2 /> Edit
              </Button>
            )}
          </div>

          {editMode ? (
            <div className="space-y-4">
              <div className="grid grid-cols-2 gap-4">
                <Input label="First name" value={form.firstName} onChange={e => setForm(f => ({ ...f, firstName: e.target.value }))} />
                <Input label="Last name"  value={form.lastName}  onChange={e => setForm(f => ({ ...f, lastName:  e.target.value }))} />
              </div>
              <Input label="Phone number" value={form.phoneNumber} onChange={e => setForm(f => ({ ...f, phoneNumber: e.target.value }))} placeholder="+91 98765 43210" />
              <Textarea label="Bio (optional)" value={form.bio} onChange={e => setForm(f => ({ ...f, bio: e.target.value }))} rows={3} placeholder="Tell guests a bit about yourself..." />
              <div className="flex gap-3 pt-2">
                <Button variant="secondary" onClick={() => { setEditMode(false); setError('') }}>Cancel</Button>
                <Button loading={saving} onClick={saveProfile}><FiSave /> Save changes</Button>
              </div>
            </div>
          ) : (
            <div className="space-y-4">
              {[
                { icon: <FiUser />,  label: 'Full name',    value: `${data?.firstName || ''} ${data?.lastName || ''}` },
                { icon: <FiMail />,  label: 'Email',        value: data?.email },
                { icon: <FiPhone />, label: 'Phone',        value: data?.phoneNumber || 'Not added' },
              ].map(item => (
                <div key={item.label} className="flex items-center gap-3">
                  <div className="w-9 h-9 bg-staynest-bg rounded-xl flex items-center justify-center text-staynest-gray shrink-0">{item.icon}</div>
                  <div>
                    <p className="text-xs text-staynest-gray">{item.label}</p>
                    <p className="text-sm font-medium text-staynest-dark">{item.value}</p>
                  </div>
                </div>
              ))}
              {data?.bio && <p className="text-sm text-staynest-gray italic border-t border-staynest-light pt-4">{data.bio}</p>}
            </div>
          )}
        </div>

        {/* Change password */}
        <div className="bg-white border border-staynest-light rounded-2xl p-6">
          <div className="flex items-center justify-between mb-5">
            <div>
              <h3 className="font-semibold text-staynest-dark">Password</h3>
              <p className="text-xs text-staynest-gray mt-0.5">Update your account password</p>
            </div>
            {!pwMode && (
              <Button variant="ghost" size="sm" onClick={() => { setPwMode(true); setError('') }}>
                <FiLock /> Change
              </Button>
            )}
          </div>

          {pwMode ? (
            <div className="space-y-4">
              <Input label="Current password" type="password" value={pwForm.currentPassword} onChange={e => setPwForm(f => ({ ...f, currentPassword: e.target.value }))} />
              <Input label="New password"      type="password" value={pwForm.newPassword}     onChange={e => setPwForm(f => ({ ...f, newPassword:     e.target.value }))} />
              <Input label="Confirm password"  type="password" value={pwForm.confirmPassword} onChange={e => setPwForm(f => ({ ...f, confirmPassword: e.target.value }))} />
              <div className="flex gap-3 pt-2">
                <Button variant="secondary" onClick={() => { setPwMode(false); setError('') }}>Cancel</Button>
                <Button loading={saving} onClick={changePassword}>Update password</Button>
              </div>
            </div>
          ) : (
            <p className="text-sm text-staynest-gray">••••••••••••</p>
          )}
        </div>
      </div>
    </Layout>
  )
}
