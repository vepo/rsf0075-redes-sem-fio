package io.vepo.redes;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import dev.vepo.openjgraph.graph.Graph;

public class LeitorGrafo {

    public Graph<String, String> ler(Path arquivo) {
        var grafo = Graph.<String, String>newGraph();
        try {
            Files.readAllLines(arquivo)
                 .stream()
                 .map(line -> line.split(" "))
                 .filter(line -> {
                     if (line.length == 0) {
                         return false;
                     } else if (line.length != 3) {
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

                     grafo.insertEdge(line[0], line[1], String.format("%s - %s", line[0], line[1]));
                 });
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return grafo;
    }

}
