package bank.view;

import bank.controller.CustomerController;
import bank.controller.LoginController;
import bank.model.Customer;
import java.util.function.Consumer;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

/** Boundary class: shows the login form and passes input to the controller. */
public class LoginView {

    private final VBox root = new VBox(12);
    private final TextField usernameField = new TextField();
    private final PasswordField passwordField = new PasswordField();
    private final Label messageLabel = new Label();

    public LoginView(LoginController loginController,
                     CustomerController customerController,
                     Consumer<Customer> onLoginSuccess) {

        Label title = new Label("Bank Login");
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold;");

        usernameField.setPromptText("Username");
        passwordField.setPromptText("Password");
        messageLabel.setStyle("-fx-text-fill: #b00020;");

        Button loginButton = new Button("Log in");
        loginButton.setDefaultButton(true);
        loginButton.setMaxWidth(Double.MAX_VALUE);
        loginButton.setOnAction(e -> {
            try {
                Customer c = loginController.login(usernameField.getText(), passwordField.getText());
                onLoginSuccess.accept(c);
            } catch (IllegalArgumentException ex) {
                messageLabel.setText(ex.getMessage());
            }
        });

        Hyperlink registerLink = new Hyperlink("New customer? Register");
        registerLink.setOnAction(e -> {
            String result = RegistrationView.show(customerController);
            if (result != null) {
                messageLabel.setStyle("-fx-text-fill: #1b6e2d;");
                messageLabel.setText(result);
            }
        });

        root.setPadding(new Insets(30));
        root.setAlignment(Pos.CENTER);
        root.getChildren().addAll(title, usernameField, passwordField,
                loginButton, messageLabel, registerLink);
    }

    public Parent getRoot() { return root; }
}