package io.vepo.redes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import org.eclipse.collections.api.tuple.Pair;
import org.eclipse.collections.impl.tuple.Tuples;

import dev.vepo.openjgraph.graph.Graph;
import io.vepo.redes.protocol.Message;
import io.vepo.redes.protocol.RouteDiscovery;

public class Roteamento {

    public static class Node {

        private final String id;
        private final Set<Pair<Node, Double>> neighborhood;
        private final Map<String, List<String>> routingTable;
        private final Map<String, Double> routingDistance;
        private final RoteamentoObserver observer;

        public Node(String id, RoteamentoObserver observer) {
            this.id = id;
            this.neighborhood = new HashSet<>();
            this.routingTable = new HashMap<>();
            this.routingDistance = new HashMap<>();
            this.observer = observer;
        }

        public void addNeighbor(Node neighbor, double weight) {
            neighborhood.add(Tuples.pair(neighbor, weight));

        }

        public String id() {
            return id;
        }

        public double distance(String destiny) {
            return this.routingDistance.getOrDefault(destiny, Double.MAX_VALUE);
        }

        public List<String> path(String destiny) {
            return this.routingTable.getOrDefault(destiny, Collections.emptyList());
        }

        public void cleanup() {
        }

        public void startDiscovery() {
            sendNodeDiscovery();
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(id);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            } else if (obj == null || getClass() != obj.getClass()) {
                return false;
            } else {
                Node other = (Node) obj;
                return Objects.equals(id, other.id);
            }
        }

        public Set<String> neighborhood() {
            return new HashSet<>(neighborhood.stream()
                                             .map(n -> n.getOne().id)
                                             .toList());
        }

        public Set<String> accessibleNodes() {
            return new HashSet<>(routingDistance.keySet());
        }

        public List<String> route(String node) {
            if (this.routingTable.containsKey(node)) {
                return new ArrayList<>(this.routingTable.get(node));
            } else {
                throw new IllegalStateException("Node does not exists!");
            }
        }

        @Override
        public String toString() {
            return String.format("Node [id=%s]", id);
        }

        private void sendNodeDiscovery() {
            neighborhood.forEach(n -> n.getOne()
                                       .accept(id,
                                               new RouteDiscovery(id, n.getTwo(), Arrays.asList(id, n.getOne().id))));
        }

        private void accept(String source, Message message) {
            observer.message(source, id, message);
            if (message instanceof RouteDiscovery rd) {

                if (!routingDistance.containsKey(rd.source()) || routingDistance.get(rd.source()) > rd.distance()) {
                    routingDistance.put(rd.source(), rd.distance());
                    var path = new ArrayList<>(rd.path());
                    Collections.reverse(path);
                    routingTable.put(rd.source(), path);
                    observer.routeFound(id, rd.source(), rd.distance(), path);

                    neighborhood.stream()
                                .filter(n -> !rd.path().contains(n.getOne().id))
                                .forEach(n -> {
                                    var newPath = new ArrayList<String>(rd.path().size() + 1);
                                    newPath.addAll(rd.path());
                                    newPath.add(n.getOne().id);
                                    n.getOne().accept(id, new RouteDiscovery(rd.source(),
                                                                             rd.distance() + n.getTwo(),
                                                                             newPath));
                                });
                }
            }
        }
    }

    public static Roteamento executa(Graph<String, String> graph, RoteamentoObserver observer) {
        var nodes = new HashMap<String, Node>();
        graph.vertices()
             .forEach(v -> {
                 var node = nodes.computeIfAbsent(v.element(), id -> new Node(id, observer));
                 graph.incidentEdges(v)
                      .stream()
                      .map(e -> Tuples.pair(graph.opposite(v, e), e.weight()))
                      .forEach(e -> node.addNeighbor(nodes.computeIfAbsent(e.getOne().element(),
                                                                           id -> new Node(id, observer)),
                                                     e.getTwo()));
             });
        nodes.values()
             .forEach(Node::startDiscovery);
        return new Roteamento(nodes);
    }

    private final Map<String, Node> nodes;

    public Roteamento(HashMap<String, Node> nodes) {
        this.nodes = nodes;
    }

    public Optional<Node> node(String nodeId) {
        return Optional.ofNullable(nodes.get(nodeId));
    }

    public List<Node> nodes() {
        return new ArrayList<>(nodes.values());
    }
}
