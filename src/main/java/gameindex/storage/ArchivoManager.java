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

  
    public void escribirRegistro(Videojuego v, long offset) {
        try {
            raf.seek(offset);
            raf.write(v.toBytes());
        } catch (IOException e) {
            throw new RuntimeException("Error escribiendo registro en offset " + offset, e);
        }
    }


    public long agregarRegistro(Videojuego v) {
        try {
            long offset = raf.length();   // apunta al final actual del archivo
            raf.seek(offset);
            raf.write(v.toBytes());
            return offset;
        } catch (IOException e) {
            throw new RuntimeException("Error agregando registro al archivo", e);
        }
    }

  
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


    public long getTotalRegistros() {
        try {
            return raf.length() / Videojuego.RECORD_SIZE;
        } catch (IOException e) {
            throw new RuntimeException("Error obteniendo tamaño del archivo", e);
        }
    }


    public long getOffset(long indice) {
        return indice * Videojuego.RECORD_SIZE;
    }

    public void cerrar() {
        if (raf != null) {
            try {
                raf.close();
            } catch (IOException e) {
                System.err.println("Advertencia: error cerrando archivo: " + e.getMessage());
            }
        }
    }

    public boolean esOffsetValido(long offset) {
        try {
            return offset >= 0
                    && offset % Videojuego.RECORD_SIZE == 0
                    && offset + Videojuego.RECORD_SIZE <= raf.length();
        } catch (IOException e) {
            return false;
        }
    }
}
