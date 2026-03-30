import { useAuth } from '../context/AuthContext'

export default function VoteButtons({ post, onLike, onDislike }) {
  const { isAuthenticated } = useAuth()

  return (
    <>
      <button
        className={`vote-btn ${post.userVote === 'LIKE' ? 'active-like' : ''}`}
        onClick={() => isAuthenticated && onLike()}
        disabled={!isAuthenticated}
        title={isAuthenticated ? 'Like' : 'Log in to vote'}
      >
        ▲ {post.likeCount}
      </button>
      <button
        className={`vote-btn ${post.userVote === 'DISLIKE' ? 'active-dislike' : ''}`}
        onClick={() => isAuthenticated && onDislike()}
        disabled={!isAuthenticated}
        title={isAuthenticated ? 'Dislike' : 'Log in to vote'}
      >
        ▼ {post.dislikeCount}
      </button>
    </>
  )
}
