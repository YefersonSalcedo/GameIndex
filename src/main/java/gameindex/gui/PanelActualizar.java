package gameindex.gui;

import gameindex.storage.ArchivoManager;
import gameindex.tree.BPlusTree;
import gameindex.model.Videojuego;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;

public class PanelActualizar extends JPanel {

    private final ArchivoManager   archivoManager;
    private final BPlusTree        bPlusTree;
    private final VentanaPrincipal ventana;

    private JTextField txtBusqueda;
    private JTextField txtTitulo, txtDesarrollador, txtAnio, txtPlataformas, txtGenero;
    private JTextArea  txtSinopsis;

    private String tituloOriginal = null;
    private long   offsetActual   = -1;

    private JPanel panelForm;

    public PanelActualizar(ArchivoManager am, BPlusTree bt, VentanaPrincipal vp) {
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

        JLabel titulo = Tema.label("Actualizar Videojuego");
        titulo.setFont(Tema.FONT_TITLE);

        JLabel sub = Tema.hint("Busca el título que quieres modificar y edita los campos");

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
        JPanel cuerpo = new JPanel();
        cuerpo.setLayout(new BoxLayout(cuerpo, BoxLayout.Y_AXIS));
        cuerpo.setBackground(Tema.BG_SURFACE);

        // Barra de búsqueda
        JPanel barra = new JPanel(new BorderLayout(12, 0));
        barra.setBackground(Tema.BG_CARD);
        barra.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Tema.BORDER),
                new EmptyBorder(16, 20, 16, 20)
        ));
        barra.setMaximumSize(new Dimension(Integer.MAX_VALUE, 76));

        txtBusqueda = new JTextField();
        Tema.estilizarTextField(txtBusqueda);
        txtBusqueda.addActionListener(e -> cargarRegistro());

        JLabel lblTitulo = Tema.hint("Título actual:  ");
        lblTitulo.setFont(Tema.FONT_NAV);

        JButton btnCargar = Tema.botonPrimario("Cargar");
        btnCargar.setPreferredSize(new Dimension(130, 38));
        btnCargar.addActionListener(e -> cargarRegistro());

        barra.add(lblTitulo,    BorderLayout.WEST);
        barra.add(txtBusqueda,  BorderLayout.CENTER);
        barra.add(btnCargar,    BorderLayout.EAST);

        // Formulario (oculto hasta cargar)
        panelForm = crearFormulario();
        panelForm.setVisible(false);

        cuerpo.add(barra);
        cuerpo.add(Box.createVerticalStrut(16));
        cuerpo.add(panelForm);

        // Envolver en scroll para que el formulario nunca tape los botones
        JScrollPane scroll = new JScrollPane(cuerpo,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getVerticalScrollBar().setUnitIncrement(16);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(Tema.BG_SURFACE);
        wrapper.add(scroll, BorderLayout.CENTER);
        return wrapper;
    }

    private JPanel crearFormulario() {
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Tema.BG_CARD);
        form.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Tema.BORDER),
                new EmptyBorder(24, 28, 24, 28)
        ));
        // Sin setMaximumSize: el scroll del cuerpo controla el desborde

        GridBagConstraints gc = new GridBagConstraints();
        gc.insets  = new Insets(6, 0, 6, 12);
        gc.anchor  = GridBagConstraints.WEST;
        gc.fill    = GridBagConstraints.HORIZONTAL;

        txtTitulo        = new JTextField(); Tema.estilizarTextField(txtTitulo);
        txtDesarrollador = new JTextField(); Tema.estilizarTextField(txtDesarrollador);
        txtAnio          = new JTextField(); Tema.estilizarTextField(txtAnio);
        txtPlataformas   = new JTextField(); Tema.estilizarTextField(txtPlataformas);
        txtGenero        = new JTextField(); Tema.estilizarTextField(txtGenero);
        txtSinopsis      = new JTextArea(4, 30);
        Tema.estilizarTextArea(txtSinopsis);

        String[] etiquetas = {"Título", "Desarrollador", "Año", "Plataformas", "Género"};
        JTextField[] campos = {txtTitulo, txtDesarrollador, txtAnio, txtPlataformas, txtGenero};

        for (int i = 0; i < campos.length; i++) {
            gc.gridx = 0; gc.gridy = i * 2;     gc.weightx = 0; gc.gridwidth = 1;
            form.add(Tema.label(etiquetas[i]), gc);
            gc.gridy = i * 2 + 1; gc.weightx = 1; gc.gridwidth = 2;
            form.add(campos[i], gc);
        }

        int row = campos.length * 2;
        gc.gridx = 0; gc.gridy = row;     gc.weightx = 0; gc.gridwidth = 1;
        form.add(Tema.label("Sinopsis"), gc);
        gc.gridy = row + 1; gc.weightx = 1; gc.gridwidth = 2;
        gc.fill  = GridBagConstraints.HORIZONTAL; gc.weighty = 0;
        JScrollPane sinScroll = new JScrollPane(txtSinopsis);
        sinScroll.setBorder(BorderFactory.createLineBorder(Tema.BORDER));
        form.add(sinScroll, gc);
        gc.fill = GridBagConstraints.HORIZONTAL; gc.weighty = 0;

        // Botones
        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        btns.setBackground(Tema.BG_CARD);
        JButton btnCancelar   = Tema.botonSecundario("Cancelar");
        JButton btnActualizar = Tema.botonPrimario("Actualizar");
        btnActualizar.setPreferredSize(new Dimension(160, 38));
        btnCancelar.addActionListener(e -> cancelar());
        btnActualizar.addActionListener(e -> actualizar());
        btns.add(btnCancelar);
        btns.add(btnActualizar);

        gc.gridx = 0; gc.gridy = row + 2; gc.gridwidth = 2;
        gc.insets = new Insets(20, 0, 0, 0);
        form.add(btns, gc);

        return form;
    }

    // ── Lógica ─────────────────────────────────────────────────────────────────
    private void cargarRegistro() {

        String titulo = txtBusqueda.getText().trim();
        if (titulo.isEmpty()) return;
        try {
            Long offset = bPlusTree.buscar(titulo);
            if (offset == null) {
                ventana.setStatusError("No se encontró: " + titulo);
                panelForm.setVisible(false);
                return;
            }
            Videojuego v = archivoManager.leerRegistro(offset);
            if (v == null || v.estaEliminado()) {
                ventana.setStatusError("El registro fue eliminado.");
                panelForm.setVisible(false);
                return;
            }
            tituloOriginal = v.getTitulo().trim();
            offsetActual   = offset;
            txtTitulo       .setText(v.getTitulo().trim());
            txtDesarrollador.setText(v.getDesarrollador().trim());
            txtAnio         .setText(String.valueOf(v.getAño()));
            txtPlataformas  .setText(v.getPlataformas().trim());
            txtGenero       .setText(v.getGenero().trim());
            txtSinopsis     .setText(v.getSinopsis().trim());
            panelForm.setVisible(true);
            ventana.setStatusOk("Registro cargado: " + tituloOriginal);
        } catch (Exception ex) {
            ventana.setStatusError("Error al cargar: " + ex.getMessage());
        }

    }

    private void actualizar() {
        String nuevoTitulo   = txtTitulo.getText().trim();
        String desarrollador = txtDesarrollador.getText().trim();
        String anioStr       = txtAnio.getText().trim();
        String plataformas   = txtPlataformas.getText().trim();
        String genero        = txtGenero.getText().trim();
        String sinopsis      = txtSinopsis.getText().trim();

        if (nuevoTitulo.isEmpty() || desarrollador.isEmpty() || anioStr.isEmpty()
                || plataformas.isEmpty() || genero.isEmpty() || sinopsis.isEmpty()) {
            ventana.setStatusError("Todos los campos son obligatorios.");
            return;
        }
        int anio;
        try {
            anio = Integer.parseInt(anioStr);
        } catch (NumberFormatException ex) {
            ventana.setStatusError("El año debe ser un número válido.");
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(
                ventana,
                "¿Confirmas la actualización de \"" + tituloOriginal + "\"?",
                "Confirmar actualización",
                JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        try {
            Videojuego v = new Videojuego(nuevoTitulo, desarrollador, anio,
                    plataformas, genero, sinopsis);
            boolean ok = bPlusTree.actualizar(tituloOriginal, v);
            if (ok) {
                ventana.setStatusOk("\"" + tituloOriginal + "\" actualizado correctamente.");
                cancelar();
            } else {
                ventana.setStatusError("No se pudo actualizar: registro no encontrado.");
            }
        } catch (Exception ex) {
            ventana.setStatusError("Error al actualizar: " + ex.getMessage());
        }
    }

    private void cancelar() {
        txtBusqueda.setText("");
        txtTitulo.setText(""); txtDesarrollador.setText("");
        txtAnio.setText("");   txtPlataformas.setText("");
        txtGenero.setText(""); txtSinopsis.setText("");
        tituloOriginal = null; offsetActual = -1;
        panelForm.setVisible(false);
    }
}