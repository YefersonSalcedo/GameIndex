package gameindex.gui;

import gameindex.storage.ArchivoManager;
import gameindex.tree.BPlusTree;
import gameindex.model.Videojuego;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;

public class PanelBuscar extends JPanel {

    private final ArchivoManager   archivoManager;
    private final BPlusTree        bPlusTree;
    private final VentanaPrincipal ventana;

    private JTextField txtBusqueda;
    private JPanel     panelResultado;

    public PanelBuscar(ArchivoManager am, BPlusTree bt, VentanaPrincipal vp) {
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

        JLabel titulo = Tema.label("Buscar Videojuego");
        titulo.setFont(Tema.FONT_TITLE);

        JLabel sub = Tema.hint("Búsqueda exacta por título en el Árbol B+");

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

        JPanel barraPanel = new JPanel(new BorderLayout(12, 0));
        barraPanel.setBackground(Tema.BG_CARD);
        barraPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Tema.BORDER),
                new EmptyBorder(16, 20, 16, 20)
        ));
        barraPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));

        txtBusqueda = new JTextField();
        Tema.estilizarTextField(txtBusqueda);
        txtBusqueda.addActionListener(e -> buscar()); // Enter para buscar

        JButton btnBuscar = Tema.botonPrimario("Buscar");
        btnBuscar.setPreferredSize(new Dimension(130, 38));
        btnBuscar.addActionListener(e -> buscar());

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

    private void buscar() {
        
         String titulo = txtBusqueda.getText().trim();
         if (titulo.isEmpty()) {
         mostrarMensaje("Escribe un título para buscar.", Tema.TEXT_MUTED);
         return;
         }

         try {
         Long offset = bPlusTree.buscar(titulo);
         if (offset == null) {
         mostrarMensaje("No se encontró ningún videojuego con ese título.", Tema.DANGER);
         ventana.setStatusError("Sin resultados para: " + titulo);
         } else {
         Videojuego v = archivoManager.leerRegistro(offset);
         if (v == null || v.estaEliminado()) {
         mostrarMensaje("El videojuego fue eliminado del sistema.", Tema.TEXT_MUTED);
         } else {
         mostrarResultado(v);
         ventana.setStatusOk("Videojuego encontrado: " + titulo);
         }
         }
         } catch (Exception ex) {
         mostrarMensaje("Error al buscar: " + ex.getMessage(), Tema.DANGER);
         }
         
    }

    private void mostrarVacio() {
        panelResultado.removeAll();
        JLabel lbl = Tema.hint("Ingresa un título para comenzar la búsqueda");
        lbl.setHorizontalAlignment(SwingConstants.CENTER);
        panelResultado.add(lbl, BorderLayout.CENTER);
        panelResultado.revalidate();
        panelResultado.repaint();
    }

    private void mostrarMensaje(String msg, Color color) {
        panelResultado.removeAll();
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

        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(Tema.BG_CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Tema.ACCENT_DIM),
                new EmptyBorder(24, 28, 24, 28)
        ));

        GridBagConstraints gc = new GridBagConstraints();
        gc.anchor = GridBagConstraints.WEST;
        gc.insets = new Insets(5, 0, 5, 20);

        String[][] campos = {
                {"Título",        v.getTitulo()},
                {"Desarrollador", v.getDesarrollador()},
                {"Año",           String.valueOf(v.getAnio())},
                {"Plataformas",   v.getPlataformas()},
                {"Género",        v.getGenero()},
                {"Sinopsis",      v.getSinopsis()},
        };

        for (int i = 0; i < campos.length; i++) {
            gc.gridx = 0; gc.gridy = i; gc.weightx = 0;
            JLabel lKey = Tema.hint(campos[i][0]);
            lKey.setFont(Tema.FONT_LABEL);
            lKey.setPreferredSize(new Dimension(130, 24));
            card.add(lKey, gc);

            gc.gridx = 1; gc.weightx = 1;
            JLabel lVal = new JLabel("<html>" + campos[i][1] + "</html>");
            lVal.setFont(Tema.FONT_BODY);
            lVal.setForeground(Tema.TEXT_PRIMARY);
            card.add(lVal, gc);
        }

        // Spacer
        gc.gridx = 0; gc.gridy = campos.length; gc.weighty = 1;
        card.add(Box.createVerticalGlue(), gc);

        panelResultado.add(card, BorderLayout.NORTH);
        panelResultado.revalidate();
        panelResultado.repaint();
    }
}