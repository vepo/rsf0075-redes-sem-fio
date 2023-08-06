package io.vepo.redes;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.layout.GridPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.converter.IntegerStringConverter;
import javafx.util.converter.DoubleStringConverter;
import org.eclipse.collections.api.tuple.Pair;
import org.eclipse.collections.impl.tuple.Tuples;

public class RandomWindow {

    static String password, username;

    public static Pair<Integer, Double> display(Stage owner) {
        var stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);

        var txtNumNos = new TextField();
        // Definir o TextFormatter para aceitar apenas valores inteiros
        var integerTextFormatter = new TextFormatter<Integer>(
                new IntegerStringConverter(),
                0,  // Valor padrão
                c -> {
                    if (c.getControlNewText().matches("\\d*")) {
                        return c; // Permite apenas dígitos inteiros
                    } else {
                        return null; // Bloqueia outros caracteres
                    }
                }
        );
        txtNumNos.setTextFormatter(integerTextFormatter);
        var txtPropAresta = new TextField();
        // Definir o TextFormatter para aceitar apenas valores decimais entre 0 e 1
        var decimalTextFormatter = new TextFormatter<Double>(
                new DoubleStringConverter(),
                0.0, // Valor padrão
                c -> {
                    String newText = c.getControlNewText();
                    if (newText.matches("^(0(\\.\\d*)?|1(\\.0*)?)?$")) {
                        return c; // Valida o formato do número entre 0 e 1
                    } else {
                        return null; // Bloqueia outros caracteres
                    }
                }
        );
        txtPropAresta.setTextFormatter(decimalTextFormatter);

        var btnCriar = new Button("Criar");
        btnCriar.setOnAction(e -> {
            username = txtNumNos.getText();
            password = txtPropAresta.getText();
            stage.close();
        });

        var label1 = new Label("  Criar grafo aleatório  ");
        var label2 = new Label("Número de nós:           ");
        var label3 = new Label("Probabilidade de conexão:");

        var layout = new GridPane();

        layout.setPadding(new Insets(10, 10, 10, 10));
        layout.setVgap(5);
        layout.setHgap(5);

        layout.add(txtNumNos, 1, 1);
        layout.add(txtPropAresta, 1, 2);
        layout.add(btnCriar, 1, 3);
        layout.add(label1, 1, 0);
        layout.add(label2, 0, 1);
        layout.add(label3, 0, 2);

        Scene scene = new Scene(layout, 250, 150);
        stage.setTitle("Dialog");
        stage.initOwner(owner);
        stage.setScene(scene);
        stage.showAndWait();

        return Tuples.pair(Integer.parseInt(txtNumNos.getText()),
                           Double.parseDouble(txtPropAresta.getText()));
    }
}
