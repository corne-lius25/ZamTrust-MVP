import type { ReactNode } from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom'
import { useEffect } from 'react'
import { useAuth } from './lib/store'
import Landing from './pages/Landing'
import Login from './pages/Login'
import Register from './pages/Register'
import Dashboard from './pages/Dashboard'
import DocumentDetail from './pages/DocumentDetail'
import Signatures from './pages/Signatures'
import SignDocument from './pages/SignDocument';
import Upgrade from './pages/Upgrade';
import PublicVerify from './pages/PublicVerify'
import Pricing from './pages/Pricing';
import AppLayout from './layouts/AppLayout'

function RequireAuth({ children }: { children: ReactNode }) {
  const { token } = useAuth()
  if (!token) return <Navigate to="/login" replace />
  return children
}

export default function App() {
  const { hydrate } = useAuth()
  useEffect(() => { hydrate() }, [hydrate])

  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<Landing />} />
        <Route path="/login" element={<Login />} />
        <Route path="/register" element={<Register />} />
        <Route path="/v/:verificationId" element={<PublicVerify />} />
        <Route path="/pricing" element={<Pricing />} />

        <Route
          path="/app"
          element={
            <RequireAuth>
              <AppLayout />
            </RequireAuth>
          }
        >
          <Route index element={<Dashboard />} />
          <Route path="documents/:id" element={<DocumentDetail />} />
          <Route path="documents/:id/sign" element={<SignDocument />} />
          <Route path="upgrade" element={<Upgrade />} />
          <Route path="signatures" element={<Signatures />} />
        </Route>

        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
    </BrowserRouter>
  )
}
