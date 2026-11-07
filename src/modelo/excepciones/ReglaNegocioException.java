package modelo.excepciones;

// Excepcion propia: se lanza cuando una operacion viola una regla de negocio (RN).
// Extiende RuntimeException para que el menu la maneje junto con los demas errores,
// pero los metodos que la lanzan la declaran con throws para dejarlo explicito.
public class ReglaNegocioException extends RuntimeException {
    public ReglaNegocioException(String mensaje) {
        super(mensaje);
    }
}
