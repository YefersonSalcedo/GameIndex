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

    // === Búsqueda exacta ====================================================

    /**
     * Busca un videojuego por título exacto y devuelve el offset del registro
     * en el archivo de datos. Si el título no existe, o el registro ya está
     * marcado como eliminado lógicamente, devuelve null.
     *
     * @param titulo título a buscar
     * @return offset del registro, o null si no se encuentra
     */
    public Long buscar(String titulo) {
        if (titulo == null) {
            return null;
        }

        String clave = titulo.trim();
        if (clave.isEmpty() || raiz == null || raiz.getNumClaves() == 0) {
            return null;
        }

        NodoBPlus hoja = buscarHoja(raiz, clave);
        if (hoja == null) {
            return null;
        }

        for (int i = 0; i < hoja.getNumClaves(); i++) {
            String claveNodo = hoja.getClave(i).trim();
            int cmp = claveNodo.compareTo(clave);
            if (cmp == 0) {
                long offset = hoja.getOffset(i);
                Videojuego videojuego = archivoManager.leerRegistro(offset);
                if (videojuego != null && !videojuego.estaEliminado()) {
                    return offset;
                }
                return null;
            }
            if (cmp > 0) return null;
        }

        return null;
    }

    // === Eliminación lógica ================================================

    /**
     * Marca lógicamente como eliminado el videojuego identificado por título.
     * No borra el registro físicamente ni modifica la estructura del árbol.
     *
     * @param titulo título del videojuego a eliminar
     * @return true si se encontró y se marcó como eliminado; false en caso contrario
     */
    public boolean eliminarLogico(String titulo) {
        if (titulo == null) {
            return false;
        }

        Long offset = buscar(titulo);
        if (offset == null) {
            return false;
        }

        Videojuego videojuego = archivoManager.leerRegistro(offset);
        if (videojuego == null || videojuego.estaEliminado()) {
            return false;
        }

        videojuego.marcarEliminado();
        archivoManager.escribirRegistro(videojuego, offset);
        return true;
    }

    /**
     * Desciende por el árbol hasta llegar a la hoja que debería contener la
     * clave indicada, respetando el orden actual del árbol.
     */
    private NodoBPlus buscarHoja(NodoBPlus nodo, String clave) {
        NodoBPlus actual = nodo;
        while (actual != null && !actual.esHoja()) {
            int posHijo = actual.buscarPosicionHijo(clave);
            actual = actual.getHijo(posHijo);
        }
        return actual;
    }

    /**
     * Desciende por el hijo más a la izquierda hasta alcanzar la primera hoja
     * del árbol.
     */
    private NodoBPlus buscarHojaMasIzquierda() {
        NodoBPlus actual = raiz;
        while (actual != null && !actual.esHoja()) {
            if (actual.getHijos().isEmpty()) {
                return null;
            }
            actual = actual.getHijo(0);
        }
        return actual;
    }

    // === Búsqueda por rango ================================================

    /**
     * Busca títulos dentro del rango alfabético [inicio, fin] y devuelve los
     * offsets de los registros activos, en el mismo orden del árbol.
     *
     * @param inicio límite inferior del rango
     * @param fin    límite superior del rango
     * @return lista de offsets ordenados por título
     */
    public List<Long> buscarRango(String inicio, String fin) {
        List<Long> resultados = new java.util.ArrayList<>();

        if (inicio == null || fin == null || raiz == null || raiz.getNumClaves() == 0) {
            return resultados;
        }

        String desde = inicio.trim();
        String hasta = fin.trim();
        if (desde.isEmpty() || hasta.isEmpty()) {
            return resultados;
        }

        if (desde.compareTo(hasta) > 0) {
            String temporal = desde;
            desde = hasta;
            hasta = temporal;
        }

        NodoBPlus hoja = buscarHoja(raiz, desde);
        if (hoja == null) {
            return resultados;
        }

        NodoBPlus actual = hoja;
        int posicion = actual.buscarPosicionInsercion(desde);

        while (actual != null) {
            for (int i = posicion; i < actual.getNumClaves(); i++) {
                String clave = actual.getClave(i);
                if (clave.compareTo(hasta) > 0) {
                    return resultados;
                }
                long offset = actual.getOffset(i);
                Videojuego videojuego = archivoManager.leerRegistro(offset);
                if (videojuego != null && !videojuego.estaEliminado()) {
                    resultados.add(offset);
                }
            }

            actual = actual.getSiguienteHoja();
            posicion = 0;
        }

        return resultados;
    }

    // === Búsqueda por prefijo ==============================================

    /**
     * Busca títulos que comienzan con el prefijo indicado y devuelve los
     * offsets de los registros activos, en orden alfabético.
     *
     * @param prefijo texto inicial a buscar
     * @return lista de offsets ordenados por título
     */
    public List<Long> buscarPrefijo(String prefijo) {
        List<Long> resultados = new java.util.ArrayList<>();

        if (prefijo == null || raiz == null || raiz.getNumClaves() == 0) {
            return resultados;
        }

        String pref = prefijo.trim();
        if (pref.isEmpty()) {
            return resultados;
        }

        NodoBPlus hoja = buscarHoja(raiz, pref);
        if (hoja == null) {
            return resultados;
        }

        NodoBPlus actual = hoja;
        int posicion = actual.buscarPosicionInsercion(pref);

        while (actual != null) {
            for (int i = posicion; i < actual.getNumClaves(); i++) {
                String clave = actual.getClave(i);
                if (!clave.startsWith(pref)) {
                    return resultados;
                }

                long offset = actual.getOffset(i);
                Videojuego videojuego = archivoManager.leerRegistro(offset);
                if (videojuego != null && !videojuego.estaEliminado()) {
                    resultados.add(offset);
                }
            }

            actual = actual.getSiguienteHoja();
            posicion = 0;
        }

        return resultados;
    }

    // === Listado de activos ================================================

    /**
     * Recorre todas las hojas del árbol en orden y devuelve los offsets de los
     * registros activos, omitiendo los eliminados lógicamente.
     *
     * @return lista de offsets de videojuegos activos
     */
    public List<Long> listarActivos() {
        List<Long> resultados = new java.util.ArrayList<>();

        if (raiz == null || raiz.getNumClaves() == 0) {
            return resultados;
        }

        NodoBPlus actual = buscarHojaMasIzquierda();
        while (actual != null) {
            for (int i = 0; i < actual.getNumClaves(); i++) {
                long offset = actual.getOffset(i);
                Videojuego videojuego = archivoManager.leerRegistro(offset);
                if (videojuego != null && !videojuego.estaEliminado()) {
                    resultados.add(offset);
                }
            }
            actual = actual.getSiguienteHoja();
        }

        return resultados;
    }

    // === Depuración por niveles ============================================

    /**
     * Organiza el árbol por niveles usando BFS y devuelve una representación
     * textual de cada nodo. Cada entrada contiene el tipo del nodo, sus claves
     * y la cantidad de hijos (si aplica).
     *
     * @return niveles del árbol, donde cada nivel contiene la descripción de
     *         los nodos en ese nivel
     */
    public List<List<String>> obtenerNiveles() {
        List<List<String>> niveles = new java.util.ArrayList<>();

        if (raiz == null || raiz.getNumClaves() == 0) {
            return niveles;
        }

        java.util.Queue<NodoBPlus> cola = new java.util.ArrayDeque<>();
        cola.add(raiz);

        while (!cola.isEmpty()) {
            int cantidadEnNivel = cola.size();
            List<String> nivel = new java.util.ArrayList<>();

            for (int i = 0; i < cantidadEnNivel; i++) {
                NodoBPlus nodo = cola.poll();
                if (nodo == null) {
                    continue;
                }

                nivel.add(describirNodo(nodo));
                if (!nodo.esHoja()) {
                    cola.addAll(nodo.getHijos());
                }
            }

            niveles.add(nivel);
        }

        return niveles;
    }

    /**
     * Imprime por consola el árbol organizado por niveles, útil para depurar
     * la estructura sin alterar datos ni persistencia.
     */
    public void imprimirPorNiveles() {
        List<List<String>> niveles = obtenerNiveles();

        if (niveles.isEmpty()) {
            System.out.println("[BPlusTree] Árbol vacío");
            return;
        }

        for (int i = 0; i < niveles.size(); i++) {
            System.out.println("Nivel " + i + ":");
            for (String nodo : niveles.get(i)) {
                System.out.println("  " + nodo);
            }
        }
    }

    /**
     * Construye una descripción legible de un nodo para depuración.
     */
    private String describirNodo(NodoBPlus nodo) {
        String tipo = nodo.esHoja() ? NivelNodo.HOJA.name() : NivelNodo.INTERNO.name();
        int hijos = nodo.esHoja() ? 0 : nodo.getHijos().size();
        return tipo + " | claves=" + nodo.getClaves() + " | hijos=" + hijos;
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
        String claveTrim = videojuego.getTitulo().trim();

        // Verificar duplicado antes de escribir en disco
        if (buscar(claveTrim) != null) {
            throw new IllegalArgumentException(
                    "Ya existe un videojuego con el título: \"" + claveTrim + "\"");
        }

        long offset = archivoManager.agregarRegistro(videojuego);
        SplitResult resultado = insertarRecursivo(raiz, claveTrim, offset);

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
            nodoDerecho.getClaves().add(nodo.eliminarClave(mitad));
            nodoDerecho.getOffsets().add(nodo.eliminarOffset(mitad));
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
            nodoDerecho.getClaves().add(nodo.eliminarClave(mitad));
        }

        int numHijosDerecho = nodoDerecho.getNumClaves() + 1;
        int inicioHijos = nodo.getHijos().size() - numHijosDerecho;
        while (nodo.getHijos().size() > inicioHijos) {
            nodoDerecho.getHijos().add(nodo.eliminarHijo(inicioHijos));
        }

        return new SplitResult(clavePromovida, nodoDerecho);
    }

    // === Actualización ===================================

    /**
     * Actualiza los datos de un videojuego existente en el árbol y en disco.
     * Si el título no cambia, sobreescribe el registro en su mismo offset.
     * Si el título cambia, marca el registro viejo como eliminado, elimina su
     * entrada del índice, escribe el nuevo registro al final del archivo e
     * inserta la nueva clave en el árbol.
     *
     * @param tituloOriginal        título con el que está indexado actualmente
     * @param videojuegoActualizado objeto con los nuevos datos
     * @return true si fue encontrado y actualizado; false si no existe
     */
    public boolean actualizar(String tituloOriginal, Videojuego videojuegoActualizado) {
        if (tituloOriginal == null || videojuegoActualizado == null
                || videojuegoActualizado.getTitulo() == null) {
            return false;
        }

        long[] offsetWrapper = new long[]{-1L};
        buscarOffsetEnArbol(raiz, tituloOriginal.trim(), offsetWrapper);
        long offsetActual = offsetWrapper[0];

        if (offsetActual == -1L) {
            System.out.println("[BPlusTree] No se encontró: " + tituloOriginal);
            return false;
        }

        String nuevoTitulo = videojuegoActualizado.getTitulo().trim();

        if (tituloOriginal.trim().equals(nuevoTitulo)) {
            // Mismo título: sobreescribir en el mismo offset
            archivoManager.escribirRegistro(videojuegoActualizado, offsetActual);
        } else {
            // Título cambia: marcar registro viejo como eliminado en disco,
            Videojuego viejo = archivoManager.leerRegistro(offsetActual);
            if (viejo != null) {
                viejo.marcarEliminado();
                archivoManager.escribirRegistro(viejo, offsetActual);
            }

            // Eliminar la entrada antigua del índice e insertar la nueva
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
        return true;
    }

    // === Métodos auxiliares ==================================================

    /**
     * Recorre el árbol recursivamente buscando la clave exacta y, si la
     * encuentra en una hoja, escribe su offset en offsetWrapper[0].
     *
     * @param nodo          nodo actual de la recursión
     * @param clave         clave a buscar
     * @param offsetWrapper arreglo de un elemento donde se deposita el offset
     */
    private void buscarOffsetEnArbol(NodoBPlus nodo, String clave, long[] offsetWrapper) {
        if (nodo == null) return;

        if (nodo.esHoja()) {
            for (int i = 0; i < nodo.getNumClaves(); i++) {
                if (clave.equals(nodo.getClave(i))) {
                    offsetWrapper[0] = nodo.getOffset(i);
                    return;
                }
            }
        } else {
            int posHijo = nodo.buscarPosicionHijo(clave);
            buscarOffsetEnArbol(nodo.getHijo(posHijo), clave, offsetWrapper);
        }
    }

    /**
     * Elimina la entrada (clave + offset) de la hoja correspondiente en el
     * índice. No rebalancea el árbol; solo limpia la referencia del índice.
     *
     * @param nodo  raíz desde la que comenzar la búsqueda
     * @param clave clave a eliminar del índice
     */
    private void eliminarEntradaIndice(NodoBPlus nodo, String clave) {
        NodoBPlus hoja = buscarHoja(nodo, clave);
        if (hoja == null) return;

        for (int i = 0; i < hoja.getNumClaves(); i++) {
            if (clave.equals(hoja.getClave(i))) {
                hoja.eliminarClave(i);
                hoja.eliminarOffset(i);
                return;
            }
        }
    }

    /** Devuelve la raíz del árbol. */
    public NodoBPlus getRaiz() {
        return raiz;
    }

    /**
     * Devuelve la primera hoja del árbol (la más a la izquierda).
     */
    public NodoBPlus getPrimeraHoja() {
        return buscarHojaMasIzquierda();
    }

    public void cerrar() {
        indiceManager.cerrar();
    }
}
