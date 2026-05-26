package gameindex.gui;

import gameindex.storage.ArchivoManager;
import gameindex.tree.BPlusTree;
import gameindex.tree.NodoBPlus;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.RenderingHints;
import java.util.List;
import java.util.ArrayList;

public class PanelArbol extends JPanel {

    private final ArchivoManager   archivoManager;
    private final BPlusTree        bPlusTree;
    private final VentanaPrincipal ventana;

    private List<List<List<String>>> niveles = new ArrayList<>();

    private static final int    NODE_H        = 36;   // alto del nodo
    private static final int    KEY_W         = 72;   // ancho por clave
    private static final int    NODE_ARC      = 8;    // radio de esquinas
    private static final int    LEVEL_GAP_Y   = 90;   // separación vertical entre niveles
    private static final int    MIN_GAP_X     = 24;   // separación mínima horizontal entre nodos

    // Colores propios del panel
    private static final Color COL_INTERNAL_BG   = new Color(30,  45,  70);
    private static final Color COL_INTERNAL_BD   = new Color(88, 166, 255);
    private static final Color COL_LEAF_BG        = new Color(20,  50,  35);
    private static final Color COL_LEAF_BD        = new Color(63, 185,  80);
    private static final Color COL_KEY_TEXT       = new Color(230, 237, 243);
    private static final Color COL_KEY_SEP        = new Color(48,  54,  61);
    private static final Color COL_EDGE           = new Color(88, 166, 255, 140);
    private static final Color COL_CHAIN          = new Color(63, 185,  80, 160);
    private static final Color COL_HOVER_BG       = new Color(56, 139, 253,  60);
    private static final Color COL_LABEL_LEVEL    = new Color(110, 118, 129);
    private static final Color COL_BG_CANVAS      = new Color(13,  17,  23);

    private double zoom      = 1.0;
    private double panX      = 0;
    private double panY      = 40;
    private int    dragStartX, dragStartY;
    private double panStartX, panStartY;
    private boolean dragging = false;

    private NodeRect hoveredNode = null;

    private final List<NodeRect> nodeRects = new ArrayList<>();

    public PanelArbol(ArchivoManager am, BPlusTree bt, VentanaPrincipal vp) {
        this.archivoManager = am;
        this.bPlusTree      = bt;
        this.ventana        = vp;
        construirUI();
        configurarInteraccion();
    }

    private void construirUI() {
        setLayout(new BorderLayout());
        setBackground(Tema.BG_SURFACE);
        setBorder(new EmptyBorder(32, 40, 32, 40));

        add(crearEncabezado(), BorderLayout.NORTH);
        add(crearCanvas(),     BorderLayout.CENTER);
    }

    private JPanel crearEncabezado() {
        JPanel p = new JPanel(new BorderLayout(16, 0));
        p.setBackground(Tema.BG_SURFACE);
        p.setBorder(new EmptyBorder(0, 0, 16, 0));

        JPanel textos = new JPanel();
        textos.setLayout(new BoxLayout(textos, BoxLayout.Y_AXIS));
        textos.setBackground(Tema.BG_SURFACE);

        JLabel titulo = new JLabel("Visualizar Árbol B+");
        titulo.setFont(Tema.FONT_TITLE);
        titulo.setForeground(Tema.TEXT_PRIMARY);

        JLabel sub = new JLabel("Scroll = zoom  ·  Arrastrar = mover  ·  Hover = ver claves");
        sub.setFont(Tema.FONT_SMALL);
        sub.setForeground(Tema.TEXT_MUTED);

        textos.add(titulo);
        textos.add(Box.createVerticalStrut(4));
        textos.add(sub);

        // Controles derechos
        JPanel controles = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        controles.setBackground(Tema.BG_SURFACE);

        JButton btnRefrescar = crearBoton("Refrescar", Tema.ACCENT, Color.WHITE);
        JButton btnCentrar   = crearBoton("Centrar",   Tema.BG_CARD, Tema.TEXT_MUTED);
        JButton btnZoomIn    = crearBoton("+",             Tema.BG_CARD, Tema.TEXT_MUTED);
        JButton btnZoomOut   = crearBoton("−",             Tema.BG_CARD, Tema.TEXT_MUTED);

        btnZoomIn .setPreferredSize(new Dimension(36, 34));
        btnZoomOut.setPreferredSize(new Dimension(36, 34));

        btnRefrescar.addActionListener(e -> refrescar());
        btnCentrar  .addActionListener(e -> centrar());
        btnZoomIn   .addActionListener(e -> { zoom = Math.min(zoom * 1.2, 4.0); canvas.repaint(); });
        btnZoomOut  .addActionListener(e -> { zoom = Math.max(zoom / 1.2, 0.2); canvas.repaint(); });

        controles.add(btnZoomOut);
        controles.add(btnZoomIn);
        controles.add(btnCentrar);
        controles.add(btnRefrescar);

        // Leyenda
        JPanel leyenda = crearLeyenda();

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

    private JPanel crearLeyenda() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        p.setBackground(Tema.BG_SURFACE);
        p.add(itemLeyenda(COL_INTERNAL_BD, "Nodo interno"));
        p.add(itemLeyenda(COL_LEAF_BD,     "Nodo hoja"));
        p.add(itemLeyenda(COL_CHAIN,       "Encadenamiento hojas"));
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
        dot.setPreferredSize(new Dimension(14, 18));
        dot.setBackground(Tema.BG_SURFACE);

        JLabel lbl = new JLabel(texto);
        lbl.setFont(Tema.FONT_SMALL);
        lbl.setForeground(Tema.TEXT_MUTED);

        p.add(dot);
        p.add(lbl);
        return p;
    }

    private ArbolCanvas canvas;

    private JScrollPane crearCanvas() {
        canvas = new ArbolCanvas();
        canvas.setBackground(COL_BG_CANVAS);
        canvas.setPreferredSize(new Dimension(2000, 1200));

        JScrollPane scroll = new JScrollPane(canvas);
        scroll.setBorder(BorderFactory.createLineBorder(Tema.BORDER));
        scroll.getViewport().setBackground(COL_BG_CANVAS);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        return scroll;
    }

    private void configurarInteraccion() {
        canvas.addMouseWheelListener(e -> {
            double factor = e.getWheelRotation() < 0 ? 1.1 : 0.9;
            zoom = Math.max(0.2, Math.min(4.0, zoom * factor));
            canvas.repaint();
        });

        canvas.addMouseListener(new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) {
                dragging   = true;
                dragStartX = e.getX();
                dragStartY = e.getY();
                panStartX  = panX;
                panStartY  = panY;
                canvas.setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
            }
            @Override public void mouseReleased(MouseEvent e) {
                dragging = false;
                canvas.setCursor(Cursor.getDefaultCursor());
            }
        });

        canvas.addMouseMotionListener(new MouseMotionAdapter() {
            @Override public void mouseDragged(MouseEvent e) {
                if (dragging) {
                    panX = panStartX + (e.getX() - dragStartX);
                    panY = panStartY + (e.getY() - dragStartY);
                    canvas.repaint();
                }
            }
            @Override public void mouseMoved(MouseEvent e) {
                // Hit-test para hover
                NodeRect found = null;
                double mx = (e.getX() - panX) / zoom;
                double my = (e.getY() - panY) / zoom;
                for (NodeRect nr : nodeRects) {
                    if (nr.rect.contains(mx, my)) { found = nr; break; }
                }
                if (found != hoveredNode) {
                    hoveredNode = found;
                    canvas.repaint();
                }
                // Tooltip
                if (found != null) {
                    canvas.setToolTipText(found.tooltip);
                } else {
                    canvas.setToolTipText(null);
                }
            }
        });
    }

    public void refrescar() {
        try {
            niveles = new ArrayList<>();
            NodoBPlus raiz = bPlusTree.getRaiz();
            if (raiz != null) {
                // BFS por niveles para extraer las claves de cada nodo
                List<NodoBPlus> nivelActual = new ArrayList<>();
                nivelActual.add(raiz);
                while (!nivelActual.isEmpty()) {
                    List<List<String>> clavesDeNivel = new ArrayList<>();
                    List<NodoBPlus> siguienteNivel  = new ArrayList<>();
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
            calcularLayout();
            canvas.repaint();
            ventana.setStatusOk("Árbol actualizado — " + niveles.size() + " nivel(es)");
        } catch (Exception ex) {
            niveles = new ArrayList<>();
            canvas.repaint();
            ventana.setStatusError("Error al cargar el árbol: " + ex.getMessage());
        }
    }

    private void centrar() {
        zoom = 1.0;
        panX = 40;
        panY = 40;
        canvas.repaint();
    }

    private void calcularLayout() {
        nodeRects.clear();
        if (niveles == null || niveles.isEmpty()) return;

        int totalLevels = niveles.size();
        for (int lvl = 0; lvl < totalLevels; lvl++) {
            List<List<String>> nodos = niveles.get(lvl);
            int n = nodos.size();

            // Calcular ancho de cada nodo y ancho total del nivel
            int totalWidth = 0;
            List<Integer> nodeWidths = new ArrayList<>();
            for (List<String> keys : nodos) {
                int w = Math.max(keys.size(), 1) * KEY_W + 16;
                nodeWidths.add(w);
                totalWidth += w + MIN_GAP_X;
            }
            totalWidth -= MIN_GAP_X;

            int startX = Math.max(40, (1800 - totalWidth) / 2);
            int y      = lvl * LEVEL_GAP_Y + 10;
            boolean esHoja = (lvl == totalLevels - 1);

            int cx = startX;
            for (int i = 0; i < n; i++) {
                List<String> keys = nodos.get(i);
                int w = nodeWidths.get(i);
                Rectangle2D.Double rect = new Rectangle2D.Double(cx, y, w, NODE_H);

                StringBuilder tip = new StringBuilder(
                        "<html><b>" + (esHoja ? "Hoja" : "Interno") + "</b><br>");
                for (String k : keys) tip.append("· ").append(k).append("<br>");
                tip.append("</html>");

                nodeRects.add(new NodeRect(rect, keys, esHoja, tip.toString(), lvl, i));
                cx += w + MIN_GAP_X;
            }
        }
    }

    private class ArbolCanvas extends JPanel {

        ArbolCanvas() {
            setBackground(COL_BG_CANVAS);
            ToolTipManager.sharedInstance().registerComponent(this);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,  RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            // Fondo con patrón de puntos
            dibujarFondo(g2);

            // Transformación global (zoom + pan)
            g2.translate(panX, panY);
            g2.scale(zoom, zoom);

            if (nodeRects.isEmpty()) {
                dibujarVacio(g2);
            } else {
                dibujarAristas(g2);
                dibujarEncadenamiento(g2);
                dibujarNodos(g2);
                dibujarEtiquetasNivel(g2);
            }

            g2.dispose();
        }

        // Fondo con grid de puntos
        private void dibujarFondo(Graphics2D g2) {
            g2.setColor(COL_BG_CANVAS);
            g2.fillRect(0, 0, getWidth(), getHeight());

            g2.setColor(new Color(30, 37, 46));
            int spacing = 28;
            for (int x = 0; x < getWidth(); x += spacing)
                for (int y = 0; y < getHeight(); y += spacing)
                    g2.fillOval(x - 1, y - 1, 2, 2);
        }

        private void dibujarVacio(Graphics2D g2) {
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 16));
            g2.setColor(Tema.TEXT_MUTED);
            g2.drawString("El árbol está vacío — inserta registros y presiona Refrescar", 60, 80);
        }

        private void dibujarAristas(Graphics2D g2) {
            if (niveles.size() < 2) return;

            g2.setStroke(new BasicStroke(1.5f));
            g2.setColor(COL_EDGE);

            int totalLevels = niveles.size();
            for (int lvl = 0; lvl < totalLevels - 1; lvl++) {
                List<NodeRect> padres = nodeRectsDeNivel(lvl);
                List<NodeRect> hijos  = nodeRectsDeNivel(lvl + 1);
                if (padres.isEmpty() || hijos.isEmpty()) continue;

                // Distribución uniforme de hijos entre padres
                int hijosPorPadre = (int) Math.ceil((double) hijos.size() / padres.size());

                for (int pi = 0; pi < padres.size(); pi++) {
                    NodeRect padre = padres.get(pi);
                    double px = padre.rect.getCenterX();
                    double py = padre.rect.getMaxY();

                    int inicio = pi * hijosPorPadre;
                    int fin    = Math.min(inicio + hijosPorPadre, hijos.size());

                    for (int hi = inicio; hi < fin; hi++) {
                        NodeRect hijo = hijos.get(hi);
                        double hx = hijo.rect.getCenterX();
                        double hy = hijo.rect.getMinY();

                        // Línea curva tipo Bezier
                        CubicCurve2D curve = new CubicCurve2D.Double(
                                px, py,
                                px, py + LEVEL_GAP_Y * 0.4,
                                hx, hy - LEVEL_GAP_Y * 0.4,
                                hx, hy
                        );
                        g2.draw(curve);

                        // Flecha pequeña en la punta
                        dibujarFlecha(g2, hx, hy, hx, hy - 10, COL_EDGE);
                    }
                }
            }
        }

        // Líneas horizontales entre hojas (encadenamiento)
        private void dibujarEncadenamiento(Graphics2D g2) {
            if (niveles.isEmpty()) return;
            List<NodeRect> hojas = nodeRectsDeNivel(niveles.size() - 1);
            if (hojas.size() < 2) return;

            g2.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND,
                    0, new float[]{6, 4}, 0));
            g2.setColor(COL_CHAIN);

            for (int i = 0; i < hojas.size() - 1; i++) {
                NodeRect a = hojas.get(i);
                NodeRect b = hojas.get(i + 1);

                double ax = a.rect.getMaxX();
                double ay = a.rect.getCenterY();
                double bx = b.rect.getMinX();
                double by = b.rect.getCenterY();

                g2.drawLine((int) ax, (int) ay, (int) bx, (int) by);
                dibujarFlecha(g2, bx, by, ax, ay, COL_CHAIN);
            }
            g2.setStroke(new BasicStroke(1.5f));
        }

        // Nodos
        private void dibujarNodos(Graphics2D g2) {
            Font fontKey = new Font("Consolas", Font.BOLD, 11);
            g2.setFont(fontKey);

            for (NodeRect nr : nodeRects) {
                boolean hovered = (nr == hoveredNode);
                Color bgColor  = nr.esHoja ? COL_LEAF_BG     : COL_INTERNAL_BG;
                Color bdColor  = nr.esHoja ? COL_LEAF_BD      : COL_INTERNAL_BD;

                int x = (int) nr.rect.x;
                int y = (int) nr.rect.y;
                int w = (int) nr.rect.width;
                int h = (int) nr.rect.height;

                // Sombra suave
                g2.setColor(new Color(0, 0, 0, 60));
                g2.fillRoundRect(x + 3, y + 3, w, h, NODE_ARC, NODE_ARC);

                // Fondo del nodo
                g2.setColor(bgColor);
                g2.fillRoundRect(x, y, w, h, NODE_ARC, NODE_ARC);

                // Hover overlay
                if (hovered) {
                    g2.setColor(COL_HOVER_BG);
                    g2.fillRoundRect(x, y, w, h, NODE_ARC, NODE_ARC);
                }

                // Borde
                g2.setStroke(new BasicStroke(hovered ? 2f : 1.5f));
                g2.setColor(bdColor);
                g2.drawRoundRect(x, y, w, h, NODE_ARC, NODE_ARC);

                // Claves dentro del nodo
                FontMetrics fm = g2.getFontMetrics(fontKey);
                int nKeys = nr.claves.size();
                int kw    = nKeys > 0 ? (w - 8) / nKeys : w - 8;

                for (int i = 0; i < nKeys; i++) {
                    String key  = nr.claves.get(i);
                    int    kx   = x + 4 + i * kw;

                    // Separador entre claves
                    if (i > 0) {
                        g2.setColor(COL_KEY_SEP);
                        g2.setStroke(new BasicStroke(1f));
                        g2.drawLine(kx, y + 6, kx, y + h - 6);
                    }

                    // Texto de la clave (truncado si es largo)
                    String display = key.length() > 9 ? key.substring(0, 8) + "…" : key;
                    int    tw      = fm.stringWidth(display);
                    int    tx      = kx + (kw - tw) / 2;
                    int    ty      = y + (h + fm.getAscent() - fm.getDescent()) / 2;

                    g2.setColor(COL_KEY_TEXT);
                    g2.setFont(fontKey);
                    g2.drawString(display, tx, ty);
                }

                // Nodo vacío (raíz recién creada)
                if (nKeys == 0) {
                    g2.setColor(Tema.TEXT_MUTED);
                    g2.setFont(new Font("Segoe UI", Font.ITALIC, 11));
                    g2.drawString("vacío", x + 8, y + h / 2 + 4);
                }
            }
        }

        private void dibujarEtiquetasNivel(Graphics2D g2) {
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            g2.setColor(COL_LABEL_LEVEL);

            int totalLevels = niveles.size();
            for (int lvl = 0; lvl < totalLevels; lvl++) {
                int y = lvl * LEVEL_GAP_Y + 10 + NODE_H / 2 + 4;
                boolean esHoja = (lvl == totalLevels - 1);
                String  label  = esHoja ? "Hojas (N" + lvl + ")" : "Nivel " + lvl;
                g2.drawString(label, 4, y);
            }
        }

        // Flecha pequeña en la dirección de destino
        private void dibujarFlecha(Graphics2D g2, double tx, double ty,
                                   double fx, double fy, Color color) {
            double angle  = Math.atan2(ty - fy, tx - fx);
            int    size   = 6;
            int[]  arrowX = {
                    (int) tx,
                    (int) (tx - size * Math.cos(angle - 0.4)),
                    (int) (tx - size * Math.cos(angle + 0.4))
            };
            int[]  arrowY = {
                    (int) ty,
                    (int) (ty - size * Math.sin(angle - 0.4)),
                    (int) (ty - size * Math.sin(angle + 0.4))
            };
            g2.setColor(color);
            g2.setStroke(new BasicStroke(1f));
            g2.fillPolygon(arrowX, arrowY, 3);
        }
    }

    private List<NodeRect> nodeRectsDeNivel(int lvl) {
        List<NodeRect> res = new ArrayList<>();
        for (NodeRect nr : nodeRects)
            if (nr.nivel == lvl) res.add(nr);
        return res;
    }

    private JButton crearBoton(String texto, Color bg, Color fg) {
        if (bg == Tema.ACCENT) return Tema.botonPrimario(texto);
        return Tema.botonSecundario(texto);
    }

    private static class NodeRect {
        final Rectangle2D.Double rect;
        final List<String>       claves;
        final boolean            esHoja;
        final String             tooltip;
        final int                nivel;
        final int                indice;

        NodeRect(Rectangle2D.Double rect, List<String> claves,
                 boolean esHoja, String tooltip, int nivel, int indice) {
            this.rect    = rect;
            this.claves  = claves;
            this.esHoja  = esHoja;
            this.tooltip = tooltip;
            this.nivel   = nivel;
            this.indice  = indice;
        }
    }
}