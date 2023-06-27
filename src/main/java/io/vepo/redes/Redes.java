package io.vepo.redes;

import static java.util.Objects.nonNull;
import static javafx.application.Platform.runLater;
import static javafx.scene.layout.VBox.setVgrow;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;

import dev.vepo.openjgraph.graph.Graph;
import dev.vepo.openjgraph.graphview.SmartCircularSortedPlacementStrategy;
import dev.vepo.openjgraph.graphview.SmartGraphPanel;
import dev.vepo.openjgraph.graphview.SmartGraphProperties;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class Redes extends Application {

    private static Logger logger = Logger.getLogger(Redes.class.getName());
    private static final LeitorGrafo leitor = new LeitorGrafo();
    private AtomicReference<Graph<String, String>> loadedGraph = new AtomicReference<>();

    public static void main(String[] args) {
        Runtime.getRuntime()
               .addShutdownHook(new Thread() {
                   @Override
                   public void run() {
                       Platform.exit();
                   }
               });
        launch();
    }

    @Override
    public void start(Stage stage) throws Exception {
        /*
         * Menu Bar
         */
        var grafoMenu = new Menu("Grafos");
        var abrir = new MenuItem("Abrir");
        grafoMenu.getItems().add(abrir);
        var menuBar = new MenuBar(grafoMenu);

        /*
         * Grafo
         */
        var graphView = inicializaGrafo(Paths.get(".", "resources", "rede.txt"));

        /*
         * Controlador
         */
        var btnCaminho = new Button("Ver caminho");
        var txtOrigem = new TextField();
        var txtDestino = new TextField();
        btnCaminho.setOnAction(action -> verCaminho(txtOrigem, txtDestino));
        var btnClean = new Button("Limpar");
        btnClean.setOnAction(action -> limpar());
        var controlador = new HBox(12,
                                   new Label("Origem: "),
                                   txtOrigem,
                                   new Label("Destino: "),
                                   txtDestino,
                                   btnCaminho,
                                   btnClean);

        controlador.setAlignment(Pos.CENTER);
        var root = new VBox(menuBar, graphView, controlador);
        abrir.setOnAction(action -> atualizaGrafo(root, stage));
        var scene = new Scene(root, 1024, 768);
        setVgrow(graphView, Priority.ALWAYS);
        stage.setMinHeight(500);
        stage.setMinWidth(800);
        stage.setTitle("Redes sem Fio");
        stage.setScene(scene);
        stage.show();
        graphView.setAutomaticLayout(true);
        graphView.init();

    }

    @Override
    public void stop() throws Exception {
        Platform.exit();
        System.exit(0);
    }

    private final AtomicReference<SmartGraphPanel<String, String>> graphView = new AtomicReference<>();

    private SmartGraphPanel<String, String> inicializaGrafo(Path arquivo) throws URISyntaxException {
        var strategy = new SmartCircularSortedPlacementStrategy();
        var panel = new SmartGraphPanel<>(loadedGraph.updateAndGet(__ -> leitor.ler(arquivo)), strategy);
        graphView.set(panel);
        return panel;
    }

    private void limpar() {
        graphView.get().clearHighlight();
    }

    private void verCaminho(TextField txtOrigem, TextField txtDestino) {
        if (txtOrigem.getText().isBlank()) {
            exibeErro("Calcular rota", "Origem não selecionada!");
        }

        var origemVertex = loadedGraph.get().vertex(txtOrigem.getText().trim());
        if (!origemVertex.isPresent()) {
            exibeErro("Calcular rota", "Origem selecionada não encontrada!");
        }

        if (txtDestino.getText().isBlank()) {
            exibeErro("Calcular rota", "Destino não selecionado!");
        }

        var destinoVertex = loadedGraph.get().vertex(txtDestino.getText().trim());
        if (!destinoVertex.isPresent()) {
            exibeErro("Calcular rota", "Destino selecionado não encontrado!");
        }

        logger.info("Procurando caminho A -> B + " + origemVertex.get() + " "+ destinoVertex.get());
        var path = loadedGraph.get().dijkstra(origemVertex.get(), destinoVertex.get());
        logger.info("Procurando caminho A -> B + " + path);
        graphView.get().highlight(path);
    }

    private void exibeErro(String titulo, String mensagem) {
        var errorAlert = new Alert(AlertType.ERROR);
        errorAlert.setHeaderText(titulo);
        errorAlert.setContentText(mensagem);
        errorAlert.showAndWait();
    }

    private void atualizaGrafo(Pane root, Stage stage) {
        var fileChooser = new FileChooser();
        fileChooser.setInitialDirectory(Paths.get(".").toFile());
        fileChooser.setTitle("Abrir grafo");

        var selectedFile = fileChooser.showOpenDialog(stage);
        if (nonNull(selectedFile)) {
            try {
                var newGraphView = inicializaGrafo(selectedFile.toPath());

                setVgrow(newGraphView, Priority.ALWAYS);
                root.getChildren().replaceAll(node -> node instanceof SmartGraphPanel ? newGraphView : node);
                runLater(() -> {
                    newGraphView.setAutomaticLayout(true);
                    newGraphView.init();
                });
            } catch (URISyntaxException e) {
                var errorAlert = new Alert(AlertType.ERROR);
                errorAlert.setHeaderText("Abrir Grafo");
                errorAlert.setContentText("Não foi possível abrir grafo!");
                errorAlert.showAndWait();
            }
        }
    }

}
