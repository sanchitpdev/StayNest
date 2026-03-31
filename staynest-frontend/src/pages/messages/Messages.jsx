import { useState, useEffect, useRef } from 'react'
import { useSearchParams } from 'react-router-dom'
import { FiSend, FiMessageSquare, FiSearch } from 'react-icons/fi'
import { messageApi } from '../../api'
import { PageLoader, Button, EmptyState } from '../../components/ui'
import Layout from '../../components/layout/Layout'
import { useAuth } from '../../context/AuthContext'
import toast from 'react-hot-toast'
import { format, isToday, isYesterday } from 'date-fns'

function formatMsgTime(ts) {
  if (!ts) return ''
  const d = new Date(ts)
  if (isToday(d))     return format(d, 'HH:mm')
  if (isYesterday(d)) return 'Yesterday'
  return format(d, 'MMM d')
}

export default function Messages() {
  const { user }       = useAuth()
  const [searchParams] = useSearchParams()
  const bottomRef      = useRef(null)

  const [conversations, setConversations] = useState([])
  const [activeConv,    setActiveConv]    = useState(null)
  const [messages,      setMessages]      = useState([])
  const [content,       setContent]       = useState('')
  const [loading,       setLoading]       = useState(true)
  const [msgLoading,    setMsgLoading]    = useState(false)
  const [sending,       setSending]       = useState(false)
  const [search,        setSearch]        = useState('')

  // Load conversations
  useEffect(() => {
    messageApi.getConversations()
      .then(r => { setConversations(r.data); setLoading(false) })
      .catch(() => setLoading(false))
  }, [])

  // Auto-open conversation from URL param
  useEffect(() => {
    const convId = searchParams.get('conv')
    if (convId && conversations.length) {
      const conv = conversations.find(c => c.conversationId === convId)
      if (conv) openConversation(conv)
    }
  }, [searchParams, conversations])

  const openConversation = async (conv) => {
    setActiveConv(conv)
    setMsgLoading(true)
    try {
      const r = await messageApi.getConversation(conv.conversationId)
      setMessages(r.data.messages || [])
    } catch { setMessages([]) }
    finally { setMsgLoading(false) }
  }

  // Scroll to bottom when messages change
  useEffect(() => {
    bottomRef.current?.scrollIntoView({ behavior: 'smooth' })
  }, [messages])

  const sendMessage = async (e) => {
    e.preventDefault()
    if (!content.trim() || !activeConv) return
    setSending(true)
    try {
      const r = await messageApi.sendMessage(activeConv.conversationId, {
        propertyId: activeConv.propertyId,
        content: content.trim(),
      })
      setMessages(m => [...m, r.data])
      setContent('')
    } catch (err) { toast.error(err.response?.data?.message || 'Failed to send') }
    finally { setSending(false) }
  }

  const filtered = conversations.filter(c =>
    c.guestName?.toLowerCase().includes(search.toLowerCase()) ||
    c.hostName?.toLowerCase().includes(search.toLowerCase()) ||
    c.propertyName?.toLowerCase().includes(search.toLowerCase())
  )

  const otherName = (conv) => conv.guestId === user?.userId ? conv.hostName : conv.guestName

  return (
    <Layout>
      <div className="max-w-6xl mx-auto px-4 sm:px-6 py-8">
        <h1 className="text-2xl font-bold text-staynest-dark mb-6">Messages</h1>

        <div className="border border-staynest-light rounded-2xl overflow-hidden bg-white" style={{ height: 'calc(100vh - 220px)', minHeight: '500px' }}>
          <div className="flex h-full">
            {/* Left: conversation list */}
            <div className="w-full sm:w-72 border-r border-staynest-light flex flex-col shrink-0">
              <div className="p-3 border-b border-staynest-light">
                <div className="relative">
                  <FiSearch className="absolute left-3 top-1/2 -translate-y-1/2 text-staynest-gray text-sm" />
                  <input value={search} onChange={e => setSearch(e.target.value)}
                    placeholder="Search conversations..."
                    className="w-full bg-staynest-bg rounded-xl pl-8 pr-3 py-2 text-sm outline-none text-staynest-dark placeholder-staynest-gray" />
                </div>
              </div>

              <div className="flex-1 overflow-y-auto">
                {loading ? (
                  <div className="flex justify-center py-10"><PageLoader /></div>
                ) : filtered.length === 0 ? (
                  <EmptyState icon={<FiMessageSquare />} title="No conversations" description="Start a conversation from a property page." />
                ) : (
                  filtered.map(conv => (
                    <button key={conv.conversationId} onClick={() => openConversation(conv)}
                      className={`w-full text-left px-4 py-3.5 flex items-start gap-3 hover:bg-staynest-bg transition-colors border-b border-staynest-light ${activeConv?.conversationId === conv.conversationId ? 'bg-primary/5' : ''}`}>
                      <div className="w-10 h-10 rounded-full bg-staynest-dark text-white flex items-center justify-center text-sm font-semibold shrink-0">
                        {otherName(conv)?.[0]}
                      </div>
                      <div className="flex-1 min-w-0">
                        <div className="flex items-center justify-between gap-1">
                          <p className="text-sm font-semibold text-staynest-dark truncate">{otherName(conv)}</p>
                          {conv.lastMessageAt && <span className="text-xs text-staynest-gray shrink-0">{formatMsgTime(conv.lastMessageAt)}</span>}
                        </div>
                        <p className="text-xs text-staynest-gray truncate mt-0.5">{conv.propertyName}</p>
                        {conv.unreadCount > 0 && (
                          <span className="inline-block mt-1 bg-primary text-white text-xs rounded-full px-1.5 py-0.5 font-medium">
                            {conv.unreadCount}
                          </span>
                        )}
                      </div>
                    </button>
                  ))
                )}
              </div>
            </div>

            {/* Right: chat area */}
            <div className="flex-1 flex flex-col min-w-0">
              {!activeConv ? (
                <div className="flex-1 flex items-center justify-center">
                  <EmptyState icon={<FiMessageSquare />} title="Select a conversation" description="Choose a conversation from the list to start messaging." />
                </div>
              ) : (
                <>
                  {/* Chat header */}
                  <div className="flex items-center gap-3 px-5 py-3.5 border-b border-staynest-light">
                    <div className="w-9 h-9 rounded-full bg-staynest-dark text-white flex items-center justify-center text-sm font-semibold">
                      {otherName(activeConv)?.[0]}
                    </div>
                    <div>
                      <p className="font-semibold text-sm text-staynest-dark">{otherName(activeConv)}</p>
                      <p className="text-xs text-staynest-gray">{activeConv.propertyName}</p>
                    </div>
                  </div>

                  {/* Messages */}
                  <div className="flex-1 overflow-y-auto px-5 py-4 space-y-3">
                    {msgLoading ? (
                      <div className="flex justify-center py-10"><PageLoader /></div>
                    ) : messages.length === 0 ? (
                      <div className="flex flex-col items-center justify-center h-full text-center">
                        <p className="text-3xl mb-3">👋</p>
                        <p className="text-staynest-gray text-sm">No messages yet. Say hello!</p>
                      </div>
                    ) : (
                      messages.map(msg => {
                        const isMe = msg.senderId === user?.userId
                        return (
                          <div key={msg.messageId} className={`flex ${isMe ? 'justify-end' : 'justify-start'}`}>
                            <div className={`max-w-[70%] px-4 py-2.5 rounded-2xl text-sm leading-relaxed ${isMe
                              ? 'bg-primary text-white rounded-br-sm'
                              : 'bg-staynest-bg text-staynest-dark rounded-bl-sm'}`}>
                              <p>{msg.content}</p>
                              <p className={`text-xs mt-1 ${isMe ? 'text-white/70' : 'text-staynest-gray'}`}>
                                {msg.createdAt && formatMsgTime(msg.createdAt)}
                                {isMe && msg.isRead && ' · Read'}
                              </p>
                            </div>
                          </div>
                        )
                      })
                    )}
                    <div ref={bottomRef} />
                  </div>

                  {/* Input */}
                  <form onSubmit={sendMessage} className="flex items-center gap-3 px-4 py-3 border-t border-staynest-light">
                    <input
                      value={content}
                      onChange={e => setContent(e.target.value)}
                      placeholder="Type a message..."
                      className="flex-1 bg-staynest-bg rounded-full px-4 py-2.5 text-sm outline-none text-staynest-dark placeholder-staynest-gray"
                      onKeyDown={e => { if (e.key === 'Enter' && !e.shiftKey) { e.preventDefault(); sendMessage(e) } }}
                    />
                    <button type="submit" disabled={!content.trim() || sending}
                      className="w-10 h-10 bg-primary text-white rounded-full flex items-center justify-center hover:bg-primary-hover transition-colors disabled:opacity-50 shrink-0">
                      <FiSend className="text-sm" />
                    </button>
                  </form>
                </>
              )}
            </div>
          </div>
        </div>
      </div>
    </Layout>
  )
}
