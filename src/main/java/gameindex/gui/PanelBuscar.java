package gameindex.gui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

/**
 * PanelBuscar — búsqueda exacta de un videojuego por título.
 * Muestra todos los atributos del registro encontrado, incluido
 * el offset en disco para verificar el funcionamiento del B+.
 */
public class PanelBuscar extends VBox {

    private final VentanaPrincipal ventana;

    private TextField tfBusqueda;

    // Etiquetas de resultado
    private Label valTitulo, valDesarrollador, valAnio, valPlataformas,
                  valGenero, valOffset, valSinopsis;

    private VBox resultCard;

    public PanelBuscar(VentanaPrincipal ventana) {
        this.ventana = ventana;
        setSpacing(18);
        setFillWidth(true);
        VBox.setVgrow(this, Priority.ALWAYS);

        getChildren().addAll(
            buildHeader(),
            buildSearchCard(),
            buildResultCard()
        );
    }

    // ── Encabezado ───────────────────────────────────────────────
    private HBox buildHeader() {
        Label icon  = new Label("🔍");
        icon.setStyle("-fx-font-size:18px;");

        Label title = new Label("Búsqueda exacta");
        title.getStyleClass().add("panel-title");

        Label badge = new Label("PanelBuscar");
        badge.getStyleClass().add("panel-badge");

        HBox h = new HBox(12, icon, title, badge);
        h.setAlignment(Pos.CENTER_LEFT);
        return h;
    }

    // ── Barra de búsqueda ────────────────────────────────────────
    private VBox buildSearchCard() {
        tfBusqueda = new TextField();
        tfBusqueda.setPromptText("Ingresa el título exacto del videojuego...");
        tfBusqueda.getStyleClass().add("gi-input");
        tfBusqueda.setMaxWidth(Double.MAX_VALUE);
        // Permitir buscar con Enter
        tfBusqueda.setOnAction(e -> buscar());

        Button btnBuscar = new Button("→  Buscar");
        btnBuscar.getStyleClass().add("btn-primary");
        btnBuscar.setOnAction(e -> buscar());

        HBox searchRow = new HBox(8, tfBusqueda, btnBuscar);
        HBox.setHgrow(tfBusqueda, Priority.ALWAYS);
        searchRow.setAlignment(Pos.CENTER);

        VBox card = new VBox(12, cardTitle("Buscar por título"), searchRow);
        card.getStyleClass().add("card");
        return card;
    }

    // ── Tarjeta de resultado ─────────────────────────────────────
    private VBox buildResultCard() {
        valTitulo       = resultVal("-");
        valDesarrollador = resultVal("-");
        valAnio          = resultVal("-");
        valGenero        = resultVal("-");
        valPlataformas   = resultVal("-");
        valOffset        = resultVal("-");
        valSinopsis      = resultVal("-");

        // Estilos especiales
        valTitulo.setStyle("-fx-font-size:13px; -fx-font-weight:bold; -fx-text-fill:#c4b5fd;");
        valOffset.setStyle("-fx-font-size:11px; -fx-font-family:'Consolas','Courier New',monospace; -fx-text-fill:#4a3d80;");

        GridPane grid = new GridPane();
        grid.setHgap(20);
        grid.setVgap(12);

        ColumnConstraints c1 = new ColumnConstraints();
        c1.setPercentWidth(50);
        ColumnConstraints c2 = new ColumnConstraints();
        c2.setPercentWidth(50);
        grid.getColumnConstraints().addAll(c1, c2);

        grid.add(fieldBox("Título",        valTitulo),        0, 0);
        grid.add(fieldBox("Desarrollador", valDesarrollador), 1, 0);
        grid.add(fieldBox("Año",           valAnio),          0, 1);
        grid.add(fieldBox("Género",        valGenero),        1, 1);
        grid.add(fieldBox("Plataformas",   valPlataformas),   0, 2);
        grid.add(fieldBox("Offset en disco", valOffset),      1, 2);
        grid.add(fieldBox("Sinopsis",      valSinopsis),      0, 3, 2, 1);

        valSinopsis.setStyle("-fx-font-size:12px; -fx-text-fill:#6b5fa8; -fx-wrap-text:true;");
        valSinopsis.setWrapText(true);
        valSinopsis.setMaxWidth(Double.MAX_VALUE);

        resultCard = new VBox(14, cardTitle("Resultado"), grid);
        resultCard.getStyleClass().add("card");
        VBox.setVgrow(resultCard, Priority.ALWAYS);
        return resultCard;
    }

    // ── Lógica de búsqueda ───────────────────────────────────────
    private void buscar() {
        String titulo = tfBusqueda.getText().trim();
        if (titulo.isBlank()) return;

        // ── Conectar con el backend ──────────────────────────────
        // long offset = ventana.getBPlusTree().buscar(titulo);
        // if (offset == -1) {
        //     mostrarVacio(titulo);
        //     return;
        // }
        // Videojuego v = ventana.getArchivoManager().leerRegistro(offset);
        // mostrarResultado(v, offset);

        // Datos de demostración (eliminar al integrar el backend):
        if (titulo.equalsIgnoreCase("Elden Ring")) {
            mostrarResultadoDemo(
                "Elden Ring", "FromSoftware", "2022",
                "Action RPG", "PC, PS5, Xbox Series",
                "0x00003A80",
                "Un RPG de acción en un vasto mundo abierto creado por FromSoftware."
            );
        } else {
            mostrarVacio(titulo);
        }

        ventana.setLastOperation("buscar(\"" + titulo + "\")");
    }

    private void mostrarResultadoDemo(String titulo, String dev, String anio,
                                      String genero, String plataformas,
                                      String offset, String sinopsis) {
        valTitulo.setText(titulo);
        valDesarrollador.setText(dev);
        valAnio.setText(anio);
        valGenero.setText(genero);
        valPlataformas.setText(plataformas);
        valOffset.setText(offset);
        valSinopsis.setText(sinopsis);
    }

    /** Usar al integrar el backend real:
     *  mostrarResultado(Videojuego v, long offset) */
    // private void mostrarResultado(Videojuego v, long offset) {
    //     valTitulo.setText(v.getTitulo());
    //     valDesarrollador.setText(v.getDesarrollador());
    //     valAnio.setText(String.valueOf(v.getAnio()));
    //     valGenero.setText(v.getGenero());
    //     valPlataformas.setText(v.getPlataformas());
    //     valOffset.setText(String.format("0x%08X", offset));
    //     valSinopsis.setText(v.getSinopsis());
    // }

    private void mostrarVacio(String titulo) {
        valTitulo.setText("No encontrado: \"" + titulo + "\"");
        valTitulo.setStyle("-fx-font-size:13px; -fx-text-fill:#f87171;");
        valDesarrollador.setText("-");
        valAnio.setText("-");
        valGenero.setText("-");
        valPlataformas.setText("-");
        valOffset.setText("-");
        valSinopsis.setText("-");
    }

    // ── Helpers ──────────────────────────────────────────────────
    private Label resultVal(String text) {
        Label l = new Label(text);
        l.setStyle("-fx-font-size:13px; -fx-text-fill:#9d8fd4;");
        return l;
    }

    private VBox fieldBox(String labelText, Label value) {
        Label lbl = new Label(labelText);
        lbl.getStyleClass().add("field-label");
        return new VBox(4, lbl, value);
    }

    private Label cardTitle(String text) {
        Label l = new Label(text.toUpperCase());
        l.getStyleClass().add("card-label");
        return l;
    }
}
