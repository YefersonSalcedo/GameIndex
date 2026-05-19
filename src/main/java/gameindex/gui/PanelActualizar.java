package gameindex.gui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

/**
 * PanelActualizar — carga un registro por título, permite editarlo
 * y guarda los cambios en disco y en el índice B+.
 */
public class PanelActualizar extends VBox {

    private final VentanaPrincipal ventana;

    private TextField tfBusqueda;
    private TextField tfTitulo, tfDesarrollador, tfAnio, tfPlataformas, tfGenero;
    private TextArea  taSinopsis;
    private VBox      editCard;

    public PanelActualizar(VentanaPrincipal ventana) {
        this.ventana = ventana;
        setSpacing(18);
        setFillWidth(true);
        VBox.setVgrow(this, Priority.ALWAYS);

        getChildren().addAll(
            buildHeader(),
            buildSearchCard(),
            buildEditCard()
        );
    }

    // ── Encabezado ───────────────────────────────────────────────
    private HBox buildHeader() {
        Label icon  = new Label("✎");
        icon.setStyle("-fx-font-size:18px; -fx-text-fill:#7c3aed;");
        Label title = new Label("Actualizar registro");
        title.getStyleClass().add("panel-title");
        Label badge = new Label("PanelActualizar");
        badge.getStyleClass().add("panel-badge");
        HBox h = new HBox(12, icon, title, badge);
        h.setAlignment(Pos.CENTER_LEFT);
        return h;
    }

    // ── Barra de localización ────────────────────────────────────
    private VBox buildSearchCard() {
        tfBusqueda = new TextField();
        tfBusqueda.setPromptText("Título actual del videojuego...");
        tfBusqueda.getStyleClass().add("gi-input");
        tfBusqueda.setMaxWidth(Double.MAX_VALUE);
        tfBusqueda.setOnAction(e -> cargar());

        Button btnCargar = new Button("⬇  Cargar");
        btnCargar.getStyleClass().add("btn-secondary");
        btnCargar.setOnAction(e -> cargar());

        HBox row = new HBox(8, tfBusqueda, btnCargar);
        HBox.setHgrow(tfBusqueda, Priority.ALWAYS);
        row.setAlignment(Pos.CENTER);

        VBox card = new VBox(12, cardTitle("Localizar registro"), row);
        card.getStyleClass().add("card");
        return card;
    }

    // ── Formulario de edición ────────────────────────────────────
    private VBox buildEditCard() {
        ColumnConstraints col = new ColumnConstraints();
        col.setPercentWidth(50);

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(12);
        grid.getColumnConstraints().addAll(col, col);

        tfTitulo        = editField();
        tfDesarrollador = editField();
        tfAnio          = editField();
        tfPlataformas   = editField();
        tfGenero        = editField();
        taSinopsis      = new TextArea();
        taSinopsis.getStyleClass().add("gi-textarea");
        taSinopsis.setPrefRowCount(3);
        taSinopsis.setWrapText(true);

        grid.add(fieldBox("Título",        tfTitulo),        0, 0, 2, 1);
        grid.add(fieldBox("Desarrollador", tfDesarrollador), 0, 1);
        grid.add(fieldBox("Año",           tfAnio),          1, 1);
        grid.add(fieldBox("Plataformas",   tfPlataformas),   0, 2);
        grid.add(fieldBox("Género",        tfGenero),        1, 2);
        grid.add(fieldBox("Sinopsis",      taSinopsis),      0, 3, 2, 1);

        Button btnCancelar = new Button("✕  Cancelar");
        btnCancelar.getStyleClass().add("btn-secondary");
        btnCancelar.setOnAction(e -> limpiarEdicion());

        Button btnGuardar = new Button("✓  Guardar cambios");
        btnGuardar.getStyleClass().add("btn-primary");
        btnGuardar.setOnAction(e -> guardar());

        HBox botones = new HBox(10, btnCancelar, btnGuardar);
        botones.setAlignment(Pos.CENTER_RIGHT);
        VBox.setMargin(botones, new Insets(4, 0, 0, 0));

        editCard = new VBox(14, cardTitle("Editar campos"), grid, botones);
        editCard.getStyleClass().add("card");
        editCard.setOpacity(0.4); // deshabilitado hasta cargar
        VBox.setVgrow(editCard, Priority.ALWAYS);
        return editCard;
    }

    // ── Lógica ───────────────────────────────────────────────────
    private void cargar() {
        String titulo = tfBusqueda.getText().trim();
        if (titulo.isBlank()) return;

        // ── Integrar backend ─────────────────────────────────────
        // long offset = ventana.getBPlusTree().buscar(titulo);
        // if (offset == -1) { showError("No encontrado: \"" + titulo + "\""); return; }
        // Videojuego v = ventana.getArchivoManager().leerRegistro(offset);
        // rellenar(v);

        // Datos de demostración (eliminar al integrar el backend):
        if (titulo.equalsIgnoreCase("Dark Souls III")) {
            rellenarDemo("Dark Souls III", "FromSoftware", "2016",
                "PC, PS4, Xbox One", "Action RPG",
                "Tercer juego de la saga Dark Souls, ambientado en el reino de Lothric.");
        } else {
            showError("No se encontró el videojuego: \"" + titulo + "\"");
        }

        ventana.setLastOperation("cargar(\"" + titulo + "\")");
    }

    private void rellenarDemo(String titulo, String dev, String anio,
                               String plat, String genero, String sin) {
        tfTitulo.setText(titulo);
        tfDesarrollador.setText(dev);
        tfAnio.setText(anio);
        tfPlataformas.setText(plat);
        tfGenero.setText(genero);
        taSinopsis.setText(sin);
        editCard.setOpacity(1.0);
        editCard.setDisable(false);
    }

    // Usar al integrar el backend real:
    // private void rellenar(Videojuego v) {
    //     tfTitulo.setText(v.getTitulo());
    //     tfDesarrollador.setText(v.getDesarrollador());
    //     tfAnio.setText(String.valueOf(v.getAnio()));
    //     tfPlataformas.setText(v.getPlataformas());
    //     tfGenero.setText(v.getGenero());
    //     taSinopsis.setText(v.getSinopsis());
    //     editCard.setOpacity(1.0);
    //     editCard.setDisable(false);
    // }

    private void guardar() {
        if (tfTitulo.getText().isBlank()) {
            showError("El título no puede estar vacío.");
            return;
        }

        // ── Integrar backend ─────────────────────────────────────
        // String tituloOriginal = tfBusqueda.getText().trim();
        // Videojuego v = new Videojuego(...campos...);
        // ventana.getBPlusTree().actualizar(tituloOriginal, v,
        //                                   ventana.getArchivoManager());

        ventana.setLastOperation("actualizar(\"" + tfTitulo.getText().trim() + "\")");
        showSuccess("Registro actualizado correctamente.");
        limpiarEdicion();
    }

    private void limpiarEdicion() {
        tfTitulo.clear(); tfDesarrollador.clear(); tfAnio.clear();
        tfPlataformas.clear(); tfGenero.clear(); taSinopsis.clear();
        editCard.setOpacity(0.4);
    }

    // ── Helpers ──────────────────────────────────────────────────
    private TextField editField() {
        TextField tf = new TextField();
        tf.getStyleClass().add("gi-input");
        tf.setMaxWidth(Double.MAX_VALUE);
        return tf;
    }

    private VBox fieldBox(String labelText, javafx.scene.Node input) {
        Label lbl = new Label(labelText);
        lbl.getStyleClass().add("field-label");
        VBox box = new VBox(5, lbl, input);
        GridPane.setHgrow(box, Priority.ALWAYS);
        return box;
    }

    private Label cardTitle(String text) {
        Label l = new Label(text.toUpperCase());
        l.getStyleClass().add("card-label");
        return l;
    }

    private void showError(String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK);
        a.setHeaderText("Error"); a.setTitle("GameIndex"); a.showAndWait();
    }

    private void showSuccess(String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK);
        a.setHeaderText("Operación exitosa"); a.setTitle("GameIndex"); a.showAndWait();
    }
}
