import { useState, useEffect } from 'react'
import './Poll.css'

const QUESTION = 'What do you think hurts most?'
const OPTION_A = '8 Ball full force to the head'
const OPTION_B = 'Or a Knee full force to the head'

export default function Poll() {
  const [voted, setVoted] = useState(() => localStorage.getItem('poll_voted') === 'true')
  const [results, setResults] = useState(null)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState(null)

  async function fetchResults() {
    const res = await fetch('/api/poll/results')
    if (!res.ok) throw new Error('Failed to fetch results')
    return res.json()
  }

  async function vote(option) {
    setLoading(true)
    setError(null)
    try {
      const res = await fetch('/api/poll/vote', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ option }),
      })
      if (!res.ok) throw new Error('Vote failed')
      const data = await fetchResults()
      setResults(data)
      localStorage.setItem('poll_voted', 'true')
      setVoted(true)
    } catch (e) {
      setError('Something went wrong. Try again.')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    if (voted) fetchResults().then(setResults).catch(() => {})
  }, [])

  const total = results ? results.optionA + results.optionB : 0
  const pctA = total > 0 ? Math.round((results.optionA / total) * 100) : 0
  const pctB = total > 0 ? Math.round((results.optionB / total) * 100) : 0

  return (
    <main className="poll-main">
      <div className="poll-card">
        <h1 className="poll-question">{QUESTION}</h1>

        {voted && !results ? (
          <p className="poll-total">Loading results…</p>
        ) : !voted ? (
          <div className="poll-options">
            <button
              className="poll-btn"
              onClick={() => vote('A')}
              disabled={loading}
            >
              {OPTION_A}
            </button>
            <button
              className="poll-btn"
              onClick={() => vote('B')}
              disabled={loading}
            >
              {OPTION_B}
            </button>
          </div>
        ) : results ? (
          <div className="poll-results">
            <p className="poll-total">{total} vote{total !== 1 ? 's' : ''} total</p>

            <div className="poll-result-item">
              <div className="poll-result-label">
                <span>{OPTION_A}</span>
                <span>{results.optionA} ({pctA}%)</span>
              </div>
              <div className="poll-bar-track">
                <div className="poll-bar" style={{ width: `${pctA}%` }} />
              </div>
            </div>

            <div className="poll-result-item">
              <div className="poll-result-label">
                <span>{OPTION_B}</span>
                <span>{results.optionB} ({pctB}%)</span>
              </div>
              <div className="poll-bar-track">
                <div className="poll-bar poll-bar-b" style={{ width: `${pctB}%` }} />
              </div>
            </div>

          </div>
        ) : null}

        {error && <p className="poll-error">{error}</p>}
      </div>
    </main>
  )
}
