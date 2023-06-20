module redes {
    requires javafx.controls;
    requires dev.vepo.openjgraph;
    opens io.vepo.redes to javafx.graphics;
}