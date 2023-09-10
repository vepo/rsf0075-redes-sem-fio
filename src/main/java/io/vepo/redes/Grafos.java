package io.vepo.redes;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;

import dev.vepo.openjgraph.graph.Graph;

public class Grafos {

    private Grafos() {
    }

    public static void escrever(Path arquivo, Graph<String, String> grafo) {
        try (PrintWriter writer = new PrintWriter(arquivo.toFile())) {
            grafo.edges()
                 .forEach(e -> writer.println(String.format("%s %s %f %d %.2f %.3f",
                                                            e.vertexA().element(),
                                                            e.vertexB().element(),
                                                            e.weight(),
                                                            e.properties().get(Roteamento.BANDWIDTH),
                                                            e.properties().get(Roteamento.DELAY),
                                                            e.properties().get(Roteamento.LOSS_PROBABILITY))));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Graph<String, String> ler(Path arquivo) {
        var grafo = Graph.<String, String>newGraph();
        try {
            Files.readAllLines(arquivo)
                 .stream()
                 .map(line -> line.split(" "))
                 .filter(line -> {
                     if (line.length == 0) {
                         return false;
                     } else if (line.length != 3 && line.length != 5) {
                         return false;
                     } else {
                         return true;
                     }
                 })
                 .forEach(line -> {
                     if (!grafo.vertices()
                               .stream()
                               .map(v -> v.element())
                               .filter(v -> v.equals(line[0]))
                               .findAny()
                               .isPresent()) {
                         grafo.insertVertex(line[0]);
                     }

                     if (!grafo.vertices()
                               .stream()
                               .map(v -> v.element())
                               .filter(v -> v.equals(line[1]))
                               .findAny()
                               .isPresent()) {
                         grafo.insertVertex(line[1]);
                     }
                     var properties = new HashMap<String, Object>();
                     if (line.length == 5) {
                         properties.put(Roteamento.BANDWIDTH, Integer.parseInt(line[3]));
                         properties.put(Roteamento.BANDWIDTH, Double.parseDouble(line[4]));
                         properties.put(Roteamento.BANDWIDTH, Float.parseFloat(line[5]));
                     }
                     grafo.insertEdge(line[0], line[1], String.format("%s - %s", line[0], line[1]),
                                      Double.parseDouble(line[2]), properties);
                 });
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return grafo;
    }

}
