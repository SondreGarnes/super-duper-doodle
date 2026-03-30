export async function votePost(postId, type, token) {
  return fetch(`/api/posts/${postId}/${type}`, {
    method: 'POST',
    headers: { Authorization: `Bearer ${token}` },
  })
}

export async function deletePost(postId, token) {
  return fetch(`/api/posts/${postId}`, {
    method: 'DELETE',
    headers: { Authorization: `Bearer ${token}` },
  })
}

export function authHeaders(token) {
  return token ? { Authorization: `Bearer ${token}` } : {}
}
