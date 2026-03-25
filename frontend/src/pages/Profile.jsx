import { useState, useEffect } from 'react'
import { Link, useParams, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import './Blog.css'

export default function Profile() {
  const { username: profileUsername } = useParams()
  const { token, username: currentUsername } = useAuth()
  const navigate = useNavigate()
  const [posts, setPosts] = useState([])
  const [loading, setLoading] = useState(true)

  const isOwnProfile = currentUsername === profileUsername

  const fetchPosts = async () => {
    try {
      const headers = token ? { Authorization: `Bearer ${token}` } : {}
      const res = await fetch(`/api/users/${profileUsername}/posts`, { headers })
      if (!res.ok) { navigate('/blog'); return }
      const data = await res.json()
      setPosts(data.posts)
    } catch {
      navigate('/blog')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    fetchPosts()
  }, [profileUsername])

  const handleVote = async (postId, type) => {
    if (!token) return
    const res = await fetch(`/api/posts/${postId}/${type}`, {
      method: 'POST',
      headers: { Authorization: `Bearer ${token}` },
    })
    if (res.ok) {
      const updated = await res.json()
      setPosts(posts.map(p => p.id === postId ? updated : p))
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
        {isOwnProfile && (
          <Link to="/blog" className="btn" style={{ textDecoration: 'none' }}>
            + New Post
          </Link>
        )}
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
            <div key={post.id} className="post-card">
              <div className="post-card-header">
                <Link to={`/blog/${post.id}`} className="post-card-title">{post.title}</Link>
                <div className="post-meta">
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
                    onClick={() => handleVote(post.id, 'like')}
                    disabled={!token}
                  >
                    ▲ {post.likeCount}
                  </button>
                  <button
                    className={`vote-btn ${post.userVote === 'DISLIKE' ? 'active-dislike' : ''}`}
                    onClick={() => handleVote(post.id, 'dislike')}
                    disabled={!token}
                  >
                    ▼ {post.dislikeCount}
                  </button>
                  <Link to={`/blog/${post.id}`} className="comment-count">
                    💬 {post.commentCount}
                  </Link>
                </div>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  )
}
