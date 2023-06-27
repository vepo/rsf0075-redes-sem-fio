module redes {
    requires javafx.controls;
    requires dev.vepo.openjgraph;
    requires java.logging;
    
    opens io.vepo.redes to javafx.graphics;
}