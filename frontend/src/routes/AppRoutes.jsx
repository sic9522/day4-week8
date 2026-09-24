import { BrowserRouter, Route, Routes } from 'react-router-dom'
import Layout from '../layouts/Layout'
import Home from '../pages/Home'

function AppRoutes() {
  return (
    <BrowserRouter>
      <Routes>
        <Route element={<Layout />}>
          <Route path="/" element={<Home />} />
        </Route>
      </Routes>
    </BrowserRouter>
  )
}

export default AppRoutes
