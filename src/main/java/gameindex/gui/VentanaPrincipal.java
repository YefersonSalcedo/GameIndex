package gameindex.gui;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;

/**
 * VentanaPrincipal — punto central de GameIndex.
 * Coordina la navegación entre paneles y comparte las instancias
 * de ArchivoManager y BPlusTree (pasar por constructor o setters).
 */
public class VentanaPrincipal extends Application {

    // ── Estado global del sistema ──────────────────────────────────
    // Reemplaza estos campos con tus instancias reales:
    // private ArchivoManager archivoManager;
    // private BPlusTree bPlusTree;

    private Label statusLabel;   // barra inferior — última operación

    // Botones de navegación para poder cambiar su estilo
    private Button btnInsertar, btnBuscar, btnRango,
                   btnActualizar, btnEliminar, btnListar;
    private Button activeButton;

    // Contenedor central donde se intercambian los paneles
    private StackPane contentPane;

    @Override
    public void start(Stage stage) {
        // ── Inicializar backend aquí ────────────────────────────────
        // archivoManager = new ArchivoManager("data.dat");
        // bPlusTree      = new BPlusTree(archivoManager);

        BorderPane root = new BorderPane();
        root.getStyleClass().add("root");

        root.setTop(buildTopBar());
        root.setLeft(buildSidebar());
        root.setCenter(buildContentArea());
        root.setBottom(buildStatusBar());

        Scene scene = new Scene(root, 860, 600);
        scene.getStylesheets().add(
                getClass().getResource("/gameindex/gameindex.css").toExternalForm()
        );

        stage.setTitle("GameIndex — Sistema de Preservación y Gestión de Videojuegos");
        stage.setScene(scene);
        stage.setMinWidth(760);
        stage.setMinHeight(520);
        stage.show();

        // Panel inicial
        switchPanel(btnInsertar, new PanelInsertar(this));
    }

    // ══════════════════════════════════════════════════════════════
    //  BARRA SUPERIOR
    // ══════════════════════════════════════════════════════════════
    private HBox buildTopBar() {
        // Logo
        Label logo = new Label("GAMEINDEX");
        logo.getStyleClass().add("logo-text");

        Label sub = new Label("Sistema de Preservación y Gestión");
        sub.getStyleClass().add("subtitle-text");

        VBox logoBox = new VBox(2, logo, sub);
        logoBox.setAlignment(Pos.CENTER_LEFT);

        // Indicador de estado (punto verde + texto)
        Circle dot = new Circle(4, Color.web("#34d399"));
        Label statusRight = new Label("B+ Tree activo");
        statusRight.getStyleClass().add("status-text");
        HBox statusBox = new HBox(6, dot, statusRight);
        statusBox.setAlignment(Pos.CENTER);

        Label fileLabel = new Label("⬡  data.dat");
        fileLabel.setStyle("-fx-font-size:11px; -fx-text-fill:#6b5fa8;");

        HBox right = new HBox(16, statusBox, fileLabel);
        right.setAlignment(Pos.CENTER_RIGHT);

        HBox bar = new HBox(logoBox, right);
        HBox.setHgrow(logoBox, Priority.ALWAYS);
        bar.setAlignment(Pos.CENTER);
        bar.getStyleClass().add("top-bar");
        return bar;
    }

    // ══════════════════════════════════════════════════════════════
    //  BARRA LATERAL
    // ══════════════════════════════════════════════════════════════
    private VBox buildSidebar() {
        VBox sidebar = new VBox();
        sidebar.getStyleClass().add("sidebar");

        // ── Sección Sistema ──
        Label secSistema = new Label("SISTEMA");
        secSistema.getStyleClass().add("nav-section-label");

        btnInsertar  = navBtn("Insertar");
        btnBuscar    = navBtn("Buscar");
        btnRango     = navBtn("Buscar rango");
        btnActualizar = navBtn("Actualizar");
        btnEliminar  = navBtn("Eliminar");

        // ── Sección Catálogo ──
        Label secCatalogo = new Label("CATÁLOGO");
        secCatalogo.getStyleClass().add("nav-section-label");
        VBox.setMargin(secCatalogo, new Insets(14, 0, 0, 0));

        btnListar = navBtn("Listar todos");

        // Eventos de navegación
        btnInsertar.setOnAction(e  -> switchPanel(btnInsertar,  new PanelInsertar(this)));
        btnBuscar.setOnAction(e    -> switchPanel(btnBuscar,    new PanelBuscar(this)));
        btnRango.setOnAction(e     -> switchPanel(btnRango,     new PanelBuscarRango(this)));
        btnActualizar.setOnAction(e -> switchPanel(btnActualizar, new PanelActualizar(this)));
        btnEliminar.setOnAction(e  -> switchPanel(btnEliminar,  new PanelEliminar(this)));
        btnListar.setOnAction(e    -> switchPanel(btnListar,    new PanelListar(this)));

        // ── Indicador B+ Tree ──
        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        Label bpLabel = new Label("ÍNDICE B+");
        bpLabel.setStyle("-fx-font-size:10px; -fx-text-fill:#3a2e6a; -fx-padding: 0 0 6 0;");

        ProgressBar bpBar = new ProgressBar(0.62);
        bpBar.setStyle("-fx-accent:#7c3aed; -fx-background-color:#0d0b1e;");
        bpBar.setMaxWidth(Double.MAX_VALUE);
        bpBar.setPrefHeight(4);

        Label bpInfo = new Label("248 / 400 nodos");
        bpInfo.setStyle("-fx-font-size:10px; -fx-text-fill:#4a3d80; -fx-padding: 4 0 0 0;");

        VBox bpBox = new VBox(4, bpLabel, bpBar, bpInfo);
        bpBox.setPadding(new Insets(10, 12, 12, 12));
        bpBox.setStyle("-fx-border-color: #1e1545; -fx-border-width: 1 0 0 0;");

        sidebar.getChildren().addAll(
            secSistema,
            btnInsertar, btnBuscar, btnRango, btnActualizar, btnEliminar,
            secCatalogo,
            btnListar,
            spacer,
            bpBox
        );
        return sidebar;
    }

    // ══════════════════════════════════════════════════════════════
    //  ÁREA DE CONTENIDO
    // ══════════════════════════════════════════════════════════════
    private StackPane buildContentArea() {
        contentPane = new StackPane();
        contentPane.getStyleClass().add("content-area");
        contentPane.setPadding(new Insets(24));
        return contentPane;
    }

    // ══════════════════════════════════════════════════════════════
    //  BARRA DE ESTADO INFERIOR
    // ══════════════════════════════════════════════════════════════
    private HBox buildStatusBar() {
        Label nodeLabel = new Label("Árbol B+ listo");
        nodeLabel.getStyleClass().add("status-bar-text");

        Label fileLabel = new Label("data.dat");
        fileLabel.getStyleClass().add("status-bar-text");

        statusLabel = new Label("Última operación:");
        statusLabel.getStyleClass().add("status-bar-text");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label versionLabel = new Label("GameIndex — Estructuras de Datos");
        versionLabel.setStyle("-fx-font-size:10px; -fx-text-fill:#3a2e6a;");

        HBox bar = new HBox(20, nodeLabel, fileLabel, statusLabel, spacer, versionLabel);
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.getStyleClass().add("status-bar");
        return bar;
    }

    // ══════════════════════════════════════════════════════════════
    //  UTILIDADES PÚBLICAS
    // ══════════════════════════════════════════════════════════════

    /** Cambia el panel visible y resalta el botón activo. */
    public void switchPanel(Button origen, Node panel) {
        if (activeButton != null) {
            activeButton.getStyleClass().remove("nav-button-active");
        }
        origen.getStyleClass().add("nav-button-active");
        activeButton = origen;

        contentPane.getChildren().setAll(panel);
    }

    /** Actualiza el texto de última operación en la barra inferior. */
    public void setLastOperation(String op) {
        statusLabel.setText("Última operación: " + op);
    }

    // ── Acceso al backend ────────────────────────────────────────
    // public ArchivoManager getArchivoManager() { return archivoManager; }
    // public BPlusTree      getBPlusTree()      { return bPlusTree; }

    // ══════════════════════════════════════════════════════════════
    //  CONSTRUCTOR DE BOTÓN LATERAL
    // ══════════════════════════════════════════════════════════════
    private Button navBtn(String text) {
        Button btn = new Button(text);
        btn.getStyleClass().add("nav-button");
        btn.setMaxWidth(Double.MAX_VALUE);
        return btn;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
