import { useState, useEffect } from 'react'
import { Link, useParams, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import PostCard from '../components/PostCard'
import { votePost, deletePost, authHeaders } from '../api/posts'
import './Blog.css'

export default function Profile() {
  const { username: profileUsername } = useParams()
  const { token, username: currentUsername, isAdmin, logout } = useAuth()
  const navigate = useNavigate()
  const [posts, setPosts] = useState([])
  const [loading, setLoading] = useState(true)

  const isOwnProfile = currentUsername === profileUsername
  const canDeleteAccount = isOwnProfile || isAdmin

  const fetchPosts = async () => {
    try {
      const res = await fetch(`/api/users/${profileUsername}/posts`, { headers: authHeaders(token) })
      if (!res.ok) { navigate('/'); return }
      const data = await res.json()
      setPosts(data.posts)
    } catch {
      navigate('/')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    fetchPosts()
  }, [profileUsername])

  const handleVote = async (postId, type) => {
    if (!token) return
    const res = await votePost(postId, type, token)
    if (res.ok) {
      const updated = await res.json()
      setPosts(posts.map(p => p.id === postId ? updated : p))
    }
  }

  const handleDeletePost = async (postId) => {
    if (!confirm('Delete this post?')) return
    const res = await deletePost(postId, token)
    if (res.ok) {
      setPosts(posts.filter(p => p.id !== postId))
    }
  }

  const handleDeleteAccount = async () => {
    const confirmMessage = isOwnProfile
      ? 'Are you sure you want to delete your account? This will also delete all your posts and cannot be undone.'
      : `Are you sure you want to delete ${profileUsername}'s account? This will also delete all their posts and cannot be undone.`
    if (!confirm(confirmMessage)) return

    const res = await fetch(`/api/users/${profileUsername}`, {
      method: 'DELETE',
      headers: { Authorization: `Bearer ${token}` },
    })
    if (res.ok) {
      if (isOwnProfile) {
        logout()
        navigate('/')
      } else {
        navigate('/blog')
      }
    }
  }

  return (
    <div className="blog-page">
      <div className="profile-header">
        <div>
          <h1 className="blog-title">{profileUsername}</h1>
          <p className="profile-subtitle">
            {isOwnProfile ? 'Your posts' : `Posts by ${profileUsername}`}
          </p>
        </div>
        <div className="profile-header-actions">
          {isOwnProfile && (
            <Link to="/" className="btn" style={{ textDecoration: 'none' }}>
              + New Post
            </Link>
          )}
          {canDeleteAccount && (
            <button className="delete-btn" onClick={handleDeleteAccount}>
              Delete Account
            </button>
          )}
        </div>
      </div>

      {loading ? (
        <p className="blog-empty">Loading…</p>
      ) : posts.length === 0 ? (
        <p className="blog-empty">
          {isOwnProfile ? "You haven't written any posts yet." : 'No posts yet.'}
        </p>
      ) : (
        <div className="post-list">
          {posts.map(post => (
            <PostCard
              key={post.id}
              post={post}
              onLike={(id) => handleVote(id, 'like')}
              onDislike={(id) => handleVote(id, 'dislike')}
              onDelete={handleDeletePost}
              showAuthor={false}
            />
          ))}
        </div>
      )}
    </div>
  )
}
