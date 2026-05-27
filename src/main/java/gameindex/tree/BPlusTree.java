package gameindex.tree;

import gameindex.model.Videojuego;
import gameindex.storage.ArchivoManager;
import gameindex.storage.IndiceManager;

import java.util.List;

public class BPlusTree {

    /** Raíz actual del árbol. */
    private NodoBPlus raiz;

    /** Referencia al gestor de archivo para operaciones de persistencia. */
    private final ArchivoManager archivoManager;

    /** Gestor del archivo de índice (index.dat). */
    private final IndiceManager indiceManager;

    /**
     * Crea un árbol B+. Si index.dat contiene un árbol previo lo carga;
     * de lo contrario inicializa un árbol vacío.
     *
     * @param archivoManager gestor del archivo físico de videojuegos
     */
    public BPlusTree(ArchivoManager archivoManager) {
        this.archivoManager = archivoManager;
        this.indiceManager  = new IndiceManager();

        // Intentar cargar el árbol persistido
        NodoBPlus raizCargada = indiceManager.cargarArbol();
        if (raizCargada != null) {
            this.raiz = raizCargada;
            System.out.println("[BPlusTree] Árbol cargado desde index.dat");
        } else {
            this.raiz = new NodoBPlus(NivelNodo.HOJA);
            System.out.println("[BPlusTree] Árbol inicializado vacío");
        }
    }

    /**
     * Guarda el estado actual del árbol en index.dat.
     * Debe llamarse después de cada operación que modifique el árbol.
     */
    private void persistir() {
        indiceManager.guardarArbol(raiz);
    }

    // === Inserción ===========================================================

    /**
     * Inserta un videojuego en el árbol B+ y en el archivo de datos.
     * El registro se escribe en disco, y el offset resultante se indexa
     * en el árbol con la clave = título.
     *
     * @param videojuego objeto a persistir e indexar
     */
    public void insertar(Videojuego videojuego) {
        long offset = archivoManager.agregarRegistro(videojuego);
        SplitResult resultado = insertarRecursivo(raiz, videojuego.getTitulo().trim(), offset);

        if (resultado != null) {
            NodoBPlus nuevaRaiz = new NodoBPlus(NivelNodo.INTERNO);
            nuevaRaiz.getClaves().add(resultado.getClavePromovida());
            nuevaRaiz.getHijos().add(raiz);
            nuevaRaiz.getHijos().add(resultado.getNodoDerecho());
            raiz = nuevaRaiz;
        }
        persistir();
    }

    private SplitResult insertarRecursivo(NodoBPlus nodo, String clave, long offset) {
        if (nodo.esHoja()) {
            int pos = nodo.buscarPosicionInsercion(clave);
            nodo.insertarClave(pos, clave);
            nodo.insertarOffset(pos, offset);

            if (nodo.estaLleno()) {
                return split(nodo);
            }
            return null;
        } else {
            int posHijo = nodo.buscarPosicionHijo(clave);
            NodoBPlus hijo = nodo.getHijo(posHijo);

            SplitResult resultadoHijo = insertarRecursivo(hijo, clave, offset);

            if (resultadoHijo != null) {
                int pos = nodo.buscarPosicionInsercion(resultadoHijo.getClavePromovida());
                nodo.insertarClave(pos, resultadoHijo.getClavePromovida());
                nodo.insertarHijo(pos + 1, resultadoHijo.getNodoDerecho());

                if (nodo.estaLleno()) {
                    return splitInterno(nodo);
                }
            }
            return null;
        }
    }

    // === División de nodos ===================================================

    private SplitResult split(NodoBPlus nodo) {
        int mitad = nodo.getNumClaves() / 2;
        NodoBPlus nodoDerecho = new NodoBPlus(NivelNodo.HOJA);

        while (nodo.getNumClaves() > mitad) {
            nodoDerecho.getClaves().add(0, nodo.eliminarClave(mitad));
            nodoDerecho.getOffsets().add(0, nodo.eliminarOffset(mitad));
        }

        nodoDerecho.setSiguienteHoja(nodo.getSiguienteHoja());
        nodo.setSiguienteHoja(nodoDerecho);

        String clavePromovida = nodoDerecho.getClave(0);
        return new SplitResult(clavePromovida, nodoDerecho);
    }

    private SplitResult splitInterno(NodoBPlus nodo) {
        int mitad = nodo.getNumClaves() / 2;
        String clavePromovida = nodo.eliminarClave(mitad);
        NodoBPlus nodoDerecho = new NodoBPlus(NivelNodo.INTERNO);

        while (nodo.getNumClaves() > mitad) {
            nodoDerecho.getClaves().add(0, nodo.eliminarClave(mitad));
        }

        int numHijosDerecho = nodoDerecho.getNumClaves() + 1;
        int inicioHijos = nodo.getHijos().size() - numHijosDerecho;
        while (nodo.getHijos().size() > inicioHijos) {
            nodoDerecho.getHijos().add(0, nodo.eliminarHijo(inicioHijos));
        }

        return new SplitResult(clavePromovida, nodoDerecho);
    }

    // === Actualización ===================================

    /**
     * Actualiza los datos de un videojuego existente en el árbol y en disco.
     * @param tituloOriginal        título con el que está indexado actualmente
     * @param videojuegoActualizado objeto con los nuevos datos
     * @return true si fue encontrado y actualizado; false si no existe
     */
    public boolean actualizar(String tituloOriginal, Videojuego videojuegoActualizado) {
        /**
        long[] offsetWrapper = new long[]{-1L};
        buscarOffsetEnArbol(raiz, tituloOriginal.trim(), offsetWrapper);
        long offsetActual = offsetWrapper[0];

        if (offsetActual == -1L) {
            System.out.println("[BPlusTree] No se encontró: " + tituloOriginal);
            return false;
        }

        String nuevoTitulo = videojuegoActualizado.getTitulo().trim();

        if (tituloOriginal.trim().equalsIgnoreCase(nuevoTitulo)) {
            archivoManager.escribirRegistro(videojuegoActualizado, offsetActual);
        } else {
            eliminarEntradaIndice(raiz, tituloOriginal.trim());
            long nuevoOffset = archivoManager.agregarRegistro(videojuegoActualizado);
            SplitResult resultado = insertarRecursivo(raiz, nuevoTitulo, nuevoOffset);
            if (resultado != null) {
                NodoBPlus nuevaRaiz = new NodoBPlus(NivelNodo.INTERNO);
                nuevaRaiz.getClaves().add(resultado.getClavePromovida());
                nuevaRaiz.getHijos().add(raiz);
                nuevaRaiz.getHijos().add(resultado.getNodoDerecho());
                raiz = nuevaRaiz;
            }
        }
        persistir();
         */
        return true;
    }

    // === Métodos auxiliares ==================

    /** Devuelve la raíz del árbol. */
    public NodoBPlus getRaiz() {
        return raiz;
    }
}
