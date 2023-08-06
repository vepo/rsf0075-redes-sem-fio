package io.vepo.redes;

import java.util.List;

import io.vepo.redes.protocol.Message;

public interface RoteamentoObserver {

    static RoteamentoObserver NOPObserver = new RoteamentoObserver() {

        @Override
        public void nodeFound(String srcNode, String dstNode) {

        }

        @Override
        public void routeFound(String srcNode, String dstNode, double distance, List<String> path) {

        }

        @Override
        public void message(String source, String destiny, Message msg) {
        }
    };

    void message(String source, String destiny, Message msg);

    void nodeFound(String srcNode, String dstNode);

    void routeFound(String srcNode, String dstNode, double distance, List<String> path);
}
