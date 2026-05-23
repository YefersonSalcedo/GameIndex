package gameindex.tree;

/**
 * Encapsula el resultado de una división (split) de nodo en el Árbol B+.
 * Cuando un nodo se desborda, se divide en dos y la clave promotora
 * sube al nodo padre junto con una referencia al nuevo nodo derecho.
 */
public class SplitResult {

    // Clave que sube al nodo padre tras la división.
    private final String clavePromovida;

    // Nuevo nodo derecho creado durante la división.
    private final NodoBPlus nodoDerechо;

    /**
     * Constructor del resultado de split.
     *
     * @param clavePromovida clave que debe insertarse en el nodo padre
     * @param nodoDerechо    nuevo nodo derecho resultado de la división
     */
    public SplitResult(String clavePromovida, NodoBPlus nodoDerechо) {
        this.clavePromovida = clavePromovida;
        this.nodoDerechо    = nodoDerechо;
    }

    /** @return la clave que sube al padre */
    public String getClavePromovida() {
        return clavePromovida;
    }

    /** @return el nuevo nodo derecho creado en la división */
    public NodoBPlus getNodoDerecho() {
        return nodoDerechо;
    }

    @Override
    public String toString() {
        return "SplitResult{clavePromovida='" + clavePromovida + "'}";
    }
}
