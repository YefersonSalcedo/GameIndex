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
    private JPanel     panelConfirmacion;
    private JLabel     lblInfo;

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

        add(crearEncabezado(),  BorderLayout.NORTH);
        add(crearCuerpo(),      BorderLayout.CENTER);
    }

    private JPanel crearEncabezado() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(Tema.BG_SURFACE);
        p.setBorder(new EmptyBorder(0, 0, 24, 0));

        JLabel titulo = new JLabel("Eliminar Videojuego");
        titulo.setFont(Tema.FONT_TITLE);
        titulo.setForeground(Tema.TEXT_PRIMARY);

        JLabel sub = new JLabel("Eliminación lógica - el registro se marca como eliminado sin borrarse físicamente");
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

        txtBusqueda = crearTextField();
        txtBusqueda.addActionListener(e -> buscarParaEliminar());

        JButton btnBuscar = crearBoton("Buscar", Tema.ACCENT);
        btnBuscar.addActionListener(e -> buscarParaEliminar());

        barra.add(new JLabel("Título:  ") {{
            setFont(Tema.FONT_NAV);
            setForeground(Tema.TEXT_MUTED);
        }}, BorderLayout.WEST);
        barra.add(txtBusqueda, BorderLayout.CENTER);
        barra.add(btnBuscar,   BorderLayout.EAST);

        // Panel de confirmación (aparece tras encontrar el registro)
        panelConfirmacion = crearPanelConfirmacion();
        panelConfirmacion.setVisible(false);

        cuerpo.add(barra);
        cuerpo.add(Box.createVerticalStrut(20));
        cuerpo.add(panelConfirmacion);
        return cuerpo;
    }

    private JPanel crearPanelConfirmacion() {
        JPanel card = new JPanel(new BorderLayout(0, 20));
        card.setBackground(Tema.BG_CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 4, 1, 1, Tema.DANGER),
                new EmptyBorder(24, 28, 24, 28)
        ));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 160));

        // Advertencia
        JPanel top = new JPanel(new BorderLayout());
        top.setBackground(Tema.BG_CARD);

        JLabel icono = new JLabel("⚠");
        icono.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 28));
        icono.setForeground(Tema.DANGER);
        icono.setBorder(new EmptyBorder(0, 0, 0, 12));

        lblInfo = new JLabel();
        lblInfo.setFont(Tema.FONT_BODY);
        lblInfo.setForeground(Tema.TEXT_PRIMARY);

        top.add(icono,   BorderLayout.WEST);
        top.add(lblInfo, BorderLayout.CENTER);

        // Botones
        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        btns.setBackground(Tema.BG_CARD);

        JButton btnCancelar  = crearBoton("Cancelar",           Tema.BG_SURFACE);
        JButton btnConfirmar = crearBoton("Sí, eliminar",       Tema.DANGER);
        btnCancelar.setForeground(Tema.TEXT_MUTED);
        btnConfirmar.setForeground(Color.WHITE);

        btnCancelar.addActionListener(e  -> cancelar());
        btnConfirmar.addActionListener(e -> confirmarEliminacion());

        btns.add(btnCancelar);
        btns.add(btnConfirmar);

        card.add(top,  BorderLayout.CENTER);
        card.add(btns, BorderLayout.SOUTH);
        return card;
    }

    private void buscarParaEliminar() {
        /**
         String titulo = txtBusqueda.getText().trim();
         if (titulo.isEmpty()) return;

         try {
         Long offset = bPlusTree.buscar(titulo);
         if (offset == null) {
         ventana.setStatusError("No se encontró: " + titulo);
         panelConfirmacion.setVisible(false);
         return;
         }
         Videojuego v = archivoManager.leerRegistro(offset);
         if (v == null || v.eliminado == 1) {
         ventana.setStatusError("El registro ya fue eliminado anteriormente.");
         panelConfirmacion.setVisible(false);
         return;
         }

         tituloEncontrado = v.titulo.trim();
         lblInfo.setText("<html>¿Estás seguro de que deseas eliminar <b>"
         + tituloEncontrado + "</b>?<br>"
         + "<span style='color:#8b949e'>Esta acción marcará el registro como eliminado.</span></html>");
         panelConfirmacion.setVisible(true);
         ventana.setStatus("Confirma la eliminación de: " + tituloEncontrado);
         } catch (Exception ex) {
         ventana.setStatusError("Error al buscar: " + ex.getMessage());
         }
         */
    }

    private void confirmarEliminacion() {
        /**
         if (tituloEncontrado == null) return;
         try {
         bPlusTree.eliminarLogico(tituloEncontrado);
         ventana.setStatusOk("\"" + tituloEncontrado + "\" eliminado correctamente.");
         ventana.actualizarConteoRegistros();
         cancelar();
         } catch (Exception ex) {
         ventana.setStatusError("Error al eliminar: " + ex.getMessage());
         }
         */
    }

    private void cancelar() {
        txtBusqueda.setText("");
        tituloEncontrado = null;
        panelConfirmacion.setVisible(false);
    }

    private JTextField crearTextField() {
        JTextField f = new JTextField();
        Tema.estilizarTextField(f);
        return f;
    }

    private JButton crearBoton(String texto, Color bg) {
        if (bg == Tema.ACCENT)   return Tema.botonPrimario(texto);
        if (bg == Tema.DANGER)   return Tema.botonDanger(texto);
        return Tema.botonSecundario(texto);
    }
}