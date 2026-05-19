package gameindex.gui;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;

/**
 * PanelBuscarRango — búsqueda por rango alfabético y por prefijo.
 * Aprovecha el encadenamiento de hojas del Árbol B+ para recorrer
 * rangos eficientemente.
 */
public class PanelBuscarRango extends VBox {

    private final VentanaPrincipal ventana;

    private TextField tfDesde, tfHasta, tfPrefijo;
    private Label     lblConteo;
    private TableView<FilaResultado> tabla;

    public PanelBuscarRango(VentanaPrincipal ventana) {
        this.ventana = ventana;
        setSpacing(18);
        setFillWidth(true);
        VBox.setVgrow(this, Priority.ALWAYS);

        getChildren().addAll(
            buildHeader(),
            buildQueryCard(),
            buildResultCard()
        );
    }

    // ── Encabezado ───────────────────────────────────────────────
    private HBox buildHeader() {
        Label icon  = new Label("≡");
        icon.setStyle("-fx-font-size:18px; -fx-text-fill:#7c3aed;");
        Label title = new Label("Búsqueda por rango");
        title.getStyleClass().add("panel-title");
        Label badge = new Label("PanelBuscarRango");
        badge.getStyleClass().add("panel-badge");
        HBox h = new HBox(12, icon, title, badge);
        h.setAlignment(Pos.CENTER_LEFT);
        return h;
    }

    // ── Formulario de consulta ───────────────────────────────────
    private VBox buildQueryCard() {
        // Rango alfabético
        tfDesde = inputField("Título inicial  (ej: D)");
        tfHasta = inputField("Título final  (ej: F)");

        ColumnConstraints col = new ColumnConstraints();
        col.setPercentWidth(50);

        GridPane rangeGrid = new GridPane();
        rangeGrid.setHgap(12);
        rangeGrid.getColumnConstraints().addAll(col, col);
        rangeGrid.add(fieldBox("Desde (título)", tfDesde), 0, 0);
        rangeGrid.add(fieldBox("Hasta (título)", tfHasta), 1, 0);

        // Separador con texto
        Label orLabel = new Label("— o buscar por prefijo —");
        orLabel.setStyle("-fx-font-size:10px; -fx-text-fill:#3a2e6a;");
        HBox orRow = new HBox(orLabel);
        orRow.setAlignment(Pos.CENTER);
        VBox.setMargin(orRow, new Insets(4, 0, 4, 0));

        // Prefijo
        tfPrefijo = inputField("Ej: Dark, Final Fantasy, Zelda…");

        // Botón
        Button btnConsultar = new Button("⊞  Consultar");
        btnConsultar.getStyleClass().add("btn-primary");
        btnConsultar.setOnAction(e -> consultar());

        HBox btnRow = new HBox(btnConsultar);
        btnRow.setAlignment(Pos.CENTER_RIGHT);
        VBox.setMargin(btnRow, new Insets(4, 0, 0, 0));

        VBox card = new VBox(12,
            cardTitle("Rango alfabético"),
            rangeGrid,
            orRow,
            fieldBox("Prefijo / franquicia", tfPrefijo),
            btnRow
        );
        card.getStyleClass().add("card");
        return card;
    }

    // ── Tabla de resultados ──────────────────────────────────────
    private VBox buildResultCard() {
        tabla = new TableView<>();
        tabla.getStyleClass().add("gi-table");
        tabla.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tabla.setPlaceholder(labelPlaceholder("Sin resultados. Realiza una consulta."));
        VBox.setVgrow(tabla, Priority.ALWAYS);

        TableColumn<FilaResultado, String> colTitulo = col("Título", "titulo", 3);
        TableColumn<FilaResultado, String> colAnio   = col("Año",    "anio",   1);
        TableColumn<FilaResultado, String> colGenero = col("Género", "genero", 1);
        TableColumn<FilaResultado, String> colPlat   = col("Plataformas", "plataformas", 2);

        tabla.getColumns().addAll(colTitulo, colAnio, colGenero, colPlat);

        // Encabezado de resultado con conteo
        lblConteo = new Label("0 coincidencias");
        lblConteo.setStyle("-fx-font-size:11px; -fx-text-fill:#6b5fa8;");

        HBox resultHeader = new HBox(cardTitle("Resultados"), new Region(), lblConteo);
        HBox.setHgrow(resultHeader.getChildren().get(1), Priority.ALWAYS);
        resultHeader.setAlignment(Pos.CENTER_LEFT);

        VBox card = new VBox(12, resultHeader, tabla);
        card.getStyleClass().add("card");
        VBox.setVgrow(card, Priority.ALWAYS);
        return card;
    }

    // ── Lógica de consulta ───────────────────────────────────────
    private void consultar() {
        ObservableList<FilaResultado> filas = FXCollections.observableArrayList();

        boolean porPrefijo = !tfPrefijo.getText().isBlank();
        boolean porRango   = !tfDesde.getText().isBlank() || !tfHasta.getText().isBlank();

        if (!porPrefijo && !porRango) return;

        // ── Integrar aquí el backend ─────────────────────────────
        // if (porPrefijo) {
        //     List<Long> offsets = ventana.getBPlusTree().buscarPrefijo(tfPrefijo.getText().trim());
        //     for (long offset : offsets) {
        //         Videojuego v = ventana.getArchivoManager().leerRegistro(offset);
        //         filas.add(new FilaResultado(v.getTitulo(), String.valueOf(v.getAnio()),
        //                                    v.getGenero(), v.getPlataformas()));
        //     }
        // } else {
        //     List<Long> offsets = ventana.getBPlusTree().buscarRango(
        //         tfDesde.getText().trim(), tfHasta.getText().trim());
        //     for (long offset : offsets) {
        //         Videojuego v = ventana.getArchivoManager().leerRegistro(offset);
        //         filas.add(new FilaResultado(v.getTitulo(), String.valueOf(v.getAnio()),
        //                                    v.getGenero(), v.getPlataformas()));
        //     }
        // }

        // Datos de demostración (eliminar al integrar el backend):
        filas.addAll(
            new FilaResultado("Dark Souls III",  "2016", "Action RPG", "PC, PS4, Xbox One"),
            new FilaResultado("Disco Elysium",   "2019", "RPG",        "PC"),
            new FilaResultado("F-Zero GX",       "2003", "Racing",     "GameCube")
        );

        tabla.setItems(filas);
        lblConteo.setText(filas.size() + " coincidencia" + (filas.size() == 1 ? "" : "s"));
        ventana.setLastOperation(porPrefijo
            ? "buscarPrefijo(\"" + tfPrefijo.getText().trim() + "\")"
            : "buscarRango(\"" + tfDesde.getText().trim() + "\", \"" + tfHasta.getText().trim() + "\")");
    }

    // ── Helpers ──────────────────────────────────────────────────
    private TextField inputField(String prompt) {
        TextField tf = new TextField();
        tf.setPromptText(prompt);
        tf.getStyleClass().add("gi-input");
        tf.setMaxWidth(Double.MAX_VALUE);
        return tf;
    }

    private VBox fieldBox(String lbl, javafx.scene.Node input) {
        Label label = new Label(lbl);
        label.getStyleClass().add("field-label");
        VBox box = new VBox(5, label, input);
        GridPane.setHgrow(box, Priority.ALWAYS);
        return box;
    }

    private Label cardTitle(String text) {
        Label l = new Label(text.toUpperCase());
        l.getStyleClass().add("card-label");
        return l;
    }

    private Label labelPlaceholder(String text) {
        Label l = new Label(text);
        l.setStyle("-fx-text-fill:#3a2e6a; -fx-font-size:12px;");
        return l;
    }

    private <T> TableColumn<FilaResultado, T> col(String header, String prop, double weight) {
        TableColumn<FilaResultado, T> c = new TableColumn<>(header);
        c.setCellValueFactory(new PropertyValueFactory<>(prop));
        c.setMaxWidth(1f * Integer.MAX_VALUE * (weight * 10));
        return c;
    }

    // ── Modelo de fila para la tabla ─────────────────────────────
    public static class FilaResultado {
        private final javafx.beans.property.SimpleStringProperty titulo, anio, genero, plataformas;

        public FilaResultado(String titulo, String anio, String genero, String plataformas) {
            this.titulo      = new javafx.beans.property.SimpleStringProperty(titulo);
            this.anio        = new javafx.beans.property.SimpleStringProperty(anio);
            this.genero      = new javafx.beans.property.SimpleStringProperty(genero);
            this.plataformas = new javafx.beans.property.SimpleStringProperty(plataformas);
        }

        public String getTitulo()      { return titulo.get(); }
        public String getAnio()        { return anio.get(); }
        public String getGenero()      { return genero.get(); }
        public String getPlataformas() { return plataformas.get(); }
    }
}
