# Informe final

TP Integrador de Programación Orientada a Objetos
Propuesta A: Gestión de un barrio cerrado (Los Aromos)
Integrantes: Thomas Delli Antoni, Agustina Lorusso, Joaquina Dias Rato

## 1. Cambios respecto del diseño original

El diagrama que entregamos en la Etapa 1 fue cambiando bastante mientras programábamos. La versión que quedó como final es la Versión 3 del draw.io, que coincide con el código. Los cambios más importantes fueron estos.

En el Administrador habíamos puesto pocas relaciones y al programar nos dimos cuenta de que necesitaba conocer al Barrio, al personal y al GeneradorReportes, así que agregamos esas asociaciones. También le sumamos los métodos para dar de alta y de baja vecinos y lotes, agregar personal, asignar un rol en un lote y cambiar el titular (RF01 y RF02). La clase Encargado, que estaba en el diagrama inicial, la sacamos porque no respondía a ningún requisito.

En expensas decidimos que el estado no se guarde como atributo sino que se calcule con getEstado(fecha), porque si se guardaba podía quedar desactualizado cuando una expensa vencía sin que nadie la tocara. El valor PAGADA_PARCIAL del enum quedó como PARCIAL. La Expensa es la que crea sus pagos, y como los pagos no se pueden borrar ni modificar (RN12), las correcciones se hacen con asientos de ajuste a través de registrarAjuste. Para saber cuánto vale un pago después de los ajustes está getImporteCorregido().

Otros cambios más chicos: usamos LocalDate y LocalDateTime en vez de Date, y el método reabrir pasó a recibir el motivo, porque el caso de uso pide que quede registrado en el historial.

En la interfaz agregamos un inicio de sesión con el documento, y según quién entra se muestra el menú del administrador, del vecino, del guardia o de mantenimiento. Los reportes se muestran por pantalla; no hicimos exportación ni impresión.

Para cumplir los requisitos técnicos de la consigna sumamos al final:

- Reclamo implementa Comparable, ordenando por prioridad y después por antigüedad, y el reporte de morosidad ordena los lotes por deuda con un Comparator.
- Una excepción propia, ReglaNegocioException, para las reglas de los reclamos (RN03, RN05, RN07 y la especialidad del empleado).
- Persistencia en archivo con las clases Persistencia y DatosSistema, para que los datos no se pierdan al cerrar el programa.

Los dos diagramas de secuencia (Registrar reclamo y Asignar personal al reclamo) los corregimos para que usen los mismos métodos que el código.

## 2. Dificultades encontradas

La mayor dificultad fue juntar lo que hacía cada uno. Trabajamos en ramas separadas y cuando alguien cambiaba una clase que usaban los demás, se rompían cosas en otras partes. Por ejemplo, cuando el constructor del Administrador pasó a recibir el Barrio tuvimos que arreglar dos de las pruebas. También pasó que una rama se había creado antes de que entraran algunas pruebas a main y hubo que revisar que todo siguiera funcionando antes de mergearla.

Otra cosa que nos llevó tiempo fue mantener el diagrama de clases igual al código. Como el diagrama se hizo antes de programar, cada vez que aparecía un método o una relación nueva había que volver a actualizarlo, y tuvimos que revisarlo varias veces hasta que quedó la Versión 3.

En la parte de expensas costó el cálculo del interés, que es del 3 % mensual pero proporcional a los días de atraso. Además, al redondear a centavos el importe de cada lote, la suma puede diferir por algunos centavos del presupuesto total, y lo dejamos así como supuesto.

Por último, con la persistencia descubrimos que la serialización no guarda los atributos static, como los contadores de reclamos y de órdenes. Si no los guardábamos, al volver a abrir el programa los números de reclamo empezaban de nuevo y se iban a repetir, así que los guardamos aparte en DatosSistema y los restauramos al iniciar.

## 3. Distribución de tareas

| Integrante | Tareas |
|---|---|
| Thomas Delli Antoni | Reclamos, equipos y mantenimiento, visitas y reportes (pasos 4, 6, 7 y 8); validaciones; asignación de roles y personas autorizadas (RF02 y RF03); Comparable, excepción propia y persistencia |
| Agustina Lorusso | Personas y enums (pasos 1 y 2) con sus pruebas; inicio de sesión y menús por rol; alta y baja de lotes y vecinos; revisión de la mayoría de los pull requests |
| Joaquina Dias Rato | Lotes, expensas, pagos, intereses, ajustes y morosidad (pasos 3 y 5) con sus pruebas |
| Entre todos | Documento de la Etapa 1 (casos de uso, diagrama de clases, SOLID y GRASP), diagramas de secuencia, supuestos y plan de pruebas |

Cada parte entró a main por un pull request que revisó otro integrante del grupo.

## 4. Reflexión del grupo

Cuando leímos la consigna nos pareció un trabajo bastante acotado. Los casos de uso y los primeros diagramas salieron sin mucha complicación y con pocas clases. Pero cuando empezamos a codear todo fue creciendo: cada caso de uso necesitaba más datos de los que habíamos pensado, aparecieron relaciones entre clases que no estaban en el diagrama y reglas de negocio que recién se veían al implementarlas. Por eso el diagrama de clases terminó teniendo tres versiones.

Nos quedamos con la idea de que el primer diseño sirve para arrancar, pero que no hay que tomarlo como algo cerrado, sino ir ajustándolo a medida que se programa. Los patrones GRASP nos ayudaron justamente en esos momentos, cuando no sabíamos en qué clase poner un método nuevo.

En cuanto a la organización, trabajar con ramas y pull requests no fue algo nuevo para nosotros, porque ya lo veníamos haciendo. Eso nos facilitó repartir el trabajo y revisar lo de cada uno antes de pasarlo a main.
