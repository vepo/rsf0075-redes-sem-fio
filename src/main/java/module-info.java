module redes {
    requires javafx.controls;
    requires javafx.web;
    requires dev.vepo.openjgraph;
    requires jdk.jsobject;
    requires java.logging;
    requires org.eclipse.collections.api;
    requires org.eclipse.collections.impl;

    opens io.vepo.redes to javafx.graphics, javafx.web;
}