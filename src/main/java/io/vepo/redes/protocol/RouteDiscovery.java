package io.vepo.redes.protocol;

import java.util.List;

public record RouteDiscovery(String source,
                             double distance,
                             double delay,
                             float lossProbability,
                             int bandwidth,
                             List<String> path)
                implements Message {
}
