package services;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import docs.Document;
import users.User;

public class Login {

	/**
	 * Checks the provided credentials against the list of all registered users.
	 * @param username The inputted username
	 * @param password The inputted password
	 * @param allUsers The system's current list of all users
	 * @return The logged-in User object if successful, or null if credentials fail.
	 */
	public static User authenticate(String username, String password, List<User> allUsers) {
		for (User u : allUsers) {
			if (u.getUsername().equals(username) && u.getPassword().equals(password)) {
				return u;
			}
		}
		return null; 
	}

	/**
	 * Scans the user's followed documents to see if any have a newer version 
	 * than what the user last saw.
	 * @param currentUser The user who just successfully logged in
	 * @param allDocs The system's current list of all documents
	 * @return A list of alert messages to display in the JavaFX popup
	 */
	public static List<String> checkNotifications(User currentUser, List<Document> allDocs) {
		List<String> alerts = new ArrayList<>();
		Map<String, Integer> followed = currentUser.getFollowedDocs();

		if (followed.isEmpty()) {
			return alerts;
		}

		for (Document doc : allDocs) {
			String title = doc.getDocTitle();
			
			if (followed.containsKey(title)) {
				int lastSeenVersion = followed.get(title);
				int currentSystemVersion = doc.getVersion();
				
				if (currentSystemVersion > lastSeenVersion) {
					alerts.add("The document '" + title + "' has been updated to version " + currentSystemVersion + "!");
					
				}
			}
		}
		
		return alerts;
	}
}