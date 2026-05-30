package gameindex.gui;

import gameindex.storage.ArchivoManager;
import gameindex.tree.BPlusTree;
import gameindex.model.Videojuego;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.util.List;

public class PanelListar extends JPanel {

    private final ArchivoManager   archivoManager;
    private final BPlusTree        bPlusTree;
    private final VentanaPrincipal ventana;

    private DefaultTableModel tableModel;
    private JLabel            lblConteo;

    private static final String[] COLUMNAS = {"#", "Título", "Desarrollador", "Año", "Plataformas"};

    public PanelListar(ArchivoManager am, BPlusTree bt, VentanaPrincipal vp) {
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
        add(crearTabla(),      BorderLayout.CENTER);
        add(crearFooter(),     BorderLayout.SOUTH);
    }

    private JPanel crearEncabezado() {
        JPanel p = new JPanel(new BorderLayout(16, 0));
        p.setBackground(Tema.BG_SURFACE);
        p.setBorder(new EmptyBorder(0, 0, 20, 0));

        // Textos
        JLabel titulo = new JLabel("Todos los Videojuegos");
        titulo.setFont(Tema.FONT_TITLE);
        titulo.setForeground(Tema.TEXT_PRIMARY);

        JLabel sub = new JLabel("Registros activos en el sistema, ordenados alfabéticamente");
        sub.setFont(Tema.FONT_BODY);
        sub.setForeground(Tema.TEXT_MUTED);

        JPanel textos = new JPanel();
        textos.setLayout(new BoxLayout(textos, BoxLayout.Y_AXIS));
        textos.setBackground(Tema.BG_SURFACE);
        textos.add(titulo);
        textos.add(Box.createVerticalStrut(4));
        textos.add(sub);

        // Botón refrescar
        JButton btnRefrescar = Tema.botonPrimario("Refrescar");
        btnRefrescar.setForeground(Color.WHITE);
        btnRefrescar.addActionListener(e -> cargarTodos());

        JPanel acciones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        acciones.setBackground(Tema.BG_SURFACE);
        acciones.add(btnRefrescar);

        p.add(textos,   BorderLayout.WEST);
        p.add(acciones, BorderLayout.EAST);
        return p;
    }

    private JScrollPane crearTabla() {
        tableModel = new DefaultTableModel(COLUMNAS, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
            @Override public Class<?> getColumnClass(int c) {
                return (c == 0 || c == 3) ? Integer.class : String.class;
            }
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
        tabla.setRowHeight(30);
        tabla.setAutoCreateRowSorter(true);

        JTableHeader header = tabla.getTableHeader();
        header.setFont(Tema.FONT_NAV);
        header.setBackground(Tema.BG_PANEL);
        header.setForeground(Tema.TEXT_MUTED);
        header.setReorderingAllowed(false);

        // Anchos de columna: #, Título, Desarrollador, Año, Plataformas
        int[] widths = {40, 240, 180, 60, 180};
        for (int i = 0; i < widths.length; i++)
            tabla.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);

        tabla.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object val,
                                                           boolean sel, boolean focus, int row, int col) {
                super.getTableCellRendererComponent(t, val, sel, focus, row, col);
                setForeground(Tema.TEXT_PRIMARY);
                setBackground(sel ? Tema.ACCENT_DIM
                        : (row % 2 == 0 ? Tema.BG_CARD : Tema.BG_PANEL));
                setBorder(new EmptyBorder(0, 8, 0, 8));
                return this;
            }
        });

        JScrollPane scroll = new JScrollPane(tabla);
        scroll.setBorder(BorderFactory.createLineBorder(Tema.BORDER));
        scroll.getViewport().setBackground(Tema.BG_CARD);
        return scroll;
    }

    private JPanel crearFooter() {
        lblConteo = new JLabel("-");
        lblConteo.setFont(Tema.FONT_SMALL);
        lblConteo.setForeground(Tema.TEXT_MUTED);

        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(Tema.BG_SURFACE);
        p.setBorder(new EmptyBorder(10, 0, 0, 0));
        p.add(lblConteo, BorderLayout.WEST);
        return p;
    }

    public void cargarTodos() {
        tableModel.setRowCount(0);
        try {
            List<Long> offsets = bPlusTree.listarActivos();
            int n = 0;
            for (Long offset : offsets) {
                Videojuego v = archivoManager.leerRegistro(offset);
                if (v != null && !v.estaEliminado()) {
                    tableModel.addRow(new Object[]{
                            ++n,
                            v.getTitulo().trim(),
                            v.getDesarrollador().trim(),
                            v.getAño(),
                            v.getPlataformas().trim()
                    });
                }
            }
            lblConteo.setText(n + " registro(s) activo(s)");
            ventana.setStatusOk("Lista actualizada - " + n + " registro(s)");
        } catch (Exception ex) {
            ventana.setStatusError("Error al listar: " + ex.getMessage());
        }
    }
}