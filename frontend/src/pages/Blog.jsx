import { useState, useEffect } from 'react'
import { Link } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import PostCard from '../components/PostCard'
import { votePost, deletePost, authHeaders } from '../api/posts'
import './Blog.css'

export default function Blog() {
  const { isAuthenticated, token } = useAuth()
  const [posts, setPosts] = useState([])
  const [loading, setLoading] = useState(true)
  const [showForm, setShowForm] = useState(false)
  const [form, setForm] = useState({ title: '', content: '' })
  const [submitting, setSubmitting] = useState(false)
  const [formError, setFormError] = useState('')

  const fetchPosts = async () => {
    try {
      const res = await fetch('/api/posts', { headers: authHeaders(token) })
      const data = await res.json()
      setPosts(data)
    } catch {
      // silently fail
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => { fetchPosts() }, [])

  const handleVote = async (postId, type) => {
    try {
      const res = await votePost(postId, type, token)
      if (res.status === 401) {
        alert('Status 401')
        return
      }
      if (res.status === 403) {
        alert('Vote was rejected (403). Please re-login if this keeps happening.')
        return
      }
      if (res.ok) {
        const updated = await res.json()
        setPosts(posts.map(p => p.id === postId ? updated : p))
      }
    } catch {
      // silently fail
    }
  }

  const handleDelete = async (postId) => {
    if (!confirm('Delete this post?')) return
    const res = await deletePost(postId, token)
    if (res.ok) {
      setPosts(posts.filter(p => p.id !== postId))
    }
  }

  const handleSubmit = async (e) => {
    e.preventDefault()
    setFormError('')
    setSubmitting(true)
    try {
      const res = await fetch('/api/posts', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          Authorization: `Bearer ${token}`,
        },
        body: JSON.stringify(form),
      })
      const data = await res.json()
      if (!res.ok) {
        setFormError(data.error || 'Failed to create post')
        return
      }
      setPosts([data, ...posts])
      setForm({ title: '', content: '' })
      setShowForm(false)
    } catch {
      setFormError('Something went wrong.')
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <div className="blog-page">
      <div className="blog-header">
        <h1 className="blog-title">Blog</h1>
        {isAuthenticated ? (
          <button className="btn" onClick={() => setShowForm(!showForm)}>
            {showForm ? 'Cancel' : '+ New Post'}
          </button>
        ) : (
          <div className="blog-auth-hint">
            <Link to="/login">Sign in</Link> to write posts
          </div>
        )}
      </div>

      {showForm && (
        <div className="post-form-card">
          <h2 className="post-form-title">New Post</h2>
          {formError && <div className="auth-error">{formError}</div>}
          <form onSubmit={handleSubmit} className="auth-form">
            <div className="form-group">
              <label>Title</label>
              <input
                type="text"
                value={form.title}
                onChange={(e) => setForm({ ...form, title: e.target.value })}
                placeholder="What's your post about?"
                maxLength={200}
                required
              />
            </div>
            <div className="form-group">
              <label>Content</label>
              <textarea
                value={form.content}
                onChange={(e) => setForm({ ...form, content: e.target.value })}
                placeholder="Write something…"
                rows={6}
                required
              />
            </div>
            <button type="submit" className="btn auth-btn" disabled={submitting}>
              {submitting ? 'Publishing…' : 'Publish'}
            </button>
          </form>
        </div>
      )}

      {loading ? (
        <p className="blog-empty">Loading posts…</p>
      ) : posts.length === 0 ? (
        <p className="blog-empty">No posts yet. Be the first to write one!</p>
      ) : (
        <div className="post-list">
          {posts.map(post => (
            <PostCard
              key={post.id}
              post={post}
              onLike={(id) => handleVote(id, 'like')}
              onDislike={(id) => handleVote(id, 'dislike')}
              onDelete={handleDelete}
            />
          ))}
        </div>
      )}
    </div>
  )
}
