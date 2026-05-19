package gameindex.gui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.shape.Circle;
import javafx.scene.paint.Color;

/**
 * PanelEliminar — eliminación lógica de registros.
 * Muestra una caja de confirmación antes de marcar
 * el campo "eliminado = 1" en disco.
 */
public class PanelEliminar extends VBox {

    private final VentanaPrincipal ventana;

    private TextField tfBusqueda;
    private VBox      confirmBox;
    private Label     lblTituloConfirm;

    // Título actualmente verificado
    private String tituloVerificado = null;

    public PanelEliminar(VentanaPrincipal ventana) {
        this.ventana = ventana;
        setSpacing(18);
        setFillWidth(true);
        VBox.setVgrow(this, Priority.ALWAYS);

        getChildren().addAll(
            buildHeader(),
            buildSearchCard(),
            buildConfirmBox()
        );
    }

    // ── Encabezado ───────────────────────────────────────────────
    private HBox buildHeader() {
        Label icon  = new Label("🗑");
        icon.setStyle("-fx-font-size:18px;");
        Label title = new Label("Eliminación lógica");
        title.getStyleClass().add("panel-title");
        Label badge = new Label("PanelEliminar");
        badge.getStyleClass().add("panel-badge");
        HBox h = new HBox(12, icon, title, badge);
        h.setAlignment(Pos.CENTER_LEFT);
        return h;
    }

    // ── Barra de búsqueda ────────────────────────────────────────
    private VBox buildSearchCard() {
        tfBusqueda = new TextField();
        tfBusqueda.setPromptText("Título del videojuego a eliminar...");
        tfBusqueda.getStyleClass().add("gi-input");
        tfBusqueda.setMaxWidth(Double.MAX_VALUE);
        tfBusqueda.setOnAction(e -> verificar());

        Button btnVerificar = new Button("🔍  Verificar");
        btnVerificar.getStyleClass().add("btn-secondary");
        btnVerificar.setOnAction(e -> verificar());

        HBox row = new HBox(8, tfBusqueda, btnVerificar);
        HBox.setHgrow(tfBusqueda, Priority.ALWAYS);
        row.setAlignment(Pos.CENTER);

        VBox card = new VBox(12, cardTitle("Seleccionar registro"), row);
        card.getStyleClass().add("card");
        return card;
    }

    // ── Caja de confirmación ─────────────────────────────────────
    private VBox buildConfirmBox() {
        // Icono de advertencia
        Label warningIcon = new Label("⚠");
        warningIcon.setStyle(
            "-fx-font-size:22px; -fx-text-fill:#fca5a5;" +
            "-fx-background-color:#4c1019; -fx-background-radius:22px;" +
            "-fx-min-width:44px; -fx-min-height:44px;" +
            "-fx-alignment:center; -fx-padding: 0;"
        );

        Label confirmTitle = new Label("¿Confirmar eliminación?");
        confirmTitle.getStyleClass().add("confirm-title");

        lblTituloConfirm = new Label(
            "Selecciona un videojuego y presiona Verificar."
        );
        lblTituloConfirm.getStyleClass().add("confirm-text");
        lblTituloConfirm.setWrapText(true);
        lblTituloConfirm.setMaxWidth(320);

        Button btnCancelar = new Button("✕  Cancelar");
        btnCancelar.getStyleClass().add("btn-secondary");
        btnCancelar.setOnAction(e -> cancelar());

        Button btnEliminar = new Button("🗑  Eliminar lógicamente");
        btnEliminar.getStyleClass().add("btn-danger");
        btnEliminar.setOnAction(e -> eliminar());
        btnEliminar.setDisable(true);  // se habilita solo al verificar

        // Guardar referencia al botón para habilitarlo después
        btnEliminar.setId("btn-eliminar-confirmar");

        HBox botones = new HBox(10, btnCancelar, btnEliminar);
        botones.setAlignment(Pos.CENTER);
        VBox.setMargin(botones, new Insets(4, 0, 0, 0));

        confirmBox = new VBox(14, warningIcon, confirmTitle, lblTituloConfirm, botones);
        confirmBox.setAlignment(Pos.CENTER);
        confirmBox.getStyleClass().add("confirm-box");
        confirmBox.setMaxWidth(Double.MAX_VALUE);
        VBox.setVgrow(confirmBox, Priority.ALWAYS);
        return confirmBox;
    }

    // ── Lógica ───────────────────────────────────────────────────
    private void verificar() {
        String titulo = tfBusqueda.getText().trim();
        if (titulo.isBlank()) return;

        // ── Integrar backend ─────────────────────────────────────
        // long offset = ventana.getBPlusTree().buscar(titulo);
        // if (offset == -1) {
        //     lblTituloConfirm.setText("No se encontró: \"" + titulo + "\"");
        //     lblTituloConfirm.setStyle("-fx-text-fill:#f87171; ...");
        //     tituloVerificado = null;
        //     return;
        // }
        // tituloVerificado = titulo;

        // Demostración:
        tituloVerificado = titulo;
        lblTituloConfirm.setText(
            "El registro \"" + titulo + "\" será marcado como eliminado lógicamente " +
            "(eliminado = 1). No se borrará físicamente del archivo data.dat ni del índice B+."
        );
        lblTituloConfirm.setStyle("-fx-font-size:12px; -fx-text-fill:#6b5fa8; -fx-wrap-text:true;");

        // Habilitar botón de eliminar
        Button btnElim = (Button) confirmBox.lookup("#btn-eliminar-confirmar");
        if (btnElim != null) btnElim.setDisable(false);

        ventana.setLastOperation("buscar(\"" + titulo + "\")");
    }

    private void eliminar() {
        if (tituloVerificado == null) return;

        // ── Integrar backend ─────────────────────────────────────
        // ventana.getBPlusTree().eliminarLogico(tituloVerificado,
        //                                       ventana.getArchivoManager());

        ventana.setLastOperation("eliminarLogico(\"" + tituloVerificado + "\")");
        showSuccess("\"" + tituloVerificado + "\" eliminado lógicamente.");
        cancelar();
    }

    private void cancelar() {
        tfBusqueda.clear();
        tituloVerificado = null;
        lblTituloConfirm.setText("Selecciona un videojuego y presiona Verificar.");
        lblTituloConfirm.setStyle("-fx-font-size:12px; -fx-text-fill:#6b5fa8;");
        Button btnElim = (Button) confirmBox.lookup("#btn-eliminar-confirmar");
        if (btnElim != null) btnElim.setDisable(true);
    }

    // ── Helpers ──────────────────────────────────────────────────
    private Label cardTitle(String text) {
        Label l = new Label(text.toUpperCase());
        l.getStyleClass().add("card-label");
        return l;
    }

    private void showSuccess(String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK);
        a.setHeaderText("Registro eliminado"); a.setTitle("GameIndex"); a.showAndWait();
    }
}
