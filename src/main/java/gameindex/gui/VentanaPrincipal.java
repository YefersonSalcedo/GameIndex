package gameindex.gui;

import com.formdev.flatlaf.FlatDarkLaf;
import gameindex.storage.ArchivoManager;
import gameindex.tree.BPlusTree;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;

public class VentanaPrincipal extends JFrame {

    private ArchivoManager archivoManager;
    private BPlusTree      bPlusTree;

    private JPanel     sidebarPanel;
    private JPanel     contentPanel;
    private CardLayout cardLayout;
    private JLabel     statusLabel;
    private JLabel     recordCountLabel;
    private NavButton[] navButtons;

    private static final String[] PANEL_NAMES  = {
            "INSERTAR","BUSCAR","RANGO","FRANQUICIA","ACTUALIZAR","ELIMINAR","LISTAR","ARBOL"
    };
    private static final String[] PANEL_LABELS = {
            "Insertar","Buscar","Buscar Rango","Buscar Franquicia", "Actualizar","Eliminar","Listar Todo","Ver Arbol B+"
    };

    public VentanaPrincipal() {
        inicializarBackend();
        configurarVentana();
        construirUI();
        navegarA("INSERTAR");
        setVisible(true);
    }

    private void inicializarBackend() {
        try {
            java.io.File dir = new java.io.File("data");
            if (!dir.exists()) dir.mkdirs();
            archivoManager = new ArchivoManager();
            bPlusTree      = new BPlusTree(archivoManager);
        } catch (Exception e) {
            mostrarError("No se pudo inicializar:\n" + e.getMessage());
        }
    }

    private void configurarVentana() {
        setTitle("GameIndex — Árbol B+");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setMinimumSize(new Dimension(1060, 680));
        setPreferredSize(new Dimension(1280, 760));
        getContentPane().setBackground(Tema.BG_BASE);
        addWindowListener(new WindowAdapter() {
            @Override public void windowClosing(WindowEvent e) { confirmarCierre(); }
        });
        pack();
        setLocationRelativeTo(null);
    }

    private void construirUI() {
        setLayout(new BorderLayout(0, 0));
        add(crearSidebar(),   BorderLayout.WEST);
        add(crearContenido(), BorderLayout.CENTER);
        add(crearStatusBar(), BorderLayout.SOUTH);
    }

    private JPanel crearSidebar() {
        sidebarPanel = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                // Línea degradada derecha como separador visual
                Graphics2D g2 = (Graphics2D) g.create();
                GradientPaint gp = new GradientPaint(
                        getWidth()-1, 0,      new Color(99,102,241,180),
                        getWidth()-1, getHeight(), new Color(167,139,250,60)
                );
                g2.setPaint(gp);
                g2.fillRect(getWidth()-1, 0, 1, getHeight());
                g2.dispose();
            }
        };
        sidebarPanel.setLayout(new BoxLayout(sidebarPanel, BoxLayout.Y_AXIS));
        sidebarPanel.setBackground(Tema.BG_PANEL);
        sidebarPanel.setPreferredSize(new Dimension(230, 0));

        sidebarPanel.add(crearLogo());
        sidebarPanel.add(crearSeparador());
        sidebarPanel.add(Box.createVerticalStrut(10));

        navButtons = new NavButton[PANEL_NAMES.length];
        for (int i = 0; i < PANEL_NAMES.length; i++) {
            if (i == 6) {
                sidebarPanel.add(Box.createVerticalStrut(6));
                sidebarPanel.add(crearSeparador());
                sidebarPanel.add(Box.createVerticalStrut(6));
            }
            final String name = PANEL_NAMES[i];
            navButtons[i] = new NavButton(PANEL_LABELS[i]);
            navButtons[i].addActionListener(e -> navegarA(name));
            sidebarPanel.add(navButtons[i]);
            sidebarPanel.add(Box.createVerticalStrut(2));
        }

        sidebarPanel.add(Box.createVerticalGlue());
        sidebarPanel.add(crearSeparador());
        sidebarPanel.add(crearFooterSidebar());
        return sidebarPanel;
    }

    private JPanel crearLogo() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(Tema.BG_PANEL);
        p.setMaximumSize(new Dimension(230, 72));
        p.setBorder(new EmptyBorder(18, 20, 14, 16));

        JLabel title = new JLabel("GameIndex");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(Tema.TEXT_PRIMARY);

        JLabel sub = new JLabel("Árbol B+  -  Grupo 6");
        sub.setFont(Tema.FONT_SMALL);
        sub.setForeground(Tema.TEXT_MUTED);

        JPanel texts = new JPanel();
        texts.setLayout(new BoxLayout(texts, BoxLayout.Y_AXIS));
        texts.setBackground(Tema.BG_PANEL);
        texts.add(title);
        texts.add(Box.createVerticalStrut(2));
        texts.add(sub);

        JPanel accent = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, Tema.GRAD_START, 0, getHeight(), Tema.GRAD_END);
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 3, 3);
                g2.dispose();
            }
        };
        accent.setPreferredSize(new Dimension(4, 36));
        accent.setBackground(Tema.BG_PANEL);

        p.add(accent, BorderLayout.WEST);
        p.add(Box.createHorizontalStrut(12), BorderLayout.CENTER);
        p.add(texts, BorderLayout.EAST);

        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setBackground(Tema.BG_PANEL);
        wrap.setMaximumSize(new Dimension(230, 72));
        wrap.add(p);
        return wrap;
    }

    private JSeparator crearSeparador() {
        JSeparator sep = new JSeparator();
        sep.setForeground(Tema.BORDER);
        sep.setMaximumSize(new Dimension(230, 1));
        return sep;
    }

    private JPanel crearFooterSidebar() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 10));
        p.setBackground(Tema.BG_PANEL);
        p.setMaximumSize(new Dimension(230, 38));
        JLabel lbl = new JLabel("Estructuras de Datos");
        lbl.setFont(Tema.FONT_SMALL);
        lbl.setForeground(Tema.TEXT_SUBTLE);
        p.add(lbl);
        return p;
    }

    private JPanel crearContenido() {
        cardLayout   = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setBackground(Tema.BG_SURFACE);

        contentPanel.add(new PanelInsertar(archivoManager, bPlusTree, this),    "INSERTAR");
        contentPanel.add(new PanelBuscar(archivoManager, bPlusTree, this),      "BUSCAR");
        contentPanel.add(new PanelBuscarRango(archivoManager, bPlusTree, this), "RANGO");
        contentPanel.add(new PanelBuscarPrefijo(archivoManager, bPlusTree, this), "FRANQUICIA");
        contentPanel.add(new PanelActualizar(archivoManager, bPlusTree, this),  "ACTUALIZAR");
        contentPanel.add(new PanelEliminar(archivoManager, bPlusTree, this),    "ELIMINAR");
        contentPanel.add(new PanelListar(archivoManager, bPlusTree, this),      "LISTAR");
        contentPanel.add(new PanelArbol(archivoManager, bPlusTree, this),       "ARBOL");

        return contentPanel;
    }

    private JPanel crearStatusBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(Tema.BG_PANEL);
        bar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, Tema.BORDER),
                new EmptyBorder(8, 20, 8, 20)  // ← más padding vertical
        ));
        bar.setPreferredSize(new Dimension(0, 36));  // ← más alto

        statusLabel = new JLabel("Sistema listo");
        statusLabel.setFont(Tema.FONT_NAV);  // ← fuente más grande que FONT_SMALL
        statusLabel.setForeground(Tema.TEXT_MUTED);

        recordCountLabel = new JLabel("Registros: -");
        recordCountLabel.setFont(Tema.FONT_NAV);  // ← igual
        recordCountLabel.setForeground(Tema.TEXT_MUTED);

        bar.add(statusLabel,      BorderLayout.WEST);
        bar.add(recordCountLabel, BorderLayout.EAST);
        return bar;
    }

    private void mostrarToast(String msg, Color bgColor) {
        JDialog toast = new JDialog(this, false);
        toast.setUndecorated(true);
        toast.setBackground(new Color(0, 0, 0, 0));

        JLabel lbl = new JLabel(msg, SwingConstants.CENTER);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lbl.setForeground(Color.WHITE);
        lbl.setBorder(new EmptyBorder(14, 32, 14, 32));

        JPanel panel = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(bgColor);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                g2.dispose();
            }
        };
        panel.setOpaque(false);
        panel.add(lbl, BorderLayout.CENTER);

        toast.setContentPane(panel);
        toast.pack();

        // Centrado en la ventana, ligeramente abajo del centro
        int x = getX() + getWidth()  - toast.getWidth() - 20;
        int y = getY() + 60;;
        toast.setLocation(x, y);
        toast.setVisible(true);

        Timer t = new Timer(3000, e -> toast.dispose());
        t.setRepeats(false);
        t.start();
    }

    public void navegarA(String panelName) {
        cardLayout.show(contentPanel, panelName);
        for (int i = 0; i < PANEL_NAMES.length; i++)
            navButtons[i].setActivo(PANEL_NAMES[i].equals(panelName));
        int idx = java.util.Arrays.asList(PANEL_NAMES).indexOf(panelName);
        setStatus("Sección: " + (idx >= 0 ? PANEL_LABELS[idx] : panelName));
        actualizarConteoRegistros();
    }

    public void setStatus(String msg) {
        SwingUtilities.invokeLater(() -> {
            statusLabel.setText(msg);
            statusLabel.setForeground(Tema.TEXT_MUTED);
        });
    }

    public void setStatusOk(String msg) {
        SwingUtilities.invokeLater(() -> {
            statusLabel.setText(msg);
            statusLabel.setForeground(Tema.SUCCESS);
            mostrarToast(msg, new Color(16, 100, 60, 240));
            Timer t = new Timer(4000, e -> {
                statusLabel.setText("Sistema listo");
                statusLabel.setForeground(Tema.TEXT_MUTED);
            });
            t.setRepeats(false); t.start();
        });
    }

    public void setStatusError(String msg) {
        SwingUtilities.invokeLater(() -> {
            statusLabel.setText(msg);
            statusLabel.setForeground(Tema.DANGER);
            mostrarToast(msg, new Color(140, 20, 40, 240));
            Timer t = new Timer(5000, e -> {
                statusLabel.setText("Sistema listo");
                statusLabel.setForeground(Tema.TEXT_MUTED);
            });
            t.setRepeats(false); t.start();
        });
    }

    public void actualizarConteoRegistros() {
        SwingUtilities.invokeLater(() -> {
            try {
                if (archivoManager != null)
                    recordCountLabel.setText("Registros: " + bPlusTree.listarActivos().size());
            } catch (Exception ex) {
                recordCountLabel.setText("Registros: -");
            }
        });
    }

    public void mostrarError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private void confirmarCierre() {
        int r = JOptionPane.showConfirmDialog(this,
                "¿Deseas cerrar GameIndex?\nLos datos están guardados en disco.",
                "Confirmar salida", JOptionPane.YES_NO_OPTION);
        if (r == JOptionPane.YES_OPTION) {
            try { if (bPlusTree != null) bPlusTree.cerrar(); } catch (Exception ignored) {}
            try { if (archivoManager != null) archivoManager.cerrar(); } catch (Exception ignored) {}
            dispose(); System.exit(0);
        }
    }

    public ArchivoManager getArchivoManager() { return archivoManager; }
    public BPlusTree      getBPlusTree()       { return bPlusTree; }

    static class NavButton extends JButton {

        private boolean activo = false;

        NavButton(String label) {
            super(label);
            setFont(Tema.FONT_NAV);
            setForeground(Tema.TEXT_MUTED);
            setBackground(Tema.BG_PANEL);
            setBorderPainted(false);
            setFocusPainted(false);
            setContentAreaFilled(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setHorizontalAlignment(SwingConstants.LEFT);
            setBorder(new EmptyBorder(10, 24, 10, 16));
            setMaximumSize(new Dimension(230, 40));
            setPreferredSize(new Dimension(230, 40));

            addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) {
                    if (!activo) setForeground(Tema.TEXT_PRIMARY);
                }
                @Override public void mouseExited(MouseEvent e) {
                    if (!activo) setForeground(Tema.TEXT_MUTED);
                }
            });
        }

        public void setActivo(boolean v) {
            activo = v;
            setForeground(v ? Tema.ACCENT_BRIGHT : Tema.TEXT_MUTED);
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            if (activo) {
                // Fondo degradado sutil
                GradientPaint gp = new GradientPaint(
                        0, 0, new Color(99, 102, 241, 45),
                        getWidth(), 0, new Color(167, 139, 250, 20)
                );
                g2.setPaint(gp);
                g2.fillRoundRect(4, 1, getWidth()-8, getHeight()-2, 8, 8);

                // Barra izquierda con degradado
                GradientPaint bar = new GradientPaint(
                        0, 0,           Tema.GRAD_START,
                        0, getHeight(), Tema.GRAD_END
                );
                g2.setPaint(bar);
                g2.fillRoundRect(0, 4, 3, getHeight()-8, 3, 3);
            } else if (getModel().isRollover()) {
                g2.setColor(Tema.BG_HOVER);
                g2.fillRoundRect(4, 1, getWidth()-8, getHeight()-2, 8, 8);
            }

            g2.dispose();
            super.paintComponent(g);
        }
    }

    // ====================================================
    // Main
    // ====================================================
    public static void main(String[] args) {
        try {
            FlatDarkLaf.setup();
            Tema.aplicarUIManager();
        } catch (Exception e) {
            System.err.println("FlatLaf no disponible.");
        }
        SwingUtilities.invokeLater(VentanaPrincipal::new);
    }
}
