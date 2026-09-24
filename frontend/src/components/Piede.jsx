import { Link } from 'react-router-dom'

/** Un piede sobrio: il marchio, tre rimandi, e la riga che dice di cosa si tratta. */
export default function Piede() {
  return (
    <footer className="piede">
      <div className="dentro">
        <div>
          <p className="sigillo">Il Tempio dei Libri</p>
          <p className="nota">Biblioteca di quartiere · dal 1998</p>
        </div>

        <nav className="righe" aria-label="Collegamenti">
          <Link to="/catalogo">Catalogo</Link>
          <Link to="/prestiti">I miei prestiti</Link>
          <Link to="/preferiti">I miei libri</Link>
        </nav>

        <p className="nota">Le tessere si rilasciano in sede.</p>
      </div>
    </footer>
  )
}
