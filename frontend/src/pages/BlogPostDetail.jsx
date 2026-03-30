import { useState, useEffect } from 'react'
import { Link, useParams, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import VoteButtons from '../components/VoteButtons'
import { votePost, deletePost, authHeaders } from '../api/posts'
import './Blog.css'

export default function BlogPostDetail() {
  const { id } = useParams()
  const { isAuthenticated, token, username, isAdmin } = useAuth()
  const navigate = useNavigate()
  const [post, setPost] = useState(null)
  const [comments, setComments] = useState([])
  const [loading, setLoading] = useState(true)
  const [commentText, setCommentText] = useState('')
  const [submittingComment, setSubmittingComment] = useState(false)

  const fetchPost = async () => {
    try {
      const headers = authHeaders(token)
      const [postRes, commentsRes] = await Promise.all([
        fetch(`/api/posts/${id}`, { headers }),
        fetch(`/api/posts/${id}/comments`, { headers }),
      ])
      if (!postRes.ok) { navigate('/blog'); return }
      setPost(await postRes.json())
      setComments(await commentsRes.json())
    } catch {
      navigate('/blog')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => { fetchPost() }, [id])

  const handleVote = async (type) => {
    if (!isAuthenticated) return
    const res = await votePost(id, type, token)
    if (res.status === 401) {
      alert('Vote was rejected status 401')
      return
    }
    if (res.status === 403) {
      alert('Vote was rejected (403). Please re-login if this keeps happening.')
      return
    }
    if (res.ok) setPost(await res.json())
  }

  const handleDelete = async () => {
    if (!confirm('Delete this post?')) return
    const res = await deletePost(id, token)
    if (res.ok) navigate('/blog')
  }

  const handleAddComment = async (e) => {
    e.preventDefault()
    if (!commentText.trim()) return
    setSubmittingComment(true)
    try {
      const res = await fetch(`/api/posts/${id}/comments`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          Authorization: `Bearer ${token}`,
        },
        body: JSON.stringify({ content: commentText }),
      })
      if (res.ok) {
        const newComment = await res.json()
        setComments([...comments, newComment])
        setCommentText('')
        setPost(prev => ({ ...prev, commentCount: prev.commentCount + 1 }))
      }
    } finally {
      setSubmittingComment(false)
    }
  }

  const handleDeleteComment = async (commentId) => {
    if (!confirm('Delete this comment?')) return
    const res = await fetch(`/api/posts/${id}/comments/${commentId}`, {
      method: 'DELETE',
      headers: { Authorization: `Bearer ${token}` },
    })
    if (res.ok) {
      setComments(comments.filter(c => c.id !== commentId))
      setPost(prev => ({ ...prev, commentCount: prev.commentCount - 1 }))
    }
  }

  if (loading) return <div className="blog-page"><p className="blog-empty">Loading…</p></div>
  if (!post) return null

  return (
    <div className="blog-page">
      <Link to="/blog" className="back-link">← Back to Blog</Link>

      <article className="post-detail">
        <h1 className="post-detail-title">{post.title}</h1>
        <div className="post-meta post-detail-meta">
          <Link to={`/profile/${post.authorUsername}`} className="post-author">
            {post.authorUsername}
          </Link>
          <span className="post-date">{new Date(post.createdAt).toLocaleDateString()}</span>
          {(username === post.authorUsername || isAdmin) && (
            <button className="delete-btn" onClick={handleDelete}>Delete</button>
          )}
        </div>

        <div className="post-detail-actions">
          <VoteButtons
            post={post}
            onLike={() => handleVote('like')}
            onDislike={() => handleVote('dislike')}
          />
        </div>

        <div className="post-detail-content">{post.content}</div>
      </article>

      <section className="comments-section">
        <h2 className="comments-title">Comments ({post.commentCount})</h2>

        {isAuthenticated && (
          <form onSubmit={handleAddComment} className="comment-form">
            <textarea
              value={commentText}
              onChange={(e) => setCommentText(e.target.value)}
              placeholder="Write a comment…"
              rows={3}
              required
            />
            <button type="submit" className="btn" disabled={submittingComment}>
              {submittingComment ? 'Posting…' : 'Post Comment'}
            </button>
          </form>
        )}

        {!isAuthenticated && (
          <p className="blog-empty small">
            <Link to="/login">Sign in</Link> to leave a comment
          </p>
        )}

        <div className="comment-list">
          {comments.map(comment => (
            <div key={comment.id} className="comment-card">
              <div className="comment-header">
                <Link to={`/profile/${comment.authorUsername}`} className="post-author">
                  {comment.authorUsername}
                </Link>
                <span className="post-date">{new Date(comment.createdAt).toLocaleDateString()}</span>
                {(username === comment.authorUsername || isAdmin) && (
                  <button
                    className="delete-btn small"
                    onClick={() => handleDeleteComment(comment.id)}
                  >
                    Delete
                  </button>
                )}
              </div>
              <p className="comment-content">{comment.content}</p>
            </div>
          ))}
          {comments.length === 0 && (
            <p className="blog-empty small">No comments yet.</p>
          )}
        </div>
      </section>
    </div>
  )
}
