import { useState } from 'react'
import './App.css'

export default function App() {
  const [clicked, setClicked] = useState(false)

  return (
    <>
      <header className="site-header">
        <nav className="nav">
          <span className="nav-logo">Super Duper Doodle</span>
          <ul className="nav-links">
            <li><a href="#">Home</a></li>
            <li><a href="#about">About</a></li>
          </ul>
        </nav>
      </header>

      <main className="main">
        <section className="hero">
          <h1 className="hero-title">Super Duper Doodle</h1>
          <p className="hero-subtitle">A place to experiment with software architecture.</p>
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

      <footer className="site-footer">
        <p>&copy; 2026 Super Duper Doodle</p>
      </footer>
    </>
  )
}
