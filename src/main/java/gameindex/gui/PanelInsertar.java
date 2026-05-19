package gameindex.gui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

/**
 * PanelInsertar — formulario para registrar un nuevo videojuego.
 * Valida los campos y envía los datos al Árbol B+ mediante ArchivoManager.
 */
public class PanelInsertar extends VBox {

    private final VentanaPrincipal ventana;

    // Campos del formulario
    private TextField tfTitulo, tfDesarrollador, tfAnio, tfPlataformas, tfGenero;
    private TextArea  taSinopsis;

    public PanelInsertar(VentanaPrincipal ventana) {
        this.ventana = ventana;
        setSpacing(18);
        setFillWidth(true);
        getStyleClass().add("content-area");

        getChildren().addAll(
            buildHeader(),
            buildForm()
        );
    }

    // ── Encabezado del panel ─────────────────────────────────────
    private HBox buildHeader() {
        Label icon  = new Label("＋");
        icon.setStyle("-fx-font-size:18px; -fx-text-fill:#7c3aed;");

        Label title = new Label("Nuevo registro");
        title.getStyleClass().add("panel-title");

        Label badge = new Label("PanelInsertar");
        badge.getStyleClass().add("panel-badge");

        HBox header = new HBox(12, icon, title, badge);
        header.setAlignment(Pos.CENTER_LEFT);
        return header;
    }

    // ── Formulario en grid 2 columnas ────────────────────────────
    private VBox buildForm() {
        // Columna 1 / Columna 2
        ColumnConstraints col1 = new ColumnConstraints();
        col1.setPercentWidth(50);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setPercentWidth(50);

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(12);
        grid.getColumnConstraints().addAll(col1, col2);

        // ── Título (ocupa todo el ancho) ──
        tfTitulo = field("Ej: Elden Ring");
        grid.add(fieldBox("Título", tfTitulo), 0, 0, 2, 1);

        // ── Fila 2 ──
        tfDesarrollador = field("Ej: FromSoftware");
        tfAnio          = field("Ej: 2022");
        grid.add(fieldBox("Desarrollador", tfDesarrollador), 0, 1);
        grid.add(fieldBox("Año", tfAnio), 1, 1);

        // ── Fila 3 ──
        tfPlataformas = field("Ej: PC, PS5, Xbox Series");
        tfGenero      = field("Ej: Action RPG");
        grid.add(fieldBox("Plataformas", tfPlataformas), 0, 2);
        grid.add(fieldBox("Género", tfGenero), 1, 2);

        // ── Sinopsis (todo el ancho) ──
        taSinopsis = new TextArea();
        taSinopsis.setPromptText("Descripción del videojuego...");
        taSinopsis.setPrefRowCount(3);
        taSinopsis.getStyleClass().add("gi-textarea");
        taSinopsis.setWrapText(true);
        grid.add(fieldBox("Sinopsis", taSinopsis), 0, 3, 2, 1);

        // ── Botones ──
        Button btnLimpiar  = new Button("✕  Limpiar");
        btnLimpiar.getStyleClass().add("btn-secondary");
        btnLimpiar.setOnAction(e -> limpiar());

        Button btnInsertar = new Button("⬡  Insertar en B+");
        btnInsertar.getStyleClass().add("btn-primary");
        btnInsertar.setOnAction(e -> insertar());

        HBox botones = new HBox(10, btnLimpiar, btnInsertar);
        botones.setAlignment(Pos.CENTER_RIGHT);

        VBox card = new VBox(14, labelCardTitle("Datos del videojuego"), grid, botones);
        card.getStyleClass().add("card");
        VBox.setVgrow(card, Priority.ALWAYS);
        return card;
    }

    // ── Lógica de inserción ──────────────────────────────────────
    private void insertar() {
        // Validación básica de campos vacíos
        if (tfTitulo.getText().isBlank()) {
            showError("El campo Título no puede estar vacío.");
            tfTitulo.requestFocus();
            return;
        }
        if (tfDesarrollador.getText().isBlank()) {
            showError("El campo Desarrollador no puede estar vacío.");
            return;
        }
        if (!tfAnio.getText().matches("\\d{4}")) {
            showError("El año debe ser un número de 4 dígitos.");
            tfAnio.requestFocus();
            return;
        }

        // ── Construir y persistir el videojuego ──────────────────
        // Videojuego v = new Videojuego(
        //     tfTitulo.getText().trim(),
        //     tfDesarrollador.getText().trim(),
        //     Integer.parseInt(tfAnio.getText().trim()),
        //     tfPlataformas.getText().trim(),
        //     tfGenero.getText().trim(),
        //     taSinopsis.getText().trim()
        // );
        // long offset = ventana.getArchivoManager().agregarRegistro(v);
        // ventana.getBPlusTree().insertar(v.getTitulo(), offset);

        ventana.setLastOperation("insertar(\"" + tfTitulo.getText().trim() + "\")");
        showSuccess("Videojuego \"" + tfTitulo.getText().trim() + "\" insertado correctamente.");
        limpiar();
    }

    private void limpiar() {
        tfTitulo.clear(); tfDesarrollador.clear(); tfAnio.clear();
        tfPlataformas.clear(); tfGenero.clear(); taSinopsis.clear();
    }

    // ── Helpers ──────────────────────────────────────────────────
    private TextField field(String prompt) {
        TextField tf = new TextField();
        tf.setPromptText(prompt);
        tf.getStyleClass().add("gi-input");
        tf.setMaxWidth(Double.MAX_VALUE);
        return tf;
    }

    private VBox fieldBox(String labelText, Control control) {
        Label lbl = new Label(labelText);
        lbl.getStyleClass().add("field-label");
        VBox box = new VBox(5, lbl, control);
        VBox.setVgrow(control, Priority.ALWAYS);
        HBox.setHgrow(box, Priority.ALWAYS);
        GridPane.setHgrow(box, Priority.ALWAYS);
        return box;
    }

    private Label labelCardTitle(String text) {
        Label lbl = new Label(text.toUpperCase());
        lbl.getStyleClass().add("card-label");
        return lbl;
    }

    private void showError(String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK);
        a.setHeaderText("Error de validación");
        a.setTitle("GameIndex");
        a.getDialogPane().setStyle("-fx-background-color:#13102a; -fx-text-fill:#c4b5fd;");
        a.showAndWait();
    }

    private void showSuccess(String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK);
        a.setHeaderText("Operación exitosa");
        a.setTitle("GameIndex");
        a.showAndWait();
    }
}
