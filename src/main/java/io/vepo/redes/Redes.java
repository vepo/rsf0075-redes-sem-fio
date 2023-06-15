package io.vepo.redes;

import java.nio.file.Paths;

import com.brunomnsilva.smartgraph.graph.Graph;
import com.brunomnsilva.smartgraph.graph.GraphEdgeList;
import com.brunomnsilva.smartgraph.graphview.SmartCircularSortedPlacementStrategy;
import com.brunomnsilva.smartgraph.graphview.SmartGraphPanel;
import com.brunomnsilva.smartgraph.graphview.SmartGraphProperties;
import com.brunomnsilva.smartgraph.graphview.SmartPlacementStrategy;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Redes extends Application {

    private static final LeitorGrafo leitor = new LeitorGrafo();

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
        var strategy = new SmartCircularSortedPlacementStrategy();
        var properties = new SmartGraphProperties(Redes.class.getResourceAsStream("/smartgraph.properties"));
        var graphView = new SmartGraphPanel<>(leitor.ler(Paths.get(".", "resources", "rede.txt")),
                                              properties, strategy,
                                              Redes.class.getResource("/smartgraph.css")
                                                         .toURI());
        var scene = new Scene(graphView, 1024, 768);
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

}
