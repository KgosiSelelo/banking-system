package bank.view;

import bank.controller.AccountController;
import bank.controller.InterestController;
import bank.controller.TransactionController;
import bank.model.Account;
import bank.model.Customer;
import java.util.List;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;

/**
 * Boundary class: shows the customer's accounts and collects input.
 * It contains NO business rules; every action goes through a controller.
 */
public class AccountView {

    private final Customer customer;
    private final AccountController accountController;
    private final TransactionController transactionController;
    private final InterestController interestController;

    private final BorderPane root = new BorderPane();
    private final ListView<Account> accountList = new ListView<>();
    private final Label balanceLabel = new Label("Select an account");
    private final TextField amountField = new TextField();
    private final Label messageLabel = new Label();
    private final ListView<String> historyList = new ListView<>();

    private final ChoiceBox<String> typeChoice = new ChoiceBox<>();
    private final TextField extraOne = new TextField();
    private final TextField extraTwo = new TextField();

    public AccountView(Customer customer, AccountController accountController,
                       TransactionController transactionController,
                       InterestController interestController, Runnable onLogout) {
        this.customer = customer;
        this.accountController = accountController;
        this.transactionController = transactionController;
        this.interestController = interestController;

        // Top bar
        Label welcome = new Label("Welcome, " + customer.getFirstName());
        welcome.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        Button interestButton = new Button("Pay monthly interest (demo)");
        interestButton.setOnAction(e -> {
            int n = interestController.payMonthlyInterest();
            showMessage("Interest paid to " + n + " account(s)", false);
            refresh();
        });
        Button logout = new Button("Log out");
        logout.setOnAction(e -> onLogout.run());
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox top = new HBox(10, welcome, spacer, interestButton, logout);
        top.setPadding(new Insets(12));
        root.setTop(top);

        // Left: account list
        VBox left = new VBox(8, new Label("My accounts"), accountList);
        left.setPadding(new Insets(12));
        left.setPrefWidth(280);
        VBox.setVgrow(accountList, Priority.ALWAYS);
        root.setLeft(left);
        accountList.getSelectionModel().selectedItemProperty()
                .addListener((obs, old, sel) -> showSelected());

        // Centre: transactions
        balanceLabel.setStyle("-fx-font-size: 16px;");
        amountField.setPromptText("Amount, e.g. 250.00");
        Button deposit = new Button("Deposit");
        deposit.setOnAction(e -> doDeposit());
        Button withdraw = new Button("Withdraw");
        withdraw.setOnAction(e -> doWithdraw());
        VBox centre = new VBox(10, balanceLabel, new HBox(10, amountField, deposit, withdraw),
                messageLabel, new Label("Transaction history"), historyList);
        centre.setPadding(new Insets(12));
        VBox.setVgrow(historyList, Priority.ALWAYS);
        root.setCenter(centre);

        // Right: open a new account
        typeChoice.getItems().addAll("Savings", "Investment", "Cheque");
        typeChoice.setValue("Savings");
        extraOne.setDisable(true);
        extraTwo.setDisable(true);
        typeChoice.setOnAction(e -> updateOpenForm());
        Button open = new Button("Open account");
        open.setOnAction(e -> doOpenAccount());
        VBox right = new VBox(8, new Label("Open a new account"), typeChoice,
                extraOne, extraTwo, open);
        right.setPadding(new Insets(12));
        right.setPrefWidth(240);
        root.setRight(right);

        refresh();
    }

    // Actions: collect input, call controller, show result

    private void doDeposit() {
        Account sel = accountList.getSelectionModel().getSelectedItem();
        if (sel == null) { showMessage("Select an account first", true); return; }
        try {
            transactionController.deposit(customer, sel.getAccountNumber(), amountField.getText());
            amountField.clear();
            showMessage("Deposit successful", false);
            refresh();
        } catch (RuntimeException ex) {
            showMessage(ex.getMessage(), true);
        }
    }

    private void doWithdraw() {
        Account sel = accountList.getSelectionModel().getSelectedItem();
        if (sel == null) { showMessage("Select an account first", true); return; }
        try {
            transactionController.withdraw(customer, sel.getAccountNumber(), amountField.getText());
            amountField.clear();
            showMessage("Withdrawal successful", false);
            refresh();
        } catch (RuntimeException ex) {
            showMessage(ex.getMessage(), true);
        }
    }

    private void doOpenAccount() {
        try {
            String type = typeChoice.getValue();
            String branch = "Main Branch";
            switch (type) {
                case "Savings" -> accountController.openSavings(customer, branch);
                case "Investment" -> accountController.openInvestment(customer, branch, extraOne.getText());
                default -> accountController.openCheque(customer, branch,
                        extraOne.getText(), extraTwo.getText());
            }
            extraOne.clear();
            extraTwo.clear();
            showMessage(type + " account opened", false);
            refresh();
        } catch (RuntimeException ex) {
            showMessage(ex.getMessage(), true);
        }
    }

    // Display helpers

    private void updateOpenForm() {
        String type = typeChoice.getValue();
        extraOne.setDisable("Savings".equals(type));
        extraTwo.setDisable(!"Cheque".equals(type));
        switch (type) {
            case "Investment" -> { extraOne.setPromptText("Opening deposit (min 500)"); extraTwo.setPromptText(""); }
            case "Cheque" -> { extraOne.setPromptText("Employer name"); extraTwo.setPromptText("Employer address"); }
            default -> { extraOne.setPromptText(""); extraTwo.setPromptText(""); }
        }
    }

    private void refresh() {
        String selected = accountList.getSelectionModel().getSelectedItem() == null
                ? null : accountList.getSelectionModel().getSelectedItem().getAccountNumber();
        List<Account> accounts = accountController.getAccounts(customer);
        accountList.getItems().setAll(accounts);
        if (selected != null) {
            accounts.stream().filter(a -> a.getAccountNumber().equals(selected))
                    .findFirst().ifPresent(a -> accountList.getSelectionModel().select(a));
        }
        accountList.refresh();
        showSelected();
    }

    private void showSelected() {
        Account sel = accountList.getSelectionModel().getSelectedItem();
        historyList.getItems().clear();
        if (sel == null) {
            balanceLabel.setText("Select an account");
            return;
        }
        balanceLabel.setText(sel.getAccountType() + " " + sel.getAccountNumber()
                + "  |  Balance: BWP " + sel.getBalance());
        transactionController.history(customer, sel.getAccountNumber())
                .forEach(t -> historyList.getItems().add(t.toString()));
    }

    private void showMessage(String text, boolean error) {
        messageLabel.setStyle(error ? "-fx-text-fill: #b00020;" : "-fx-text-fill: #1b6e2d;");
        messageLabel.setText(text);
    }

    public Parent getRoot() { return root; }
}