package taller

import org.scalatest.funsuite.AnyFunSuite
import org.junit.runner.RunWith
import org.scalatestplus.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class RobotCuadriculaTest extends AnyFunSuite {

  val r = new RobotCuadricula()
  import r._

  val cuadrado = Repetir(4, Secuencia(List(Avanzar(1), Girar(Derecha))))

  // Punto 1 -------------------------------------------------------------------

  test("girar: Norte a la derecha da Este") { assert(girar(Norte, Derecha) == Este) }
  test("girar: Norte a la izquierda da Oeste") { assert(girar(Norte, Izquierda) == Oeste) }
  test("girar: Oeste a la derecha da Norte") { assert(girar(Oeste, Derecha) == Norte) }
  test("girar: Sur a la izquierda da Este") { assert(girar(Sur, Izquierda) == Este) }

  test("girar: cuatro giros al mismo lado vuelven al principio") {
    val cuatro = List(Derecha, Derecha, Derecha, Derecha)
      .foldLeft(Norte: Direccion)((d, s) => girar(d, s))
    assert(cuatro == Norte)
  }

  test("girar: un giro y su contrario se cancelan") {
    assert(girar(girar(Este, Derecha), Izquierda) == Este)
  }

  // Punto 2 -------------------------------------------------------------------

  test("ejecutar: avanzar tres al norte") {
    assert(ejecutar(inicial, Avanzar(3)) == Estado(0, 3, Norte))
  }

  test("ejecutar: una secuencia de avance, giro y avance") {
    assert(ejecutar(inicial, Secuencia(List(Avanzar(2), Girar(Derecha), Avanzar(4)))) ==
      Estado(4, 2, Este))
  }

  test("ejecutar: el cuadrado vuelve al punto de partida") {
    assert(ejecutar(inicial, cuadrado) == Estado(0, 0, Norte))
  }

  test("ejecutar: avanzar con paso negativo retrocede") {
    assert(ejecutar(inicial, Avanzar(-2)) == Estado(0, -2, Norte))
  }

  test("ejecutar: repetir cero veces no hace nada") {
    assert(ejecutar(inicial, Repetir(0, Avanzar(5))) == inicial)
  }

  test("ejecutar: la secuencia vacía no hace nada") {
    assert(ejecutar(inicial, Secuencia(Nil)) == inicial)
  }

  test("ejecutar: avanzar al sur disminuye y") {
    assert(ejecutar(Estado(5, 5, Sur), Avanzar(2)) == Estado(5, 3, Sur))
  }

  test("ejecutar: avanzar al oeste disminuye x") {
    assert(ejecutar(Estado(5, 5, Oeste), Avanzar(2)) == Estado(3, 5, Oeste))
  }

  // Punto 3 -------------------------------------------------------------------

  test("cuantasBasicas: una orden básica cuenta uno") {
    assert(cuantasBasicas(Avanzar(3)) == 1)
  }

  test("cuantasBasicas: el cuadrado ejecuta ocho básicas") {
    assert(cuantasBasicas(cuadrado) == 8)
  }

  test("cuantasBasicas: repetir cero veces no ejecuta nada") {
    assert(cuantasBasicas(Repetir(0, cuadrado)) == 0)
  }

  test("cuantasBasicas: la secuencia vacía no ejecuta nada") {
    assert(cuantasBasicas(Secuencia(Nil)) == 0)
  }

  test("profundidad: una orden básica tiene profundidad 1") {
    assert(profundidad(Avanzar(3)) == 1)
  }

  test("profundidad: una secuencia de básicas tiene profundidad 2") {
    assert(profundidad(Secuencia(List(Avanzar(1), Girar(Derecha)))) == 2)
  }

  test("profundidad: el cuadrado tiene profundidad 3") {
    assert(profundidad(cuadrado) == 3)
  }

  test("profundidad: la secuencia vacía tiene profundidad 1") {
    assert(profundidad(Secuencia(Nil)) == 1)
  }

  test("profundidad: manda la rama más honda") {
    assert(profundidad(Secuencia(List(Avanzar(1), cuadrado))) == 4)
  }

  // Punto 4 -------------------------------------------------------------------

  test("componer: deja las dos órdenes en secuencia") {
    assert(componer(Avanzar(1), Girar(Derecha)) ==
      Secuencia(List(Avanzar(1), Girar(Derecha))))
  }

  test("componer: ejecutar la compuesta es ejecutar una y después la otra") {
    assert(ejecutar(inicial, componer(Avanzar(2), Girar(Derecha))) ==
      ejecutar(ejecutar(inicial, Avanzar(2)), Girar(Derecha)))
  }

  test("rutina: currificada, el cuadrado de cuatro vuelve al origen") {
    val cuadradoDe = rutina(1)(Derecha) _
    assert(ejecutar(inicial, cuadradoDe(4)) == Estado(0, 0, Norte))
  }

  test("rutina: con los mismos argumentos construye el cuadrado del enunciado") {
    assert(rutina(1)(Derecha)(4) == cuadrado)
  }

  test("aplicarA: ejecuta la misma orden sobre varios estados") {
    assert(aplicarA(List(inicial, Estado(5, 5, Sur)), Avanzar(2)) ==
      List(Estado(0, 2, Norte), Estado(5, 3, Sur)))
  }

  test("aplicarA: sobre la lista vacía no hay nada que hacer") {
    assert(aplicarA(Nil, Avanzar(2)) == Nil)
  }

  test("transformar: duplicar los avances deja los giros como estaban") {
    val doble = transformar(cuadrado, {
      case Avanzar(n) => Avanzar(2 * n)
      case otra => otra
    })
    assert(doble == Repetir(4, Secuencia(List(Avanzar(2), Girar(Derecha)))))
  }

  test("transformar: con la identidad no cambia nada") {
    assert(transformar(cuadrado, (o: Orden) => o) == cuadrado)
  }

  // Punto 5 -------------------------------------------------------------------

  test("simplificar: dos avances seguidos se juntan") {
    assert(simplificar(Secuencia(List(Avanzar(2), Avanzar(3)))) == Avanzar(5))
  }

  test("simplificar: repetir una vez es la orden misma") {
    assert(simplificar(Repetir(1, Avanzar(4))) == Avanzar(4))
  }

  test("simplificar: avanzar cero desaparece de la secuencia") {
    assert(simplificar(Secuencia(List(Avanzar(0), Girar(Derecha)))) == Girar(Derecha))
  }

  test("simplificar: junta solo los avances contiguos") {
    assert(simplificar(Secuencia(List(Avanzar(1), Avanzar(2), Girar(Derecha), Avanzar(3)))) ==
      Secuencia(List(Avanzar(3), Girar(Derecha), Avanzar(3))))
  }

  test("simplificar: repetir cero veces no hace nada") {
    assert(simplificar(Repetir(0, Avanzar(9))) == Secuencia(Nil))
  }

  test("simplificar: dos avances que se cancelan desaparecen") {
    assert(simplificar(Secuencia(List(Avanzar(2), Avanzar(-2)))) == Secuencia(Nil))
  }

  test("simplificar: es idempotente") {
    val ordenes = List(cuadrado,
      Secuencia(List(Avanzar(1), Avanzar(2), Girar(Derecha), Avanzar(3))),
      Repetir(1, Secuencia(List(Avanzar(0), Avanzar(4)))),
      Secuencia(List(Secuencia(Nil), Repetir(0, Avanzar(3)))))
    assert(ordenes.forall(o => simplificar(simplificar(o)) == simplificar(o)))
  }

  test("simplificar: no cambia el resultado de ejecutar") {
    val estados = List(inicial, Estado(3, -2, Este), Estado(-1, 4, Sur))
    val ordenes = List(cuadrado,
      Secuencia(List(Avanzar(2), Avanzar(3))),
      Secuencia(List(Avanzar(1), Avanzar(2), Girar(Derecha), Avanzar(3))),
      Repetir(1, Avanzar(4)),
      Repetir(0, Avanzar(9)),
      Secuencia(List(Avanzar(0), Girar(Derecha))),
      Secuencia(List(Avanzar(2), Avanzar(-2))),
      Repetir(3, Secuencia(List(Repetir(1, Avanzar(2)), Girar(Izquierda)))))
    assert(ordenes.forall(o =>
      estados.forall(e => ejecutar(e, o) == ejecutar(e, simplificar(o)))))
  }

  test("simplificar: el anidamiento interior también se reduce") {
    assert(simplificar(Repetir(3, Repetir(1, Avanzar(2)))) == Repetir(3, Avanzar(2)))
  }



/**
 * Casos de prueba propios del equipo (rúbrica: al menos 5 por punto, distintos
 * de los del enunciado y de los del material).
 */

// Punto 1 -------------------------------------------------------------------

// TODO: responsable del punto 1 (mínimo 5 casos propios).

// Punto 2 -------------------------------------------------------------------

// TODO: responsable del punto 2 (mínimo 5 casos propios).

  // Punto 3 -------------------------------------------------------------------

  test("propia cuantasBasicas: repetir con veces negativas no ejecuta nada") {
    assert(cuantasBasicas(Repetir(-3, Avanzar(1))) == 0)
  }

  test("propia cuantasBasicas: repeticiones anidadas se multiplican") {
    assert(cuantasBasicas(Repetir(3, Repetir(2, Avanzar(1)))) == 6)
  }

  test("propia cuantasBasicas: secuencia que contiene una repetición") {
    assert(cuantasBasicas(Secuencia(List(cuadrado, Avanzar(5)))) == 9)
  }

  test("propia profundidad: secuencias anidadas, con una secuencia vacía al fondo") {
    val o = Secuencia(List(Avanzar(1), Secuencia(List(Girar(Derecha), Secuencia(Nil)))))
    assert(profundidad(o) == 3)
  }

  test("propia profundidad: la rama más honda en el medio de la secuencia") {
    val o = Secuencia(List(Avanzar(1), Repetir(2, Repetir(2, Avanzar(1))), Girar(Izquierda)))
    assert(profundidad(o) == 4)
  }

  // Punto 4 -------------------------------------------------------------------

  test("propia componer: el orden cambia el estado final") {
    val primeroAvanza = ejecutar(inicial, componer(Avanzar(1), Girar(Derecha)))
    val primeroGira = ejecutar(inicial, componer(Girar(Derecha), Avanzar(1)))
    assert(primeroAvanza == Estado(0, 1, Este))
    assert(primeroGira == Estado(1, 0, Este))
  }

  test("propia componer: es asociativa respecto al estado final") {
    val a = Avanzar(3)
    val b = Girar(Izquierda)
    val c = Avanzar(2)
    val e = Estado(2, -1, Sur)
    val izq = ejecutar(e, componer(componer(a, b), c))
    val der = ejecutar(e, componer(a, componer(b, c)))
    assert(izq == der)
    assert(izq == Estado(4, -4, Este))
  }

  // TODO: responsable de rutina, aplicarA y transformar (mínimo 5 casos propios)
// para el punto 4 en conjunto).
  test("propia rutina: dos pasos a la izquierda, tres veces, arma la orden esperada") {
    assert(rutina(2)(Izquierda)(3) ==
      Repetir(3, Secuencia(List(Avanzar(2), Girar(Izquierda)))))
  }

  test("propia rutina: ejecutada deja media vuelta o un cuadrado de lado tres") {
    assert(ejecutar(inicial, rutina(2)(Derecha)(2)) == Estado(2, 2, Sur))
    assert(ejecutar(inicial, rutina(3)(Izquierda)(4)) == inicial)
  }

  test("propia aplicarA: una orden compuesta sobre estados que miran a lados distintos") {
    val estados = List(Estado(1, 1, Este), Estado(-2, 0, Oeste), Estado(0, 0, Sur))
    val o = Secuencia(List(Avanzar(3), Girar(Izquierda)))
    assert(aplicarA(estados, o) ==
      List(Estado(4, 1, Norte), Estado(-5, 0, Sur), Estado(0, -3, Este)))
  }

  // Punto 5 -------------------------------------------------------------------

// TODO: responsable del punto 5 (mínimo 5 casos propios).
}
