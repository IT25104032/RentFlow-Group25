import { Navigate, Route, Routes } from 'react-router-dom'
import Module4Routes from './routes/Module4Routes'
import './App.css'

// Module 4 on its own. When the branches are merged, the shared
// AppRoutes / AppLayout (login + sidebar) take over from this file.
function App() {
  return (
    <div className="app-shell">
      <header className="app-header">
        <strong>RentFlow</strong>
        <span>Module 4 · Returns &amp; Settlement</span>
      </header>
      <main className="app-main">
        <Routes>
          {Module4Routes}
          <Route path="*" element={<Navigate to="/returns" replace />} />
        </Routes>
      </main>
    </div>
  )
}

export default App
