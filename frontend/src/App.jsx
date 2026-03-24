import { Routes, Route, Link } from 'react-router-dom'
import { useState } from 'react'
import Poll from './Poll.jsx'
import './App.css'

function Home() {
  const [clicked, setClicked] = useState(false)

  return (
    <>
      <main className="main">
        <section className="hero">
          <h1 className="hero-title">Super Duper Doodle</h1>
          <p className="hero-subtitle">A place to experiment with software architecture. And also maybe some drinking games</p>
          <button className="btn" onClick={() => setClicked(true)}>
            {clicked ? 'Work in Progress' : 'Get Started'}
          </button>
        </section>
      </main>

      <section className="about" id="about">
        <div className="about-inner">
          <h2 className="about-title">About Me</h2>
          <p className="about-text">
            Hey, I'm <strong>Sondre</strong> — a student software engineer with a passion for building things
            and a serious itch for entrepreneurship. I'm interested in the intersection of technology and
            business: writing clean code, shipping real products, and figuring out what actually makes ideas work
            in the real world.
          </p>
          <p className="about-text">
            Super Duper Doodle is my playground — a place to experiment, learn, and occasionally break things
            in the name of progress.
          </p>
        </div>
      </section>
    </>
  )
}

export default function App() {
  return (
    <>
      <header className="site-header">
        <nav className="nav">
          <span className="nav-logo">Super Duper Doodle</span>
          <ul className="nav-links">
            <li><Link to="/">Home</Link></li>
            <li><Link to="/poll">Poll</Link></li>
            <li><a href="#about">About</a></li>
          </ul>
        </nav>
      </header>

      <Routes>
        <Route path="/" element={<Home />} />
        <Route path="/poll" element={<Poll />} />
      </Routes>

      <footer className="site-footer">
        <p>&copy; 2026 Super Duper Doodle</p>
      </footer>
    </>
  )
}
