package application;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.geometry.*;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Login Page");

        // Create both scenes
        Scene loginScene = createLoginScene(primaryStage);
        Scene createScene = createCreateAccountScene(primaryStage, loginScene);

        // Start with login scene
        primaryStage.setScene(loginScene);
        //Setting default height and width of login page, matches with Create New User dimensions.
        primaryStage.setWidth(500);
        primaryStage.setHeight(400);
        primaryStage.show();
    }

    private Scene createLoginScene(Stage stage) { //Everything that is in Login Page
    	//Welcome + subtitle text
        Label welcome = new Label("Welcome Back");
        welcome.setStyle("-fx-font-size: 26px; -fx-font-weight: bold; -fx-text-fill: white");
        Label subtitle = new Label("Please log in to continue");
        subtitle.setStyle("-fx-font-size: 14px; -fx-text-fill: white");
        
        //Username + Password entry
        Label userLabel = new Label("Username:");
        userLabel.setStyle("-fx-text-fill:white");
        Label passLabel = new Label("Password:");
        passLabel.setStyle("-fx-text-fill:white");
        TextField usernameField = new TextField();
        PasswordField passwordField = new PasswordField();
        
        //Buttons 
        Button loginButton = new Button("Login");
        Button createAccountButton = new Button("Create New User");
        Label messageLabel = new Label();

        //Basic login check (doesn't save users, going to work with MySQL to implement this)
        loginButton.setOnAction(e -> { //when this login button is pressed, do these actions
            String user = usernameField.getText();
            String pass = passwordField.getText();
            if (user.equals("admin") && pass.equals("1234")) {
                messageLabel.setText("✅ Login Successful!");
            } else {
                messageLabel.setText("❌ Invalid username or password.");
            }
        });

        //Go to "Create Account" scene
        createAccountButton.setOnAction(e -> {
            Scene createScene = createCreateAccountScene(stage, stage.getScene());
            stage.setTitle("Create Account Page");
            stage.setScene(createScene);
        });
        
        //Grid alignment for all of the components of login page
        GridPane grid = new GridPane();
        grid.setVgap(10);
        grid.setHgap(10);
        grid.setPadding(new Insets(20));
        grid.setAlignment(Pos.CENTER);
        grid.add(welcome, 0, 0, 2, 1);
        GridPane.setHalignment(welcome, HPos.CENTER);

        grid.add(subtitle, 0, 1, 2, 1);
        GridPane.setHalignment(subtitle, HPos.CENTER);
        grid.add(userLabel, 0, 2);
        grid.add(usernameField, 1, 2);
        grid.add(passLabel, 0, 3);
        grid.add(passwordField, 1, 3);
        grid.add(loginButton, 1, 4);
        grid.add(createAccountButton, 1, 5);
        grid.add(messageLabel, 1, 6);
        grid.setStyle("-fx-background-color: #D22B2B;");
        return new Scene(grid, 400, 250);
    }

    private Scene createCreateAccountScene(Stage stage, Scene loginScene) {//Everything on Create Account Scene
        Label infoLabel = new Label("Create a new account");
        infoLabel.setStyle("-fx-font-size: 26px; -fx-font-weight: bold; -fx-text-fill:D22B2B");
        TextField newUserField = new TextField();
        PasswordField newPassField = new PasswordField();
        TextField newEmailField = new TextField();
        newEmailField.setPromptText("forexample@email.com");
        TextField newPhoneNumber = new TextField();
        newPhoneNumber.setPromptText("xxx-xxx-xxxx");
        Button createButton = new Button("Create Account");
        Button backButton = new Button("Back to Login");
        Label messageLabel = new Label();

        createButton.setOnAction(e -> {//Clicking "Create Account" button
            if (newUserField.getText().isEmpty() || newPassField.getText().isEmpty()) { //Check if anything is empty
                messageLabel.setText("⚠️ Please fill all fields.");
            } else {
                messageLabel.setText("✅ Account created."); //If successful, create the account (not real yet, need to implement in MySQL).
            }
        });

        backButton.setOnAction(e -> {//Back button goes back to Login page (keeping for now until backend logic implemented).
            stage.setTitle("Login Page");
            stage.setScene(loginScene);
        }); 

        GridPane grid = new GridPane(); //New Window to display everything
        grid.setVgap(10);
        grid.setHgap(10);
        grid.setPadding(new Insets(20));
        grid.setAlignment(Pos.CENTER);
        grid.add(infoLabel, 0, 0, 2, 1); //"Create a new account"
        
        //Create username + passsword, add phone # + email for account creation
        grid.add(new Label("New Username:"), 0, 1);
        grid.add(newUserField, 1, 1);
        grid.add(new Label("New Password:"), 0, 2);
        grid.add(newPassField, 1, 2);
        grid.add(new Label("Phone Number:"), 0, 3);
        grid.add(newPhoneNumber, 1, 3);
        grid.add(new Label("Email:"), 0, 4);
        grid.add(newEmailField, 1, 4);
        
        //Buttons
        grid.add(createButton, 1, 5);
        grid.add(backButton, 1, 6);
        grid.add(messageLabel, 1, 7);

        return new Scene(grid, 500, 400);
    }

    public static void main(String[] args) {
        launch(args);
    }
}

