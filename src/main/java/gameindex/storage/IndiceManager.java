package gameindex.storage;

import gameindex.tree.NodoBPlus;
import gameindex.tree.NivelNodo;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class IndiceManager {

    private static final String RUTA = "data/index.dat";

    // Longitud máxima de una clave (igual que Videojuego.LEN_TITULO)
    private static final int KEY_CHARS = 100;

    // Tamaño en bytes de cada nodo serializado
    // 1 (tipo) + 4 (numClaves)
    // + ORDEN * KEY_CHARS * 2      (claves en UTF-16)
    // + ORDEN * 8                  (offsets de datos, hojas)
    // + (ORDEN+1) * 8              (offsets de hijos, internos)
    // + 8                          (siguienteHoja)
    private static final int ORDEN      = NodoBPlus.ORDEN;
    public  static final int NODE_SIZE  =
            1 + 4
                    + ORDEN       * KEY_CHARS * 2
                    + ORDEN       * 8
                    + (ORDEN + 1) * 8
                    + 8;

    // Offset del encabezado (donde guardamos el offset de la raíz)
    private static final long HEADER_OFFSET_RAIZ = 0L;
    private static final int  HEADER_SIZE        = 8;

    private RandomAccessFile raf;

    public IndiceManager() {
        try {
            File dir = new File("data");
            if (!dir.exists()) dir.mkdirs();
            raf = new RandomAccessFile(new File(RUTA), "rw");
            // Si el archivo es nuevo, escribir encabezado vacío
            if (raf.length() == 0) {
                raf.seek(HEADER_OFFSET_RAIZ);
                raf.writeLong(-1L); // sin raíz todavía
            }
        } catch (IOException e) {
            throw new RuntimeException("No se pudo abrir index.dat", e);
        }
    }

    /**
     * Guarda el árbol completo en index.dat.
     * Sobreescribe el archivo desde cero (después del encabezado).
     *
     * @param raiz nodo raíz del árbol en memoria
     */
    public void guardarArbol(NodoBPlus raiz) {
        try {
            // Truncar contenido anterior (dejar solo el encabezado)
            raf.setLength(HEADER_SIZE);

            if (raiz == null || raiz.getNumClaves() == 0) {
                raf.seek(HEADER_OFFSET_RAIZ);
                raf.writeLong(-1L);
                return;
            }

            // Asignar un offset en index.dat a cada nodo (BFS)
            Map<NodoBPlus, Long> mapaOffsets = new HashMap<>();
            List<NodoBPlus> cola = new ArrayList<>();
            cola.add(raiz);
            long offsetActual = HEADER_SIZE;
            while (!cola.isEmpty()) {
                NodoBPlus nodo = cola.remove(0);
                mapaOffsets.put(nodo, offsetActual);
                offsetActual += NODE_SIZE;
                if (!nodo.esHoja()) {
                    cola.addAll(nodo.getHijos());
                }
            }

            // Escribir encabezado con offset de la raíz
            raf.seek(HEADER_OFFSET_RAIZ);
            raf.writeLong(mapaOffsets.get(raiz));

            // Escribir cada nodo en su offset asignado
            for (Map.Entry<NodoBPlus, Long> entry : mapaOffsets.entrySet()) {
                escribirNodo(entry.getKey(), entry.getValue(), mapaOffsets);
            }

        } catch (IOException e) {
            throw new RuntimeException("Error guardando el árbol en index.dat", e);
        }
    }

    /**
     * Carga el árbol desde index.dat y lo reconstruye en memoria.
     *
     * @return nodo raíz reconstruido, o null si el archivo está vacío
     */
    public NodoBPlus cargarArbol() {
        try {
            if (raf.length() <= HEADER_SIZE) return null;

            raf.seek(HEADER_OFFSET_RAIZ);
            long offsetRaiz = raf.readLong();
            if (offsetRaiz == -1L) return null;

            // Leer todos los nodos del archivo en un mapa offset → nodo
            Map<Long, NodoBPlus> nodosLeidos    = new HashMap<>();
            // También guardamos los offsets de hijos/siguienteHoja para reconectar
            Map<Long, long[]>    hijoOffsets    = new HashMap<>();
            Map<Long, Long>      siguienteOffsets = new HashMap<>();

            long pos = HEADER_SIZE;
            while (pos + NODE_SIZE <= raf.length()) {
                leerNodo(pos, nodosLeidos, hijoOffsets, siguienteOffsets);
                pos += NODE_SIZE;
            }

            // Reconectar hijos
            for (Map.Entry<Long, long[]> entry : hijoOffsets.entrySet()) {
                NodoBPlus nodo = nodosLeidos.get(entry.getKey());
                for (long hOffset : entry.getValue()) {
                    if (hOffset != -1L) {
                        NodoBPlus hijo = nodosLeidos.get(hOffset);
                        if (hijo != null) nodo.getHijos().add(hijo);
                    }
                }
            }

            // Reconectar cadena de hojas
            for (Map.Entry<Long, Long> entry : siguienteOffsets.entrySet()) {
                NodoBPlus nodo = nodosLeidos.get(entry.getKey());
                long sigOffset = entry.getValue();
                if (sigOffset != -1L) {
                    NodoBPlus sig = nodosLeidos.get(sigOffset);
                    if (sig != null) nodo.setSiguienteHoja(sig);
                }
            }

            return nodosLeidos.get(offsetRaiz);

        } catch (IOException e) {
            throw new RuntimeException("Error cargando el árbol desde index.dat", e);
        }
    }

    public void cerrar() {
        if (raf != null) {
            try { raf.close(); } catch (IOException ignored) {}
        }
    }

    private void escribirNodo(NodoBPlus nodo, long offset,
                              Map<NodoBPlus, Long> mapaOffsets) throws IOException {
        raf.seek(offset);

        // tipo
        raf.writeByte(nodo.esHoja() ? 0 : 1);

        // numClaves
        raf.writeInt(nodo.getNumClaves());

        // claves (siempre ORDEN slots, rellenos con espacios)
        for (int i = 0; i < ORDEN; i++) {
            String clave = (i < nodo.getNumClaves()) ? nodo.getClave(i) : "";
            escribirStringFijo(clave, KEY_CHARS);
        }

        // offsets de datos (hojas) — siempre ORDEN slots
        for (int i = 0; i < ORDEN; i++) {
            if (nodo.esHoja() && i < nodo.getNumClaves()) {
                raf.writeLong(nodo.getOffset(i));
            } else {
                raf.writeLong(-1L);
            }
        }

        // offsets de hijos (internos) — siempre ORDEN+1 slots
        for (int i = 0; i <= ORDEN; i++) {
            if (!nodo.esHoja() && i < nodo.getHijos().size()) {
                NodoBPlus hijo = nodo.getHijo(i);
                Long hOffset = mapaOffsets.get(hijo);
                raf.writeLong(hOffset != null ? hOffset : -1L);
            } else {
                raf.writeLong(-1L);
            }
        }

        // siguienteHoja
        NodoBPlus sig = nodo.getSiguienteHoja();
        if (sig != null && mapaOffsets.containsKey(sig)) {
            raf.writeLong(mapaOffsets.get(sig));
        } else {
            raf.writeLong(-1L);
        }
    }

    private void leerNodo(long offset,
                          Map<Long, NodoBPlus>  nodosLeidos,
                          Map<Long, long[]>     hijoOffsets,
                          Map<Long, Long>       siguienteOffsets) throws IOException {
        raf.seek(offset);

        int tipo = raf.readByte();
        NivelNodo nivel = (tipo == 0) ? NivelNodo.HOJA : NivelNodo.INTERNO;
        NodoBPlus nodo = new NodoBPlus(nivel);

        int numClaves = raf.readInt();

        // Leer claves
        String[] claves = new String[ORDEN];
        for (int i = 0; i < ORDEN; i++) {
            claves[i] = leerStringFijo(KEY_CHARS).trim();
        }
        for (int i = 0; i < numClaves; i++) {
            nodo.getClaves().add(claves[i]);
        }

        // Leer offsets de datos
        long[] datOffsets = new long[ORDEN];
        for (int i = 0; i < ORDEN; i++) {
            datOffsets[i] = raf.readLong();
        }
        if (nivel == NivelNodo.HOJA) {
            for (int i = 0; i < numClaves; i++) {
                nodo.getOffsets().add(datOffsets[i]);
            }
        }

        // Leer offsets de hijos
        long[] hijos = new long[ORDEN + 1];
        for (int i = 0; i <= ORDEN; i++) {
            hijos[i] = raf.readLong();
        }
        if (nivel == NivelNodo.INTERNO) {
            hijoOffsets.put(offset, hijos);
        }

        // Leer siguienteHoja
        long sigOffset = raf.readLong();
        siguienteOffsets.put(offset, sigOffset);

        nodosLeidos.put(offset, nodo);
    }

    private void escribirStringFijo(String valor, int longitud) throws IOException {
        String s = (valor == null) ? "" : valor;
        if (s.length() > longitud) s = s.substring(0, longitud);
        for (int i = 0; i < s.length(); i++)      raf.writeChar(s.charAt(i));
        for (int i = s.length(); i < longitud; i++) raf.writeChar(' ');
    }

    private String leerStringFijo(int longitud) throws IOException {
        StringBuilder sb = new StringBuilder(longitud);
        for (int i = 0; i < longitud; i++) sb.append(raf.readChar());
        return sb.toString();
    }
}