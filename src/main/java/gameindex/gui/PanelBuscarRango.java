package gameindex.gui;

import gameindex.storage.ArchivoManager;
import gameindex.tree.BPlusTree;
import gameindex.model.Videojuego;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.util.List;

public class PanelBuscarRango extends JPanel {

    private final ArchivoManager   archivoManager;
    private final BPlusTree        bPlusTree;
    private final VentanaPrincipal ventana;

    private JTextField     txtDesde, txtHasta;
    private DefaultTableModel tableModel;
    private JLabel            lblConteo;

    private static final String[] COLUMNAS = {"Título", "Desarrollador", "Año", "Género", "Plataformas"};

    public PanelBuscarRango(ArchivoManager am, BPlusTree bt, VentanaPrincipal vp) {
        this.archivoManager = am;
        this.bPlusTree      = bt;
        this.ventana        = vp;
        construirUI();
    }

    private void construirUI() {
        setLayout(new BorderLayout());
        setBackground(Tema.BG_SURFACE);
        setBorder(new EmptyBorder(32, 40, 32, 40));

        add(crearEncabezado(), BorderLayout.NORTH);
        add(crearCuerpo(),     BorderLayout.CENTER);
    }

    private JPanel crearEncabezado() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(Tema.BG_SURFACE);
        p.setBorder(new EmptyBorder(0, 0, 24, 0));

        JLabel titulo = Tema.label("Buscar por Rango Alfabético");
        titulo.setFont(Tema.FONT_TITLE);

        JLabel sub = Tema.hint("Consulta títulos dentro de un rango aprovechando el encadenamiento de hojas del Árbol B+");

        JPanel texts = new JPanel();
        texts.setLayout(new BoxLayout(texts, BoxLayout.Y_AXIS));
        texts.setBackground(Tema.BG_SURFACE);
        texts.add(titulo);
        texts.add(Box.createVerticalStrut(4));
        texts.add(sub);
        p.add(texts, BorderLayout.WEST);
        return p;
    }

    private JPanel crearCuerpo() {
        JPanel cuerpo = new JPanel(new BorderLayout(0, 16));
        cuerpo.setBackground(Tema.BG_SURFACE);
        cuerpo.add(crearCardRango(), BorderLayout.NORTH);
        cuerpo.add(crearTabla(),     BorderLayout.CENTER);
        return cuerpo;
    }

    private JPanel crearCardRango() {
        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(Tema.BG_CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Tema.BORDER),
                new EmptyBorder(16, 20, 16, 20)
        ));

        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(4, 4, 4, 4);
        gc.fill   = GridBagConstraints.HORIZONTAL;

        JLabel lbl = Tema.label("Rango alfabético");
        gc.gridx = 0; gc.gridy = 0; gc.gridwidth = 3; gc.weightx = 1;
        card.add(lbl, gc);

        // Hint explicativo
        gc.gridy = 1;
        JLabel hint = Tema.hint("Ingresa una letra por campo  (ej: A-C ó C-A muestra todos los títulos de A hasta C)");
        card.add(hint, gc);

        gc.gridwidth = 1;
        gc.gridx = 0; gc.gridy = 2; gc.weightx = 0.4;
        card.add(Tema.hint("Desde (ej: A)"), gc);
        txtDesde = new JTextField();
        Tema.estilizarTextField(txtDesde);
        // Limitar a 1 carácter
        txtDesde.setDocument(new javax.swing.text.PlainDocument() {
            @Override public void insertString(int offs, String str, javax.swing.text.AttributeSet a)
                    throws javax.swing.text.BadLocationException {
                if (str == null) return;
                if ((getLength() + str.length()) <= 1) super.insertString(offs, str, a);
            }
        });
        gc.gridy = 3; card.add(txtDesde, gc);

        gc.gridx = 1; gc.gridy = 2; gc.weightx = 0;
        card.add(Tema.hint("—"), gc);
        gc.gridy = 3; card.add(new JLabel(), gc);

        gc.gridx = 2; gc.gridy = 2; gc.weightx = 0.4;
        card.add(Tema.hint("Hasta (ej: C)"), gc);
        txtHasta = new JTextField();
        Tema.estilizarTextField(txtHasta);
        txtHasta.setDocument(new javax.swing.text.PlainDocument() {
            @Override public void insertString(int offs, String str, javax.swing.text.AttributeSet a)
                    throws javax.swing.text.BadLocationException {
                if (str == null) return;
                if ((getLength() + str.length()) <= 1) super.insertString(offs, str, a);
            }
        });
        gc.gridy = 3; card.add(txtHasta, gc);

        gc.gridx = 0; gc.gridy = 4; gc.gridwidth = 3; gc.weightx = 1;
        JButton btn = Tema.botonPrimario("Buscar rango");
        btn.setPreferredSize(new Dimension(0, 34));
        btn.addActionListener(e -> buscarRango());
        card.add(btn, gc);

        return card;
    }

    private JPanel crearTabla() {
        tableModel = new DefaultTableModel(COLUMNAS, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        JTable tabla = new JTable(tableModel);
        tabla.setFont(Tema.FONT_BODY);
        tabla.setForeground(Tema.TEXT_PRIMARY);
        tabla.setBackground(Tema.BG_CARD);
        tabla.setGridColor(Tema.BORDER);
        tabla.setSelectionBackground(Tema.ACCENT_DIM);
        tabla.setSelectionForeground(Tema.TEXT_PRIMARY);
        tabla.setShowGrid(true);
        tabla.setIntercellSpacing(new Dimension(1, 1));
        tabla.setRowHeight(28);
        tabla.getTableHeader().setFont(Tema.FONT_NAV);
        tabla.getTableHeader().setBackground(Tema.BG_PANEL);
        tabla.getTableHeader().setForeground(Tema.TEXT_MUTED);

        int[] widths = {220, 160, 60, 100, 160};
        for (int i = 0; i < widths.length; i++)
            tabla.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);

        JScrollPane scroll = new JScrollPane(tabla);
        scroll.setBorder(BorderFactory.createLineBorder(Tema.BORDER));
        scroll.getViewport().setBackground(Tema.BG_CARD);

        lblConteo = Tema.hint("Sin resultados");

        JPanel p = new JPanel(new BorderLayout(0, 8));
        p.setBackground(Tema.BG_SURFACE);
        p.add(scroll,    BorderLayout.CENTER);
        p.add(lblConteo, BorderLayout.SOUTH);
        return p;
    }

    private void buscarRango() {
        String desde = txtDesde.getText().trim().toLowerCase();
        String hasta  = txtHasta.getText().trim().toLowerCase();
        if (desde.isEmpty()) {
            ventana.setStatusError("Ingresa al menos la letra inicial.");
            return;
        }
        // Si solo ingresaron una letra en "desde" y dejaron "hasta" vacío,
        // buscar solo esa letra
        if (hasta.isEmpty()) hasta = desde;

        try {
            List<Long> offsets = bPlusTree.buscarRango(desde, hasta);
            cargarTabla(offsets);
        } catch (Exception ex) {
            ventana.setStatusError("Error en búsqueda por rango: " + ex.getMessage());
        }
    }

    private void cargarTabla(List<Long> offsets) throws Exception {
        tableModel.setRowCount(0);
        for (Long offset : offsets) {
            Videojuego v = archivoManager.leerRegistro(offset);
            if (v != null && !v.estaEliminado())
                tableModel.addRow(new Object[]{v.getTitulo(), v.getDesarrollador(), v.getAño(), v.getGenero(), v.getPlataformas()});
        }
        int total = tableModel.getRowCount();
        lblConteo.setText(total == 0 ? "Sin resultados" : total + " resultado(s) encontrado(s)");
        if (total > 0) ventana.setStatusOk(total + " resultado(s) encontrado(s)");
        else           ventana.setStatusError("Sin resultados para esa consulta.");
    }
}