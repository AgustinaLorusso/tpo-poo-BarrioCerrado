# Informe final

TP Integrador de Programación Orientada a Objetos. Propuesta A: Gestión del barrio cerrado Los Aromos.

Integrantes: Thomas Delli Antoni, Agustina Lorusso y Joaquina Dias Rato.

## 1. Cambios respecto del diseño original

Diferencias entre el diagrama de clases de la Etapa 1 y la implementación final (diagrama de clases, Versión 3):

**Modelo**
- **Administrador:** se le agregaron las asociaciones con Barrio, Personal y GeneradorReportes. También los métodos de alta y baja de vecinos y lotes (`darDeAltaVecino`, `darDeAltaLote`, `darDeBajaLote`, `darDeBajaVecino`), `agregarPersonal`, `asignarRolEnLote` y `cambiarTitular` (RF01 y RF02).
- **Encargado:** se eliminó esa clase del diagrama porque no correspondía a ningún requisito.
- **Expensa:** el estado no se guarda en un atributo; se calcula con `getEstado(fecha)` (patrón Experto). El valor `PAGADA_PARCIAL` del enum pasó a llamarse `PARCIAL`.
- **Pago y AsientoAjuste:** la Expensa crea sus pagos (patrón Creador). Los pagos nunca se modifican ni se eliminan (RN12): las correcciones se registran como asientos de ajuste con `Expensa.registrarAjuste`, y `Pago.getImporteCorregido()` devuelve el importe con los ajustes aplicados.
- Se usaron `LocalDate` y `LocalDateTime` en lugar de `Date`, y `reabrir` recibe el motivo de la reapertura.

**Interfaz**
- Se agregó el inicio de sesión por documento. Cada rol (administrador, vecino, guardia y mantenimiento) ve un menú con sus opciones. Main guarda al usuario que inició sesión.
- Los reportes se muestran por pantalla: no se implementó la exportación ni la impresión.

**Requisitos técnicos**
- `Reclamo` implementa `Comparable<Reclamo>` (orden por prioridad y antigüedad). El reporte de morosidad usa un `Comparator` por deuda.
- Se agregó la excepción propia `ReglaNegocioException` para las reglas de negocio de los reclamos (RN03, RN05, RN07 y la especialidad del empleado).
- Se agregó la persistencia en archivo: las clases `Persistencia` y `DatosSistema`, y las clases del modelo implementan `Serializable`.

**Diagramas de secuencia:** se corrigieron CU01 (Registrar reclamo) y CU02 (Asignar personal al reclamo) para que coincidan con los métodos implementados.

## 2. Dificultades encontradas

- **Integrar el trabajo en paralelo:** cada integrante trabajó en una rama distinta. Cuando un cambio tocaba una clase compartida, había que adaptar el código y las pruebas de los demás. Por ejemplo, el constructor de `Administrador` pasó a recibir el `Barrio` y hubo que actualizar dos pruebas. Una rama se había creado antes de que se sumaran otras pruebas, así que hubo que verificar la integración antes de mergear.
- **Mantener el diagrama igual al código:** el diagrama se hizo antes de programar y durante la implementación aparecieron métodos y asociaciones nuevos. Hicieron falta varias revisiones para que la Versión 3 coincidiera con el código.
- **Cálculo de intereses y redondeo:** el interés es del 3 % mensual, proporcional a los días. El redondeo a centavos de cada lote puede dar una diferencia de centavos con el presupuesto total.
- **Persistencia de los atributos static:** la serialización no guarda los atributos `static`, como los contadores de números de reclamo y de orden y la lista de ingresos. Hubo que guardarlos aparte en `DatosSistema` y restaurarlos al cargar.

## 3. Distribución de tareas

| Integrante | Tareas | Pull requests |
|---|---|---|
| Thomas Delli Antoni | Documento de la Etapa 1; reclamos, equipos y mantenimiento, visitas y reportes (pasos 4, 6, 7 y 8); diagramas de secuencia; validaciones; asignación de roles y personas autorizadas (RF02 y RF03); requisitos técnicos (Comparable, excepción propia y persistencia); plan de pruebas | #1, #6, #8, #10, #11, #12 |

| Agustina Lorusso | Clases de personas y enums (pasos 1 y 2) y sus pruebas; inicio de sesión y menús por rol; alta y baja de lotes y vecinos; revisión e integración de la mayoría de los PR | #4, #7, #9 |

| Joaquina Dias Rato | Lotes, expensas, pagos, intereses, asientos de ajuste y morosidad (pasos 3 y 5), con sus pruebas | #5 |
| Trabajo conjunto del grupo | Diagrama de clases (draw.io), justificación de SOLID y GRASP, supuestos | — |

Cada cambio se integró a `main` mediante un pull request revisado por otro integrante.

## 4. Reflexión del grupo

Al leer la consigna, el problema parecía acotado: los casos de uso y los primeros diagramas eran simples y con pocas clases. Al empezar a programar, la complejidad aumentó de forma considerable. Cada caso de uso requería más información de la prevista, aparecieron nuevas relaciones entre clases y reglas de negocio que en el diseño no se veían. Por ese motivo, el diagrama de clases tuvo que actualizarse varias veces, hasta llegar a la Versión 3.

Esta experiencia nos mostró que el diseño inicial es un punto de partida y no una versión definitiva, y que conviene revisarlo a medida que avanza la implementación. También comprobamos la utilidad de los patrones GRASP y los principios SOLID para decidir qué clase debe asumir cada responsabilidad cuando el modelo crece.

En cuanto a la organización, el trabajo con ramas y pull requests no fue una novedad para el grupo, ya que era una práctica que veníamos aplicando. Esto facilitó dividir las tareas, trabajar en paralelo y revisar los cambios de cada integrante antes de integrarlos a la rama principal.
