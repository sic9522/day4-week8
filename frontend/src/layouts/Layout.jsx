import { Container } from 'react-bootstrap'
import { Outlet } from 'react-router-dom'

function Layout() {
  return (
    <Container className="py-5" style={{ maxWidth: '42rem' }}>
      <Outlet />
    </Container>
  )
}

export default Layout
