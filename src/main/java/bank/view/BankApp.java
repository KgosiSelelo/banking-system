package bank.view;

import bank.SampleData;
import bank.controller.*;
import bank.dao.*;
import bank.model.Customer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

/** Entry point: wires the layers together and switches between screens. */
public class BankApp extends Application {

    private final LoginController loginController;
    private final CustomerController customerController;
    private final AccountController accountController;
    private final TransactionController transactionController;
    private final InterestController interestController;

    private Stage stage;

    public BankApp() {
        DatabaseManager.initialise();
        CustomerDAO customerDAO = new JdbcCustomerDAO();
        AccountDAO accountDAO = new JdbcAccountDAO(customerDAO);

        loginController = new LoginController(customerDAO);
        customerController = new CustomerController(customerDAO);
        accountController = new AccountController(accountDAO);
        transactionController = new TransactionController(accountDAO);
        interestController = new InterestController(accountDAO);

        if (SampleData.seedIfEmpty(customerDAO, customerController,
                accountController, transactionController)) {
            System.out.println("Sample data created: 10 customers, password for all: "
                    + SampleData.PASSWORD);
        }
    }

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
        stage.centerOnScreen();
    }

    private void showAccounts(Customer customer) {
        AccountView view = new AccountView(customer, accountController,
                transactionController, interestController, this::showLogin);
        stage.setScene(new Scene(view.getRoot(), 900, 560));
        stage.setMaximized(true);
    }

    public static void main(String[] args) {
        launch(args);
    }
}