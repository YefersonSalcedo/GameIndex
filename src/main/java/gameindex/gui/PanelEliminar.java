package gameindex.gui;

import gameindex.storage.ArchivoManager;
import gameindex.tree.BPlusTree;
import gameindex.model.Videojuego;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;

public class PanelEliminar extends JPanel {

    private final ArchivoManager   archivoManager;
    private final BPlusTree        bPlusTree;
    private final VentanaPrincipal ventana;

    private JTextField txtBusqueda;
    private JPanel     panelResultado;

    private String tituloEncontrado = null;

    public PanelEliminar(ArchivoManager am, BPlusTree bt, VentanaPrincipal vp) {
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

        JLabel titulo = new JLabel("Eliminar Videojuego");
        titulo.setFont(Tema.FONT_TITLE);
        titulo.setForeground(Tema.TEXT_PRIMARY);

        JLabel sub = Tema.hint("Eliminación lógica — el registro se marca como eliminado sin borrarse físicamente");

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
        JPanel barraPanel = new JPanel(new BorderLayout(12, 0));
        barraPanel.setBackground(Tema.BG_CARD);
        barraPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Tema.BORDER),
                new EmptyBorder(16, 20, 16, 20)
        ));
        barraPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));

        txtBusqueda = new JTextField();
        Tema.estilizarTextField(txtBusqueda);
        txtBusqueda.addActionListener(e -> buscarParaEliminar());

        JButton btnBuscar = Tema.botonPrimario("Buscar");
        btnBuscar.setPreferredSize(new Dimension(130, 38));
        btnBuscar.addActionListener(e -> buscarParaEliminar());

        barraPanel.add(txtBusqueda, BorderLayout.CENTER);
        barraPanel.add(btnBuscar,   BorderLayout.EAST);

        panelResultado = new JPanel(new BorderLayout());
        panelResultado.setBackground(Tema.BG_SURFACE);
        panelResultado.setBorder(new EmptyBorder(20, 0, 0, 0));
        mostrarVacio();

        cuerpo.add(barraPanel);
        cuerpo.add(panelResultado);
        return cuerpo;
    }

    // ── Lógica ─────────────────────────────────────────────────────────────────

    private void buscarParaEliminar() {
        String titulo = txtBusqueda.getText().trim();
        if (titulo.isEmpty()) {
            ventana.setStatusError("Ingresa un título para eliminar.");
            return;
        }
        try {
            Long offset = bPlusTree.buscar(titulo);
            if (offset == null) {
                mostrarMensaje("No se encontró ningún videojuego con ese título.", Tema.DANGER);
                ventana.setStatusError("Sin resultados para: " + titulo);
                return;
            }
            Videojuego v = archivoManager.leerRegistro(offset);
            if (v == null || v.estaEliminado()) {
                mostrarMensaje("El videojuego ya fue eliminado del sistema.", Tema.TEXT_MUTED);
                return;
            }
            tituloEncontrado = v.getTitulo().trim();
            mostrarResultado(v);
            ventana.setStatusOk("Videojuego encontrado: " + tituloEncontrado);
        } catch (Exception ex) {
            mostrarMensaje("Error al buscar: " + ex.getMessage(), Tema.DANGER);
        }
    }

    private void confirmarEliminacion() {
        if (tituloEncontrado == null) return;
        try {
            bPlusTree.eliminarLogico(tituloEncontrado);
            ventana.setStatusOk("\"" + tituloEncontrado + "\" eliminado correctamente.");
            ventana.actualizarConteoRegistros();
            cancelar();
        } catch (Exception ex) {
            ventana.setStatusError("Error al eliminar: " + ex.getMessage());
        }
    }

    private void cancelar() {
        txtBusqueda.setText("");
        tituloEncontrado = null;
        mostrarVacio();
    }

    // ── Renderizado ────────────────────────────────────────────────────────────

    private void mostrarVacio() {
        panelResultado.removeAll();
        JLabel lbl = Tema.hint("Ingresa un título para buscar el videojuego a eliminar");
        lbl.setHorizontalAlignment(SwingConstants.CENTER);
        panelResultado.add(lbl, BorderLayout.CENTER);
        panelResultado.revalidate();
        panelResultado.repaint();
    }

    private void mostrarMensaje(String msg, Color color) {
        panelResultado.removeAll();
        tituloEncontrado = null;
        JLabel lbl = new JLabel(msg);
        lbl.setFont(Tema.FONT_BODY);
        lbl.setForeground(color);
        lbl.setHorizontalAlignment(SwingConstants.CENTER);
        panelResultado.add(lbl, BorderLayout.CENTER);
        panelResultado.revalidate();
        panelResultado.repaint();
    }

    private void mostrarResultado(Videojuego v) {
        panelResultado.removeAll();

        // Card de información — igual que PanelBuscar
        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(Tema.BG_CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 4, 1, 1, Tema.DANGER),
                new EmptyBorder(24, 28, 24, 28)
        ));

        GridBagConstraints gcKey = new GridBagConstraints();
        gcKey.anchor  = GridBagConstraints.NORTHWEST;
        gcKey.fill    = GridBagConstraints.NONE;
        gcKey.insets  = new Insets(8, 0, 8, 24);
        gcKey.gridx   = 0;
        gcKey.weightx = 0;

        GridBagConstraints gcVal = new GridBagConstraints();
        gcVal.anchor  = GridBagConstraints.NORTHWEST;
        gcVal.fill    = GridBagConstraints.HORIZONTAL;
        gcVal.insets  = new Insets(8, 0, 8, 0);
        gcVal.gridx   = 1;
        gcVal.weightx = 1;

        String[][] campos = {
                {"Título",        v.getTitulo()},
                {"Desarrollador", v.getDesarrollador()},
                {"Año",           String.valueOf(v.getAño())},
                {"Plataformas",   v.getPlataformas()},
                {"Género",        v.getGenero()},
                {"Sinopsis",      v.getSinopsis()},
        };

        for (int i = 0; i < campos.length; i++) {
            gcKey.gridy = i;
            JLabel lKey = Tema.hint(campos[i][0]);
            lKey.setFont(Tema.FONT_LABEL);
            lKey.setPreferredSize(new Dimension(130, 20));
            card.add(lKey, gcKey);

            gcVal.gridy = i;
            card.add(crearValor(campos[i][1]), gcVal);
        }

        // Botones debajo de la info
        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        btns.setBackground(Tema.BG_CARD);

        JButton btnCancelar  = Tema.botonSecundario("Cancelar");
        JButton btnConfirmar = Tema.botonDanger("Sí, eliminar");

        btnCancelar.addActionListener(e  -> cancelar());
        btnConfirmar.addActionListener(e -> confirmarEliminacion());

        btns.add(btnCancelar);
        btns.add(btnConfirmar);

        GridBagConstraints gcBtns = new GridBagConstraints();
        gcBtns.gridy     = campos.length;
        gcBtns.gridx     = 0;
        gcBtns.gridwidth = 2;
        gcBtns.fill      = GridBagConstraints.HORIZONTAL;
        gcBtns.insets    = new Insets(16, 0, 0, 0);
        card.add(btns, gcBtns);

        panelResultado.add(card, BorderLayout.NORTH);
        panelResultado.revalidate();
        panelResultado.repaint();
    }

    private JTextArea crearValor(String texto) {
        JTextArea ta = new JTextArea(texto);
        ta.setFont(Tema.FONT_BODY);
        ta.setForeground(Tema.TEXT_PRIMARY);
        ta.setBackground(Tema.BG_CARD);
        ta.setEditable(false);
        ta.setFocusable(false);
        ta.setLineWrap(true);
        ta.setWrapStyleWord(true);
        ta.setBorder(null);
        ta.setOpaque(false);
        return ta;
    }
}