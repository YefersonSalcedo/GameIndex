package gameindex.gui;

import gameindex.model.Videojuego;
import gameindex.storage.ArchivoManager;
import gameindex.tree.BPlusTree;
import gameindex.tree.NodoBPlus;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PanelArbol extends JPanel {

    private final ArchivoManager   archivoManager;
    private final BPlusTree        bPlusTree;
    private final VentanaPrincipal ventana;

    private List<List<List<String>>> niveles          = new ArrayList<>();
    private Set<String>              clavesEliminadas = new HashSet<>();

    // Colores
    private static final Color COL_INTERNAL_BG = new Color(30,  45,  70);
    private static final Color COL_INTERNAL_BD = new Color(88, 166, 255);
    private static final Color COL_LEAF_BG     = new Color(20,  50,  35);
    private static final Color COL_LEAF_BD     = new Color(63, 185,  80);
    private static final Color COL_KEY_TEXT    = new Color(230, 237, 243);
    private static final Color COL_BG_CANVAS  = new Color(13,  17,  23);

    // Panel donde se muestran los niveles en texto
    private JPanel panelNiveles;

    public PanelArbol(ArchivoManager am, BPlusTree bt, VentanaPrincipal vp) {
        this.archivoManager = am;
        this.bPlusTree      = bt;
        this.ventana        = vp;
        construirUI();
    }

    private void construirUI() {
        setLayout(new BorderLayout());
        setBackground(Tema.BG_SURFACE);
        setBorder(new EmptyBorder(36, 48, 36, 48));

        add(crearEncabezado(), BorderLayout.NORTH);
        add(crearAreaNiveles(), BorderLayout.CENTER);
    }

    // === Encabezado ==========================================================

    private JPanel crearEncabezado() {
        JPanel p = new JPanel(new BorderLayout(16, 0));
        p.setBackground(Tema.BG_SURFACE);
        p.setBorder(new EmptyBorder(0, 0, 16, 0));

        JPanel textos = new JPanel();
        textos.setLayout(new BoxLayout(textos, BoxLayout.Y_AXIS));
        textos.setBackground(Tema.BG_SURFACE);

        JLabel titulo = new JLabel("Niveles del Árbol B+");
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 28));
        titulo.setForeground(Tema.TEXT_PRIMARY);

        JLabel sub = new JLabel("Cada nivel muestra sus nodos con las claves que contienen");
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        sub.setForeground(Tema.TEXT_MUTED);

        textos.add(titulo);
        textos.add(Box.createVerticalStrut(4));
        textos.add(sub);

        // Botón refrescar
        JPanel controles = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        controles.setBackground(Tema.BG_SURFACE);

        JButton btnRefrescar = Tema.botonPrimario("Refrescar");
        btnRefrescar.addActionListener(e -> refrescar());
        controles.add(btnRefrescar);

        // Leyenda
        JPanel leyenda = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        leyenda.setBackground(Tema.BG_SURFACE);
        leyenda.add(itemLeyenda(COL_INTERNAL_BD, "Nodo interno"));
        leyenda.add(itemLeyenda(COL_LEAF_BD,     "Nodo hoja"));

        JPanel derecha = new JPanel();
        derecha.setLayout(new BoxLayout(derecha, BoxLayout.Y_AXIS));
        derecha.setBackground(Tema.BG_SURFACE);
        derecha.add(controles);
        derecha.add(Box.createVerticalStrut(6));
        derecha.add(leyenda);

        p.add(textos,  BorderLayout.WEST);
        p.add(derecha, BorderLayout.EAST);
        return p;
    }

    private JPanel itemLeyenda(Color color, String texto) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        p.setBackground(Tema.BG_SURFACE);

        JPanel dot = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.setColor(color);
                g.fillRoundRect(0, 3, 12, 12, 4, 4);
            }
        };
        dot.setPreferredSize(new Dimension(16, 22));
        dot.setBackground(Tema.BG_SURFACE);

        JLabel lbl = new JLabel(texto);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lbl.setForeground(Tema.TEXT_MUTED);

        p.add(dot);
        p.add(lbl);
        return p;
    }

    // === Área de niveles =====================================================

    private JScrollPane crearAreaNiveles() {
        panelNiveles = new JPanel();
        panelNiveles.setLayout(new BoxLayout(panelNiveles, BoxLayout.Y_AXIS));
        panelNiveles.setBackground(COL_BG_CANVAS);
        panelNiveles.setBorder(new EmptyBorder(32, 32, 32, 32));

        JScrollPane scroll = new JScrollPane(panelNiveles);
        scroll.setBorder(BorderFactory.createLineBorder(Tema.BORDER));
        scroll.getViewport().setBackground(COL_BG_CANVAS);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

        scroll.getVerticalScrollBar().setUnitIncrement(20);
        scroll.getHorizontalScrollBar().setUnitIncrement(20);

        return scroll;
    }

    // === Refrescar ===========================================================

    public void refrescar() {
        try {
            niveles          = new ArrayList<>();
            clavesEliminadas = new HashSet<>();

            NodoBPlus raiz = bPlusTree.getRaiz();
            boolean arbolConDatos = raiz != null && !raiz.getClaves().isEmpty();

            if (arbolConDatos) {
                // Recorrer hojas para detectar claves eliminadas lógicamente.
                // CORRECCIÓN: se reemplazó archivoManager.estaEliminado(offset)
                // (método eliminado de ArchivoManager) por leer el registro
                // directamente y llamar v.estaEliminado(), igual que el resto
                // del proyecto.
                NodoBPlus cursor = bPlusTree.getPrimeraHoja();
                while (cursor != null) {
                    List<String> claves  = cursor.getClaves();
                    List<Long>   offsets = cursor.getOffsets();
                    for (int i = 0; i < claves.size(); i++) {
                        Videojuego v = archivoManager.leerRegistro(offsets.get(i));
                        if (v != null && v.estaEliminado()) {
                            clavesEliminadas.add(claves.get(i));
                        }
                    }
                    cursor = cursor.getSiguienteHoja();
                }

                // Construir niveles por BFS
                List<NodoBPlus> nivelActual = new ArrayList<>();
                nivelActual.add(raiz);
                while (!nivelActual.isEmpty()) {
                    List<List<String>> clavesDeNivel  = new ArrayList<>();
                    List<NodoBPlus>    siguienteNivel = new ArrayList<>();
                    for (NodoBPlus nodo : nivelActual) {
                        clavesDeNivel.add(new ArrayList<>(nodo.getClaves()));
                        if (!nodo.esHoja()) {
                            siguienteNivel.addAll(nodo.getHijos());
                        }
                    }
                    niveles.add(clavesDeNivel);
                    nivelActual = siguienteNivel;
                }
            }

            renderizarNiveles();
            String msg = niveles.isEmpty()
                    ? "El árbol está vacío"
                    : "Árbol actualizado — " + niveles.size() + " nivel(es)";
            ventana.setStatusOk(msg);
        } catch (Exception ex) {
            niveles = new ArrayList<>();
            renderizarNiveles();
            ventana.setStatusError("Error al cargar el árbol: " + ex.getMessage());
        }
    }

    // === Renderizado =========================================================

    private void renderizarNiveles() {
        panelNiveles.removeAll();

        if (niveles.isEmpty()) {
            JLabel vacio = new JLabel("El árbol está vacío — inserta registros y presiona Refrescar");
            vacio.setFont(new Font("Segoe UI", Font.ITALIC, 16));
            vacio.setForeground(Tema.TEXT_MUTED);
            vacio.setAlignmentX(Component.LEFT_ALIGNMENT);
            panelNiveles.add(Box.createVerticalStrut(40));
            panelNiveles.add(vacio);
            panelNiveles.revalidate();
            panelNiveles.repaint();
            return;
        }

        int totalNiveles = niveles.size();

        for (int lvl = 0; lvl < totalNiveles; lvl++) {
            boolean            esHoja = (lvl == totalNiveles - 1);
            List<List<String>> nodos  = niveles.get(lvl);

            // Etiqueta del nivel
            String plural  = nodos.size() != 1 ? "s" : "";
            String tipo    = esHoja ? "Hojas" : "Internos";
            String etiqueta = "Nivel " + lvl + "  —  " + tipo + "  (" + nodos.size() + " nodo" + plural + ")";

            JLabel lblNivel = new JLabel(etiqueta);
            lblNivel.setFont(new Font("Segoe UI", Font.BOLD, 15));
            lblNivel.setForeground(esHoja ? COL_LEAF_BD : COL_INTERNAL_BD);
            lblNivel.setAlignmentX(Component.LEFT_ALIGNMENT);
            lblNivel.setBorder(new EmptyBorder(0, 0, 10, 0));
            panelNiveles.add(lblNivel);

            // Fila de nodos
            JPanel filaPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 6));
            filaPanel.setBackground(COL_BG_CANVAS);
            filaPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

            for (int i = 0; i < nodos.size(); i++) {
                filaPanel.add(crearTarjetaNodo(nodos.get(i), esHoja, i));
            }

            panelNiveles.add(filaPanel);

            // Separador entre niveles (excepto el último)
            if (lvl < totalNiveles - 1) {
                panelNiveles.add(Box.createVerticalStrut(12));
                JSeparator sep = new JSeparator(JSeparator.HORIZONTAL);
                sep.setForeground(new Color(40, 50, 65));
                sep.setBackground(new Color(40, 50, 65));
                sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
                sep.setAlignmentX(Component.LEFT_ALIGNMENT);
                panelNiveles.add(sep);
                panelNiveles.add(Box.createVerticalStrut(16));
            }
        }

        panelNiveles.add(Box.createVerticalGlue());
        panelNiveles.revalidate();
        panelNiveles.repaint();
    }

    private JPanel crearTarjetaNodo(List<String> claves, boolean esHoja, int indice) {
        Color bgColor = esHoja ? COL_LEAF_BG : COL_INTERNAL_BG;
        Color bdColor = esHoja ? COL_LEAF_BD : COL_INTERNAL_BD;

        JPanel tarjeta = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(bgColor);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.setColor(bdColor);
                g2.setStroke(new BasicStroke(1.8f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
                g2.dispose();
            }
        };
        tarjeta.setOpaque(false);
        tarjeta.setLayout(new BoxLayout(tarjeta, BoxLayout.Y_AXIS));
        tarjeta.setBorder(new EmptyBorder(12, 16, 12, 16));

        // Encabezado pequeño: "Hoja N" / "Nodo N"
        JLabel lblIdx = new JLabel((esHoja ? "Hoja" : "Nodo") + " " + indice);
        lblIdx.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblIdx.setForeground(new Color(bdColor.getRed(), bdColor.getGreen(), bdColor.getBlue(), 200));
        lblIdx.setAlignmentX(Component.CENTER_ALIGNMENT);
        tarjeta.add(lblIdx);
        tarjeta.add(Box.createVerticalStrut(6));

        // Chips de claves
        JPanel chipsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 0));
        chipsPanel.setOpaque(false);

        if (claves.isEmpty()) {
            JLabel vacio = new JLabel("vacío");
            vacio.setFont(new Font("Consolas", Font.ITALIC, 13));
            vacio.setForeground(Tema.TEXT_MUTED);
            chipsPanel.add(vacio);
        } else {
            for (String clave : claves) {
                chipsPanel.add(crearChipClave(clave, bdColor, clavesEliminadas.contains(clave)));
            }
        }

        tarjeta.add(chipsPanel);

        int anchoMin = Math.max(claves.size() * 155 + 32, 180);
        tarjeta.setPreferredSize(new Dimension(anchoMin, 72));
        tarjeta.setMaximumSize(new Dimension(anchoMin, 72));

        return tarjeta;
    }

    private JLabel crearChipClave(String clave, Color bdColor, boolean eliminada) {
        String display = clave.length() > 18 ? clave.substring(0, 17) + "…" : clave;

        JLabel lbl = new JLabel(display, SwingConstants.CENTER) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color fondoChip = eliminada
                        ? new Color(80, 20, 20, 120)
                        : new Color(bdColor.getRed(), bdColor.getGreen(), bdColor.getBlue(), 35);
                g2.setColor(fondoChip);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 6, 6);
                if (eliminada) {
                    g2.setColor(new Color(220, 80, 80, 180));
                    g2.setStroke(new BasicStroke(1.5f));
                    int midY = getHeight() / 2;
                    g2.drawLine(8, midY, getWidth() - 8, midY);
                }
                g2.dispose();
                super.paintComponent(g);
            }
        };

        lbl.setFont(new Font("Consolas", Font.BOLD, 14));
        lbl.setForeground(eliminada ? new Color(180, 100, 100) : COL_KEY_TEXT);
        lbl.setOpaque(false);
        lbl.setBorder(new EmptyBorder(3, 8, 3, 8));
        lbl.setPreferredSize(new Dimension(140, 26));
        lbl.setToolTipText(eliminada
                ? clave + "  [eliminado lógicamente — clave preservada en el índice]"
                : clave);
        return lbl;
    }
}