package gameindex.gui;

import gameindex.storage.ArchivoManager;
import gameindex.tree.BPlusTree;
import gameindex.model.Videojuego;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;

public class PanelInsertar extends JPanel {

    private final ArchivoManager archivoManager;
    private final BPlusTree      bPlusTree;
    private final VentanaPrincipal ventana;

    // Campos del formulario
    private JTextField  txtTitulo;
    private JTextField  txtDesarrollador;
    private JTextField  txtAnio;
    private JTextField  txtPlataformas;
    private JTextField  txtGenero;
    private JTextArea   txtSinopsis;

    public PanelInsertar(ArchivoManager am, BPlusTree bt, VentanaPrincipal vp) {
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
        add(crearFormulario(), BorderLayout.CENTER);
    }

    private JPanel crearEncabezado() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(Tema.BG_SURFACE);
        p.setBorder(new EmptyBorder(0, 0, 24, 0));

        JLabel titulo = new JLabel("Insertar Videojuego");
        titulo.setFont(Tema.FONT_TITLE);
        titulo.setForeground(Tema.TEXT_PRIMARY);

        JLabel sub = new JLabel("Completa todos los campos para registrar un nuevo título");
        sub.setFont(Tema.FONT_BODY);
        sub.setForeground(Tema.TEXT_MUTED);

        JPanel texts = new JPanel();
        texts.setLayout(new BoxLayout(texts, BoxLayout.Y_AXIS));
        texts.setBackground(Tema.BG_SURFACE);
        texts.add(titulo);
        texts.add(Box.createVerticalStrut(4));
        texts.add(sub);

        p.add(texts, BorderLayout.WEST);
        return p;
    }

    private JScrollPane crearFormulario() {
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Tema.BG_CARD);
        form.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Tema.BORDER, 1),
                new EmptyBorder(28, 32, 28, 32)
        ));

        GridBagConstraints gc = new GridBagConstraints();
        gc.insets  = new Insets(8, 0, 8, 16);
        gc.anchor  = GridBagConstraints.WEST;
        gc.fill    = GridBagConstraints.HORIZONTAL;

        // Fila 0: Título
        txtTitulo = crearCampo(100);
        agregarFila(form, gc, 0, "Título", txtTitulo, "Máx. 100 caracteres");

        // Fila 1: Desarrollador
        txtDesarrollador = crearCampo(80);
        agregarFila(form, gc, 1, "Desarrollador", txtDesarrollador, "Máx. 80 caracteres");

        // Fila 2: Año | Género (dos columnas)
        txtAnio   = crearCampo(6);
        txtGenero = crearCampo(50);
        agregarFilaDoble(form, gc, 2,
                "Año",   txtAnio,   "Ej: 2024",
                "Género", txtGenero, "Máx. 50 caracteres");

        // Fila 3: Plataformas
        txtPlataformas = crearCampo(120);
        agregarFila(form, gc, 3, "Plataformas", txtPlataformas, "Ej: PC, PS5, Xbox  —  Máx. 120 caracteres");

        // Fila 4: Sinopsis
        txtSinopsis = new JTextArea(4, 30);
        Tema.estilizarTextArea(txtSinopsis);
        JScrollPane sinScroll = new JScrollPane(txtSinopsis);
        sinScroll.setBorder(BorderFactory.createLineBorder(Tema.BORDER));
        agregarFilaTextArea(form, gc, 4, "Sinopsis", sinScroll, "Máx. 300 caracteres");

        // Fila 5: Botones
        agregarBotones(form, gc, 5);

        JScrollPane scroll = new JScrollPane(form);
        scroll.setBorder(null);
        scroll.setBackground(Tema.BG_SURFACE);
        scroll.getViewport().setBackground(Tema.BG_SURFACE);

        // Aumentar velocidad del scroll
        scroll.getVerticalScrollBar().setUnitIncrement(20);
        scroll.getHorizontalScrollBar().setUnitIncrement(20);

        return scroll;
    }

    private void agregarFila(JPanel p, GridBagConstraints gc,
                             int fila, String label, JComponent campo, String hint) {
        gc.gridx = 0; gc.gridy = fila * 3;
        gc.gridwidth = 4; gc.weightx = 0;
        p.add(crearLabel(label), gc);

        gc.gridy = fila * 3 + 1; gc.weightx = 1;
        p.add(campo, gc);

        gc.gridy = fila * 3 + 2; gc.weightx = 0;
        p.add(crearHint(hint), gc);
    }

    private void agregarFilaDoble(JPanel p, GridBagConstraints gc, int fila,
                                  String l1, JComponent c1, String h1,
                                  String l2, JComponent c2, String h2) {
        GridBagConstraints g = (GridBagConstraints) gc.clone();

        g.gridwidth = 2; g.weightx = 0.3;
        g.gridx = 0; g.gridy = fila * 3;     p.add(crearLabel(l1), g);
        g.gridy = fila * 3 + 1; g.weightx = 0.3; p.add(c1, g);
        g.gridy = fila * 3 + 2; p.add(crearHint(h1), g);

        g.gridx = 2; g.gridy = fila * 3;     p.add(crearLabel(l2), g);
        g.gridy = fila * 3 + 1; g.weightx = 0.7; p.add(c2, g);
        g.gridy = fila * 3 + 2; p.add(crearHint(h2), g);
    }

    private void agregarFilaTextArea(JPanel p, GridBagConstraints gc,
                                     int fila, String label, JComponent campo, String hint) {
        gc.gridx = 0; gc.gridy = fila * 3;
        gc.gridwidth = 4; gc.weightx = 0;
        p.add(crearLabel(label), gc);

        gc.gridy = fila * 3 + 1; gc.weightx = 1; gc.fill = GridBagConstraints.BOTH; gc.weighty = 1;
        p.add(campo, gc);
        gc.fill = GridBagConstraints.HORIZONTAL; gc.weighty = 0;

        gc.gridy = fila * 3 + 2; gc.weightx = 0;
        p.add(crearHint(hint), gc);
    }

    private void agregarBotones(JPanel p, GridBagConstraints gc, int fila) {
        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        btns.setBackground(Tema.BG_CARD);

        JButton btnLimpiar = crearBoton("Limpiar", false);
        JButton btnGuardar = crearBoton("Guardar registro", true);

        btnLimpiar.addActionListener(e -> limpiarFormulario());
        btnGuardar.addActionListener(e -> guardar());

        btns.add(btnLimpiar);
        btns.add(btnGuardar);

        gc.gridx = 0; gc.gridy = fila * 3 + 1;
        gc.gridwidth = 4; gc.weightx = 1;
        gc.insets = new Insets(20, 0, 0, 0);
        p.add(btns, gc);
    }

    private JTextField crearCampo(int maxChars) {
        JTextField f = new JTextField();
        Tema.estilizarTextField(f);
        return f;
    }

    private JLabel crearLabel(String text) {
        return Tema.label(text);
    }

    private JLabel crearHint(String text) {
        return Tema.hint(text);
    }

    private JButton crearBoton(String texto, boolean primario) {
        return primario ? Tema.botonPrimario(texto) : Tema.botonSecundario(texto);
    }

    private void guardar() {
        // Validaciones
        String titulo       = txtTitulo.getText().trim();
        String desarrollador = txtDesarrollador.getText().trim();
        String anioStr      = txtAnio.getText().trim();
        String plataformas  = txtPlataformas.getText().trim();
        String genero       = txtGenero.getText().trim();
        String sinopsis     = txtSinopsis.getText().trim();

        if (titulo.isEmpty() || desarrollador.isEmpty() || anioStr.isEmpty()
                || plataformas.isEmpty() || genero.isEmpty() || sinopsis.isEmpty()) {
            marcarError("Todos los campos son obligatorios.");
            return;
        }

        int anio;
        try {
            anio = Integer.parseInt(anioStr);
            if (anio < 1950 || anio > 2030) throw new NumberFormatException();
        } catch (NumberFormatException ex) {
            marcarError("El año debe ser un número entre 1950 y 2030.");
            return;
        }

        if (titulo.length()       > 100) { marcarError("El título excede 100 caracteres.");        return; }
        if (desarrollador.length() > 80) { marcarError("El desarrollador excede 80 caracteres.");  return; }
        if (plataformas.length()  > 120) { marcarError("Las plataformas exceden 120 caracteres.");  return; }
        if (genero.length()       >  50) { marcarError("El género excede 50 caracteres.");          return; }
        if (sinopsis.length()     > 300) { marcarError("La sinopsis excede 300 caracteres.");       return; }

        try {
            Videojuego v = new Videojuego(titulo, desarrollador, anio, plataformas, genero, sinopsis);
            bPlusTree.insertar(v);

            ventana.setStatusOk("Videojuego '" + titulo + "' insertado correctamente.");
            ventana.actualizarConteoRegistros();
            limpiarFormulario();
        } catch (Exception ex) {
            marcarError("Error al guardar: " + ex.getMessage());
        }
    }

    private void limpiarFormulario() {
        txtTitulo.setText("");
        txtDesarrollador.setText("");
        txtAnio.setText("");
        txtPlataformas.setText("");
        txtGenero.setText("");
        txtSinopsis.setText("");
        txtTitulo.requestFocus();
    }

    private void marcarError(String msg) {
        ventana.setStatusError(msg);
        JOptionPane.showMessageDialog(ventana, msg, "Error de validación", JOptionPane.WARNING_MESSAGE);
    }
}