package io.vepo.redes;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import dev.vepo.openjgraph.graph.Graph;

public class RoteamentoTest {
    @Test
    void roteamentoTest() {
        var rede = Graph.<String, String>newGraph();
        var va = rede.insertVertex("A");
        var vb = rede.insertVertex("B");
        var vc = rede.insertVertex("C");
        var vd = rede.insertVertex("D");
        var ve = rede.insertVertex("E");
        rede.insertEdge(va, vb, "A-B", 1);
        rede.insertEdge(va, vc, "A-C", 1);
        rede.insertEdge(vb, ve, "B-E", 1);
        rede.insertEdge(vc, vd, "C-D", 1);
        rede.insertEdge(vd, ve, "D-E", 1);
        var roteamento = Roteamento.executa(rede, RoteamentoObserver.NOPObserver);
        var nodeA = roteamento.node("A");
        assertThat(nodeA).as("Validando no A")
                         .isNotEmpty()
                         .hasValueSatisfying(a -> {
                             assertThat(a.neighborhood()).as("Validando vizinhos de A")
                                                         .hasSize(2)
                                                         .containsExactlyInAnyOrder("B", "C");
                             assertThat(a.accessibleNodes()).as("Validando nós acessíveis de A")
                                                            .hasSize(4)
                                                            .containsExactlyInAnyOrder("B", "C", "E", "D");
                             assertThat(a.route("D")).as("Validando caminho")
                                                     .containsExactly("A", "C", "D");
                             assertThat(a.route("B")).as("Validando caminho")
                                                     .containsExactly("A", "B");
                             assertThat(a.route("E")).as("Validando caminho")
                                                     .containsExactly("A", "B", "E");
                             assertThat(a.route("C")).as("Validando caminho")
                                                     .containsExactly("A", "C");
                         });

    }

    @Test
    void roteamentoComPesoTest() {
        var rede = Graph.<String, String>newGraph();
        var va = rede.insertVertex("A");
        var vb = rede.insertVertex("B");
        var vc = rede.insertVertex("C");
        var vd = rede.insertVertex("D");
        var ve = rede.insertVertex("E");
        rede.insertEdge(va, vb, "A-B", 1);
        rede.insertEdge(va, vc, "A-C", 1);
        rede.insertEdge(vb, ve, "B-E", 1);
        rede.insertEdge(vc, vd, "C-D", 100);
        rede.insertEdge(vd, ve, "D-E", 1);
        var roteamento = Roteamento.executa(rede, RoteamentoObserver.NOPObserver);
        var nodeA = roteamento.node("A");
        assertThat(nodeA).as("Validando no A")
                         .isNotEmpty()
                         .hasValueSatisfying(a -> {
                             assertThat(a.neighborhood()).as("Validando vizinhos de A")
                                                         .hasSize(2)
                                                         .containsExactlyInAnyOrder("B", "C");
                             assertThat(a.accessibleNodes()).as("Validando nós acessíveis de A")
                                                            .hasSize(4)
                                                            .containsExactlyInAnyOrder("B", "C", "E", "D");
                             assertThat(a.route("D")).as("Validando caminho")
                                                     .containsExactly("A", "B", "E", "D");
                             assertThat(a.route("B")).as("Validando caminho")
                                                     .containsExactly("A", "B");
                             assertThat(a.route("E")).as("Validando caminho")
                                                     .containsExactly("A", "B", "E");
                             assertThat(a.route("C")).as("Validando caminho")
                                                     .containsExactly("A", "C");
                         });

    }
}
