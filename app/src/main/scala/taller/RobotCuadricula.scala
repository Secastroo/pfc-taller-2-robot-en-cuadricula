package taller

/**
 * Taller 2 — órdenes para un robot en una cuadrícula infinita.
 *
 * El tipo `Orden` es recursivo: `Repetir` y `Secuencia` contienen otras
 * órdenes. Toda función que lo recorra lo es también, con un caso por cada
 * alternativa del tipo.
 *
 * Tal como está, el proyecto compila y las pruebas quedan en rojo.
 */
class RobotCuadricula {

  /** El robot empieza en el origen mirando al norte. */
  val inicial = Estado(0, 0, Norte)

  // Punto 1 -------------------------------------------------------------------

  /** La dirección que queda tras girar noventa grados hacia el sentido dado. */
  def girar(d: Direccion, s: Sentido): Direccion =
{
    (d,s) match {
      case (Norte,Izquierda) => Oeste
      case (Norte,Derecha) => Este
      case (Este,Izquierda) => Norte
      case (Este,Derecha) => Sur
      case (Sur,Izquierda) => Este
      case (Sur,Derecha) => Oeste
      case (Oeste,Izquierda) => Sur
      case (Oeste,Derecha) => Norte
    }
  }

  // Punto 2 -------------------------------------------------------------------

  /** El estado que resulta de aplicarle la orden al estado dado. */
  def ejecutar(e: Estado, o: Orden): Estado = {
    o match {
      case Avanzar(n) =>
        e.mira match {
          case Norte => Estado(e.x, e.y + n, e.mira)
          case Sur => Estado(e.x, e.y - n, e.mira)
          case Este => Estado(e.x + n, e.y, e.mira)
          case Oeste => Estado(e.x - n, e.y, e.mira)
        }
      case Girar(sentido) => Estado(e.x, e.y, girar(e.mira, sentido))
      case Repetir(veces, _) if veces <= 0 => e
      case Repetir(veces, orden) =>
        ejecutar(ejecutar(e, orden), Repetir(veces - 1, orden))
      case Secuencia(Nil) => e
      case Secuencia(primera :: resto) =>
        ejecutar(ejecutar(e, primera), Secuencia(resto))
    }
  }
  // Punto 3 -------------------------------------------------------------------

  /** Cuántos Avanzar y Girar llega a ejecutar la orden, contando repeticiones. */
  def cuantasBasicas(o: Orden): Int ={
    o match {
      case Avanzar(_) => 1
      case Girar(_) => 1
      case Repetir(veces, _) if veces <= 0 => 0
      case Repetir(veces, orden) => veces * cuantasBasicas(orden)
      case Secuencia(Nil) => 0
      case Secuencia(c::cs)=> cuantasBasicas(c)+cuantasBasicas(Secuencia(cs))
    }
  }

  /** Niveles de anidamiento: una básica tiene 1, una compuesta uno más que la más honda. */
  def profundidad(o: Orden): Int = {
    def mayorP(ordenes: List[Orden]): Int={
      ordenes match {
        case Nil => 0
        case c :: cs =>
          val actual = profundidad(c)
          val resto = mayorP(cs)
          if (actual>resto) actual
          else resto
      }
    }
    o match {
      case Avanzar(_) => 1
      case Girar(_) => 1
      case Repetir(_,orden) => 1+profundidad(orden)
      case Secuencia(Nil)=> 1
      case Secuencia(ordenes) => 1 + mayorP(ordenes)
    }
  }

  // Punto 4 -------------------------------------------------------------------

  /** La secuencia de las dos órdenes. */
  def componer(o1: Orden, o2: Orden): Orden = Secuencia(List(o1,o2))

  /** Currificada: fijados los pasos y el giro, queda una función de las veces. */
  def rutina(pasos: Int)(giro: Sentido)(veces: Int): Orden =
    Repetir(veces, Secuencia(List(Avanzar(pasos), Girar(giro))))

  /** La misma orden ejecutada sobre cada uno de los estados. */
  def aplicarA(estados: List[Estado], o: Orden): List[Estado] = estados
    match {
      case Nil => Nil
      case e :: resto => ejecutar(e, o) :: aplicarA(resto, o)
    }

  /** Reemplaza cada orden básica por f(orden), respetando la estructura de las compuestas. */
  def transformar(o: Orden, f: Orden => Orden): Orden = {
    // Transforma cada orden de la lista, conservando el orden de la lista.
    def transformarTodas(ordenes: List[Orden]): List[Orden] =
      ordenes match {
        case Nil => Nil
        case primera :: resto => transformar(primera, f) :: transformarTodas(resto)
      }

    o match {
      case Avanzar(_) => f(o)
      case Girar(_) => f(o)
      case Repetir(veces, orden) => Repetir(veces, transformar(orden, f))
      case Secuencia(ordenes) => Secuencia(transformarTodas(ordenes))
    }
  }


  // Punto 5 -------------------------------------------------------------------

  /** Una orden equivalente más corta, con las reglas del enunciado. */
  def simplificar(o: Orden): Orden = { /** Una orden equivalente más corta, con las reglas del enunciado. */

    // si hay dos Avanzar seguidos, los sumo en uno solo
    def juntar(lista: List[Orden]): List[Orden] = lista match {
      case Avanzar(a) :: Avanzar(b) :: resto => juntar(Avanzar(a + b) :: resto)
      case x :: resto => x :: juntar(resto)
      case Nil => Nil
    }

    // Hace una pasada de simplificación por toda la orden
    def unaPasada(orden: Orden): Orden = orden match {
      case Avanzar(0) => Secuencia(Nil)
      case Avanzar(n) => Avanzar(n)
      case Girar(s) => Girar(s)
      case Repetir(veces, _) if veces <= 0 => Secuencia(Nil)
      case Repetir(1, x) => unaPasada(x)
      case Repetir(veces, x) => unaPasada(x) match {
        case Secuencia(Nil) => Secuencia(Nil)
        case y => Repetir(veces, y)
      }
      case Secuencia(ordenes) => {
        val simples = ordenes.map(unaPasada)
        val sinNada = simples.filter(x => x != Secuencia(Nil))
        juntar(sinNada) match {
          case Nil => Secuencia(Nil)
          case List(x) => x
          case lista => Secuencia(lista)
        }
      }
    }

    // Repito las pasadas hasta que la orden ya no cambie
    def hastaQueNoCambie(orden: Orden): Orden = {
      val nueva = unaPasada(orden)
      nueva match {
        case `orden` => orden
        case _ => hastaQueNoCambie(nueva)
      }
    }

    hastaQueNoCambie(o)
  }
}
