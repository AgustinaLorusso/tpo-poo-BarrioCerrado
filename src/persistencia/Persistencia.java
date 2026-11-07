package persistencia;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

// Guarda y lee los datos del sistema en un archivo para conservarlos entre ejecuciones.
public class Persistencia {
    private static final String ARCHIVO = "datos/barrio.dat";

    public static boolean existenDatos() {
        return new File(ARCHIVO).exists();
    }

    public static boolean guardar(DatosSistema datos) {
        ObjectOutputStream salida = null;
        try {
            new File(ARCHIVO).getParentFile().mkdirs();
            salida = new ObjectOutputStream(new FileOutputStream(ARCHIVO));
            salida.writeObject(datos);
            return true;
        } catch (IOException e) {
            System.out.println("No se pudieron guardar los datos: " + e.getMessage());
            return false;
        } finally {
            // Se cierra el archivo tanto si se guardo bien como si hubo error
            cerrar(salida);
        }
    }

    // Devuelve null si no hay archivo o no se puede leer
    public static DatosSistema cargar() {
        ObjectInputStream entrada = null;
        try {
            entrada = new ObjectInputStream(new FileInputStream(ARCHIVO));
            return (DatosSistema) entrada.readObject();
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("No se pudieron leer los datos guardados: " + e.getMessage());
            return null;
        } finally {
            cerrar(entrada);
        }
    }

    private static void cerrar(Closeable archivo) {
        if (archivo == null) {
            return;
        }
        try {
            archivo.close();
        } catch (IOException e) {
            System.out.println("No se pudo cerrar el archivo: " + e.getMessage());
        }
    }
}
