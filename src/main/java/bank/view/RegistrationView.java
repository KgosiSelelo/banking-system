package bank.view;

import bank.controller.CustomerController;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

/** Boundary class: a pop-up form for registering a new customer. */
public class RegistrationView {

    /** Shows the form; returns a success message, or null if cancelled. */
    public static String show(CustomerController controller) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Register customer");

        TextField first = new TextField();
        TextField surname = new TextField();
        TextField address = new TextField();
        TextField username = new TextField();
        PasswordField password = new PasswordField();
        Label error = new Label();
        error.setStyle("-fx-text-fill: #b00020;");
        String[] result = new String[1];

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.addRow(0, new Label("First name"), first);
        grid.addRow(1, new Label("Surname"), surname);
        grid.addRow(2, new Label("Address"), address);
        grid.addRow(3, new Label("Username"), username);
        grid.addRow(4, new Label("Password"), password);

        Button save = new Button("Register");
        save.setOnAction(e -> {
            try {
                controller.register(first.getText(), surname.getText(), address.getText(),
                        username.getText(), password.getText());
                result[0] = "Registered. You can now log in.";
                dialog.close();
            } catch (IllegalArgumentException ex) {
                error.setText(ex.getMessage());
            }
        });
        Button cancel = new Button("Cancel");
        cancel.setOnAction(e -> dialog.close());

        VBox box = new VBox(12, grid, error, new HBox(10, save, cancel));
        box.setPadding(new Insets(20));
        dialog.setScene(new Scene(box));
        dialog.showAndWait();
        return result[0];
    }
}