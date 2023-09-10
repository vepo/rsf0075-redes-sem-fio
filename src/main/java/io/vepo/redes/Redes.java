package io.vepo.redes;

import static java.util.Objects.nonNull;
import static javafx.application.Platform.runLater;
import static javafx.scene.layout.HBox.setHgrow;
import static javafx.scene.layout.VBox.setVgrow;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import dev.vepo.openjgraph.graph.Graph;
import dev.vepo.openjgraph.graph.Graph.EdgeInfo;
import dev.vepo.openjgraph.graphview.SmartCircularSortedPlacementStrategy;
import dev.vepo.openjgraph.graphview.SmartGraphPanel;
import io.vepo.redes.protocol.Message;
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
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import netscape.javascript.JSObject;

public class Redes extends Application implements RoteamentoObserver {

    public class JavaBridge {
        public void log(String text) {
            // System.out.println(text);
        }
    }

    private static Logger logger = Logger.getLogger(Redes.class.getName());
    private final Historico historico = new Historico();

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

    private AtomicReference<Graph<String, String>> loadedGraph = new AtomicReference<>();

    private WebEngine engine;

    private final JavaBridge bridge = new JavaBridge();

    private final AtomicReference<SmartGraphPanel<String, String>> graphView = new AtomicReference<>();

    @Override
    public void start(Stage stage) throws Exception {
        /*
         * Menu Bar
         */
        var mnuGrafo = new Menu("Grafos");

        var mniAbrir = new MenuItem("Abrir");
        var mniAleatorio = new MenuItem("Aleatório");
        mnuGrafo.getItems().addAll(mniAbrir, mniAleatorio);
        var mnbApp = new MenuBar(mnuGrafo);

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

        var btnRoteamento = new Button("Roteamento");
        btnRoteamento.setOnAction(action -> {
            btnRoteamento.setDisable(true);
            reset();
            Executors.newSingleThreadExecutor()
                     .execute(() -> {
                         var rotas = Roteamento.executa(loadedGraph.get(), this);
                         historico.salva(rotas);
                         runLater(() -> btnRoteamento.setDisable(false));
                     });
        });

        var controlador = new HBox(12,
                                   new Label("Origem: "),
                                   txtOrigem,
                                   new Label("Destino: "),
                                   txtDestino,
                                   btnCaminho,
                                   btnClean,
                                   btnRoteamento);

        controlador.setAlignment(Pos.CENTER);
        var vwLog = new WebView();
        vwLog.setMinWidth(750);
        vwLog.setMaxWidth(750);
        var mainArea = new HBox(graphView, vwLog);
        var root = new VBox(mnbApp, mainArea, controlador);
        mniAbrir.setOnAction(action -> atualizaGrafo(root, stage));
        mniAleatorio.setOnAction(action -> {
            var config = RandomWindow.display(stage);
            var random = new SecureRandom();
            var newGraphView = inicializaGrafo(Graph.random(Graph.<String, String>randomConfig()
                                                                 .nodeSize(config.getOne())
                                                                 .edgeProbability(config.getTwo())
                                                                 .vertexGenerator(i -> String.format("%02d", i))
                                                                 .edgeGenerator((u, v) -> {
                                                                     var weight = 0.5 + random.nextDouble();
                                                                     var properties = new HashMap<String, Object>();
                                                                     properties.put(Roteamento.BANDWIDTH, (int) (100 * weight));
                                                                     properties.put(Roteamento.DELAY, (double) (10 * weight));
                                                                     properties.put(Roteamento.LOSS_PROBABILITY , random.nextFloat(0.1f));
                                                                     return new EdgeInfo<>(String.format("%s - %s", u, v),
                                                                                           weight,
                                                                                           properties);
                                                                 })
                                                                 .allowSelfLoop(false)
                                                                 .build()));

            setHgrow(newGraphView, Priority.ALWAYS);
            mainArea.getChildren().replaceAll(node -> node instanceof SmartGraphPanel ? newGraphView : node);
            runLater(() -> {
                newGraphView.setAutomaticLayout(true);
                newGraphView.init();
            });

        });
        var scene = new Scene(root, 1024, 768);
        setVgrow(mainArea, Priority.ALWAYS);
        setHgrow(graphView, Priority.ALWAYS);
        stage.setMinHeight(500);
        stage.setMinWidth(800);
        stage.setTitle("Redes sem Fio");
        stage.setScene(scene);
        stage.show();
        graphView.setAutomaticLayout(true);
        graphView.init();
        engine = vwLog.getEngine();
        engine.load(Redes.class.getResource("/view.html").toExternalForm());

        engine.getLoadWorker()
              .stateProperty()
              .addListener((observable, oldValue, newValue) -> {
                  JSObject window = (JSObject) engine.executeScript("window");
                  window.setMember("java", bridge);
                  engine.executeScript("""
                                           console.log = function(msg) {
                                               java.log(msg);
                                           }
                                       """);
              });
    }

    @Override
    public void stop() throws Exception {
        Platform.exit();
        System.exit(0);
    }

    private void addMessage(String message) {
        runLater(() -> {
            JSObject window = (JSObject) engine.executeScript("window");
            window.setMember("msg", message);
            engine.executeScript("addMensagem(msg)");
        });
    }

    @Override
    public void nodeFound(String srcNode, String dstNode) {
        addMessage(String.format("Nó %s encontrou %s!", srcNode, dstNode));
    }

    @Override
    public void routeFound(String srcNode, String dstNode, double distance, List<String> path) {
        runLater(() -> {
            JSObject window = (JSObject) engine.executeScript("window");
            window.setMember("srcNode", srcNode);
            window.setMember("dstNode", dstNode);
            window.setMember("distance", distance);
            window.setMember("path", path.stream().collect(Collectors.joining(", ")));
            engine.executeScript("addRoute(srcNode, dstNode, distance, path)");
        });
    }

    @Override
    public void message(String source, String destiny, Message message) {
        addMessage(message.toString());
        synchronized (this) {
            historico.salva(source, destiny, message);
            try {
                Files.write(Paths.get(".", "Mensagens.txt"),
                            String.format("%s -> %s: %s\n", source, destiny, message).getBytes(),
                            StandardOpenOption.APPEND,
                            StandardOpenOption.CREATE);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void reset() {
        engine.executeScript("reset()");
        Paths.get(".", "Mensagens.txt").toFile().delete();
    }

    private SmartGraphPanel<String, String> inicializaGrafo(Path arquivo) throws URISyntaxException {
        return inicializaGrafo(Grafos.ler(arquivo));
    }

    private SmartGraphPanel<String, String> inicializaGrafo(Graph<String, String> graph) {
        var strategy = new SmartCircularSortedPlacementStrategy();
        var panel = new SmartGraphPanel<>(loadedGraph.updateAndGet(__ -> graph), strategy);
        graphView.set(panel);
        historico.salva(graph);
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

        logger.info("Procurando caminho A -> B + " + origemVertex.get() + " " + destinoVertex.get());
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
