package gameindex.gui;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;

/**
 * PanelListar — muestra todos los videojuegos activos del sistema
 * en una tabla con estadísticas de totales, activos y eliminados.
 */
public class PanelListar extends VBox {

    private final VentanaPrincipal ventana;

    private Label lblTotal, lblActivos, lblEliminados;
    private TableView<FilaVideojuego> tabla;

    public PanelListar(VentanaPrincipal ventana) {
        this.ventana = ventana;
        setSpacing(18);
        setFillWidth(true);
        VBox.setVgrow(this, Priority.ALWAYS);

        getChildren().addAll(
            buildHeader(),
            buildStatsRow(),
            buildTableCard()
        );

        cargarDatos();
    }

    // ── Encabezado ───────────────────────────────────────────────
    private HBox buildHeader() {
        Label icon  = new Label("☰");
        icon.setStyle("-fx-font-size:18px; -fx-text-fill:#7c3aed;");
        Label title = new Label("Catálogo completo");
        title.getStyleClass().add("panel-title");
        Label badge = new Label("PanelListar");
        badge.getStyleClass().add("panel-badge");

        Button btnRefresh = new Button("↻  Actualizar");
        btnRefresh.getStyleClass().add("btn-secondary");
        btnRefresh.setOnAction(e -> cargarDatos());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox h = new HBox(12, icon, title, badge, spacer, btnRefresh);
        h.setAlignment(Pos.CENTER_LEFT);
        return h;
    }

    // ── Tarjetas de estadísticas ─────────────────────────────────
    private HBox buildStatsRow() {
        lblTotal      = statValue("0", "-fx-text-fill:#a78bfa;");
        lblActivos    = statValue("0", "-fx-text-fill:#34d399;");
        lblEliminados = statValue("0", "-fx-text-fill:#f87171;");

        HBox row = new HBox(10,
            statCard("Total registros",  lblTotal),
            statCard("Activos",          lblActivos),
            statCard("Eliminados lóg.",  lblEliminados)
        );
        row.setFillHeight(true);
        HBox.setHgrow(row.getChildren().get(0), Priority.ALWAYS);
        HBox.setHgrow(row.getChildren().get(1), Priority.ALWAYS);
        HBox.setHgrow(row.getChildren().get(2), Priority.ALWAYS);
        return row;
    }

    private VBox statCard(String label, Label valueLabel) {
        Label lbl = new Label(label.toUpperCase());
        lbl.getStyleClass().add("stat-label");
        VBox card = new VBox(4, lbl, valueLabel);
        card.getStyleClass().add("stat-card");
        card.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(card, Priority.ALWAYS);
        return card;
    }

    private Label statValue(String val, String style) {
        Label l = new Label(val);
        l.getStyleClass().add("stat-value");
        l.setStyle(l.getStyle() + style);
        return l;
    }

    // ── Tabla principal ──────────────────────────────────────────
    private VBox buildTableCard() {
        tabla = new TableView<>();
        tabla.getStyleClass().add("gi-table");
        tabla.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tabla.setPlaceholder(placeholder("No hay registros en el sistema."));
        VBox.setVgrow(tabla, Priority.ALWAYS);

        TableColumn<FilaVideojuego, String> colNum  = col("#",           "numero",      0.5);
        TableColumn<FilaVideojuego, String> colTit  = col("Título",      "titulo",      3);
        TableColumn<FilaVideojuego, String> colAnio = col("Año",         "anio",        1);
        TableColumn<FilaVideojuego, String> colGen  = col("Género",      "genero",      1.5);
        TableColumn<FilaVideojuego, String> colPlat = col("Plataformas", "plataformas", 2);

        // Columna de acciones
        TableColumn<FilaVideojuego, Void> colAcc = new TableColumn<>("Acciones");
        colAcc.setMaxWidth(1f * Integer.MAX_VALUE * 10);
        colAcc.setCellFactory(tc -> new TableCell<>() {
            final Button btnEditar   = accionBtn("✎");
            final Button btnEliminar = accionBtn("🗑");
            final HBox box = new HBox(4, btnEditar, btnEliminar);
            {
                box.setAlignment(Pos.CENTER);
                btnEditar.setOnAction(e -> {
                    FilaVideojuego fila = getTableView().getItems().get(getIndex());
                    // Navegar a PanelActualizar con el título precargado:
                    // ventana.switchPanel(btnActualizar, new PanelActualizar(ventana, fila.getTitulo()));
                });
                btnEliminar.setOnAction(e -> {
                    FilaVideojuego fila = getTableView().getItems().get(getIndex());
                    // ventana.getBPlusTree().eliminarLogico(fila.getTitulo(), ventana.getArchivoManager());
                    // cargarDatos();
                });
            }
            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : box);
            }
        });

        tabla.getColumns().addAll(colNum, colTit, colAnio, colGen, colPlat, colAcc);

        VBox card = new VBox(12, tabla);
        card.getStyleClass().add("card");
        VBox.setVgrow(card, Priority.ALWAYS);
        return card;
    }

    // ── Carga de datos ───────────────────────────────────────────
    private void cargarDatos() {
        ObservableList<FilaVideojuego> filas = FXCollections.observableArrayList();

        // ── Integrar backend ─────────────────────────────────────
        // List<Long> offsets = ventana.getBPlusTree().listarTodos();
        // int total = offsets.size();
        // int eliminados = 0;
        // int idx = 1;
        // for (long offset : offsets) {
        //     Videojuego v = ventana.getArchivoManager().leerRegistro(offset);
        //     if (v.getEliminado() == 1) { eliminados++; continue; }
        //     filas.add(new FilaVideojuego(String.format("%03d", idx++),
        //         v.getTitulo(), String.valueOf(v.getAnio()),
        //         v.getGenero(), v.getPlataformas()));
        // }
        // lblTotal.setText(String.valueOf(total));
        // lblActivos.setText(String.valueOf(filas.size()));
        // lblEliminados.setText(String.valueOf(eliminados));

        // Datos de demostración (eliminar al integrar el backend):
        filas.addAll(
            new FilaVideojuego("001", "Elden Ring",    "2022", "Action RPG", "PC, PS5"),
            new FilaVideojuego("002", "Dark Souls III","2016", "Action RPG", "PC, PS4"),
            new FilaVideojuego("003", "Disco Elysium", "2019", "RPG",        "PC"),
            new FilaVideojuego("004", "Celeste",       "2018", "Platformer", "PC, Switch")
        );
        lblTotal.setText("248");
        lblActivos.setText("231");
        lblEliminados.setText("17");

        tabla.setItems(filas);
        ventana.setLastOperation("listarTodos()");
    }

    // ── Helpers ──────────────────────────────────────────────────
    private <T> TableColumn<FilaVideojuego, T> col(String header, String prop, double weight) {
        TableColumn<FilaVideojuego, T> c = new TableColumn<>(header);
        c.setCellValueFactory(new PropertyValueFactory<>(prop));
        c.setMaxWidth(1f * Integer.MAX_VALUE * (weight * 10));
        return c;
    }

    private Button accionBtn(String text) {
        Button b = new Button(text);
        b.getStyleClass().add("btn-icon");
        return b;
    }

    private Label placeholder(String text) {
        Label l = new Label(text);
        l.setStyle("-fx-text-fill:#3a2e6a; -fx-font-size:12px;");
        return l;
    }

    // ── Modelo de fila ───────────────────────────────────────────
    public static class FilaVideojuego {
        private final javafx.beans.property.SimpleStringProperty
            numero, titulo, anio, genero, plataformas;

        public FilaVideojuego(String numero, String titulo, String anio,
                               String genero, String plataformas) {
            this.numero      = new javafx.beans.property.SimpleStringProperty(numero);
            this.titulo      = new javafx.beans.property.SimpleStringProperty(titulo);
            this.anio        = new javafx.beans.property.SimpleStringProperty(anio);
            this.genero      = new javafx.beans.property.SimpleStringProperty(genero);
            this.plataformas = new javafx.beans.property.SimpleStringProperty(plataformas);
        }

        public String getNumero()      { return numero.get(); }
        public String getTitulo()      { return titulo.get(); }
        public String getAnio()        { return anio.get(); }
        public String getGenero()      { return genero.get(); }
        public String getPlataformas() { return plataformas.get(); }
    }
}
