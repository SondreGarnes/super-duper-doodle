import { Routes, Route, Link, useNavigate } from 'react-router-dom'
import Poll from './Poll.jsx'
import Login from './pages/Login.jsx'
import Register from './pages/Register.jsx'
import Blog from './pages/Blog.jsx'
import BlogPostDetail from './pages/BlogPostDetail.jsx'
import Profile from './pages/Profile.jsx'
import Toby from './pages/Toby.jsx'
import { useAuth } from './context/AuthContext.jsx'
import './App.css'

function About() {
  return (
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
  )
}

function NavBar() {
  const { isAuthenticated, username, logout } = useAuth()
  const navigate = useNavigate()

  const handleLogout = () => {
    logout()
    navigate('/')
  }

  return (
    <header className="site-header">
      <nav className="nav">
        <Link to="/" className="nav-logo">Super Duper Doodle</Link>
        <ul className="nav-links">
          <li><Link to="/">Home</Link></li>
          <li><Link to="/poll">Poll</Link></li>
          <li><Link to="/about">About</Link></li>
          <li><Link to="/toby">Toby AI</Link></li>
          {isAuthenticated ? (
            <>
              <li> {username}</li>
              <li><Link to={`/profile/${username}`}>Profile</Link></li>
              <li>
                <button className="nav-logout" onClick={handleLogout}>Logout</button>
              </li>
            </>
          ) : (
            <>
              <li><Link to="/login">Login</Link></li>
              <li><Link to="/register" className="nav-register">Register</Link></li>
            </>
          )}
        </ul>
      </nav>
    </header>
  )
}

export default function App() {
  return (
    <>
      <NavBar />

      <Routes>
        <Route path="/" element={<Blog />} />
        <Route path="/about" element={<About />} />
        <Route path="/poll" element={<Poll />} />
        <Route path="/login" element={<Login />} />
        <Route path="/register" element={<Register />} />
        <Route path="/blog" element={<Blog />} />
        <Route path="/blog/:id" element={<BlogPostDetail />} />
        <Route path="/profile/:username" element={<Profile />} />
        <Route path="/toby" element={<Toby />} />
      </Routes>

      <footer className="site-footer">
        <p>&copy; 2026 Super Duper Doodle</p>
      </footer>
    </>
  )
}
