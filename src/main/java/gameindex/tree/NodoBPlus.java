package gameindex.tree;

import java.util.ArrayList;
import java.util.List;

/**
 * Representa un nodo del Árbol B+.
 * Puede ser de dos tipos (definidos en NivelNodo):
 * HOJA: almacena claves y los offsets de disco de los registros
 *       correspondientes. Tiene un puntero al siguiente nodo hoja para
 *       recorridos secuenciales eficientes.
 * INTERNO: almacena claves separadoras y punteros a nodos hijo.
 *       Un nodo interno con 'n' claves tiene 'n+1' hijos.
 */
public class NodoBPlus {

    // Orden del árbol: número máximo de claves por nodo.
    public static final int ORDEN = 4;
    private final NivelNodo nivel;

    // Claves almacenadas en este nodo (títulos de videojuegos).
    private final List<String> claves;


    //Hijos del nodo (sólo válido para nodos INTERNOS).
    //Si hay 'n' claves, hay 'n+1' hijos.
    private final List<NodoBPlus> hijos;

    // Offsets de disco correspondientes a cada clave (sólo válido para HOJAS).
    // offsets.get(i) apunta al registro de claves.get(i).
    private final List<Long> offsets;

    // Puntero al siguiente nodo hoja (sólo válido para HOJAS).
    private NodoBPlus siguienteHoja;

    // Constructor
    public NodoBPlus(NivelNodo nivel) {
        this.nivel         = nivel;
        this.claves        = new ArrayList<>();
        this.hijos         = new ArrayList<>();
        this.offsets       = new ArrayList<>();
        this.siguienteHoja = null;
    }

    // === Consultas de estado ==============
    /** @return true si el nodo es una hoja */
    public boolean esHoja() {
        return nivel == NivelNodo.HOJA;
    }

    /** @return true si el nodo ha alcanzado su capacidad máxima */
    public boolean estaLleno() {
        return claves.size() >= ORDEN;
    }

    /** @return número de claves actualmente en el nodo */
    public int getNumClaves() {
        return claves.size();
    }

    // === Acceso a claves ==============================
    /** @return lista mutable de claves del nodo. */
    public List<String> getClaves() {
        return claves;
    }

    /** @return clave en la posición indicada. */
    public String getClave(int i) {
        return claves.get(i);
    }

    /** Inserta una clave en la posición indicada desplazando las siguientes. */
    public void insertarClave(int i, String clave) {
        claves.add(i, clave);
    }

    /** Elimina la clave en la posición indicada. */
    public String eliminarClave(int i) {
        return claves.remove(i);
    }

    // === Acceso a hijos (nodos INTERNOS) ================================
    /** @return lista mutable de hijos (sólo nodos internos) */
    public List<NodoBPlus> getHijos() {
        return hijos;
    }

    /** Obtiene el hijo en la posición indicada. */
    public NodoBPlus getHijo(int i) {
        return hijos.get(i);
    }

    /** Inserta un nodo hijo en la posición indicada. */
    public void insertarHijo(int i, NodoBPlus hijo) {
        hijos.add(i, hijo);
    }

    /** Elimina el hijo en la posición indicada. */
    public NodoBPlus eliminarHijo(int i) {
        return hijos.remove(i);
    }

    // === Acceso a offsets (nodos HOJA) =================================
    /** @return lista mutable de offsets (sólo nodos hoja) */
    public List<Long> getOffsets() {
        return offsets;
    }

    /** Obtiene el offset en la posición indicada. */
    public long getOffset(int i) {
        return offsets.get(i);
    }

    /** Inserta un offset en la posición indicada. */
    public void insertarOffset(int i, long offset) {
        offsets.add(i, offset);
    }

    /** Elimina el offset en la posición indicada. */
    public long eliminarOffset(int i) {
        return offsets.remove(i);
    }

    // === Encadenamiento de hojas ==============================
    /** @return el siguiente nodo hoja, o null si es el último */
    public NodoBPlus getSiguienteHoja() {
        return siguienteHoja;
    }

    /** Establece el puntero al siguiente nodo hoja. */
    public void setSiguienteHoja(NodoBPlus siguienteHoja) {
        this.siguienteHoja = siguienteHoja;
    }

    // === Búsqueda de posición ========================================
    /**
     * Devuelve el índice del primer hijo al que se debe descender para
     * buscar o insertar la clave en este nodo interno.
     * @param clave clave de búsqueda
     * @return índice del hijo correspondiente
     */
    public int buscarPosicionHijo(String clave) {
        int i = 0;
        while (i < claves.size() && clave.compareTo(claves.get(i)) >= 0) {
            i++;
        }
        return i;
    }

    /**
     * Devuelve el índice de inserción ordenada de la clave entre las
     * claves de este nodo hoja.
     * @param clave clave a insertar
     * @return índice donde debe insertarse la clave
     */
    public int buscarPosicionInsercion(String clave) {
        int i = 0;
        while (i < claves.size() && clave.compareTo(claves.get(i)) > 0) {
            i++;
        }
        return i;
    }

    @Override
    public String toString() {
        return (esHoja() ? "Hoja" : "Interno") + claves.toString();
    }
}
