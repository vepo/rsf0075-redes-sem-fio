module redes {
    requires javafx.controls;
    requires smartgraph;
    //requires javafx.fxml;

    //opens org.openjfx to javafx.fxml;

    //exports org.openjfx;
    opens io.vepo.redes to javafx.graphics;
}