import { useState, useRef, useEffect } from 'react'
import './Toby.css'

function getTobyResponse() {
  const roll = Math.random()
  if (roll < 0.20) return null          // silence ~20%
  if (roll < 0.45) return `${Math.floor(Math.random() * 10) + 1}/10`  // rating ~25%
  return Math.random() < 0.5 ? 'Yes.' : 'No.'  // yes/no ~55%
}

export default function Toby() {
  const [messages, setMessages] = useState([
    { role: 'toby', text: 'Hello. I am Toby AI. Ask me anything.' }
  ])
  const [input, setInput] = useState('')
  const [thinking, setThinking] = useState(false)
  const bottomRef = useRef(null)

  useEffect(() => {
    bottomRef.current?.scrollIntoView({ behavior: 'smooth' })
  }, [messages, thinking])

  const send = () => {
    const text = input.trim()
    if (!text || thinking) return
    setInput('')
    setMessages(prev => [...prev, { role: 'user', text }])
    setThinking(true)

    setTimeout(() => {
      const response = getTobyResponse()
      setThinking(false)
      if (response !== null) {
        setMessages(prev => [...prev, { role: 'toby', text: response }])
      }
    }, 800 + Math.random() * 1200)
  }

  return (
    <div className="toby-page">
      <div className="toby-header">
        <h1>Toby AI</h1>
        <p>State-of-the-art intelligence. Probably.</p>
      </div>

      <div className="toby-chat">
        <div className="toby-messages">
          {messages.map((msg, i) => (
            <div key={i} className={`msg ${msg.role}`}>
              <div className="msg-sender">{msg.role === 'user' ? 'You' : 'Toby AI'}</div>
              <div className="msg-bubble">{msg.text}</div>
            </div>
          ))}
          {thinking && (
            <div className="msg toby thinking">
              <div className="msg-sender">Toby AI</div>
              <div className="msg-bubble">
                <span className="typing-dots">
                  <span>●</span><span>●</span><span>●</span>
                </span>
              </div>
            </div>
          )}
          <div ref={bottomRef} />
        </div>

        <div className="toby-input-row">
          <input
            className="toby-input"
            type="text"
            placeholder="Ask Toby anything..."
            value={input}
            onChange={e => setInput(e.target.value)}
            onKeyDown={e => e.key === 'Enter' && send()}
            autoComplete="off"
          />
          <button className="toby-send" onClick={send} disabled={thinking}>Send</button>
        </div>
      </div>

      <p className="toby-disclaimer">Toby AI may occasionally choose not to respond. This is a feature.</p>
    </div>
  )
}
