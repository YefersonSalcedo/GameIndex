package gameindex.storage;

import gameindex.model.Videojuego;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

public class ArchivoManager {

    /** Ruta del archivo de datos. */
    private static final String RUTA_ARCHIVO = "data/data.dat";

    /** Referencia al archivo abierto en modo lectura/escritura aleatoria. */
    private RandomAccessFile raf;

    public ArchivoManager() {
        try {
            File dir = new File("data");
            if (!dir.exists()) {
                dir.mkdirs();
            }
            File archivo = new File(RUTA_ARCHIVO);
            raf = new RandomAccessFile(archivo, "rw");
        } catch (IOException e) {
            throw new RuntimeException("No se pudo abrir el archivo de datos: " + RUTA_ARCHIVO, e);
        }
    }

    /**
     * Sobreescribe un registro existente en el offset indicado.
     *
     * @param v      videojuego a escribir
     * @param offset posición en el archivo donde escribir
     */
    public void escribirRegistro(Videojuego v, long offset) {
        try {
            raf.seek(offset);
            raf.write(v.toBytes());
        } catch (IOException e) {
            throw new RuntimeException("Error escribiendo registro en offset " + offset, e);
        }
    }

    /**
     * Agrega un nuevo registro al final del archivo y devuelve su offset.
     *
     * @param v videojuego a agregar
     * @return offset donde fue escrito el registro
     */
    public long agregarRegistro(Videojuego v) {
        try {
            long offset = raf.length();
            raf.seek(offset);
            raf.write(v.toBytes());
            return offset;
        } catch (IOException e) {
            throw new RuntimeException("Error agregando registro al archivo", e);
        }
    }

    /**
     * Lee y reconstruye un registro desde el offset indicado.
     *
     * @param offset posición en el archivo a leer
     * @return videojuego leído, o lanza excepción si falla
     */
    public Videojuego leerRegistro(long offset) {
        try {
            raf.seek(offset);
            byte[] buffer = new byte[Videojuego.RECORD_SIZE];
            raf.readFully(buffer);
            return Videojuego.fromBytes(buffer);
        } catch (IOException e) {
            throw new RuntimeException("Error leyendo registro en offset " + offset, e);
        }
    }

    /**
     * Devuelve el número total de registros en el archivo
     * (incluyendo los eliminados lógicamente).
     *
     * @return cantidad de registros
     */
    public long getTotalRegistros() {
        try {
            return raf.length() / Videojuego.RECORD_SIZE;
        } catch (IOException e) {
            throw new RuntimeException("Error obteniendo tamaño del archivo", e);
        }
    }

    /**
     * Verifica si el offset apunta a un registro válido dentro del archivo.
     *
     * @param offset posición a verificar
     * @return true si el offset es válido y alineado al tamaño de registro
     */
    public boolean esOffsetValido(long offset) {
        try {
            return offset >= 0
                    && offset % Videojuego.RECORD_SIZE == 0
                    && offset + Videojuego.RECORD_SIZE <= raf.length();
        } catch (IOException e) {
            return false;
        }
    }

    /** Cierra el archivo de datos. */
    public void cerrar() {
        if (raf != null) {
            try {
                raf.close();
            } catch (IOException e) {
                System.err.println("Advertencia: error cerrando archivo: " + e.getMessage());
            }
        }
    }
}
