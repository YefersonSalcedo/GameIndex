package gameindex.gui;

import java.awt.*;

public final class Tema {

    private Tema() {}



    // Fondos
    public static final Color BG_BASE     = new Color( 8,  9, 18);   // casi negro azulado
    public static final Color BG_SURFACE  = new Color(13, 15, 30);   // fondo principal
    public static final Color BG_PANEL    = new Color(17, 20, 40);   // sidebar / barras
    public static final Color BG_CARD     = new Color(22, 26, 52);   // tarjetas / formularios
    public static final Color BG_INPUT    = new Color(15, 18, 36);   // campos de texto
    public static final Color BG_HOVER    = new Color(99, 102, 241, 35); // hover sutil

    // Acentos
    public static final Color ACCENT      = new Color(129, 140, 248); // indigo-400
    public static final Color ACCENT_BRIGHT = new Color(165, 180, 252); // indigo-300 (hover)
    public static final Color ACCENT_DIM  = new Color( 99, 102, 241, 55); // indigo translúcido
    public static final Color VIOLET      = new Color(167, 139, 250); // violet-400
    public static final Color VIOLET_DIM  = new Color(139, 92,  246, 50); // violet translúcido

    // Texto
    public static final Color TEXT_PRIMARY = new Color(224, 231, 255); // indigo-100
    public static final Color TEXT_MUTED   = new Color(100, 116, 139); // slate-500
    public static final Color TEXT_SUBTLE  = new Color( 71,  85, 105); // slate-600

    // Semánticos
    public static final Color SUCCESS     = new Color( 52, 211, 153); // emerald-400
    public static final Color DANGER      = new Color(251, 113, 133); // rose-400
    public static final Color WARNING     = new Color(251, 191,  36); // amber-400

    // Bordes
    public static final Color BORDER      = new Color(30,  35,  70);  // borde base
    public static final Color BORDER_FOCUS = new Color(99, 102, 241, 180); // borde activo

    // Gradientes (para uso con GradientPaint)
    public static final Color GRAD_START  = new Color( 99, 102, 241); // indigo-500
    public static final Color GRAD_END    = new Color(167, 139, 250); // violet-400

    // FUENTES
    public static final Font FONT_TITLE  = new Font("Segoe UI", Font.BOLD,  22);
    public static final Font FONT_NAV    = new Font("Segoe UI", Font.BOLD,  13);
    public static final Font FONT_BODY   = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_SMALL  = new Font("Segoe UI", Font.PLAIN, 11);
    public static final Font FONT_MONO   = new Font("Consolas",  Font.PLAIN, 12);
    public static final Font FONT_LABEL  = new Font("Segoe UI", Font.BOLD,  12);

    // UTILIDADES DE ESTILO

    public static javax.swing.border.Border bordeCard() {
        return javax.swing.BorderFactory.createCompoundBorder(
                javax.swing.BorderFactory.createLineBorder(BORDER, 1),
                new javax.swing.border.EmptyBorder(24, 28, 24, 28)
        );
    }

    public static javax.swing.border.Border bordeInput() {
        return javax.swing.BorderFactory.createCompoundBorder(
                javax.swing.BorderFactory.createLineBorder(BORDER, 1),
                new javax.swing.border.EmptyBorder(8, 11, 8, 11)
        );
    }

    public static void estilizarTextField(javax.swing.JTextField f) {
        f.setFont(FONT_BODY);
        f.setBackground(BG_INPUT);
        f.setForeground(TEXT_PRIMARY);
        f.setCaretColor(ACCENT);
        f.setBorder(bordeInput());
    }

    public static void estilizarTextArea(javax.swing.JTextArea ta) {
        ta.setFont(FONT_BODY);
        ta.setBackground(BG_INPUT);
        ta.setForeground(TEXT_PRIMARY);
        ta.setCaretColor(ACCENT);
        ta.setBorder(new javax.swing.border.EmptyBorder(8, 11, 8, 11));
        ta.setLineWrap(true);
        ta.setWrapStyleWord(true);
    }

    public static javax.swing.JLabel label(String texto) {
        javax.swing.JLabel l = new javax.swing.JLabel(texto);
        l.setFont(FONT_LABEL);
        l.setForeground(TEXT_PRIMARY);
        return l;
    }

    public static javax.swing.JLabel hint(String texto) {
        javax.swing.JLabel l = new javax.swing.JLabel(texto);
        l.setFont(FONT_SMALL);
        l.setForeground(TEXT_MUTED);
        return l;
    }

    public static javax.swing.JButton botonPrimario(String texto) {
        return new BotonPrimario(texto);
    }

    public static javax.swing.JButton botonSecundario(String texto) {
        javax.swing.JButton b = new javax.swing.JButton(texto);
        b.setFont(FONT_NAV);
        b.setBackground(BG_CARD);
        b.setForeground(TEXT_MUTED);
        b.setBorderPainted(true);
        b.setBorder(javax.swing.BorderFactory.createLineBorder(BORDER, 1));
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setPreferredSize(new Dimension(110, 38));
        return b;
    }

    public static javax.swing.JButton botonDanger(String texto) {
        javax.swing.JButton b = new javax.swing.JButton(texto);
        b.setFont(FONT_NAV);
        b.setBackground(new Color(190, 18, 60));
        b.setForeground(Color.WHITE);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setPreferredSize(new Dimension(140, 38));
        return b;
    }

    public static class BotonPrimario extends javax.swing.JButton {
        public BotonPrimario(String texto) {
            super(texto);
            setFont(FONT_NAV);
            setForeground(Color.WHITE);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setPreferredSize(new Dimension(180, 38));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            boolean hover = getModel().isRollover();
            Color c1 = hover ? ACCENT_BRIGHT : GRAD_START;
            Color c2 = hover ? new Color(192, 132, 252) : GRAD_END;

            GradientPaint gp = new GradientPaint(0, 0, c1, getWidth(), 0, c2);
            g2.setPaint(gp);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);

            // Brillo superior sutil
            g2.setColor(new Color(255, 255, 255, 25));
            g2.fillRoundRect(0, 0, getWidth(), getHeight() / 2, 10, 10);

            g2.dispose();
            super.paintComponent(g);
        }
    }

    // FlatLaf UIManager — llama esto ANTES de crear la ventana
    public static void aplicarUIManager() {
        javax.swing.UIManager.put("Panel.background",              BG_SURFACE);
        javax.swing.UIManager.put("RootPane.background",           BG_BASE);
        javax.swing.UIManager.put("TextField.background",          BG_INPUT);
        javax.swing.UIManager.put("TextField.foreground",          TEXT_PRIMARY);
        javax.swing.UIManager.put("TextField.caretForeground",     ACCENT);
        javax.swing.UIManager.put("TextArea.background",           BG_INPUT);
        javax.swing.UIManager.put("TextArea.foreground",           TEXT_PRIMARY);
        javax.swing.UIManager.put("ScrollPane.background",         BG_SURFACE);
        javax.swing.UIManager.put("Table.background",              BG_CARD);
        javax.swing.UIManager.put("Table.foreground",              TEXT_PRIMARY);
        javax.swing.UIManager.put("Table.gridColor",               BORDER);
        javax.swing.UIManager.put("Table.selectionBackground",     ACCENT_DIM);
        javax.swing.UIManager.put("Table.selectionForeground",     TEXT_PRIMARY);
        javax.swing.UIManager.put("TableHeader.background",        BG_PANEL);
        javax.swing.UIManager.put("TableHeader.foreground",        TEXT_MUTED);
        javax.swing.UIManager.put("OptionPane.background",         BG_CARD);
        javax.swing.UIManager.put("OptionPane.messageForeground",  TEXT_PRIMARY);
        javax.swing.UIManager.put("Button.arc",                    10);
        javax.swing.UIManager.put("Component.arc",                 8);
        javax.swing.UIManager.put("TextComponent.arc",             6);
        javax.swing.UIManager.put("ScrollBar.showButtons",         false);
        javax.swing.UIManager.put("ScrollBar.thumbArc",            999);
        javax.swing.UIManager.put("ScrollBar.thumb",               BORDER_FOCUS);
        javax.swing.UIManager.put("ScrollBar.track",               BG_PANEL);
        javax.swing.UIManager.put("Table.rowHeight",               28);
        javax.swing.UIManager.put("TabbedPane.showTabSeparators",  false);
        javax.swing.UIManager.put("Component.focusColor",          ACCENT);
        javax.swing.UIManager.put("Component.borderColor",         BORDER);
    }
}
