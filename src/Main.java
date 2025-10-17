import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.geometry.*;
import javafx.stage.Stage;
import java.sql.SQLException;

public class Main extends Application {
    
    private AuthService authService;

    @Override
    public void start(Stage primaryStage) {
        try {
            authService = new AuthService();
        } catch (SQLException e) {
            showAlert("Database Error", "Cannot connect to database: " + e.getMessage());
            return;
        }
        
        primaryStage.setTitle("Login Page");
        Scene loginScene = createLoginScene(primaryStage);
        Scene createScene = createCreateAccountScene(primaryStage, loginScene);

        primaryStage.setScene(loginScene);
        primaryStage.setWidth(500);
        primaryStage.setHeight(400);
        primaryStage.show();
    }

    @Override
    public void stop() {
        DatabaseConnection.closeConnection();
    }

    private Scene createLoginScene(Stage stage) {
        Label welcomeLabel = new Label("Welcome Back");
        welcomeLabel.setStyle("-fx-font-size: 26px; -fx-font-weight: bold; -fx-text-fill: white");
        Label subtitleLabel = new Label("Please log in to continue");
        subtitleLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: white");
        
        Label usernameLabel = new Label("Username:");
        usernameLabel.setStyle("-fx-text-fill:white");
        Label passwordLabel = new Label("Password:");
        passwordLabel.setStyle("-fx-text-fill:white");
        TextField usernameField = new TextField();
        PasswordField passwordField = new PasswordField();
        
        Button loginButton = new Button("Login");
        Button createAccountButton = new Button("Create New User");
        Label messageLabel = new Label();
        messageLabel.setStyle("-fx-text-fill: white");

        loginButton.setOnAction(event -> {
            String username = usernameField.getText().trim();
            String password = passwordField.getText();
            
            if (username.isEmpty() || password.isEmpty()) {
                messageLabel.setText("Please enter both username and password.");
                return;
            }
            
            try {
                if (authService.login(username, password)) {
                    messageLabel.setText("Login Successful");
                    // Navigate to main application interface here
                } else {
                    messageLabel.setText("Invalid username or password.");
                }
            } catch (SQLException exception) {
                messageLabel.setText("Database error. Please try again.");
                exception.printStackTrace();
            }
        });

        createAccountButton.setOnAction(event -> {
            Scene createScene = createCreateAccountScene(stage, stage.getScene());
            stage.setTitle("Create Account");
            stage.setScene(createScene);
        });
        
        GridPane grid = new GridPane();
        grid.setVgap(10);
        grid.setHgap(10);
        grid.setPadding(new Insets(20));
        grid.setAlignment(Pos.CENTER);
        
        grid.add(welcomeLabel, 0, 0, 2, 1);
        GridPane.setHalignment(welcomeLabel, HPos.CENTER);
        grid.add(subtitleLabel, 0, 1, 2, 1);
        GridPane.setHalignment(subtitleLabel, HPos.CENTER);
        grid.add(usernameLabel, 0, 2);
        grid.add(usernameField, 1, 2);
        grid.add(passwordLabel, 0, 3);
        grid.add(passwordField, 1, 3);
        grid.add(loginButton, 1, 4);
        grid.add(createAccountButton, 1, 5);
        grid.add(messageLabel, 1, 6);
        grid.setStyle("-fx-background-color: #D22B2B;");
        
        return new Scene(grid, 400, 250);
    }

    private Scene createCreateAccountScene(Stage stage, Scene loginScene) {
        Label titleLabel = new Label("Create New Account");
        titleLabel.setStyle("-fx-font-size: 26px; -fx-font-weight: bold; -fx-text-fill:#D22B2B");
        
        TextField usernameField = new TextField();
        PasswordField passwordField = new PasswordField();
        PasswordField confirmPasswordField = new PasswordField();
        TextField firstNameField = new TextField();
        TextField lastNameField = new TextField();
        TextField emailField = new TextField();
        emailField.setPromptText("example@email.com");
        TextField phoneField = new TextField();
        phoneField.setPromptText("xxx-xxx-xxxx");
        
        Button createButton = new Button("Create Account");
        Button backButton = new Button("Back to Login");
        Label messageLabel = new Label();

        createButton.setOnAction(event -> {
            String username = usernameField.getText().trim();
            String password = passwordField.getText();
            String confirmPassword = confirmPasswordField.getText();
            String firstName = firstNameField.getText().trim();
            String lastName = lastNameField.getText().trim();
            String email = emailField.getText().trim();
            String phone = phoneField.getText().trim();
            
            if (username.isEmpty() || password.isEmpty() || firstName.isEmpty() || 
                lastName.isEmpty() || email.isEmpty() || phone.isEmpty()) {
                messageLabel.setText("Please complete all required fields.");
                return;
            }
            
            if (!password.equals(confirmPassword)) {
                messageLabel.setText("Password confirmation does not match.");
                return;
            }
            
            User newUser = new User(username, password, firstName, lastName, email, phone);
            
            try {
                if (authService.register(newUser)) {
                    messageLabel.setText("Account created successfully.");
                    // Clear fields
                    usernameField.clear();
                    passwordField.clear();
                    confirmPasswordField.clear();
                    firstNameField.clear();
                    lastNameField.clear();
                    emailField.clear();
                    phoneField.clear();
                }
            } catch (SQLException exception) {
                messageLabel.setText("Registration error: " + exception.getMessage());
            }
        });

        backButton.setOnAction(event -> {
            stage.setTitle("Login");
            stage.setScene(loginScene);
        });

        GridPane grid = new GridPane();
        grid.setVgap(10);
        grid.setHgap(10);
        grid.setPadding(new Insets(20));
        grid.setAlignment(Pos.CENTER);
        
        grid.add(titleLabel, 0, 0, 2, 1);
        grid.add(new Label("First Name:"), 0, 1);
        grid.add(firstNameField, 1, 1);
        grid.add(new Label("Last Name:"), 0, 2);
        grid.add(lastNameField, 1, 2);
        grid.add(new Label("Username:"), 0, 3);
        grid.add(usernameField, 1, 3);
        grid.add(new Label("Password:"), 0, 4);
        grid.add(passwordField, 1, 4);
        grid.add(new Label("Confirm Password:"), 0, 5);
        grid.add(confirmPasswordField, 1, 5);
        grid.add(new Label("Email:"), 0, 6);
        grid.add(emailField, 1, 6);
        grid.add(new Label("Phone:"), 0, 7);
        grid.add(phoneField, 1, 7);
        grid.add(createButton, 1, 8);
        grid.add(backButton, 1, 9);
        grid.add(messageLabel, 1, 10);

        return new Scene(grid, 500, 500);
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}