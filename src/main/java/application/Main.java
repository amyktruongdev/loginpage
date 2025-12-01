package application;

import javafx.application.Application;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.sql.Timestamp;

public class Main extends Application {

    // service to handle login and registration
    private AuthService authService;
    // holds the username of whoever logged in
    private String currentUser;

    @Override
    public void start(Stage primaryStage) {
        try {
            // set up the auth service (uses database)
            authService = new AuthService();
        } catch (SQLException e) {
            // show error if we cannot connect
            showAlert("Database Error", "Cannot connect to database: " + e.getMessage());
            return;
        }

        // start on the login screen
        primaryStage.setTitle("Login Page");
        Scene loginScene = createLoginScene(primaryStage);
        primaryStage.setScene(loginScene);
        primaryStage.setWidth(500);
        primaryStage.setHeight(400);
        primaryStage.show();
    }

    @Override
    public void stop() {
        // close database connection when app stops
        DatabaseConnection.closeConnection();
    }

    // --------------------------------------------------------------------
    // LOGIN SCREEN
    // --------------------------------------------------------------------
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

        // when user clicks login
        loginButton.setOnAction(event -> {
            String username = usernameField.getText().trim();
            String password = passwordField.getText();

            // simple empty check
            if (username.isEmpty() || password.isEmpty()) {
                messageLabel.setText("Please enter both username and password.");
                return;
            }

            try {
                // try to log in using AuthService
                boolean ok = authService.login(username, password);
                if (ok) {
                    currentUser = username;
                    messageLabel.setText("Login Successful");
                    // go to blog insertion page after login
                    Scene blogScene = createBlogInsertionScene(stage, username);
                    stage.setTitle("Insert Blog - " + username);
                    stage.setScene(blogScene);
                } else {
                    messageLabel.setText("Invalid username or password.");
                }
            } catch (Exception e) {
                e.printStackTrace();
                messageLabel.setText("Database error. Please try again.");
            }
        });

        // go to account creation screen
        createAccountButton.setOnAction(event -> {
            Scene createScene = createCreateAccountScene(stage, stage.getScene());
            stage.setTitle("Create Account");
            stage.setScene(createScene);
        });

        // layout of login form
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

    // --------------------------------------------------------------------
    // SEARCH + ADVANCED QUERIES SCREEN (items 1–7)
    // --------------------------------------------------------------------
    private Scene createSearchScene(Stage stage, String currentUser) {
        VBox root = new VBox(12);
        root.setPadding(new Insets(16));
        root.setStyle("-fx-background-color: #C62828;");

        Label title = new Label("Search & Advanced Queries");
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: white;");

        // --- Single Tag Search (blogs table) ---
        HBox bar = new HBox(8);
        Label tagLbl = new Label("Tag:");
        tagLbl.setStyle("-fx-text-fill: white;");
        TextField tagField = new TextField();
        tagField.setPromptText("e.g., blockchain");
        Button searchBtn = new Button("Search Blogs");
        bar.getChildren().addAll(tagLbl, tagField, searchBtn);
        bar.setAlignment(Pos.CENTER_LEFT);

        // --- Query 1: Two tags same day ---
        HBox tagPairBox = new HBox(8);
        Label ltx = new Label("Tag X:");
        Label lty = new Label("Tag Y:");
        ltx.setStyle("-fx-text-fill: white;");
        lty.setStyle("-fx-text-fill: white;");
        TextField tag1Field = new TextField();
        TextField tag2Field = new TextField();
        tag1Field.setPromptText("Tag X");
        tag2Field.setPromptText("Tag Y");
        Button tagSearchBtn = new Button("Users posting both tags (same day)");
        tagPairBox.getChildren().addAll(ltx, tag1Field, lty, tag2Field, tagSearchBtn);
        tagPairBox.setAlignment(Pos.CENTER_LEFT);

        // --- Query 2: Top bloggers on a date ---
        Button topBloggersBtn = new Button("Most Active Users on 10/10/2025");

        // --- Query 3: Followed by X and Y ---
        HBox followedByBothBox = new HBox(8);
        Label lx = new Label("Follower X:");
        Label ly = new Label("Follower Y:");
        lx.setStyle("-fx-text-fill: white;");
        ly.setStyle("-fx-text-fill: white;");
        TextField userXField = new TextField();
        TextField userYField = new TextField();
        userXField.setPromptText("User X");
        userYField.setPromptText("User Y");
        Button followedByBothBtn = new Button("Users followed by BOTH X and Y");
        followedByBothBox.getChildren().addAll(lx, userXField, ly, userYField, followedByBothBtn);
        followedByBothBox.setAlignment(Pos.CENTER_LEFT);

        // --- Query 4: Users who never posted ---
        Button neverPostedBtn = new Button("Users who never posted a blog");

        // --- Query 5: Blogs of X with all positive comments ---
        HBox positiveBlogsBox = new HBox(8);
        Label lpx = new Label("Username:");
        lpx.setStyle("-fx-text-fill: white;");
        TextField posUserField = new TextField();
        posUserField.setPromptText("User");
        Button positiveBlogsBtn = new Button("Blogs with ONLY positive comments");
        positiveBlogsBox.getChildren().addAll(lpx, posUserField, positiveBlogsBtn);
        positiveBlogsBox.setAlignment(Pos.CENTER_LEFT);

        // --- Query 6: Users who ONLY post negative comments ---
        Button onlyNegativeUsersBtn = new Button("Users who ONLY posted negative comments");

        // --- Query 7: Users whose blogs NEVER received negative comments ---
        Button blogsNoNegativeUsersBtn = new Button("Users whose blogs have NO negative comments");

        // feedback/result message
        Label msg = new Label();
        msg.setStyle("-fx-text-fill: white;");

        // ----------------------------------------------------------------
        // Generic list TableView<String> for user/blog lists (queries 1–7)
        // ----------------------------------------------------------------
        Label listTitle = new Label("Query Results:");
        listTitle.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");

        TableView<String> listTable = new TableView<>();
        listTable.setPrefHeight(150);
        listTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<String, String> colResult = new TableColumn<>("Value");
        colResult.setCellValueFactory(d -> new SimpleStringProperty(d.getValue()));
        listTable.getColumns().add(colResult);

        // ----------------------------------------------------------------
        // Blog table for "Search Blogs"
        // ----------------------------------------------------------------
        Label blogTitle = new Label("Blog Search Results:");
        blogTitle.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");

        TableView<SearchService.Row> blogTable = new TableView<>();
        blogTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<SearchService.Row, Number> cId = new TableColumn<>("ID");
        cId.setCellValueFactory(d -> new SimpleIntegerProperty(d.getValue().blogid));

        TableColumn<SearchService.Row, String> cUser = new TableColumn<>("User");
        cUser.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().username));

        TableColumn<SearchService.Row, String> cSubj = new TableColumn<>("Subject");
        cSubj.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().subject));

        TableColumn<SearchService.Row, String> cDesc = new TableColumn<>("Description");
        cDesc.setCellValueFactory(d -> {
            String s = d.getValue().description == null ? "" : d.getValue().description;
            if (s.length() > 160) s = s.substring(0, 160) + "...";
            return new SimpleStringProperty(s);
        });

        TableColumn<SearchService.Row, String> cTags = new TableColumn<>("Tags");
        cTags.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().tags == null ? "" : d.getValue().tags));

        TableColumn<SearchService.Row, String> cDate = new TableColumn<>("Posted");
        cDate.setCellValueFactory(d -> {
            Timestamp t = d.getValue().postDate;
            String s = (t == null) ? "" : t.toLocalDateTime().toString().replace('T', ' ');
            return new SimpleStringProperty(s);
        });

        blogTable.getColumns().addAll(cId, cUser, cSubj, cDesc, cTags, cDate);

        // double-click a blog row to open comment scene
        blogTable.setRowFactory(tv -> {
            TableRow<SearchService.Row> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (!row.isEmpty())) {
                    SearchService.Row selectedBlog = row.getItem();
                    stage.setScene(createCommentScene(stage, selectedBlog.blogid, currentUser));
                }
            });
            return row;
        });

        // back button
        Button back = new Button("Back");
        back.setOnAction(e -> {
            stage.setScene(createBlogInsertionScene(stage, currentUser));
            stage.setTitle("Insert Blog - " + currentUser);
        });

        SearchService searchService = new SearchService();

        // --- Single-tag blog search (fills blogTable) ---
        searchBtn.setOnAction(e -> {
            String tag = tagField.getText() == null ? "" : tagField.getText().trim();
            if (tag.isEmpty()) {
                msg.setText("Please enter a tag.");
                blogTable.getItems().clear();
                return;
            }
            try {
                var rows = searchService.searchByTag(tag);
                blogTable.getItems().setAll(rows);
                msg.setText(rows.isEmpty()
                        ? ("No blogs found for tag: " + tag)
                        : ("Found " + rows.size() + " blog(s) with tag '" + tag + "'."));
            } catch (Exception ex) {
                ex.printStackTrace();
                msg.setText("Error: " + ex.getMessage());
            }
        });

        // --- Query 1: two tags, same day ---
        tagSearchBtn.setOnAction(e -> {
            String tagX = tag1Field.getText() == null ? "" : tag1Field.getText().trim();
            String tagY = tag2Field.getText() == null ? "" : tag2Field.getText().trim();

            if (tagX.isEmpty() || tagY.isEmpty()) {
                msg.setText("Please enter both tags.");
                listTable.getItems().clear();
                return;
            }

            try {
                BlogService bs = new BlogService();
                java.util.List<String> users = bs.getUsersWithTwoTagsSameDay(tagX, tagY);
                listTable.getItems().setAll(users);
                if (users.isEmpty()) {
                    msg.setText("No users found with both tags on the same day.");
                } else {
                    msg.setText("Users with both '" + tagX + "' and '" + tagY +
                            "' on the same day are listed below.");
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                msg.setText("Error: " + ex.getMessage());
            }
        });

        // --- Query 2: top bloggers on specific date ---
        topBloggersBtn.setOnAction(e -> {
            try {
                BlogService bs = new BlogService();
                String date = "2025-10-10";  // assignment date
                java.util.List<String> topUsers = bs.getTopBloggersOnDate(date);
                listTable.getItems().setAll(topUsers);
                if (topUsers.isEmpty()) {
                    msg.setText("No blogs found for 10/10/2025.");
                } else {
                    msg.setText("Most active user(s) on 10/10/2025: " + String.join(", ", topUsers));
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                msg.setText("Error: " + ex.getMessage());
            }
        });

        // --- Query 3: users followed by both X and Y ---
        followedByBothBtn.setOnAction(e -> {
            String userX = userXField.getText() == null ? "" : userXField.getText().trim();
            String userY = userYField.getText() == null ? "" : userYField.getText().trim();

            if (userX.isEmpty() || userY.isEmpty()) {
                msg.setText("Please enter both User X and User Y.");
                listTable.getItems().clear();
                return;
            }

            try {
                BlogService bs = new BlogService();
                java.util.List<String> users = bs.getUsersFollowedByBoth(userX, userY);
                listTable.getItems().setAll(users);
                if (users.isEmpty()) {
                    msg.setText("No users are followed by both " + userX + " and " + userY + ".");
                } else {
                    msg.setText("Users followed by both " + userX + " and " + userY +
                            " are listed below.");
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                msg.setText("Error: " + ex.getMessage());
            }
        });

        // --- Query 4: users who never posted ---
        neverPostedBtn.setOnAction(e -> {
            try {
                BlogService bs = new BlogService();
                java.util.List<String> users = bs.getUsersWhoNeverPostedBlog();
                listTable.getItems().setAll(users);
                if (users.isEmpty()) {
                    msg.setText("All users have posted at least one blog.");
                } else {
                    msg.setText("Users who never posted a blog are listed below.");
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                msg.setText("Error: " + ex.getMessage());
            }
        });

        // --- Query 5: blogs of X where all comments are positive ---
        positiveBlogsBtn.setOnAction(e -> {
            String userX = posUserField.getText() == null ? "" : posUserField.getText().trim();
            if (userX.isEmpty()) {
                msg.setText("Please enter a username.");
                listTable.getItems().clear();
                return;
            }

            try {
                BlogService bs = new BlogService();
                java.util.List<String> blogs = bs.getBlogsAllPositiveCommentsForUser(userX);
                listTable.getItems().setAll(blogs);
                if (blogs.isEmpty()) {
                    msg.setText("No blogs of " + userX + " have only positive comments.");
                } else {
                    msg.setText("Blogs of " + userX +
                            " with all positive comments are listed below.");
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                msg.setText("Error: " + ex.getMessage());
            }
        });

        // --- Query 6: users with only negative comments ---
        onlyNegativeUsersBtn.setOnAction(e -> {
            try {
                BlogService bs = new BlogService();
                java.util.List<String> users = bs.getUsersWithOnlyNegativeComments();
                listTable.getItems().setAll(users);
                if (users.isEmpty()) {
                    msg.setText("No users found who have only posted negative comments.");
                } else {
                    msg.setText("Users with only negative comments are listed below.");
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                msg.setText("Error: " + ex.getMessage());
            }
        });

        // --- Query 7: users whose blogs never received negative comments ---
        blogsNoNegativeUsersBtn.setOnAction(e -> {
            try {
                BlogService bs = new BlogService();
                java.util.List<String> users = bs.getUsersWhoseBlogsNeverGotNegative();
                listTable.getItems().setAll(users);
                if (users.isEmpty()) {
                    msg.setText("No users match the criteria (blogs without negative comments).");
                } else {
                    msg.setText("Users whose blogs have never received negative comments " +
                            "are listed below.");
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                msg.setText("Error: " + ex.getMessage());
            }
        });

        VBox controls = new VBox(10,
                bar,
                tagPairBox,
                topBloggersBtn,
                new Separator(),
                followedByBothBox,
                neverPostedBtn,
                positiveBlogsBox,
                onlyNegativeUsersBtn,
                blogsNoNegativeUsersBtn
        );

        root.getChildren().addAll(
                title,
                controls,
                msg,
                listTitle,
                listTable,
                blogTitle,
                blogTable,
                back
        );

        return new Scene(root, 900, 600);
    }

    // --------------------------------------------------------------------
    // CREATE ACCOUNT SCREEN
    // --------------------------------------------------------------------
    private Scene createCreateAccountScene(Stage stage, Scene loginScene) {
        Label titleLabel = new Label("Create New Account");
        titleLabel.setStyle("-fx-font-size: 26px; -fx-font-weight: bold; -fx-text-fill:#D22B2B");

        TextField usernameField = new TextField();
        PasswordField passwordField = new PasswordField();
        PasswordField confirmPasswordField = new PasswordField();
        TextField firstNameField = new TextField();
        TextField lastNameField = new TextField();
        TextField emailField = new TextField();
        TextField phoneField = new TextField();

        Button createButton = new Button("Create Account");
        Button backButton = new Button("Back to Login");
        Label messageLabel = new Label();

        // when user clicks create account
        createButton.setOnAction(event -> {
            String username = usernameField.getText().trim();
            String password = passwordField.getText();
            String confirmPassword = confirmPasswordField.getText();
            String firstName = firstNameField.getText().trim();
            String lastName = lastNameField.getText().trim();
            String email = emailField.getText().trim();
            String phone = phoneField.getText().trim();

            // basic validation
            if (username.isEmpty() || password.isEmpty() || firstName.isEmpty() ||
                    lastName.isEmpty() || email.isEmpty() || phone.isEmpty()) {
                messageLabel.setText("Please complete all required fields.");
                return;
            }

            if (!password.equals(confirmPassword)) {
                messageLabel.setText("Password confirmation does not match.");
                return;
            }

            // create User object and send to AuthService
            User newUser = new User(username, password, firstName, lastName, email, phone);

            try {
                if (authService.register(newUser)) {
                    messageLabel.setText("Account created successfully.");
                    usernameField.clear();
                    passwordField.clear();
                    confirmPasswordField.clear();
                    firstNameField.clear();
                    lastNameField.clear();
                    emailField.clear();
                    phoneField.clear();
                } else {
                    messageLabel.setText("Username, email, or phone already exists.");
                }
            } catch (SQLException exception) {
                messageLabel.setText("Registration error: " + exception.getMessage());
            }
        });

        // go back to login screen
        backButton.setOnAction(event -> {
            stage.setTitle("Login");
            stage.setScene(loginScene);
        });

        // layout for create account form
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

    // --------------------------------------------------------------------
    // COMMENT SCREEN
    // --------------------------------------------------------------------
    private Scene createCommentScene(Stage stage, int blogId, String currentUser) {
        CommentService commentService = new CommentService();
        BlogService blogService = new BlogService();

        VBox layout = new VBox(20);
        layout.setStyle("-fx-background-color: linear-gradient(to bottom, #D32F2F, #B71C1C); -fx-padding: 30;");
        layout.setAlignment(Pos.TOP_CENTER);

        Label title = new Label("Leave a Comment");
        title.setStyle("-fx-text-fill: white; -fx-font-size: 24px; -fx-font-weight: bold;");

        // Blog Info Card
        VBox blogInfo = new VBox(8);
        blogInfo.setAlignment(Pos.TOP_LEFT);
        blogInfo.setPadding(new Insets(15));
        blogInfo.setMaxWidth(700);
        blogInfo.setStyle("-fx-background-color: white; -fx-background-radius: 10; "
                + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.25), 8, 0, 0, 2);");

        try {
            Blog blog = blogService.getBlogById(blogId);
            if (blog != null) {
                Label subject = new Label("Subject: " + blog.getSubject());
                subject.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #D22B2B;");

                Label author = new Label("By: " + blog.getUsername());
                author.setStyle("-fx-font-size: 13px; -fx-text-fill: #444;");

                Label desc = new Label(blog.getDescription());
                desc.setWrapText(true);
                desc.setStyle("-fx-font-size: 14px; -fx-text-fill: #222;");

                Label tags = new Label("Tags: " + (blog.getTags() != null ? blog.getTags() : "None"));
                tags.setStyle("-fx-font-size: 13px; -fx-text-fill: #666;");

                Label date = new Label("Posted on: " + blog.getPostDate());
                date.setStyle("-fx-font-size: 12px; -fx-text-fill: #888;");

                blogInfo.getChildren().addAll(subject, author, desc, tags, date);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Comments Table
        Label commentsTitle = new Label("Existing Comments:");
        commentsTitle.setStyle("-fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold;");
        commentsTitle.setPadding(new Insets(10, 0, 0, 0));

        TableView<Comment> commentTable = new TableView<>();
        commentTable.setMaxWidth(700);
        commentTable.setPrefHeight(200);
        commentTable.setStyle("-fx-background-radius: 10;");

        TableColumn<Comment, String> cUser = new TableColumn<>("User");
        cUser.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getUsername()));

        TableColumn<Comment, String> cSentiment = new TableColumn<>("Sentiment");
        cSentiment.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getSentiment()));

        TableColumn<Comment, String> cText = new TableColumn<>("Comment");
        cText.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getCommentText()));

        TableColumn<Comment, String> cDate = new TableColumn<>("Date");
        cDate.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getCommentDate().toString()));

        commentTable.getColumns().addAll(cUser, cSentiment, cText, cDate);

        try {
            var comments = commentService.getCommentsByBlogId(blogId);
            commentTable.getItems().setAll(comments);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Comment form
        VBox commentBox = new VBox(10);
        commentBox.setAlignment(Pos.CENTER_LEFT);
        commentBox.setMaxWidth(700);
        commentBox.setPadding(new Insets(15));
        commentBox.setStyle("-fx-background-color: white; -fx-background-radius: 10; "
                + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.25), 8, 0, 0, 2);");

        Label sentimentLabel = new Label("Sentiment:");
        sentimentLabel.setStyle("-fx-text-fill: #D22B2B; -fx-font-weight: bold;");

        ComboBox<String> sentimentBox = new ComboBox<>();
        sentimentBox.getItems().addAll("Positive", "Negative");
        sentimentBox.setValue("Positive");
        sentimentBox.setStyle("-fx-pref-width: 200;");

        Label commentLabel = new Label("Comment:");
        commentLabel.setStyle("-fx-text-fill: #D22B2B; -fx-font-weight: bold;");

        TextArea commentArea = new TextArea();
        commentArea.setPromptText("Write your comment here...");
        commentArea.setWrapText(true);
        commentArea.setPrefWidth(650);
        commentArea.setPrefHeight(100);

        Label messageLabel = new Label();
        messageLabel.setStyle("-fx-text-fill: #D22B2B; -fx-font-size: 13px; -fx-font-weight: bold;");

        Button submitBtn = new Button("Submit Comment");
        submitBtn.setStyle("""
            -fx-background-color: #D22B2B;
            -fx-text-fill: white;
            -fx-font-weight: bold;
            -fx-background-radius: 6;
            -fx-pref-width: 200;
            -fx-cursor: hand;
            """);

        submitBtn.setOnAction(e -> {
            String sentiment = sentimentBox.getValue();
            String text = commentArea.getText().trim();

            if (text.isEmpty()) {
                messageLabel.setText("Please enter a comment.");
                return;
            }

            try {
                boolean success = commentService.addComment(currentUser, blogId, sentiment, text);
                if (success) {
                    messageLabel.setText("✅ Comment added successfully!");
                    commentArea.clear();
                    var comments = commentService.getCommentsByBlogId(blogId);
                    commentTable.getItems().setAll(comments);
                }
            } catch (SQLException ex) {
                messageLabel.setText("❌ " + ex.getMessage());
            }
        });

        HBox formRow = new HBox(20, sentimentLabel, sentimentBox);
        formRow.setAlignment(Pos.CENTER_LEFT);

        commentBox.getChildren().addAll(formRow, commentLabel, commentArea, submitBtn, messageLabel);

        Button backBtn = new Button("Back");
        backBtn.setStyle("-fx-background-color: white; -fx-text-fill: #D22B2B; "
                + "-fx-font-weight: bold; -fx-background-radius: 6;");
        backBtn.setOnAction(e -> stage.setScene(createSearchScene(stage, currentUser)));

        layout.getChildren().addAll(title, blogInfo, commentsTitle, commentTable, commentBox, backBtn);

        return new Scene(layout, 850, 750);
    }

    // --------------------------------------------------------------------
    // BLOG INSERTION SCREEN
    // --------------------------------------------------------------------
    private Scene createBlogInsertionScene(Stage stage, String username) {
        VBox layout = new VBox(15);
        layout.setPadding(new Insets(20));
        layout.setAlignment(Pos.TOP_CENTER);
        layout.setStyle("-fx-background-color: #D22B2B;");

        Label titleLabel = new Label("Insert New Blog");
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: white");

        GridPane form = new GridPane();
        form.setVgap(10);
        form.setHgap(10);
        form.setAlignment(Pos.CENTER);

        TextField subjectField = new TextField();
        subjectField.setPrefWidth(400);

        TextArea descriptionArea = new TextArea();
        descriptionArea.setPrefRowCount(8);
        descriptionArea.setPrefWidth(400);

        TextField tagsField = new TextField();
        tagsField.setPrefWidth(400);

        Button submitButton = new Button("Submit Blog");
        Label messageLabel = new Label();
        messageLabel.setStyle("-fx-text-fill: white");

        // show how many blogs user posted today
        Label countLabel = new Label();
        countLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");
        updateBlogCountDisplay(countLabel, username);

        // handle blog submission
        submitButton.setOnAction(event -> {
            String subject = subjectField.getText().trim();
            String description = descriptionArea.getText().trim();
            String tags = tagsField.getText().trim();

            if (subject.isEmpty() || description.isEmpty()) {
                messageLabel.setText("Please fill in both subject and description.");
                messageLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");
                return;
            }

            try {
                BlogService blogService = new BlogService();
                int blogId = blogService.createBlog(username, subject, description, tags);

                messageLabel.setText("Blog created successfully! Blog ID: " + blogId);
                messageLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");

                subjectField.clear();
                descriptionArea.clear();
                tagsField.clear();

                updateBlogCountDisplay(countLabel, username);

            } catch (SQLException e) {
                messageLabel.setText("Error: " + e.getMessage());
                messageLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");
            }
        });

        // back to login screen
        Button backButton = new Button("Back to Login");
        backButton.setOnAction(event -> {
            stage.setTitle("Login");
            stage.setScene(createLoginScene(stage));
        });

        // open search + queries page
        Button searchPageButton = new Button("Search & Advanced Queries");
        searchPageButton.setOnAction(e -> {
            Scene searchScene = createSearchScene(stage, username);
            stage.setTitle("Search & Advanced Queries");
            stage.setScene(searchScene);
        });

        HBox queryButtons = new HBox(10, searchPageButton);
        queryButtons.setAlignment(Pos.CENTER);

        Label subjectLabel = new Label("Subject:");
        subjectLabel.setStyle("-fx-text-fill: white");
        Label descriptionLabel = new Label("Description:");
        descriptionLabel.setStyle("-fx-text-fill: white");
        Label tagsLabel = new Label("Tags:");
        tagsLabel.setStyle("-fx-text-fill: white");

        form.add(subjectLabel, 0, 0);
        form.add(subjectField, 1, 0);
        form.add(descriptionLabel, 0, 1);
        form.add(descriptionArea, 1, 1);
        form.add(tagsLabel, 0, 2);
        form.add(tagsField, 1, 2);
        form.add(submitButton, 1, 3);
        form.add(messageLabel, 1, 4);

        layout.getChildren().addAll(
                titleLabel,
                countLabel,
                form,
                queryButtons,
                backButton
        );

        return new Scene(layout, 600, 500);
    }

    // --------------------------------------------------------------------
    // HELPERS
    // --------------------------------------------------------------------
    private void updateBlogCountDisplay(Label countLabel, String username) {
        try {
            BlogService blogService = new BlogService();
            int count = blogService.getTodayBlogCount(username);
            int remaining = 2 - count;
            countLabel.setText("Today's blogs: " + count + "/2 (Remaining: " + remaining + ")");
        } catch (SQLException e) {
            countLabel.setText("Error retrieving blog count");
        }
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
