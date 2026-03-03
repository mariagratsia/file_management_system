package app;

import java.util.List;

import dataManager.ReadData;
import docs.Category;
import docs.Document;
import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import services.FileSystem;
import users.User;

public class Main extends Application {

	// Instantiate the central brain of the app
	private FileSystem fs = new FileSystem();
	private User currentUser = null; // who is logged in
	private Label followedCountLabel = new Label();

	/**
	 * App initialization:
	 * - Loads all data from json
	 * - Login screen
	 */
	@Override
	public void start(Stage primaryStage) {

		ReadData.loadAll(fs.getUsers(), fs.getDocuments(), fs.getCategories());

		primaryStage.setTitle("MediaLab Documents");

		primaryStage.setOnCloseRequest(event -> {
			System.out.println("Closing application. Saving data...");
			dataManager.SaveData.saveAll(fs.getUsers(), fs.getDocuments(), fs.getCategories());
		});

		showLoginScreen(primaryStage);
	}

	/**
	 * Launches app
	 * @param args Main args
	 */
	public static void main(String[] args) {
		launch(args);
	}

	/**
	 * Login screen gui. If login is successfull, opens app's dashboard
	 * @param primaryStage
	 */
	private void showLoginScreen(Stage primaryStage) {
		VBox loginLayout = new VBox(10);
		loginLayout.setPadding(new Insets(20));
		loginLayout.setAlignment(Pos.CENTER);

		Label welcomeLabel = new Label("Welcome! Please Login.");
		TextField userField = new TextField(); userField.setPromptText("Username"); userField.setMaxWidth(200);
		PasswordField passField = new PasswordField(); passField.setPromptText("Password"); passField.setMaxWidth(200);
		Button loginButton = new Button("Login");
		Label errorLabel = new Label(); errorLabel.setStyle("-fx-text-fill: red;");

		loginLayout.getChildren().addAll(welcomeLabel, userField, passField, loginButton, errorLabel);
		Scene loginScene = new Scene(loginLayout, 400, 300);

		loginButton.setOnAction(e -> {
			currentUser = services.Login.authenticate(userField.getText(), passField.getText(), fs.getUsers());
			if (currentUser != null) {
				showMainDashboard(primaryStage); // Go to dashboard
			} else {
				errorLabel.setText("Invalid username or password.");
			}
		});

		primaryStage.setScene(loginScene);
		primaryStage.show();
	}



	/**
	 * App's main dashboard gui
	 * @param primaryStage
	 */
	private void showMainDashboard(Stage primaryStage) {
		javafx.scene.layout.BorderPane root = new javafx.scene.layout.BorderPane();

		// NOTIFICATIONS FOR FOLLOWING DOCS
		String today = java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"));
		List<String> docsToStopTracking = new java.util.ArrayList<>();

		for (String trackedTitle : currentUser.getFollowedDocs().keySet()) {
			int trackedVersion = currentUser.getFollowedDocs().get(trackedTitle);
			boolean docStillExists = false;

			for (Document liveDoc : fs.getDocuments()) {
				if (liveDoc.getDocTitle().equals(trackedTitle)) {
					docStillExists = true;
					if (liveDoc.getVersion() > trackedVersion) {
						currentUser.addNotification("[" + today + "] Document '" + trackedTitle + "' was updated to Version " + liveDoc.getVersion());
						currentUser.getFollowedDocs().put(trackedTitle, liveDoc.getVersion());
					}
					break;
				}
			}

			if (!docStillExists) {
				docsToStopTracking.add(trackedTitle);
				currentUser.addNotification("[" + today + "] 🗑️ Document Deleted: '" + trackedTitle + "' is no longer available.");
			}
		}

		for (String deadDoc : docsToStopTracking) {
			currentUser.getFollowedDocs().remove(deadDoc);
		}

		int todaysAlerts = 0;
		for (String notif : currentUser.getNotifications()) {
			if (notif.startsWith("[" + today + "]")) {
				todaysAlerts++;
			}
		}

		if (todaysAlerts > 0) {
			String finalMessage = "🔔 You have " + todaysAlerts + " new update(s) today!\nCheck your Update History tab.";

			javafx.application.Platform.runLater(() -> {
				showToastNotification(finalMessage);
			});
		}




		// PROFIL + SUMMARY PANEL
		VBox leftPanel = new VBox(15);
		leftPanel.setPadding(new Insets(15));
		leftPanel.setStyle("-fx-background-color: #f4f4f4; -fx-border-color: #cccccc; -fx-border-width: 0 1 0 0;");
		leftPanel.setPrefWidth(230); 

		Label profileTitle = new Label("My Profile");
		profileTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: #2c3e50;");

		Label nameLabel = new Label("Name: " + currentUser.getUserFirstName() + " " + currentUser.getUserLastName());
		Label roleLabel = new Label("Role: " + currentUser.getUserType());
		roleLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #2980b9;"); 

		Label categoriesTitle = new Label("My Categories:");
		categoriesTitle.setStyle("-fx-underline: true; -fx-padding: 5 0 0 0;");

		javafx.scene.control.Label allowedCatsText = new javafx.scene.control.Label();
		allowedCatsText.setWrapText(true); 

		if (currentUser.getUserType().equals("ADMIN")) {
			allowedCatsText.setText("ALL CATEGORIES");
			allowedCatsText.setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
		} else {
			if (currentUser.getUserCategories().isEmpty()) {
				allowedCatsText.setText("None Assigned, contact system admin");
				allowedCatsText.setStyle("-fx-text-fill: red;");
			} else {
				allowedCatsText.setText(String.join(", ", currentUser.getUserCategories()));
			}
		}

		VBox profileBox = new VBox(8, profileTitle, nameLabel, roleLabel, categoriesTitle, allowedCatsText);

		javafx.scene.control.Separator separator = new javafx.scene.control.Separator();
		separator.setPadding(new Insets(10, 0, 10, 0));

		Label summaryTitle = new Label("System Summary");
		summaryTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: #2c3e50;");

		Label catCountLabel = new Label("Total Categories: " + fs.getCategories().size());
		Label docCountLabel = new Label("Total Documents: " + fs.getDocuments().size());
		followedCountLabel.setText("Followed Docs: " + currentUser.getFollowedDocs().size());
		VBox summaryBox = new VBox(8, summaryTitle, catCountLabel, docCountLabel, followedCountLabel);

		Button btnLogout = new Button("Logout");
		btnLogout.setStyle("-fx-background-color: #ff4c4c; -fx-text-fill: white; -fx-font-weight: bold; -fx-pref-width: 100px;");
		btnLogout.setOnAction(e -> {
			currentUser = null; 
			showLoginScreen(primaryStage); 
		});

		leftPanel.getChildren().addAll(profileBox, separator, summaryBox, new Label(""), btnLogout);
		root.setLeft(leftPanel);


		// ALL APP'S OPERATIONS

		javafx.scene.control.TabPane operationsPane = new javafx.scene.control.TabPane();

		// DOCS TAB
		javafx.scene.control.Tab docsTab = new javafx.scene.control.Tab("All Documents");
		docsTab.setClosable(false);
		docsTab.setContent(buildDocsTab()); 
		docsTab.setOnSelectionChanged(e -> { if (docsTab.isSelected()) docsTab.setContent(buildDocsTab()); });

		// FOLLOWED DOCS TAB
		javafx.scene.control.Tab followedTab = new javafx.scene.control.Tab("Followed Docs");
		followedTab.setClosable(false);
		followedTab.setContent(buildFollowedDocsTab());
		followedTab.setOnSelectionChanged(e -> { if (followedTab.isSelected()) followedTab.setContent(buildFollowedDocsTab()); });

		// NOTIFICATIONS TAB
		javafx.scene.control.Tab notifTab = new javafx.scene.control.Tab("Notifications");
		notifTab.setClosable(false);
		notifTab.setContent(buildNotificationsTab());
		notifTab.setOnSelectionChanged(e -> { if (notifTab.isSelected()) notifTab.setContent(buildNotificationsTab()); });

		operationsPane.getTabs().addAll(docsTab, followedTab, notifTab);

		// USER & CATEGORY MANAGEMENT -> ADMIN ONLY
		if (currentUser.getUserType().equals("ADMIN")) {
			javafx.scene.control.Tab categoriesTab = new javafx.scene.control.Tab("Manage Categories");
			categoriesTab.setClosable(false);
			categoriesTab.setContent(buildCategoriesTab(primaryStage)); 

			javafx.scene.control.Tab usersTab = new javafx.scene.control.Tab("Manage Users");
			usersTab.setClosable(false);

			usersTab.setContent(buildUsersTab()); 

			usersTab.setOnSelectionChanged(e -> {
				if (usersTab.isSelected()) {
					usersTab.setContent(buildUsersTab());
				}
			});

			operationsPane.getTabs().addAll(categoriesTab, usersTab);
		}

		root.setCenter(operationsPane);

		Scene mainScene = new Scene(root, 1000, 700);
		primaryStage.setScene(mainScene);

		primaryStage.centerOnScreen(); 
	}
	/**
	 * @return builds manage category's tab gui
	 */
	private VBox buildCategoriesTab(Stage primaryStage) {
		VBox layout = new VBox(15);
		layout.setPadding(new Insets(15));

		// current categories table
		TableView<Category> catTable = new TableView<>();
		ObservableList<Category> observableCats = FXCollections.observableArrayList(fs.getCategories());
		catTable.setItems(observableCats);

		TableColumn<Category, String> nameCol = new TableColumn<>("Category Name");
		nameCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getCatName()));
		nameCol.setPrefWidth(300);
		catTable.getColumns().add(nameCol);
		catTable.setPrefHeight(200);

		// Add, Rename, Delete a category
		GridPane controls = new GridPane();
		controls.setHgap(10);
		controls.setVgap(10);

		TextField newCatInput = new TextField(); 
		newCatInput.setPromptText("New Category Name");
		Button btnAdd = new Button("Add Category");

		TextField renameInput = new TextField(); 
		renameInput.setPromptText("New Name for Selected");
		Button btnRename = new Button("Rename Selected");

		Button btnDelete = new Button("Delete Selected Category");
		btnDelete.setStyle("-fx-text-fill: red;");

		btnAdd.setPrefWidth(160);
		btnRename.setPrefWidth(160);
		btnDelete.setPrefWidth(160);

		btnAdd.setPrefHeight(35);
		btnRename.setPrefHeight(35);
		btnDelete.setPrefHeight(35);

		Label messageLabel = new Label();

		controls.addRow(0, newCatInput, btnAdd);
		controls.addRow(1, renameInput, btnRename);
		controls.add(btnDelete, 0, 2);

		// buttins actions
		btnAdd.setOnAction(e -> {
			String name = newCatInput.getText().trim();
			if (!name.isEmpty()) {
				Category newCat = new Category(fs.getCategories().size() + 1, name); 
				fs.addCategory(newCat);       
				observableCats.add(newCat);
				newCatInput.clear();
				messageLabel.setText("Category added.");
			}
		});

		btnRename.setOnAction(e -> {
			Category selected = catTable.getSelectionModel().getSelectedItem();
			String newName = renameInput.getText().trim();
			if (selected != null && !newName.isEmpty()) {
				fs.modifyCategoryName(selected, newName); 
				catTable.refresh();
				renameInput.clear();
				messageLabel.setText("Category renamed.");
			}
		});

		btnDelete.setOnAction(e -> {
			Category selected = catTable.getSelectionModel().getSelectedItem();
			if (selected != null) {

				javafx.scene.control.Alert confirmBox = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.CONFIRMATION);
				confirmBox.setTitle("Confirm Deletion");
				confirmBox.setHeaderText("Are you sure you want to delete this category?");
				confirmBox.setContentText("User: " + selected.getCatName() + "\nThis action will delete all documents in this category.");

				java.util.Optional<javafx.scene.control.ButtonType> result = confirmBox.showAndWait();

				if (result.isPresent() && result.get() == javafx.scene.control.ButtonType.OK) {
					fs.deleteCategory(selected);    
					observableCats.remove(selected); 
					messageLabel.setText("Category deleted along with its documents.");
					showToastNotification("Category '" + selected.getCatName() + "' deleted.");
				} else {
					messageLabel.setText("Deletion cancelled.");
					messageLabel.setStyle("-fx-text-fill: orange;");
				}
			} else {
				messageLabel.setText("Please select a category to delete.");
				messageLabel.setStyle("-fx-text-fill: red;");
			}



		});

		layout.getChildren().addAll(new Label("Manage Categories"), catTable, controls, messageLabel);
		return layout;
	}

	/**
	 * @return builds manage users tab gui
	 */
	private VBox buildUsersTab() {
		VBox layout = new VBox(15);
		layout.setPadding(new Insets(15));

		// users display table
		TableView<User> userTable = new TableView<>();
		ObservableList<User> observableUsers = FXCollections.observableArrayList(fs.getUsers());
		userTable.setItems(observableUsers);

		TableColumn<User, String> userCol = new TableColumn<>("Username");
		userCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getUsername()));
		TableColumn<User, String> nameCol = new TableColumn<>("Name");
		nameCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getUserFirstName() + " " + data.getValue().getUserLastName()));
		TableColumn<User, String> roleCol = new TableColumn<>("Role");
		roleCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getUserType()));

		TableColumn<User, String> catCol = new TableColumn<>("Categories");
		catCol.setCellValueFactory(data -> new SimpleStringProperty(String.join(", ", data.getValue().getUserCategories())));
		catCol.setPrefWidth(250);

		userTable.getColumns().add(userCol);
		userTable.getColumns().add(nameCol);
		userTable.getColumns().add(roleCol);
		userTable.getColumns().add(catCol);
		userTable.setPrefHeight(200);

		// show password
		GridPane form = new GridPane();
		form.setHgap(10); form.setVgap(10);

		TextField firstInput = new TextField(); firstInput.setPromptText("First Name");
		TextField lastInput = new TextField(); lastInput.setPromptText("Last Name");
		TextField userInput = new TextField(); userInput.setPromptText("Username");

		PasswordField passHidden = new PasswordField(); passHidden.setPromptText("Password");
		TextField passVisible = new TextField(); passVisible.setPromptText("Password");
		passVisible.setManaged(false); passVisible.setVisible(false);
		passHidden.textProperty().bindBidirectional(passVisible.textProperty());

		javafx.scene.control.CheckBox showPassCheck = new javafx.scene.control.CheckBox("Show");
		showPassCheck.selectedProperty().addListener((obs, oldVal, newVal) -> {
			if (newVal) {
				passHidden.setVisible(false); passHidden.setManaged(false);
				passVisible.setVisible(true); passVisible.setManaged(true);
			} else {
				passVisible.setVisible(false); passVisible.setManaged(false);
				passHidden.setVisible(true); passHidden.setManaged(true);
			}
		});
		HBox passBox = new HBox(5, passHidden, passVisible, showPassCheck);

		ComboBox<String> roleInput = new ComboBox<>(FXCollections.observableArrayList("READER", "AUTHOR", "ADMIN"));
		roleInput.setPromptText("Select Role");

		javafx.scene.control.ListView<String> catListView = new javafx.scene.control.ListView<>();
		catListView.getSelectionModel().setSelectionMode(javafx.scene.control.SelectionMode.MULTIPLE);
		catListView.setPrefHeight(75);

		userTable.getProperties().put("catList", catListView);
		for (int i = 0; i < fs.getCategories().size(); i++) {
			catListView.getItems().add(fs.getCategories().get(i).getCatName());
		}

		form.addRow(0, new Label("First Name:"), firstInput, new Label("Last Name:"), lastInput);
		form.addRow(1, new Label("Username:"), userInput, new Label("Password:"), passBox);
		form.addRow(2, new Label("Role:"), roleInput, new Label("Categories:"), catListView);

		userTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
			if (newSelection != null) {
				firstInput.setText(newSelection.getUserFirstName());
				lastInput.setText(newSelection.getUserLastName());
				userInput.setText(newSelection.getUsername());
				passHidden.setText(newSelection.getPassword());

				firstInput.setEditable(false);
				lastInput.setEditable(false);
				userInput.setEditable(false);

				String lockedStyle = "-fx-background-color: #e0e0e0; -fx-text-fill: #555555;";
				firstInput.setStyle(lockedStyle);
				lastInput.setStyle(lockedStyle);
				userInput.setStyle(lockedStyle);

				roleInput.setValue(newSelection.getUserType());

				catListView.getSelectionModel().clearSelection();
				for (String cat : newSelection.getUserCategories()) {
					catListView.getSelectionModel().select(cat);
				}
			}
		});

		// add, modify, delete user + clear form
		Button btnAdd = new Button("Add New User");
		Button btnModify = new Button("Apply Changes to User");
		Button btnDelete = new Button("Delete Selected User");
		Button btnClear = new Button("Clear Form"); 

		btnAdd.setPrefWidth(160);
		btnModify.setPrefWidth(160);
		btnDelete.setPrefWidth(160);
		btnClear.setPrefWidth(160);

		btnAdd.setPrefHeight(35);
		btnModify.setPrefHeight(35);
		btnDelete.setPrefHeight(35);
		btnClear.setPrefHeight(35);

		Label messageLabel = new Label();

		Runnable resetForm = () -> {
			firstInput.clear(); lastInput.clear(); userInput.clear(); passHidden.clear();
			roleInput.setValue(null); catListView.getSelectionModel().clearSelection();
			userTable.getSelectionModel().clearSelection();

			firstInput.setEditable(true);
			lastInput.setEditable(true);
			userInput.setEditable(true);
			firstInput.setStyle(""); lastInput.setStyle(""); userInput.setStyle("");
		};

		btnClear.setOnAction(e -> {
			resetForm.run();
			messageLabel.setText("Form cleared. Ready to add a new user.");
			messageLabel.setStyle("-fx-text-fill: gray;");
		});

		btnAdd.setOnAction(e -> {
			try {
				String requestedUsername = userInput.getText().trim();
				if (requestedUsername.isEmpty()) { messageLabel.setText("Username is required."); return; }

				for (User u : fs.getUsers()) {
					if (u.getUsername().equalsIgnoreCase(requestedUsername)) {
						messageLabel.setText("Error: Username '" + requestedUsername + "' is already taken!");
						messageLabel.setStyle("-fx-text-fill: red;");
						return;
					}
				}
				List<String> allowedCats = new java.util.ArrayList<>(catListView.getSelectionModel().getSelectedItems());
				if (allowedCats.isEmpty()) { messageLabel.setText("Select at least one category."); return; }

				int newId = fs.getUsers().size() + 1;
				String role = roleInput.getValue();
				if (role == null) { messageLabel.setText("Select a role."); return; }

				User newUser;

				if (role.equals("ADMIN")) {
					newUser = new users.Admin(newId, firstInput.getText(), lastInput.getText(), userInput.getText(), passHidden.getText(), allowedCats);
				} else if (role.equals("AUTHOR")) {
					newUser = new users.Author(newId, firstInput.getText(), lastInput.getText(), userInput.getText(), passHidden.getText(), allowedCats);
				} else {
					newUser = new users.Reader(newId, firstInput.getText(), lastInput.getText(), userInput.getText(), passHidden.getText(), allowedCats);
				}

				fs.addUser(newUser);
				observableUsers.add(newUser);

				resetForm.run();

				messageLabel.setText("User added successfully.");
				messageLabel.setStyle("-fx-text-fill: green;");
			} catch (Exception ex) { messageLabel.setText("Fill all fields."); }
		});

		btnModify.setOnAction(e -> {
			User selected = userTable.getSelectionModel().getSelectedItem();
			if (selected != null) {
				if (selected.getUsername().equals("medialab")) {
					messageLabel.setText("Cannot modify the default admin!");
					messageLabel.setStyle("-fx-text-fill: red;");
					return;
				}

				List<String> allowedCats = new java.util.ArrayList<>(catListView.getSelectionModel().getSelectedItems());
				if (allowedCats.isEmpty()) { messageLabel.setText("Select at least one category."); return; }

				String role = roleInput.getValue();
				if (role == null) { messageLabel.setText("Select a role."); return; }

				javafx.scene.control.Alert confirmBox = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.CONFIRMATION);
				confirmBox.setTitle("Confirm Modification");
				confirmBox.setHeaderText("Apply changes to " + selected.getUsername() + "?");
				confirmBox.setContentText("You are updating their role/categories/password.");

				java.util.Optional<javafx.scene.control.ButtonType> result = confirmBox.showAndWait();

				if (result.isPresent() && result.get() == javafx.scene.control.ButtonType.OK) {

					User updatedUser = role.equals("AUTHOR") ? 
							new users.Author(selected.getUserId(), selected.getUserFirstName(), selected.getUserLastName(), selected.getUsername(), passHidden.getText(), allowedCats) :
								new users.Reader(selected.getUserId(), selected.getUserFirstName(), selected.getUserLastName(), selected.getUsername(), passHidden.getText(), allowedCats);

					for (java.util.Map.Entry<String, Integer> entry : selected.getFollowedDocs().entrySet()) {
						updatedUser.followDoc(entry.getKey(), entry.getValue());
					}
					updatedUser.getNotifications().addAll(selected.getNotifications());

					int index = fs.getUsers().indexOf(selected);
					fs.getUsers().set(index, updatedUser);
					observableUsers.set(index, updatedUser);

					resetForm.run();

					messageLabel.setText("User modified successfully.");
					messageLabel.setStyle("-fx-text-fill: green;");
				} else {
					messageLabel.setText("Modification cancelled.");
					messageLabel.setStyle("-fx-text-fill: orange;");
				}
			} else {
				messageLabel.setText("Please select a user to modify.");
				messageLabel.setStyle("-fx-text-fill: red;");
			}
		});

		btnDelete.setOnAction(e -> {
			User selected = userTable.getSelectionModel().getSelectedItem();
			if (selected != null) {
				if (selected.getUsername().equals("medialab")) {
					messageLabel.setText("Cannot delete the default admin!");
					messageLabel.setStyle("-fx-text-fill: red;");
					return;
				}
				javafx.scene.control.Alert confirmBox = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.CONFIRMATION);
				confirmBox.setTitle("Confirm Deletion");
				confirmBox.setHeaderText("Are you sure you want to delete this user?");
				confirmBox.setContentText("User: " + selected.getUsername() + "\nThis action cannot be undone.");

				java.util.Optional<javafx.scene.control.ButtonType> result = confirmBox.showAndWait();

				if (result.isPresent() && result.get() == javafx.scene.control.ButtonType.OK) {
					fs.removeUser(selected);
					observableUsers.remove(selected);
					resetForm.run();

					messageLabel.setText("User deleted.");
					messageLabel.setStyle("-fx-text-fill: green;");
				} else {
					messageLabel.setText("Deletion cancelled.");
					messageLabel.setStyle("-fx-text-fill: orange;");
				}
			} else {
				messageLabel.setText("Please select a user to delete.");
				messageLabel.setStyle("-fx-text-fill: red;");
			}
		});

		HBox buttonBox = new HBox(10, btnAdd, btnModify, btnDelete, btnClear, messageLabel);
		layout.getChildren().addAll(new Label("Manage Users"), userTable, form, buttonBox);
		return layout;
	}

	/**
	 * @return builds doc's tab gui
	 */
	private VBox buildDocsTab() {
		VBox layout = new VBox(15);
		layout.setPadding(new Insets(15));


		HBox filterBox = new HBox(15);
		filterBox.setAlignment(Pos.CENTER_LEFT);

		// Category dropdown list
		ComboBox<String> filterCombo = new ComboBox<>();
		filterCombo.getItems().add("All Allowed Categories");
		if (currentUser.getUserType().equals("ADMIN")) {
			for (docs.Category c : fs.getCategories()) filterCombo.getItems().add(c.getCatName());
		} else {
			filterCombo.getItems().addAll(currentUser.getUserCategories());
		}
		filterCombo.getSelectionModel().selectFirst();

		// Search bar
		TextField searchField = new TextField();
		searchField.setPromptText("Search Title or Author...");
		searchField.setPrefWidth(250);

		filterBox.getChildren().addAll(
				new Label("Filter Category:"), filterCombo, 
				new Label("  Search:"), searchField
				);

		// docs table with applied search filters
		TableView<Document> docTable = new TableView<>();
		ObservableList<Document> observableDocs = FXCollections.observableArrayList();
		docTable.setItems(observableDocs);

		Runnable refreshTableData = () -> {
			String selectedFilter = filterCombo.getValue();
			String searchText = searchField.getText().toLowerCase().trim();

			List<Document> visibleDocs = new java.util.ArrayList<>();

			for (Document doc : fs.getDocuments()) {
				boolean hasAccess = currentUser.getUserType().equals("ADMIN") || currentUser.getUserCategories().contains(doc.getCategory());

				boolean matchesFilter = selectedFilter.equals("All Allowed Categories") || doc.getCategory().equals(selectedFilter);

				boolean matchesSearch = searchText.isEmpty() || 
						doc.getDocTitle().toLowerCase().contains(searchText) || 
						doc.getAuthorName().toLowerCase().contains(searchText);

				if (hasAccess && matchesFilter && matchesSearch) {
					visibleDocs.add(doc);
				}
			}
			observableDocs.setAll(visibleDocs); 
		};

		refreshTableData.run();

		filterCombo.setOnAction(e -> refreshTableData.run());
		searchField.textProperty().addListener((obs, oldVal, newVal) -> refreshTableData.run());


		TableColumn<Document, String> titleCol = new TableColumn<>("Title");
		titleCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getDocTitle()));

		TableColumn<Document, String> authorCol = new TableColumn<>("Author");
		authorCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getAuthorName()));

		TableColumn<Document, String> catCol = new TableColumn<>("Category");
		catCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getCategory()));

		TableColumn<Document, String> verCol = new TableColumn<>("Ver.");
		verCol.setCellValueFactory(data -> new SimpleStringProperty(String.valueOf(data.getValue().getVersion())));

		TableColumn<Document, String> createdCol = new TableColumn<>("Created");
		createdCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getCreationDate()));

		TableColumn<Document, String> modCol = new TableColumn<>("Modified");
		modCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getLastModified()));

		TableColumn<Document, String> statusCol = new TableColumn<>("Status");
		statusCol.setCellValueFactory(data -> {
			if (currentUser.getFollowedDocs().containsKey(data.getValue().getDocTitle())) {
				return new javafx.beans.property.SimpleStringProperty("FOLLOWING");
			}
			return new javafx.beans.property.SimpleStringProperty("");
		});

		statusCol.setCellFactory(col -> new javafx.scene.control.TableCell<Document, String>() {
			@Override
			protected void updateItem(String item, boolean empty) {
				super.updateItem(item, empty);
				if (empty || item == null || item.isEmpty()) {
					setGraphic(null);
				} else {

					Label tag = new Label(item);
					tag.setStyle("-fx-background-color: #d0ebff; -fx-text-fill: #005bb5; -fx-font-weight: bold; -fx-padding: 3px 8px; -fx-background-radius: 12px; -fx-font-size: 11px;");
					setGraphic(tag);
					setAlignment(javafx.geometry.Pos.CENTER);
				}
			}
		});
		statusCol.setPrefWidth(100);

		docTable.getColumns().add(titleCol);
		docTable.getColumns().add(authorCol);
		docTable.getColumns().add(catCol);
		docTable.getColumns().add(verCol);
		docTable.getColumns().add(createdCol);
		docTable.getColumns().add(modCol);
		docTable.getColumns().add(statusCol);
		docTable.setPrefHeight(200);

		// Double click to read document in a popup
		docTable.setRowFactory(tv -> {
			javafx.scene.control.TableRow<Document> row = new javafx.scene.control.TableRow<>();
			row.setOnMouseClicked(event -> {
				if (event.getClickCount() == 2 && (!row.isEmpty())) {
					showDocumentReader(row.getItem());
				}
			});
			return row;
		});


		// FOLLOW + UNFOLLOW BUTTONS
		Button btnTrack = new Button("Follow Selected Document");
		Button btnUntrack = new Button("Unfollow Selected Document");
		Label trackMsg = new Label();

		btnTrack.setPrefWidth(190);
		btnUntrack.setPrefWidth(190);

		String trackStyle = "-fx-background-color: #2980b9; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 13px; -fx-padding: 6px 12px; -fx-cursor: hand;  -fx-border-width: 1px; -fx-border-radius: 5px;";
		btnTrack.setStyle(trackStyle);

		String untrackStyle = "-fx-background-color: #2980b9; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 13px; -fx-padding: 6px 12px; -fx-cursor: hand; -fx-border-width: 1px; -fx-border-radius: 5px;";
		btnUntrack.setStyle(untrackStyle);

		btnTrack.setOnAction(e -> {
			Document selected = docTable.getSelectionModel().getSelectedItem();
			btnTrack.setDisable(true);
			btnUntrack.setDisable(false);
			if (selected != null) {
				// save the version 
				currentUser.getFollowedDocs().put(selected.getDocTitle(), selected.getVersion());
				trackMsg.setText("Now following: " + selected.getDocTitle());
				trackMsg.setStyle("-fx-text-fill: green;");
				followedCountLabel.setText("Followed Docs: " + currentUser.getFollowedDocs().size());
				docTable.refresh();
			} else {
				trackMsg.setText("Select a document first.");
				trackMsg.setStyle("-fx-text-fill: red;");
			}
		});

		btnUntrack.setOnAction(e -> {
			Document selected = docTable.getSelectionModel().getSelectedItem();
			btnUntrack.setDisable(true); 
			btnTrack.setDisable(false);
			if (selected != null) {
				currentUser.getFollowedDocs().remove(selected.getDocTitle());
				trackMsg.setText("Stopped following: " + selected.getDocTitle());
				trackMsg.setStyle("-fx-text-fill: orange;");
				followedCountLabel.setText("Followed Docs: " + currentUser.getFollowedDocs().size());
				docTable.refresh();
			}
		});

		HBox trackingBox = new HBox(10, btnTrack, btnUntrack, trackMsg);
		trackingBox.setAlignment(Pos.CENTER_LEFT);

		// edit docs
		GridPane form = new GridPane();
		form.setHgap(10); form.setVgap(10);

		TextField titleInput = new TextField(); titleInput.setPromptText("Document Title");

		ComboBox<String> catInput = new ComboBox<>();
		catInput.setPromptText("Select Category");
		if (currentUser.getUserType().equals("ADMIN")) {
			for (docs.Category c : fs.getCategories()) catInput.getItems().add(c.getCatName());
		} else {
			catInput.getItems().addAll(currentUser.getUserCategories());
		}

		javafx.scene.control.TextArea textInput = new javafx.scene.control.TextArea();
		textInput.setPromptText("Write document content here...");
		textInput.setPrefRowCount(6);
		textInput.setWrapText(true);

		form.addRow(0, new Label("Title:"), titleInput, new Label("Category:"), catInput);
		form.add(new Label("Content:"), 0, 1);
		form.add(textInput, 1, 1, 3, 1);

		// docs operations
		Button btnAdd = new Button("Create New Document");
		Button btnModify = new Button("Save Changes");
		Button btnDelete = new Button("Delete Document");
		Button btnClear = new Button("Clear Editor");
		Label messageLabel = new Label();

		String btnStyle = "-fx-font-size: 14px; -fx-padding: 8px 15px; -fx-cursor: hand;";
		btnAdd.setStyle(btnStyle);
		btnModify.setStyle(btnStyle);
		btnDelete.setStyle(btnStyle + " -fx-text-fill: red;");
		btnClear.setStyle(btnStyle);

		if (currentUser.getUserType().equals("READER")) {
			btnAdd.setVisible(false); btnAdd.setManaged(false);
			btnModify.setVisible(false); btnModify.setManaged(false);
			btnDelete.setVisible(false); btnDelete.setManaged(false);
			textInput.setEditable(false);
			titleInput.setEditable(false);
		}

		Runnable resetForm = () -> {
			titleInput.clear(); textInput.clear(); catInput.setValue(null);
			docTable.getSelectionModel().clearSelection();
			titleInput.setEditable(true); catInput.setDisable(false);
		};

		btnClear.setOnAction(e -> { resetForm.run(); messageLabel.setText("Editor cleared."); });

		docTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
			if (newSel != null) {
				titleInput.setText(newSel.getDocTitle());
				catInput.setValue(newSel.getCategory());
				textInput.setText(newSel.getText()); 
				titleInput.setEditable(false);
				catInput.setDisable(true);

				if (currentUser.getUserType().equals("AUTHOR")) {
					boolean isMyDoc = newSel.getAuthorName().equals(currentUser.getUsername());

					btnModify.setDisable(!isMyDoc);
					btnDelete.setDisable(!isMyDoc);

					if (!isMyDoc) {
						messageLabel.setText("Read-Only: You can only modify your own documents.");
						messageLabel.setStyle("-fx-text-fill: orange;");
					} else {
						messageLabel.setText("");
					}
				}
				boolean isFollowing = currentUser.getFollowedDocs().containsKey(newSel.getDocTitle());

				btnTrack.setDisable(isFollowing);   
				btnUntrack.setDisable(!isFollowing);
			}
		});

		btnAdd.setOnAction(e -> {
			String title = titleInput.getText().trim();
			String cat = catInput.getValue();
			String text = textInput.getText().trim();

			if (title.isEmpty() || cat == null || text.isEmpty()) {
				messageLabel.setText("All fields are required to create a document.");
				return;
			}

			// Check for duplicate title
			for (Document d : fs.getDocuments()) {
				if (d.getDocTitle().equals(title)) {
					messageLabel.setText("A document with this title already exists!");
					return;
				}
			}

			Document newDoc = new Document(title, currentUser.getUsername(), cat, text);
			fs.addDocument(newDoc);
			observableDocs.add(newDoc);

			resetForm.run();
			messageLabel.setText("Document created!");
			messageLabel.setStyle("-fx-text-fill: green;");
		});

		btnModify.setOnAction(e -> {
			Document selected = docTable.getSelectionModel().getSelectedItem();
			if (selected != null) {


				if (currentUser.getUserType().equals("AUTHOR") && !selected.getAuthorName().equals(currentUser.getUsername())) {
					messageLabel.setText("Security Error: You cannot edit someone else's document.");
					messageLabel.setStyle("-fx-text-fill: red;");
					return; 
				}

				javafx.scene.control.Alert confirmBox = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.CONFIRMATION);
				confirmBox.setTitle("Confirm Modification");
				confirmBox.setHeaderText("Save changes to '" + selected.getDocTitle() + "'?");
				confirmBox.setContentText("This will update the document's contents and increase its version number.");

				java.util.Optional<javafx.scene.control.ButtonType> result = confirmBox.showAndWait();

				if (result.isPresent() && result.get() == javafx.scene.control.ButtonType.OK) {

					String newText = textInput.getText().trim();
					if (newText.isEmpty()) { 
						messageLabel.setText("Text cannot be empty."); 
						return; 
					}

					String today = java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"));
					selected.updateDoc(newText, today);

					docTable.refresh();
					resetForm.run();
					messageLabel.setText("Success! Updated to Version " + selected.getVersion() + ".");
					messageLabel.setStyle("-fx-text-fill: green;");

					messageLabel.setText("Document saved successfully.");
					messageLabel.setStyle("-fx-text-fill: green;");
				} else {
					messageLabel.setText("Save cancelled.");
					messageLabel.setStyle("-fx-text-fill: orange;");
				}
			} else {
				messageLabel.setText("Please select a document to save.");
				messageLabel.setStyle("-fx-text-fill: red;");
			}

		});

		btnDelete.setOnAction(e -> {
			Document selected = docTable.getSelectionModel().getSelectedItem();
			if (selected != null) {

				javafx.scene.control.Alert confirmBox = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.CONFIRMATION);
				confirmBox.setTitle("Confirm Deletion");
				confirmBox.setHeaderText("Are you sure you want to delete this document?");
				confirmBox.setContentText("User: " + selected.getDocTitle() + "\nThis action cannot be undone.");

				java.util.Optional<javafx.scene.control.ButtonType> result = confirmBox.showAndWait();

				if (result.isPresent() && result.get() == javafx.scene.control.ButtonType.OK) {
					fs.deleteDocument(selected);
					refreshTableData.run();
					resetForm.run();
					messageLabel.setText("Document deleted.");
					messageLabel.setStyle("-fx-text-fill: green;");

				} else {
					messageLabel.setText("Deletion cancelled.");
					messageLabel.setStyle("-fx-text-fill: orange;");
				}
			} else {
				messageLabel.setText("Please select a document to delete.");
				messageLabel.setStyle("-fx-text-fill: red;");
			}

		});
		Label hintLabel = new Label("Double-click any document to read its content.");
		hintLabel.setStyle("-fx-font-style: italic; -fx-text-fill: #7f8c8d; -fx-font-size: 13px; -fx-padding: 0 0 5px 0;");

		HBox buttonBox = new HBox(10, btnAdd, btnModify, btnDelete, btnClear, messageLabel);
		if (currentUser.getUserType().equals("READER")) {
			docTable.setPrefHeight(400); 
			layout.getChildren().addAll(new Label("Document Repository"), filterBox, hintLabel, docTable, trackingBox);
		} else {
			layout.getChildren().addAll(new Label("Document Repository"), filterBox, hintLabel, docTable, trackingBox, form, buttonBox);
		}

		return layout;
	}

	/**
	 * Helper to display a reading window with version dropdown for Authors and Admins
	 * Pops up when double click a doc
	 * @param doc Document object
	 */
	private void showDocumentReader(Document doc) {
		Stage readerStage = new Stage();
		readerStage.initModality(javafx.stage.Modality.APPLICATION_MODAL); 
		readerStage.setTitle("Reading: " + doc.getDocTitle());

		VBox layout = new VBox(10);
		layout.setPadding(new Insets(20));

		// Title + Version Dropdown
		HBox headerBox = new HBox(15);
		headerBox.setAlignment(Pos.CENTER_LEFT);

		Label titleLabel = new Label(doc.getDocTitle());
		titleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 20px;");
		headerBox.getChildren().add(titleLabel);
		javafx.scene.control.TextArea contentArea = new javafx.scene.control.TextArea(doc.getText());
		contentArea.setEditable(false); 
		contentArea.setWrapText(true);
		contentArea.setPrefSize(500, 350);

		if (!currentUser.getUserType().equals("READER")) {
			ComboBox<String> versionCombo = new ComboBox<>();
			versionCombo.getItems().add("Current (v" + doc.getVersion() + ")");

			List<Integer> oldVersions = new java.util.ArrayList<>(doc.getDocHistory().keySet());
			java.util.Collections.sort(oldVersions, java.util.Collections.reverseOrder());

			for (Integer v : oldVersions) {
				versionCombo.getItems().add("Version " + v);
			}

			versionCombo.getSelectionModel().selectFirst();
			versionCombo.setOnAction(e -> {
				String selected = versionCombo.getValue();
				if (selected != null) {
					if (selected.startsWith("Current")) {
						contentArea.setText(doc.getText()); 
					} else {
						int vNum = Integer.parseInt(selected.replace("Version ", ""));
						contentArea.setText(doc.pullOldVersion(vNum));
					}
				}
			});

			headerBox.getChildren().addAll(new Label("  View Version:"), versionCombo);
		}
		String rawAuthorName = doc.getAuthorName();
		String displayAuthorName = "Deleted User";

		if (!rawAuthorName.contains("(Deleted Account)")) {
			for (users.User u : fs.getUsers()) {
				if (u.getUsername().equals(rawAuthorName)) {
					displayAuthorName = u.getUserFirstName() + " " + u.getUserLastName();
					break; 
				}
			}
		}
		Label authorLabel = new Label("Author: " + displayAuthorName);		
		Label infoLabel = new Label( "Category: " + doc.getCategory() + 
				"\nCreated: " + doc.getCreationDate() + "  |  Modified: " + doc.getLastModified());
		infoLabel.setStyle("-fx-text-fill: gray; -fx-font-size: 12px;");
		authorLabel.setStyle("-fx-font-weight: bold;");

		Button closeBtn = new Button("Close");
		closeBtn.setStyle("-fx-font-size: 14px; -fx-cursor: hand;");
		closeBtn.setOnAction(e -> readerStage.close());

		layout.getChildren().addAll(headerBox, authorLabel, infoLabel, contentArea, closeBtn);

		Scene scene = new Scene(layout, 700, 700);
		readerStage.setScene(scene);
		readerStage.showAndWait();
	}


	/**
	 * Creates a bulletproof, independent notification window in the bottom-right corner.
	 */
	private void showToastNotification(String message) {
		javafx.stage.Stage toastStage = new javafx.stage.Stage();
		toastStage.initStyle(javafx.stage.StageStyle.TRANSPARENT);
		toastStage.setAlwaysOnTop(true);

		javafx.scene.control.Label text = new javafx.scene.control.Label(message);
		text.setStyle("-fx-background-color: rgba(0, 0, 0, 0.8); -fx-text-fill: white; "
				+ "-fx-padding: 15px 25px; -fx-font-size: 14px; -fx-font-weight: bold; -fx-background-radius: 8px;");

		javafx.scene.layout.StackPane rootPane = new javafx.scene.layout.StackPane(text);
		rootPane.setStyle("-fx-background-color: transparent; -fx-padding: 10px;"); // Padding for a soft drop shadow if you ever add one

		javafx.scene.Scene scene = new javafx.scene.Scene(rootPane);
		scene.setFill(javafx.scene.paint.Color.TRANSPARENT);
		toastStage.setScene(scene);

		toastStage.setOpacity(0);
		toastStage.show();

		javafx.geometry.Rectangle2D screenBounds = javafx.stage.Screen.getPrimary().getVisualBounds();
		toastStage.setX(screenBounds.getMaxX() - toastStage.getWidth() - 10);
		toastStage.setY(screenBounds.getMaxY() - toastStage.getHeight() - 10);

		toastStage.setOpacity(1);

		javafx.animation.PauseTransition delay = new javafx.animation.PauseTransition(javafx.util.Duration.seconds(4));
		delay.setOnFinished(e -> toastStage.close());
		delay.play();
	}
	/**
	 * @return followed docs tab gui
	 */
	private VBox buildFollowedDocsTab() {
		VBox layout = new VBox(15);
		layout.setPadding(new Insets(15));

		List<Document> followedDocs = new java.util.ArrayList<>();
		for (Document doc : fs.getDocuments()) {
			if (currentUser.getFollowedDocs().containsKey(doc.getDocTitle())) {
				followedDocs.add(doc);
			}
		}

		TableView<Document> table = new TableView<>();
		table.setItems(FXCollections.observableArrayList(followedDocs));

		TableColumn<Document, String> titleCol = new TableColumn<>("Title");
		titleCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getDocTitle()));

		TableColumn<Document, String> catCol = new TableColumn<>("Category");
		catCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getCategory()));

		TableColumn<Document, String> verCol = new TableColumn<>("Current Ver.");
		verCol.setCellValueFactory(data -> new SimpleStringProperty(String.valueOf(data.getValue().getVersion())));

		table.getColumns().add(titleCol);
		table.getColumns().add(catCol);
		table.getColumns().add(verCol);
		table.setPrefHeight(400);

		// Double-click to read
		table.setRowFactory(tv -> {
			javafx.scene.control.TableRow<Document> row = new javafx.scene.control.TableRow<>();
			row.setOnMouseClicked(event -> {
				if (event.getClickCount() == 2 && (!row.isEmpty())) {
					showDocumentReader(row.getItem());
				}
			});
			return row;
		});

		Button btnUntrack = new Button("Stop Following Selected");
		btnUntrack.setStyle("-fx-font-size: 14px; -fx-padding: 8px 15px; -fx-cursor: hand; -fx-text-fill: red;");

		btnUntrack.setOnAction(e -> {
			Document selected = table.getSelectionModel().getSelectedItem();
			if (selected != null) {
				currentUser.getFollowedDocs().remove(selected.getDocTitle());
				table.getItems().remove(selected);
				followedCountLabel.setText("Followed Docs: " + currentUser.getFollowedDocs().size());

			}
		});

		layout.getChildren().addAll(new Label("My Followed Documents"), table, btnUntrack);
		return layout;
	}

	/**
	 * @return notification's tab gui
	 */
	private VBox buildNotificationsTab() {
		VBox layout = new VBox(15);
		layout.setPadding(new Insets(15));

		javafx.scene.control.ListView<String> notifList = new javafx.scene.control.ListView<>();
		notifList.setPrefHeight(400);

		if (currentUser.getNotifications() != null) {
			List<String> reversed = new java.util.ArrayList<>(currentUser.getNotifications());
			java.util.Collections.reverse(reversed);
			notifList.getItems().addAll(reversed);
		}

		Button btnClear = new Button("Clear All Notifications");
		btnClear.setStyle("-fx-font-size: 14px; -fx-padding: 8px 15px; -fx-cursor: hand;");

		btnClear.setOnAction(e -> {
			currentUser.getNotifications().clear();
			notifList.getItems().clear();
		});

		layout.getChildren().addAll(new Label("Update History"), notifList, btnClear);
		return layout;
	}
}