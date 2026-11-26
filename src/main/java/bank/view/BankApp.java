package bank.view;

import bank.controller.*;
import bank.dao.*;
import bank.model.Customer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

/** Entry point: wires the layers together and switches between screens. */
public class BankApp extends Application {

    private final CustomerDAO customerDAO = new InMemoryCustomerDAO();
    private final AccountDAO accountDAO = new InMemoryAccountDAO();

    private final LoginController loginController = new LoginController(customerDAO);
    private final CustomerController customerController = new CustomerController(customerDAO);
    private final AccountController accountController = new AccountController(accountDAO);
    private final TransactionController transactionController = new TransactionController(accountDAO);
    private final InterestController interestController = new InterestController(accountDAO);

    private Stage stage;

    @Override
    public void start(Stage primaryStage) {
        this.stage = primaryStage;
        stage.setTitle("CSE202 Banking System");
        showLogin();
        stage.show();
    }

    private void showLogin() {
        LoginView view = new LoginView(loginController, customerController, this::showAccounts);
        stage.setScene(new Scene(view.getRoot(), 420, 380));
    }

    private void showAccounts(Customer customer) {
        AccountView view = new AccountView(customer, accountController,
                transactionController, interestController, this::showLogin);
        stage.setScene(new Scene(view.getRoot(), 960, 600));
    }

    public static void main(String[] args) {
        launch(args);
    }
}