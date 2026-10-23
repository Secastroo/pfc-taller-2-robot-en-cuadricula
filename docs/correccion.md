# Informe de corrección: Taller 2, un robot en la cuadrícula

## Definición de referencia de `ejecutar`

Para justificar `componer` se usan los dos casos de `ejecutar` sobre
secuencias, tal como están implementados en `RobotCuadricula.scala`:

```scala
case Secuencia(Nil) => e
case Secuencia(primera :: resto) =>
  ejecutar(ejecutar(e, primera), Secuencia(resto))
```

En notación matemática:

```math
\text{ejecutar}(e, \text{Secuencia}(\text{Nil})) = e
```

```math
\text{ejecutar}(e, \text{Secuencia}(c :: cs)) = \text{ejecutar}(\text{ejecutar}(e, c), \text{Secuencia}(cs))
```

Es decir, una secuencia ejecuta sus órdenes de izquierda a derecha, pasando el
estado resultante de una a la siguiente.

## Punto 1: `girar`

### Especificación

Se numeran las direcciones en el sentido de las agujas del reloj con la función
$\iota : \text{Direccion} \to \{0, 1, 2, 3\}$:

```math
\iota(\text{Norte}) = 0 \quad \iota(\text{Este}) = 1 \quad \iota(\text{Sur}) = 2 \quad \iota(\text{Oeste}) = 3
```

Girar a la derecha avanza una posición en ese orden y girar a la izquierda
retrocede una (equivale a avanzar tres módulo 4). Sea $g$ la función que describe
el enunciado:

```math
\iota(g(d, \text{Derecha})) = (\iota(d) + 1) \bmod 4 \qquad \iota(g(d, \text{Izquierda})) = (\iota(d) + 3) \bmod 4
```

Se quiere demostrar:

```math
\forall d \in \text{Direccion}, \forall s \in \text{Sentido} : \text{girar}(d, s) == g(d, s)
```

**Método:** el dominio es finito, con $4 \times 2 = 8$ casos, así que se
demuestra por análisis exhaustivo de casos, uno por cada caso del `match`:

| $d$ | $\iota(d)$ | $g(d, \text{Derecha})$ | `girar(d, Derecha)` | $g(d, \text{Izquierda})$ | `girar(d, Izquierda)` |
|-----|------------|------------------------|---------------------|--------------------------|-----------------------|
| Norte | 0 | $\iota^{-1}(1) = $ Este | Este | $\iota^{-1}(3) = $ Oeste | Oeste |
| Este | 1 | $\iota^{-1}(2) = $ Sur | Sur | $\iota^{-1}(0) = $ Norte | Norte |
| Sur | 2 | $\iota^{-1}(3) = $ Oeste | Oeste | $\iota^{-1}(1) = $ Este | Este |
| Oeste | 3 | $\iota^{-1}(0) = $ Norte | Norte | $\iota^{-1}(2) = $ Sur | Sur |

En las 8 filas el resultado del programa coincide con $g$. Por lo tanto
$\forall d, \forall s : \text{girar}(d, s) == g(d, s)$.

**Totalidad y terminación:** `Direccion` y `Sentido` son `sealed`, así que las
8 parejas posibles están cubiertas y el `match` nunca falla. La función no es
recursiva: termina en un paso.

**Propiedades que se siguen de la especificación:**

```math
\iota(g(g(d, \text{Derecha}), \text{Izquierda})) = (\iota(d) + 1 + 3) \bmod 4 = \iota(d)
```

es decir, un giro y su contrario se cancelan. Y

```math
\iota(g^4(d, \text{Derecha})) = (\iota(d) + 4) \bmod 4 = \iota(d)
```

es decir, cuatro giros al mismo lado devuelven la dirección inicial. Estas dos
propiedades las verifican las pruebas de `girar`.

## Punto 2: `ejecutar`

### Especificación

Sea $E : \text{Estado} \times \text{Orden} \to \text{Estado}$ la función que
describe el enunciado. Se define con el desplazamiento de cada dirección:

```math
\delta(\text{Norte}) = (0, 1) \quad \delta(\text{Este}) = (1, 0) \quad \delta(\text{Sur}) = (0, -1) \quad \delta(\text{Oeste}) = (-1, 0)
```

```math
E((x, y, m), \text{Avanzar}(n)) = (x + n \ast \delta_x(m),\ y + n \ast \delta_y(m),\ m)
```

```math
E((x, y, m), \text{Girar}(s)) = (x, y, g(m, s))
```

donde $g$ es la función de giro del punto 1. Para las órdenes compuestas, sea
$f_o(e) = E(e, o)$ y sea $f^k$ la composición de $f$ consigo misma $k$ veces:

```math
f^0(e) = e \qquad f^{k+1}(e) = f^k(f(e))
```

```math
E(e, \text{Repetir}(v, o)) = f_o^{\max(v, 0)}(e)
```

```math
E(e, \text{Secuencia}(\text{Nil})) = e \qquad E(e, \text{Secuencia}(c :: cs)) = E(E(e, c), \text{Secuencia}(cs))
```

Se quiere demostrar:

```math
\forall e \in \text{Estado}, \forall o \in \text{Orden} : \text{ejecutar}(e, o) == E(e, o)
```

**Método:** inducción estructural sobre `Orden`, con dos casos base (`Avanzar` y
`Girar`) y dos casos de inducción (`Repetir` y `Secuencia`). La hipótesis de
inducción (HI) es que `ejecutar(e', o') == E(e', o')` para todo estado $e'$ y
toda orden $o'$ contenida en $o$.

**Caso base `Avanzar(n)`:** sea $e = (x, y, m)$. Según $m$, `ejecutar` devuelve:

| $m$ | `ejecutar` devuelve | $E$ da (con $\delta(m)$) |
|-----|---------------------|--------------------------|
| Norte | $(x, y + n, m)$ | $(x + n \ast 0,\ y + n \ast 1,\ m)$ |
| Sur | $(x, y - n, m)$ | $(x + n \ast 0,\ y + n \ast (-1),\ m)$ |
| Este | $(x + n, y, m)$ | $(x + n \ast 1,\ y + n \ast 0,\ m)$ |
| Oeste | $(x - n, y, m)$ | $(x + n \ast (-1),\ y + n \ast 0,\ m)$ |

En los cuatro casos los resultados coinciden, también cuando $n$ es negativo.
Como $m$ solo toma esos cuatro valores, el análisis es exhaustivo.

**Caso base `Girar(s)`:**

```math
\text{ejecutar}((x, y, m), \text{Girar}(s)) \rightarrow (x, y, \text{girar}(m, s)) = (x, y, g(m, s)) = E((x, y, m), \text{Girar}(s))
```

La igualdad $\text{girar}(m, s) = g(m, s)$ es el teorema del punto 1.

**Caso de inducción `Repetir(v, o)`:** la HI es que $\text{ejecutar}(e', o) == f_o(e')$ para todo $e'$.
Se demuestra por inducción sobre $k = \max(v, 0)$ que
$\text{ejecutar}(e, \text{Repetir}(v, o)) == f_o^{k}(e)$ para todo $e$:

- $v \leq 0$: la guarda `if veces <= 0` devuelve $e$, y $f_o^{0}(e) = e$.
- $v = k + 1 \geq 1$:

```math
\text{ejecutar}(e, \text{Repetir}(k+1, o)) \rightarrow \text{ejecutar}(\text{ejecutar}(e, o), \text{Repetir}(k, o))
```

Por la HI de la orden, $\text{ejecutar}(e, o) = f_o(e)$. Por la hipótesis
interna sobre $k$, aplicada al estado $f_o(e)$, el resultado es $f_o^{k}(f_o(e))$,
que por definición es $f_o^{k+1}(e) = E(e, \text{Repetir}(k+1, o))$.

**Caso de inducción `Secuencia(L)`:** la HI es que $\text{ejecutar}(e', o_i) == E(e', o_i)$
para cada $o_i$ de $L$ y todo $e'$. Se demuestra por inducción sobre la longitud
$k$ de $L$:

- $k = 0$: `ejecutar` devuelve $e$, y $E(e, \text{Secuencia}(\text{Nil})) = e$.
- $k \geq 1$, con $L = o_1 :: L'$:

```math
\text{ejecutar}(e, \text{Secuencia}(L)) \rightarrow \text{ejecutar}(\text{ejecutar}(e, o_1), \text{Secuencia}(L'))
```

Por la HI sobre $o_1$, $\text{ejecutar}(e, o_1) = E(e, o_1)$. Por la HI sobre la
lista más corta $L'$, el resultado es $E(E(e, o_1), \text{Secuencia}(L')) = E(e, \text{Secuencia}(L))$.

Concluimos por inducción estructural que
$\forall e, \forall o : \text{ejecutar}(e, o) == E(e, o)$.

**Terminación:** sea $m$ la medida $m(\text{Avanzar}(n)) = m(\text{Girar}(s)) = 1$,
$m(\text{Repetir}(v, o)) = 1 + \max(v, 0) \ast m(o)$ y
$m(\text{Secuencia}(L)) = 1 + \sum_{o_i \in L} m(o_i)$. Cada llamada recursiva
tiene un argumento con $m$ estrictamente menor:

- el cuerpo $o$ de `Repetir(v, o)` con $v \geq 1$ cumple $m(o) < 1 + v \ast m(o)$;
- `Repetir(v - 1, o)` cumple $1 + (v-1) \ast m(o) < 1 + v \ast m(o)$ porque $m(o) \geq 1$;
- la primera orden $c$ y la cola $cs$ de `Secuencia(c :: cs)` cumplen
  $m(c) < m(\text{Secuencia}(c :: cs))$ y $m(\text{Secuencia}(cs)) < m(\text{Secuencia}(c :: cs))$.

Como $m$ toma valores en los naturales, la recursión termina. Se supone que
la aritmética de `Int` no se desborda.

## Punto 3: `cuantasBasicas` y `profundidad`

### Especificación de `cuantasBasicas`

Sea $B : \text{Orden} \to \mathbb{N}$ la función que cuenta cuántos `Avanzar` y
`Girar` se ejecutan, contando repeticiones:

```math
B(\text{Avanzar}(n)) = 1 \qquad B(\text{Girar}(s)) = 1
```

```math
B(\text{Repetir}(v, o)) = \max(v, 0) \ast B(o)
```

```math
B(\text{Secuencia}(\text{List}(o_1, \ldots, o_k))) = \sum_{i=1}^{k} B(o_i)
```

Se quiere demostrar:

```math
\forall o \in \text{Orden} : \text{cuantasBasicas}(o) == B(o)
```

**Método:** inducción estructural sobre `Orden`. Hay dos casos base (`Avanzar`
y `Girar`) y dos casos de inducción (`Repetir` y `Secuencia`).

**Caso base `Avanzar(n)`:**

```math
\text{cuantasBasicas}(\text{Avanzar}(n)) \rightarrow 1 = B(\text{Avanzar}(n))
```

**Caso base `Girar(s)`:**

```math
\text{cuantasBasicas}(\text{Girar}(s)) \rightarrow 1 = B(\text{Girar}(s))
```

**Caso de inducción `Repetir(v, o)`:** la hipótesis de inducción (HI) es
$\text{cuantasBasicas}(o) == B(o)$. Hay dos posibilidades:

- Si $v \leq 0$, la guarda `if veces <= 0` devuelve $0$, y
  $B(\text{Repetir}(v, o)) = \max(v, 0) \ast B(o) = 0 \ast B(o) = 0$.
- Si $v > 0$, se devuelve $v \ast \text{cuantasBasicas}(o)$. Por la HI,
  esto es $v \ast B(o) = \max(v, 0) \ast B(o) = B(\text{Repetir}(v, o))$.

**Caso de inducción `Secuencia(L)`:** la HI es que
$\text{cuantasBasicas}(o_i) == B(o_i)$ para cada $o_i$ de $L$. Se demuestra
por inducción sobre la longitud $k$ de la lista:

- $k = 0$: $\text{cuantasBasicas}(\text{Secuencia}(\text{Nil})) \rightarrow 0 = B(\text{Secuencia}(\text{Nil}))$,
  porque la suma vacía es 0.
- $k \geq 1$, con $L = o_1 :: \text{List}(o_2, \ldots, o_k)$:

```math
\text{cuantasBasicas}(\text{Secuencia}(L)) \rightarrow \text{cuantasBasicas}(o_1) + \text{cuantasBasicas}(\text{Secuencia}(\text{List}(o_2, \ldots, o_k)))
```

Por la HI sobre $o_1$ y por la HI sobre la lista más corta, esto es
$B(o_1) + \sum_{i=2}^{k} B(o_i) = \sum_{i=1}^{k} B(o_i) = B(\text{Secuencia}(L))$.

Concluimos por inducción estructural que
$\forall o \in \text{Orden} : \text{cuantasBasicas}(o) == B(o)$.

**Terminación:** en cada llamada recursiva el argumento es una subexpresión
estricta de la orden original (el cuerpo del `Repetir`, un elemento de la
lista o la cola de la lista), y el tipo es finito, así que la recursión
termina.

### Especificación de `profundidad`

Sea $P : \text{Orden} \to \mathbb{N}$:

```math
P(\text{Avanzar}(n)) = P(\text{Girar}(s)) = 1 \qquad P(\text{Repetir}(v, o)) = 1 + P(o)
```

```math
P(\text{Secuencia}(\text{Nil})) = 1 \qquad P(\text{Secuencia}(\text{List}(o_1, \ldots, o_k))) = 1 + \max_{i} P(o_i), \; k \geq 1
```

Se quiere demostrar $\forall o \in \text{Orden} : \text{profundidad}(o) == P(o)$.

**Lema (función auxiliar `mayorP`).** Si la HI vale para cada elemento de una
lista $L = \text{List}(o_1, \ldots, o_k)$, entonces

```math
\text{mayorP}(L) = \begin{cases} 0 & k = 0 \\ \max_i P(o_i) & k \geq 1 \end{cases}
```

*Prueba por inducción sobre $k$:*

- $k = 0$: `mayorP(Nil)` devuelve $0$.
- $k \geq 1$: `mayorP(o_1 :: resto)` calcula `actual = profundidad(o_1)` y
  `resto = mayorP(List(o_2, ..., o_k))`, y devuelve el mayor de los dos. Por
  la HI, $\text{actual} = P(o_1)$. Por la hipótesis sobre la lista más corta,
  $\text{resto} = \max_{i \geq 2} P(o_i)$, que es $0$ si $k = 1$. Como
  $P(o_1) \geq 1 > 0$, el mayor de los dos es $\max_{i \geq 1} P(o_i)$. $\square$

**Caso base `Avanzar` y `Girar`:** devuelven $1 = P(\cdot)$.

**Caso de inducción `Repetir(v, o)`:** por la HI,
$\text{profundidad}(\text{Repetir}(v, o)) \rightarrow 1 + \text{profundidad}(o) = 1 + P(o) = P(\text{Repetir}(v, o))$.

**Caso de inducción `Secuencia(L)`:**

- Si $L = \text{Nil}$, se devuelve $1 = P(\text{Secuencia}(\text{Nil}))$.
- Si $L \neq \text{Nil}$, se devuelve $1 + \text{mayorP}(L)$, que por el lema y
  la HI es $1 + \max_i P(o_i) = P(\text{Secuencia}(L))$.

Concluimos por inducción estructural que
$\forall o \in \text{Orden} : \text{profundidad}(o) == P(o)$.

## Punto 4: `componer`, `rutina`, `aplicarA` y `transformar`

### `componer`

Se quiere demostrar que la orden compuesta equivale a ejecutar sus partes en
orden:

```math
\forall e \in \text{Estado},\ \forall o_1, o_2 \in \text{Orden} : \text{ejecutar}(e, \text{componer}(o_1, o_2)) == \text{ejecutar}(\text{ejecutar}(e, o_1), o_2)
```

`componer` no es recursiva, así que no necesita inducción. Basta con el
modelo de sustitución y la definición de `ejecutar` sobre secuencias:

```math
\text{ejecutar}(e, \text{componer}(o_1, o_2)) \rightarrow \text{ejecutar}(e, \text{Secuencia}(\text{List}(o_1, o_2)))
```

```math
\rightarrow \text{ejecutar}(\text{ejecutar}(e, o_1), \text{Secuencia}(\text{List}(o_2)))
```

```math
\rightarrow \text{ejecutar}(\text{ejecutar}(\text{ejecutar}(e, o_1), o_2), \text{Secuencia}(\text{Nil}))
```

```math
\rightarrow \text{ejecutar}(\text{ejecutar}(e, o_1), o_2)
```

El último paso usa $\text{ejecutar}(e', \text{Secuencia}(\text{Nil})) = e'$.

Por lo tanto, ejecutar `componer(o1, o2)` equivale a ejecutar `o1` y luego
`o2` sobre el estado resultante.

**Consecuencia sobre las otras medidas.** Por los resultados del punto 3:

```math
\text{cuantasBasicas}(\text{componer}(o_1, o_2)) = B(o_1) + B(o_2)
```

```math
\text{profundidad}(\text{componer}(o_1, o_2)) = 1 + \max(P(o_1), P(o_2))
```

**Ejemplo:** con $e = (0, 0, \text{Norte})$,
$o_1 = \text{Avanzar}(2)$ y $o_2 = \text{Girar}(\text{Derecha})$:
$\text{ejecutar}(e, o_1) = (0, 2, \text{Norte})$ y
$\text{ejecutar}((0, 2, \text{Norte}), o_2) = (0, 2, \text{Este})$, que es
el resultado de `ejecutar(e, componer(o1, o2))`.

### Dos casos de `ejecutar` que se usan aquí

Además de los casos de `Secuencia` que ya se dieron arriba, estas demostraciones usan los dos casos de `ejecutar` sobre `Repetir`, tal como están en `RobotCuadricula.scala`:

```scala
case Repetir(veces, _) if veces <= 0 => e
case Repetir(veces, orden) => ejecutar(ejecutar(e, orden), Repetir(veces - 1, orden))
```

Es decir, repetir con cero o menos veces deja el estado como estaba, y repetir $k + 1$ veces es ejecutar la orden una vez y luego repetirla $k$ veces sobre el estado que quedó:

```math
\text{ejecutar}(e, \text{Repetir}(k, o)) = e \qquad \text{si } k \leq 0
```

```math
\text{ejecutar}(e, \text{Repetir}(k + 1, o)) = \text{ejecutar}(\text{ejecutar}(e, o), \text{Repetir}(k, o)) \qquad \text{si } k + 1 > 0
```

### `rutina`

Sea $p$ un número de pasos, $g$ un sentido y $o_{p,g} = \text{Secuencia}(\text{List}(\text{Avanzar}(p), \text{Girar}(g)))$ el par avanzar-girar. Por la definición del programa, $\text{rutina}(p)(g)(k) = \text{Repetir}(k, o_{p,g})$.

Llamemos $\text{paso}(e)$ al estado que queda de hacer una vez ese par desde $e$, es decir $\text{paso}(e) = \text{ejecutar}(e, o_{p,g})$. Por lo que se mostró para `componer`, ejecutar una secuencia de dos órdenes es ejecutar la primera y después la segunda sobre el estado que queda:

```math
\text{paso}(e) = \text{ejecutar}(\text{ejecutar}(e, \text{Avanzar}(p)), \text{Girar}(g))
```

Y sea $\text{paso}^k$ aplicar $\text{paso}$ $k$ veces, con $\text{paso}^0(e) = e$ y $\text{paso}^{k+1}(e) = \text{paso}^k(\text{paso}(e))$.

Se quiere demostrar que la rutina compuesta equivale a ejecutar sus partes, el par avanzar-girar, tantas veces como se pida:

```math
\forall k \in \mathbb{N},\ \forall e \in \text{Estado} : \text{ejecutar}(e, \text{rutina}(p)(g)(k)) == \text{paso}^k(e)
```

La demostración es por inducción sobre $k$. Es importante que la afirmación valga para todo estado $e$, porque en el paso inductivo la hipótesis se va a usar sobre un estado distinto del de partida.

Caso base: $k = 0$.

```math
\text{ejecutar}(e, \text{rutina}(p)(g)(0)) \rightarrow \text{ejecutar}(e, \text{Repetir}(0, o_{p,g})) \rightarrow e
```

El último paso es el primer caso de `Repetir`, porque $0 \leq 0$. Por otro lado, $\text{paso}^0(e) = e$. Entonces los dos lados coinciden.

Caso de inducción: $k + 1$, con $k \geq 0$. La hipótesis de inducción (HI) dice que $\forall e' : \text{ejecutar}(e', \text{rutina}(p)(g)(k)) == \text{paso}^k(e')$. Como $k + 1 > 0$, se aplica el segundo caso de `Repetir`:

```math
\text{ejecutar}(e, \text{Repetir}(k + 1, o_{p,g})) \rightarrow \text{ejecutar}(\text{ejecutar}(e, o_{p,g}), \text{Repetir}(k, o_{p,g}))
```

```math
= \text{ejecutar}(\text{paso}(e), \text{rutina}(p)(g)(k)) \overset{\text{HI}}{=} \text{paso}^k(\text{paso}(e)) = \text{paso}^{k+1}(e)
```

En la HI se tomó $e' = \text{paso}(e)$. Concluimos por inducción que la afirmación vale para todo $k \in \mathbb{N}$. Si $k$ es negativo se aplica el primer caso de `Repetir` y el estado no cambia, que es lo mismo que $k = 0$.

La currificación no cambia el valor: $\text{rutina}(p)(g)$ es la función $k \mapsto \text{Repetir}(k, o_{p,g})$, y aplicarla a $k$ da exactamente $\text{rutina}(p)(g)(k)$.

Consecuencia: el cuadrado vuelve al punto de partida. Para cualquier $p$, cualquier $g$ y cualquier estado $e = (x, y, d)$, ejecutar `rutina(p)(g)(4)` devuelve $e$. Se ve así. Sea $u(d)$ el vector unitario de la dirección $d$:

```math
u(\text{Norte}) = (0, 1) \quad u(\text{Este}) = (1, 0) \quad u(\text{Sur}) = (0, -1) \quad u(\text{Oeste}) = (-1, 0)
```

Entonces $\text{paso}(x, y, d) = (x + p \cdot u_x(d),\ y + p \cdot u_y(d),\ \text{girar}(d, g))$. Sean $d_0 = d$ y $d_{i+1} = \text{girar}(d_i, g)$. Girar siempre al mismo lado recorre las cuatro direcciones una vez (Norte, Este, Sur, Oeste si es a la derecha, y al revés si es a la izquierda), así que $d_0, d_1, d_2, d_3$ son las cuatro direcciones, cada una una sola vez, y $\text{girar}(d_3, g) = d_0$. Por lo tanto, después de cuatro pasos la dirección es la misma y la posición cambió en

```math
p \cdot \big(u(d_0) + u(d_1) + u(d_2) + u(d_3)\big) = p \cdot \big((0,1) + (1,0) + (0,-1) + (-1,0)\big) = (0, 0)
```

Entonces $\text{paso}^4(e) = e$, y por lo demostrado, $\text{ejecutar}(e, \text{rutina}(p)(g)(4)) == e$. Esto explica por qué los cuatro avances con un giro entre cada uno dibujan un cuadrado, sea de lado 1, de lado 3 o del que sea (suponiendo que no se desborda un `Int`).

### `aplicarA`

Sea $[e_1, \ldots, e_n]$ una lista de estados y $o$ una orden. La especificación es la lista que resulta de ejecutar $o$ sobre cada estado, conservando el orden:

```math
\text{AplicarA}([e_1, \ldots, e_n], o) = [\text{ejecutar}(e_1, o), \ldots, \text{ejecutar}(e_n, o)]
```

Se quiere demostrar que $\forall es \in \text{List}[\text{Estado}],\ \forall o \in \text{Orden} : \text{aplicarA}(es, o) == \text{AplicarA}(es, o)$. Como las listas están definidas recursivamente (o son `Nil`, o son un estado seguido de otra lista), se usa inducción estructural sobre la lista.

Caso base: $es = \text{Nil}$.

```math
\text{aplicarA}(\text{Nil}, o) \rightarrow \text{Nil}
```

Por otro lado, $\text{AplicarA}(\text{Nil}, o)$ es la lista vacía, porque no hay estados que ejecutar. Entonces $\text{aplicarA}(\text{Nil}, o) == \text{AplicarA}(\text{Nil}, o)$.

Caso de inducción: $es = e :: r$, con $r = [r_1, \ldots, r_m]$. La HI dice que $\text{aplicarA}(r, o) == [\text{ejecutar}(r_1, o), \ldots, \text{ejecutar}(r_m, o)]$.

```math
\text{aplicarA}(e :: r, o) \rightarrow \text{ejecutar}(e, o) :: \text{aplicarA}(r, o)
```

```math
\overset{\text{HI}}{=} \text{ejecutar}(e, o) :: [\text{ejecutar}(r_1, o), \ldots, \text{ejecutar}(r_m, o)] = \text{AplicarA}(e :: r, o)
```

Concluimos por inducción que $\forall es,\ \forall o : \text{aplicarA}(es, o) == \text{AplicarA}(es, o)$.

De ahí se sacan dos cosas que usan las pruebas: el resultado tiene el mismo largo que la entrada, y el resultado en la posición $i$ solo depende del estado $e_i$ y de la orden, no de los otros estados.

### `transformar`

Para no repetir, se escribe $T(o)$ en lugar de $\text{transformar}(o, f)$ y $T^*(l)$ para la función auxiliar `transformarTodas` sobre una lista $l$, que transforma cada orden de la lista en el mismo orden. Por la definición del programa:

```math
T(\text{Avanzar}(n)) = f(\text{Avanzar}(n)) \qquad T(\text{Girar}(s)) = f(\text{Girar}(s))
```

```math
T(\text{Repetir}(k, o)) = \text{Repetir}(k, T(o)) \qquad T(\text{Secuencia}(l)) = \text{Secuencia}(T^*(l))
```

```math
T^*(\text{Nil}) = \text{Nil} \qquad T^*(x :: r) = T(x) :: T^*(r)
```

Lo que dice el enunciado es que `transformar` reemplaza cada orden básica por el resultado de aplicarle $f$ y respeta la estructura de las compuestas. La estructura se ve en las ecuaciones: las veces de un `Repetir` no cambian, y una `Secuencia` conserva sus elementos en el mismo orden. Faltan dos cosas por argumentar: que con la identidad no cambia nada, y qué pasa con el resultado de ejecutar.

#### Con la identidad no cambia nada

Se quiere demostrar que $\forall o \in \text{Orden} : \text{transformar}(o, \text{id}) == o$, por inducción estructural sobre $o$. Recordemos que en esta inducción las órdenes de una lista de `Secuencia` también cuentan como órdenes más pequeñas.

Caso base: $o = \text{Avanzar}(n)$ o $o = \text{Girar}(s)$. $T(o) = \text{id}(o) = o$.

Caso de inducción: $o = \text{Repetir}(k, o')$. La HI dice que $T(o') == o'$, así que $T(o) = \text{Repetir}(k, T(o')) = \text{Repetir}(k, o') = o$.

Caso de inducción: $o = \text{Secuencia}(l)$. Hay que ver que $T^*(l) == l$, por inducción sobre la lista. Si $l = \text{Nil}$, $T^*(\text{Nil}) = \text{Nil}$. Si $l = x :: r$, $T^*(x :: r) = T(x) :: T^*(r)$, y por la HI sobre la orden $x$ y sobre la lista $r$ eso es $x :: r$. Entonces $T(\text{Secuencia}(l)) = \text{Secuencia}(l)$.

#### Qué pasa con el resultado de ejecutar

Se dice que dos órdenes son equivalentes, y se escribe $o_1 \equiv o_2$, si dejan el mismo estado desde cualquier estado de partida:

```math
o_1 \equiv o_2 \iff \forall e \in \text{Estado} : \text{ejecutar}(e, o_1) == \text{ejecutar}(e, o_2)
```

Se quiere demostrar lo siguiente. Si $f$ cumple la hipótesis H, que para toda orden básica $b$ se tiene $f(b) \equiv b$, entonces transformar con $f$ no cambia lo que hace la orden:

```math
\forall o \in \text{Orden} : T(o) \equiv o
```

La demostración es por inducción estructural sobre $o$.

Caso base: $o = \text{Avanzar}(n)$ o $o = \text{Girar}(s)$. $T(o) = f(o) \equiv o$ por la hipótesis H.

Caso de inducción: $o = \text{Repetir}(k, o')$. La HI dice que $T(o') \equiv o'$. Hay que ver que $\text{Repetir}(k, T(o')) \equiv \text{Repetir}(k, o')$, para lo cual se hace una inducción interna sobre $k$, con la afirmación para todo estado $e$:

* Si $k \leq 0$, las dos órdenes dejan el estado como estaba: $\text{ejecutar}(e, \text{Repetir}(k, T(o'))) = e = \text{ejecutar}(e, \text{Repetir}(k, o'))$.
* Si es $k + 1$, con la hipótesis interna (HIk) de que el resultado vale para $k$ y todo estado:

```math
\text{ejecutar}(e, \text{Repetir}(k+1, T(o'))) = \text{ejecutar}(\text{ejecutar}(e, T(o')), \text{Repetir}(k, T(o')))
```

```math
\overset{\text{HI}}{=} \text{ejecutar}(\text{ejecutar}(e, o'), \text{Repetir}(k, T(o'))) \overset{\text{HIk}}{=} \text{ejecutar}(\text{ejecutar}(e, o'), \text{Repetir}(k, o')) = \text{ejecutar}(e, \text{Repetir}(k+1, o'))
```

Caso de inducción: $o = \text{Secuencia}(l)$. La HI dice que $T(x) \equiv x$ para cada orden $x$ de $l$. Hay que ver que $\text{Secuencia}(T^*(l)) \equiv \text{Secuencia}(l)$, por inducción sobre la lista, para todo estado $e$:

* Si $l = \text{Nil}$, las dos secuencias son vacías y dejan a $e$ como estaba.
* Si $l = x :: r$, con la hipótesis interna (HIl) de que el resultado vale para $r$ y todo estado:

```math
\text{ejecutar}(e, \text{Secuencia}(T(x) :: T^*(r))) = \text{ejecutar}(\text{ejecutar}(e, T(x)), \text{Secuencia}(T^*(r)))
```

```math
\overset{\text{HI}}{=} \text{ejecutar}(\text{ejecutar}(e, x), \text{Secuencia}(T^*(r))) \overset{\text{HIl}}{=} \text{ejecutar}(\text{ejecutar}(e, x), \text{Secuencia}(r)) = \text{ejecutar}(e, \text{Secuencia}(x :: r))
```

Concluimos por inducción que $T(o) \equiv o$ para toda orden $o$. 

Y un ejemplo en que H no se cumple: duplicar los avances. $\text{Avanzar}(2n) \not\equiv \text{Avanzar}(n)$ cuando $n \neq 0$, y por eso el resultado sí cambia. Desde $\text{inicial}$, la orden $\text{Secuencia}(\text{List}(\text{Avanzar}(2), \text{Girar}(\text{Derecha}), \text{Avanzar}(4)))$ deja $(4, 2, \text{Este})$ y su versión con los avances duplicados deja $(8, 4, \text{Este})$. La hipótesis H es lo que separa a las transformaciones que respetan la ejecución de las que no.

Por último, las cuatro funciones del punto terminan siempre, porque cada llamado recursivo trabaja sobre una parte estrictamente más pequeña de la entrada: `aplicarA` sobre la cola de la lista, y `transformar` sobre la orden de adentro de un `Repetir` o sobre un elemento de una `Secuencia`. `rutina` y `componer` no son recursivas.

## Punto 5 Informe de corrección: `simplificar`

## Notación

Un estado es una terna $e = (x, y, d)$ con $d \in \{N, E, S, O\}$ y vectores unitarios

$$\delta(N) = (0, 1), \quad \delta(E) = (1, 0), \quad \delta(S) = (0, -1), \quad \delta(O) = (-1, 0)$$

Se escribe $\mathcal{E}(o, e)$ para el estado que devuelve `ejecutar(e, o)`, y $o_1 \equiv o_2$ cuando

$$\forall e.\ \mathcal{E}(o_1, e) = \mathcal{E}(o_2, e)$$

Las definiciones de `ejecutar` que se usan son:

$$\mathcal{E}(\texttt{Avanzar}(n), (x, y, d)) = (x + n\,\delta_x(d),\ y + n\,\delta_y(d),\ d)$$

$$\mathcal{E}(\texttt{Repetir}(v, o), e) = \begin{cases} e & \text{si } v \le 0 \\ \mathcal{E}(\texttt{Repetir}(v-1, o), \mathcal{E}(o, e)) & \text{si } v > 0 \end{cases}$$

$$\mathcal{E}(\texttt{Secuencia}(\text{Nil}), e) = e, \qquad \mathcal{E}(\texttt{Secuencia}(o :: l), e) = \mathcal{E}(\texttt{Secuencia}(l), \mathcal{E}(o, e))$$

Se escribe $\text{nada} = \texttt{Secuencia}(\text{Nil})$.

## Teorema

Para toda orden $o$ y todo estado $e$:

$$\mathcal{E}(\texttt{simplificar}(o), e) = \mathcal{E}(o, e)$$

## Lemas previos

**Lema 1 (secuencia de dos listas).** Para listas $l_1$ y $l_2$:

$$\mathcal{E}(\texttt{Secuencia}(l_1 \mathbin{+\!\!+} l_2), e) = \mathcal{E}(\texttt{Secuencia}(l_2), \mathcal{E}(\texttt{Secuencia}(l_1), e))$$

*Demostración.* Inducción sobre $l_1$.

- Base, $l_1 = \text{Nil}$: ambos lados valen $\mathcal{E}(\texttt{Secuencia}(l_2), e)$.
- Paso, $l_1 = o :: l_1'$: por definición el lado izquierdo es $\mathcal{E}(\texttt{Secuencia}(l_1' \mathbin{+\!\!+} l_2), \mathcal{E}(o, e))$. Por hipótesis de inducción esto es $\mathcal{E}(\texttt{Secuencia}(l_2), \mathcal{E}(\texttt{Secuencia}(l_1'), \mathcal{E}(o, e)))$, y como $\mathcal{E}(\texttt{Secuencia}(l_1'), \mathcal{E}(o, e)) = \mathcal{E}(\texttt{Secuencia}(o :: l_1'), e)$, queda demostrado. $\blacksquare$

**Lema 2.** $\mathcal{E}(\texttt{Avanzar}(n), (x, y, d))$ conserva la dirección $d$. Se sigue de la definición.

## Equivalencia de cada regla

**Regla 1a: $\texttt{Avanzar}(0) \equiv \text{nada}$.** Con $n = 0$ el desplazamiento es $(0, 0)$, así que $\mathcal{E}(\texttt{Avanzar}(0), e) = e = \mathcal{E}(\text{nada}, e)$.

**Regla 1b: $\texttt{Repetir}(v, o) \equiv \text{nada}$ si $v \le 0$.** Por definición, $\mathcal{E}(\texttt{Repetir}(v, o), e) = e$ cuando $v \le 0$.

**Regla 2: $\texttt{Avanzar}(a), \texttt{Avanzar}(b)$ seguidos $\equiv \texttt{Avanzar}(a + b)$.** Sea $e = (x, y, d)$. Tras $\texttt{Avanzar}(a)$ el estado es $(x + a\delta_x, y + a\delta_y, d)$ y la dirección sigue siendo $d$ (Lema 2). Tras $\texttt{Avanzar}(b)$ es

$$(x + (a+b)\delta_x,\ y + (a+b)\delta_y,\ d) = \mathcal{E}(\texttt{Avanzar}(a+b), e)$$

**Regla 3: $\texttt{Repetir}(1, o) \equiv o$.** Por definición, $\mathcal{E}(\texttt{Repetir}(1, o), e) = \mathcal{E}(\texttt{Repetir}(0, o), \mathcal{E}(o, e)) = \mathcal{E}(o, e)$, porque repetir cero veces no hace nada.

**Regla 4: $\texttt{Secuencia}(o :: \text{Nil}) \equiv o$.** Por definición, $\mathcal{E}(\texttt{Secuencia}(o :: \text{Nil}), e) = \mathcal{E}(\text{nada}, \mathcal{E}(o, e)) = \mathcal{E}(o, e)$.

**Regla 5: quitar una orden neutra.** Si $n \equiv \text{nada}$, entonces

$$\texttt{Secuencia}(l_1 \mathbin{+\!\!+} (n :: l_2)) \equiv \texttt{Secuencia}(l_1 \mathbin{+\!\!+} l_2)$$

Por el Lema 1, ejecutar $l_1$, luego $n$ y luego $l_2$ da el mismo estado que ejecutar $l_1$ y luego $l_2$, porque $n$ deja el estado intacto.

**Caso auxiliar: $\texttt{Repetir}(v, n) \equiv \text{nada}$ si $n \equiv \text{nada}$.** Inducción sobre $v$. Si $v \le 0$ es la regla 1b. Si $v > 0$, $\mathcal{E}(\texttt{Repetir}(v, n), e) = \mathcal{E}(\texttt{Repetir}(v-1, n), \mathcal{E}(n, e)) = \mathcal{E}(\texttt{Repetir}(v-1, n), e) = e$ por hipótesis de inducción. Esto justifica devolver $\text{nada}$ cuando el cuerpo de un `Repetir` se simplifica a `Secuencia(Nil)`.

## La función `juntar`

**Lema 3.** $\texttt{Secuencia}(\texttt{juntar}(l)) \equiv \texttt{Secuencia}(l)$.

*Demostración.* Inducción sobre la longitud de $l$. La rama que sustituye $\texttt{Avanzar}(a) :: \texttt{Avanzar}(b) :: \text{resto}$ por $\texttt{Avanzar}(a+b) :: \text{resto}$ usa la regla 2 y el Lema 1. La rama $x :: \text{resto}$ conserva $x$ y trata el resto por hipótesis de inducción. La rama $\text{Nil}$ es trivial. $\blacksquare$

## Una pasada conserva el resultado

**Lema 4.** Para toda orden $o$: $\texttt{unaPasada}(o) \equiv o$.

*Demostración.* Inducción estructural sobre $o$.

- $\texttt{Avanzar}(0)$: devuelve $\text{nada}$, equivalente por la regla 1a.
- $\texttt{Avanzar}(n)$ con $n \ne 0$ y $\texttt{Girar}(s)$: devuelven la misma orden.
- $\texttt{Repetir}(v, x)$ con $v \le 0$: devuelve $\text{nada}$, equivalente por la regla 1b.
- $\texttt{Repetir}(1, x)$: devuelve $\texttt{unaPasada}(x)$, equivalente a $x$ por hipótesis de inducción, y $x \equiv \texttt{Repetir}(1, x)$ por la regla 3.
- $\texttt{Repetir}(v, x)$ con $v > 1$: sea $y = \texttt{unaPasada}(x) \equiv x$. Si $y = \text{nada}$ se devuelve $\text{nada}$, equivalente por el caso auxiliar. Si no, se devuelve $\texttt{Repetir}(v, y)$, equivalente a $\texttt{Repetir}(v, x)$ porque repetir $v$ veces órdenes equivalentes da el mismo resultado.
- $\texttt{Secuencia}(l)$: cada elemento se simplifica y por hipótesis de inducción es equivalente al original. Luego se filtran los $\text{nada}$ (regla 5) y se aplica `juntar` (Lema 3). Por último, el resultado vacío es $\text{nada}$, el de un elemento es ese elemento (regla 4) y en otro caso es una `Secuencia`. Todos estos pasos conservan el resultado. $\blacksquare$

## Repetir hasta que no cambie

`simplificar(o)` aplica `unaPasada` repetidamente hasta que el resultado sea igual a la entrada.

**Terminación.** Para una orden se define el par $(\text{nodos}, \text{ceros})$, donde `nodos` es el número de constructores y `ceros` la cantidad de $\texttt{Avanzar}(0)$ que contiene. Una pasada que cambia algo hace una de estas cosas:

- elimina al menos un nodo (reglas 1b, 2, 3, 4 o 5), o
- convierte un $\texttt{Avanzar}(0)$ en $\text{nada}$ (regla 1a), que mantiene los nodos y baja `ceros`.

La regla 2 puede crear un $\texttt{Avanzar}(0)$ cuando $a + b = 0$, pero a la vez elimina un nodo, así que `nodos` baja. El par decrece en orden lexicográfico en cada pasada que cambia la orden, y como ese orden es bien fundado el proceso termina.

**Corrección.** Por el Lema 4, cada pasada conserva el resultado de `ejecutar`. Por inducción sobre el número de pasadas, $\texttt{simplificar}(o) \equiv o$, es decir,

$$\mathcal{E}(\texttt{simplificar}(o), e) = \mathcal{E}(o, e) \quad \blacksquare$$

## La salida cumple las reglas

Cuando $\texttt{unaPasada}(o) = o$, ninguna regla es aplicable a $o$. Entonces la salida no contiene $\texttt{Avanzar}(0)$, ni $\texttt{Repetir}$ con $v \le 1$, ni secuencias de un solo elemento, ni órdenes que no hacen nada dentro de secuencias, ni dos `Avanzar` contiguos.

De aquí sale también la idempotencia: $\texttt{simplificar}(\texttt{simplificar}(o)) = \texttt{simplificar}(o)$, porque la salida ya es un punto fijo de `unaPasada`.

## Relación con las pruebas

Las pruebas de ScalaTest comprueban el teorema sobre casos concretos: para varias órdenes y estados verifican que $\mathcal{E}(o, e) = \mathcal{E}(\texttt{simplificar}(o), e)$, además de la idempotencia.