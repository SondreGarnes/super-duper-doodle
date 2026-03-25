import { useState, useEffect } from 'react'
import { Link } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import './Blog.css'

function PostCard({ post, onLike, onDislike, onDelete }) {
  const { isAuthenticated, username, isAdmin } = useAuth()
  const isOwn = username === post.authorUsername
  const canDelete = isOwn || isAdmin

  return (
    <div className="post-card">
      <div className="post-card-header">
        <Link to={`/blog/${post.id}`} className="post-card-title">{post.title}</Link>
        <div className="post-meta">
          <Link to={`/profile/${post.authorUsername}`} className="post-author">
            {post.authorUsername}
          </Link>
          <span className="post-date">{new Date(post.createdAt).toLocaleDateString()}</span>
        </div>
      </div>

      <p className="post-excerpt">
        {post.content.length > 220 ? post.content.slice(0, 220) + '…' : post.content}
      </p>

      <div className="post-card-footer">
        <div className="post-actions">
          <button
            className={`vote-btn ${post.userVote === 'LIKE' ? 'active-like' : ''}`}
            onClick={() => isAuthenticated && onLike(post.id)}
            disabled={!isAuthenticated}
            title={isAuthenticated ? 'Like' : 'Log in to vote'}
          >
            ▲ {post.likeCount}
          </button>
          <button
            className={`vote-btn ${post.userVote === 'DISLIKE' ? 'active-dislike' : ''}`}
            onClick={() => isAuthenticated && onDislike(post.id)}
            disabled={!isAuthenticated}
            title={isAuthenticated ? 'Dislike' : 'Log in to vote'}
          >
            ▼ {post.dislikeCount}
          </button>
          <Link to={`/blog/${post.id}`} className="comment-count">
            💬 {post.commentCount}
          </Link>
        </div>
        <div className="post-card-actions">
          {isOwn && <span className="own-badge">Your post</span>}
          {canDelete && (
            <button className="delete-btn small" onClick={() => onDelete(post.id)}>
              Delete
            </button>
          )}
        </div>
      </div>
    </div>
  )
}

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
      const headers = token ? { Authorization: `Bearer ${token}` } : {}
      const res = await fetch('/api/posts', { headers })
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
      const res = await fetch(`/api/posts/${postId}/${type}`, {
        method: 'POST',
        headers: { Authorization: `Bearer ${token}` },
      })
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
    const res = await fetch(`/api/posts/${postId}`, {
      method: 'DELETE',
      headers: { Authorization: `Bearer ${token}` },
    })
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
