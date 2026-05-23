package gameindex.tree;

import gameindex.model.Videojuego;
import gameindex.storage.ArchivoManager;

public class BPlusTree {

    /** Raíz actual del árbol. */
    private NodoBPlus raiz;

    /** Referencia al gestor de archivo para operaciones de persistencia. */
    private final ArchivoManager archivoManager;


    /**
     * Crea un árbol B+ vacío.
     *
     * @param archivoManager gestor del archivo físico de videojuegos
     */
    public BPlusTree(ArchivoManager archivoManager) {
        this.archivoManager = archivoManager;
        this.raiz = new NodoBPlus(NivelNodo.HOJA); // árbol vacío: raíz es hoja
    }

    // === Inserción ===================================

    /**
     * Inserta un videojuego en el árbol B+ y en el archivo de datos.
     * El registro se escribe en disco, y el offset resultante se indexa
     * en el árbol con la clave = título.
     *
     * @param videojuego objeto a persistir e indexar
     */
    public void insertar(Videojuego videojuego) {
        /**
        long offset = archivoManager.agregarRegistro(videojuego);
        SplitResult resultado = insertarRecursivo(raiz, videojuego.getTitulo().trim(), offset);

        // Si la raíz se dividió, creamos una nueva raíz interna
        if (resultado != null) {
            NodoBPlus nuevaRaiz = new NodoBPlus(NivelNodo.INTERNO);
            nuevaRaiz.getClaves().add(resultado.getClavePromovida());
            nuevaRaiz.getHijos().add(raiz);
            nuevaRaiz.getHijos().add(resultado.getNodoDerecho());
            raiz = nuevaRaiz;
        }
         */
    }

    // Inserción recursiva
    /**
     * Recorre el árbol de forma recursiva e inserta la clave con su offset
     * en el nodo hoja correspondiente. Si el nodo queda lleno, lo divide y
     * devuelve un SplitResult para que el padre lo incorpore.
     *
     * @param nodo   nodo actual en la recursión
     * @param clave  título del videojuego (clave de indexación)
     * @param offset posición en disco del registro
     * @return SplitResult si hubo división; null en caso contrario
     */
    private SplitResult insertarRecursivo(NodoBPlus nodo, String clave, long offset) {

        if (nodo.esHoja()) {
            // Caso base: insertar en la hoja
            int pos = nodo.buscarPosicionInsercion(clave);
            nodo.insertarClave(pos, clave);
            nodo.insertarOffset(pos, offset);

            if (nodo.estaLleno()) {
                return split(nodo); // la hoja se desbordó: dividir
            }
            return null; // inserción sin división

        } else {
            // Caso recursivo: bajar al hijo correcto
            int posHijo = nodo.buscarPosicionHijo(clave);
            NodoBPlus hijo = nodo.getHijo(posHijo);

            SplitResult resultadoHijo = insertarRecursivo(hijo, clave, offset);

            if (resultadoHijo != null) {
                // El hijo se dividió: incorporar la clave promovida
                int pos = nodo.buscarPosicionInsercion(resultadoHijo.getClavePromovida());
                nodo.insertarClave(pos, resultadoHijo.getClavePromovida());
                nodo.insertarHijo(pos + 1, resultadoHijo.getNodoDerecho());

                if (nodo.estaLleno()) {
                    return splitInterno(nodo); // el nodo interno también se desbordó
                }
            }
            return null;
        }
    }

    // === División de nodos =========================================
    /**
     * Divide un nodo hoja lleno en dos mitades.
     * La mitad inferior permanece en el nodo; la mitad superior
     * pasa al nuevo nodo derecho. La primera clave del nodo derecho se
     * copia (no promovida) al padre para actuar como separador.
     *
     * @param nodo nodo hoja lleno
     * @return SplitResult con la clave separadora y el nuevo nodo derecho
     */
    private SplitResult split(NodoBPlus nodo) {
        int mitad = nodo.getNumClaves() / 2;

        NodoBPlus nodoDerecho = new NodoBPlus(NivelNodo.HOJA);

        // Mover la mitad superior al nodo derecho
        while (nodo.getNumClaves() > mitad) {
            nodoDerecho.getClaves().add(0, nodo.eliminarClave(mitad));
            nodoDerecho.getOffsets().add(0, nodo.eliminarOffset(mitad));
        }

        // Encadenar hojas
        nodoDerecho.setSiguienteHoja(nodo.getSiguienteHoja());
        nodo.setSiguienteHoja(nodoDerecho);

        // En hojas, la clave que sube es una COPIA de la primera del nodo derecho
        String clavePromovida = nodoDerecho.getClave(0);
        return new SplitResult(clavePromovida, nodoDerecho);
    }

    /**
     * Divide un nodo interno lleno en dos mitades.
     * La clave del medio se promueve (no se copia) al padre,
     * por lo que no queda en ninguno de los dos nodos resultantes.
     *
     * @param nodo nodo interno lleno
     * @return SplitResult con la clave promovida y el nuevo nodo derecho
     */
    private SplitResult splitInterno(NodoBPlus nodo) {
        int mitad = nodo.getNumClaves() / 2;

        // La clave del medio sube al padre
        String clavePromovida = nodo.eliminarClave(mitad);

        NodoBPlus nodoDerecho = new NodoBPlus(NivelNodo.INTERNO);

        // Mover claves e hijos de la mitad superior al nodo derecho
        // (tras eliminar la clave del medio, 'mitad' apunta al primer elemento superior)
        while (nodo.getNumClaves() > mitad) {
            nodoDerecho.getClaves().add(0, nodo.eliminarClave(mitad));
        }
        // Los hijos: hay getNumClaves()+1 hijos; el hijo extra está al final
        // Después de mover k claves, debemos mover k+1 hijos al nodo derecho
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
     * Hay dos escenarios:
     * 1- El título no cambia: se sobrescribe el registro en el mismo offset.
     * 2- El título cambia: se elimina la entrada antigua del índice,
     *       se escribe el nuevo registro en disco y se reinserta con la nueva clave.
     *
     * @param tituloOriginal título con el que se indexó el registro actualmente
     * @param videojuegoActualizado objeto con los nuevos datos (puede tener título diferente)
     * @return true si el registro fue encontrado y actualizado; false si no existe
     */
    public boolean actualizar(String tituloOriginal, Videojuego videojuegoActualizado) {
        /**
        // Buscar el offset actual del registro (lo realiza el Integrante 3 en buscar())

        long[] offsetWrapper = new long[]{-1L};
        buscarOffsetEnArbol(raiz, tituloOriginal.trim(), offsetWrapper);
        long offsetActual = offsetWrapper[0];

        if (offsetActual == -1L) {
            System.out.println("[BPlusTree] No se encontró el registro con título: " + tituloOriginal);
            return false;
        }

        String nuevoTitulo = videojuegoActualizado.getTitulo().trim();

        if (tituloOriginal.trim().equalsIgnoreCase(nuevoTitulo)) {
            // Caso 1: el título no cambia -> sobrescribir en el mismo offset
            archivoManager.escribirRegistro(videojuegoActualizado, offsetActual);
            System.out.println("[BPlusTree] Registro actualizado en offset " + offsetActual);
        } else {
            // Caso 2: el título cambia -> reinsertar
            // 1. Eliminar la entrada antigua del índice
            eliminarEntradaIndice(raiz, tituloOriginal.trim());
            // 2. Escribir nuevo registro en disco
            long nuevoOffset = archivoManager.agregarRegistro(videojuegoActualizado);
            // 3. Insertar la nueva clave en el árbol
            SplitResult resultado = insertarRecursivo(raiz, nuevoTitulo, nuevoOffset);
            if (resultado != null) {
                NodoBPlus nuevaRaiz = new NodoBPlus(NivelNodo.INTERNO);
                nuevaRaiz.getClaves().add(resultado.getClavePromovida());
                nuevaRaiz.getHijos().add(raiz);
                nuevaRaiz.getHijos().add(resultado.getNodoDerecho());
                raiz = nuevaRaiz;
            }
            System.out.println("[BPlusTree] Título cambiado: '" + tituloOriginal
                    + "' -> '" + nuevoTitulo + "' (nuevo offset: " + nuevoOffset + ")");
        }
         */
        return true;
    }

    // === Métodos auxiliares ==================

    /** Devuelve la raíz del árbol. */
    public NodoBPlus getRaiz() {
        return raiz;
    }
}
