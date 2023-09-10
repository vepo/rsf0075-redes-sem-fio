package io.vepo.redes;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Comparator;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import dev.vepo.openjgraph.graph.Graph;
import io.vepo.redes.Roteamento.Node;
import io.vepo.redes.protocol.Message;

public class Historico {

    private final AtomicInteger index;

    public Historico() {
        index = new AtomicInteger(0);
    }

    public void salva(Graph<String, String> graph) {
        criaDiretorio();

        while (Paths.get(".", "grafos", String.format("grafo-%03d.txt", index.incrementAndGet())).toFile().exists()) {
        }

        Grafos.escrever(Paths.get(".", "grafos", String.format("grafo-%03d.txt", index.get())), graph);

    }

    private void criaDiretorio() {
        var dir = Paths.get(".", "grafos").toFile();
        if (!dir.exists()) {
            dir.mkdir();
        }
    }

    public void salva(String source, String destiny, Message message) {
        try {
            Files.write(Paths.get(".", "grafos", String.format("mensagens-%03d.txt", index.get())),
                        String.format("%s -> %s: %s\n", source, destiny, message).getBytes(),
                        StandardOpenOption.APPEND,
                        StandardOpenOption.CREATE);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void salva(Roteamento rotas) {
        File arquivo = Paths.get(".", "grafos", String.format("roteamento-%03d.txt", index.get())).toFile();
        if (arquivo.exists()) {
            arquivo.delete();
        }
        try (PrintWriter writer = new PrintWriter(arquivo)) {
            writer.println("source\tdestiny\tdistance\tbandwidth\tdelay\tloss-probability\tpath");
            rotas.nodes()
                 .stream()
                 .sorted(Comparator.comparing(Node::id))
                 .forEachOrdered(origin -> origin.accessibleNodes()
                                                 .stream()
                                                 .sorted()
                                                 .forEachOrdered(destiny -> writer.println(String.format("%s\t%s\t%.2f\t%d\t%.2f\t%.2f\t%s",
                                                                                                         origin.id(),
                                                                                                         destiny,
                                                                                                         origin.distance(destiny),
                                                                                                         origin.bandwidth(destiny),
                                                                                                         origin.delay(destiny),
                                                                                                         origin.lossProbability(destiny),
                                                                                                         String.format("[%s]",
                                                                                                                       origin.path(destiny)
                                                                                                                             .stream()
                                                                                                                             .collect(Collectors.joining(", ")))))));

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
