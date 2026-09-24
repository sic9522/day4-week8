import { useEffect, useState } from 'react'
import { Alert, Card } from 'react-bootstrap'
import { indirizzo, statoAPI } from '../services/api'

function Home() {
  const [stato, setStato] = useState(null)
  const [errore, setErrore] = useState(null)

  useEffect(() => {
    statoAPI
      .leggi()
      .then((risposta) => setStato(risposta.data))
      .catch((e) => setErrore(e.response?.data?.message ?? e.message))
  }, [])

  return (
    <>
      <h1 className="h3 fw-semibold">day4-week8</h1>
      <p className="text-secondary small">React + JavaScript, Spring Boot, PostgreSQL.</p>

      <Card className="mt-4">
        <Card.Body className="small">
          <div className="d-flex justify-content-between gap-3">
            <span className="text-secondary">API</span>
            <code className="text-truncate">{indirizzo}</code>
          </div>
          <div className="d-flex justify-content-between gap-3 mt-2">
            <span className="text-secondary">Database</span>
            <span className="font-monospace">{stato ? stato.database : '...'}</span>
          </div>
          <div className="d-flex justify-content-between gap-3 mt-2">
            <span className="text-secondary">Ora del server</span>
            <span className="font-monospace">{stato ? stato.ora : '...'}</span>
          </div>
        </Card.Body>
      </Card>

      {errore && <Alert variant="danger" className="mt-4 small">{errore}</Alert>}
    </>
  )
}

export default Home
