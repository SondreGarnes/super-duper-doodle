import { Link } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import VoteButtons from './VoteButtons'

export default function PostCard({ post, onLike, onDislike, onDelete, showAuthor = true }) {
  const { username, isAdmin } = useAuth()
  const isOwn = username === post.authorUsername
  const canDelete = isOwn || isAdmin

  return (
    <div className="post-card">
      <div className="post-card-header">
        <Link to={`/blog/${post.id}`} className="post-card-title">{post.title}</Link>
        <div className="post-meta">
          {showAuthor && (
            <Link to={`/profile/${post.authorUsername}`} className="post-author">
              {post.authorUsername}
            </Link>
          )}
          <span className="post-date">{new Date(post.createdAt).toLocaleDateString()}</span>
        </div>
      </div>

      <p className="post-excerpt">
        {post.content.length > 220 ? post.content.slice(0, 220) + '…' : post.content}
      </p>

      <div className="post-card-footer">
        <div className="post-actions">
          <VoteButtons
            post={post}
            onLike={() => onLike(post.id)}
            onDislike={() => onDislike(post.id)}
          />
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
