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
import io.vepo.redes.protocol.NodeDiscovery;
import io.vepo.redes.protocol.RouteDiscovery;

public class Roteamento {

    public static class Node {

        private final String id;
        private final Set<Pair<Node, Double>> neighborhood;
        private final Set<String> accessibleNodes;
        private final Map<String, List<String>> routingTable;
        private final Map<String, Double> routingDistance;
        private final RoteamentoObserver observer;

        public Node(String id, RoteamentoObserver observer) {
            this.id = id;
            this.neighborhood = new HashSet<>();
            this.accessibleNodes = new HashSet<>();
            this.routingTable = new HashMap<>();
            this.routingDistance = new HashMap<>();
            this.observer = observer;
        }

        public void addNeighbor(Node neighbor, double weight) {
            neighborhood.add(Tuples.pair(neighbor, weight));

        }

        public void startDiscovery() {
            accessibleNodes.clear();
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
            return new HashSet<>(accessibleNodes);
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
            neighborhood.forEach(n -> n.getOne().accept(id, new NodeDiscovery(id)));
        }

        private void accept(String source, Message message) {
            observer.message(source, id, message);
            if (message instanceof NodeDiscovery nd) {
                if (!id.equals(nd.sourceId()) && !accessibleNodes.contains(nd.sourceId())) {
                    observer.nodeFound(nd.sourceId(), id);
                    accessibleNodes.add(nd.sourceId());
                    neighborhood.forEach(n -> n.getOne().accept(id, nd));
                    accessibleNodes.stream()
                                   .forEach(dst -> neighborhood.forEach(n -> n.getOne().accept(id, new RouteDiscovery(id, dst, n.getTwo(), Arrays.asList(id, n.getOne().id)))));
                }
            } else if (message instanceof RouteDiscovery rd) {
                if (id.equals(rd.destiny()) && (!routingTable.containsKey(rd.source()) || routingDistance.get(rd.source()) > rd.distance())) {
                    var fullPath = new ArrayList<>(rd.path());
                    Collections.reverse(fullPath);
                    routingTable.put(rd.source(), fullPath);
                    routingDistance.put(rd.source(), rd.distance());
                    observer.routeFound(rd.destiny(), rd.source(), rd.distance(), fullPath);
                } else {
                    neighborhood.stream()
                                .filter(n -> !rd.path().contains(n.getOne().id))
                                .forEach(n -> {
                                    var fullPath = new ArrayList<String>(rd.path().size() + 1);
                                    fullPath.addAll(rd.path());
                                    fullPath.add(n.getOne().id);
                                    n.getOne()
                                     .accept(id, new RouteDiscovery(rd.source(), rd.destiny(), rd.distance() + n.getTwo(), fullPath));
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

    public Optional<Node> getNode(String nodeId) {
        return Optional.ofNullable(nodes.get(nodeId));
    }
}
