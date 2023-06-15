package io.vepo.redes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.nio.file.Paths;

import org.junit.jupiter.api.Test;

public class LeitorGrafoTest {

    /**
     * 
     */
    @Test
    void simpleTest() {
        var leitor = new LeitorGrafo();
        var g = leitor.ler(Paths.get(".","resources", "rede.txt"));
        assertNotNull(g, "Deve retornar um grafo");
        assertEquals(4, g.numVertices(), "Arquivo tem 4 nós");
        assertEquals(5, g.numEdges(), "Arquivo tem 5 vértices");
    }
}
