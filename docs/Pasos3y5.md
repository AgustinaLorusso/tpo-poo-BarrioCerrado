# Pasos 3 y 5

Implementacion basada en el enunciado y la pagina Version 2 del UML compartido.
Paquetes: `modelo.barrio` y `modelo.expensas`. Se conservan las clases de personas existentes.

## Uso

Crear un `Barrio`, agregar objetos `Lote` con un `Vecino` titular y llamar
`barrio.emitirExpensas(new PresupuestoMensual("2026-11", monto))`.
La superficie total se obtiene de los lotes cargados. Cada lote genera su expensa.
`lote.registrarPago(fecha, importe, MedioPago.TRANSFERENCIA)` distribuye el importe
por periodo mas antiguo; cada `Expensa` crea su `Pago`, abonando interes antes de capital.
Las consultas aceptan `LocalDate` para poder probar sin depender del reloj.

## Supuestos y decisiones para confirmar con el grupo

- Java 8 o superior; `LocalDate` en lugar de `Date`. Periodos en formato `AAAA-MM`.
- Se admiten hasta 150 lotes, permitiendo cargar y probar el barrio progresivamente.
- Un titular por lote; familiares e inquilinos se vinculan mediante `Residencia`.
  Se aplica RN07: residir no autoriza a reclamar; se requiere autorizacion del titular.
  Las personas se comparan por documento. Al cambiar titular se revocan las autorizaciones.
- Interes simple del 3 % mensual proporcional a los dias, tomando meses de 30 dias.
  En la fecha de vencimiento no hay dias transcurridos; el dia siguiente suma un dia.
  Los pagos reducen la base de calculo desde su fecha; no se capitalizan intereses.
- Importes redondeados a centavos con HALF_UP. El UML usa `double`, que se conserva;
  puede existir una diferencia de centavos entre el presupuesto y la suma de expensas
  por el redondeo individual del coeficiente. No se redistribuye esa diferencia.
- Se rechazan sobrepagos, importes no positivos y movimientos fuera de orden cronologico.
  No se implementa saldo a favor ni persistencia.
- `AsientoAjuste` se registra a traves de `Expensa.registrarAjuste`: positivo aumenta
  el pago efectivo y negativo lo reduce. No se modifica ni elimina el pago original.
  La correccion recalcula su imputacion desde la fecha original y las posteriores;
  se rechaza si genera un pago negativo o algun sobrepago. Los getters de imputacion
  del pago conservan los valores originales como evidencia; el saldo refleja los ajustes.
- Moroso significa mas de tres periodos vencidos con saldo. Se quita la marca cuando
  quedan tres o menos. Se guarda una notificacion formal en memoria al entrar en morosidad;
  no se envia correo. Consultas o pagos actualizan la marca; no hay proceso en segundo plano.
- No se implementan los pasos 4, 6, 7 y 8. La baja de lotes que consulta reclamos abiertos
  queda para integracion, pues `Reclamo` esta vacio en el repositorio de base.

## Prueba

Compilar todas las fuentes en `src` y `test` con codificacion UTF-8 y ejecutar
`PruebaPasos3y5`. No requiere Maven ni bibliotecas externas.

En una terminal con un JDK disponible:

```powershell
New-Item -ItemType Directory -Force bin | Out-Null
$fuentesJava = @(Get-ChildItem src,test -Recurse -Filter *.java | ForEach-Object FullName)
javac -encoding UTF-8 -d bin $fuentesJava
java -cp bin PruebaPasos3y5
```

La prueba cubre autorizaciones, coeficientes, duplicados, interes, pagos parciales,
ajustes con historial, orden de imputacion y entrada y salida de morosidad.
