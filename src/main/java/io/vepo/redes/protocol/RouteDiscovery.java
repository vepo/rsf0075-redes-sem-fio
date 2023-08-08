package io.vepo.redes.protocol;

import java.util.List;

public record RouteDiscovery(String source,  double distance, List<String> path) implements Message {
}
