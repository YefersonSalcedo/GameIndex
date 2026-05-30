package gameindex.gui;

import gameindex.storage.ArchivoManager;
import gameindex.tree.BPlusTree;
import gameindex.model.Videojuego;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.util.List;

public class PanelBuscarPrefijo extends JPanel {

    private final ArchivoManager   archivoManager;
    private final BPlusTree        bPlusTree;
    private final VentanaPrincipal ventana;

    private JTextField        txtPrefijo;
    private DefaultTableModel tableModel;
    private JLabel            lblConteo;

    private static final String[] COLUMNAS = {"Título", "Desarrollador", "Año", "Género", "Plataformas"};

    public PanelBuscarPrefijo(ArchivoManager am, BPlusTree bt, VentanaPrincipal vp) {
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

        JLabel titulo = Tema.label("Buscar por Prefijo / Franquicia");
        titulo.setFont(Tema.FONT_TITLE);

        JLabel sub = Tema.hint("Encuentra todos los títulos que comienzan con un texto dado, ideal para buscar franquicias completas");

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
        cuerpo.add(crearCardPrefijo(), BorderLayout.NORTH);
        cuerpo.add(crearTabla(),       BorderLayout.CENTER);
        return cuerpo;
    }

    private JPanel crearCardPrefijo() {
        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(Tema.BG_CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Tema.BORDER),
                new EmptyBorder(16, 20, 16, 20)
        ));

        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(4, 4, 4, 4);
        gc.fill   = GridBagConstraints.HORIZONTAL;

        JLabel lbl = Tema.label("Prefijo / Franquicia");
        gc.gridx = 0; gc.gridy = 0; gc.gridwidth = 2; gc.weightx = 1;
        card.add(lbl, gc);

        gc.gridy = 1;
        card.add(Tema.hint("Prefijo (ej: \"Super Mario\", \"The Legend of Zelda\")"), gc);

        gc.gridy = 2;
        txtPrefijo = new JTextField(); Tema.estilizarTextField(txtPrefijo);
        card.add(txtPrefijo, gc);

        gc.gridy = 3;
        JButton btn = Tema.botonPrimario("Buscar prefijo");
        btn.setPreferredSize(new Dimension(0, 34));
        btn.addActionListener(e -> buscarPrefijo());
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

    private void buscarPrefijo() {
        String prefijo = txtPrefijo.getText().trim();
        if (prefijo.isEmpty()) {
            ventana.setStatusError("Escribe un prefijo para buscar.");
            return;
        }
        try {
            List<Long> offsets = bPlusTree.buscarPrefijo(prefijo);
            cargarTabla(offsets);
        } catch (Exception ex) {
            ventana.setStatusError("Error en búsqueda por prefijo: " + ex.getMessage());
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
