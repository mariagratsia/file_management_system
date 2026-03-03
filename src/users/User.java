package users;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public abstract class User {

	private int userId;
	private String userFirstName; 
	private String userLastName; 
	private String username;
	private String userType;

	private String password; 
	private List<String> userCategories;     
	private Map<String, Integer> followedDocs;
	
	protected java.util.List<String> notifications = new java.util.ArrayList<>();

	
	/**
	 * @param userId User's unique Id, defined by the system
	 * @param userFirstName User's first name
	 * @param userLastName User's last name
	 * @param username User's username, this is gonna appear publically in the app
	 * @param password User's password set by the Admin
	 * @param userType READER, AUTHOR, ADMIN
	 * @param userCategories Categories assigned to user by Admin during sign up
	 */
	public User(int userId, String userFirstName, String userLastName, String username, String password, String userType, List<String> userCategories) {
		this.userId = userId;
		this.userFirstName = userFirstName;
		this.userLastName = userLastName;
		this.username = username;
		this.userType = userType;
		this.password = password;


		this.userCategories = (userCategories != null) ? userCategories : new ArrayList<>();
		this.followedDocs = new HashMap<>(); 
	}

	/**
	 * @return userid
	 */
	public int getUserId() {
		return userId;
	}


	/**
	 * @return userFirstName
	 */
	public String getUserFirstName() {
		return userFirstName;
	}


	/**
	 * @return userLastName
	 */
	public String getUserLastName() {
		return userLastName;
	}


	/**
	 * @return username
	 */
	public String getUsername() {
		return username;
	}

	/**
	 * @return password
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * @param password User's password set by Admin
	 */
	public void setPassword(String password) {
		this.password = password;
	}

	/**
	 * @return userType
	 */
	public String getUserType() {
		return userType;
	}

	/**
	 * @param userType userType
	 */
	public void setUserType(String userType) {
		this.userType = userType;
	}


	/**
	 * @return list of userCategories
	 */
	public List<String> getUserCategories() {
		return userCategories;
	}


	/**
	 * Add a category to getUserCategories 
	 * @param category Category name
	 */
	public void addUserToCategory(String category) {
		this.userCategories.add(category);
	}


	/**
	 * @return followedDocs
	 */
	public Map<String, Integer> getFollowedDocs() {
		return followedDocs;
	}

	/**
	 * User follows a doc
	 * @param docTitle Title of document  user want to follow
	 * @param currentVersion Current version of doc 
	 */
	public void followDoc(String docTitle, int currentVersion) {
		this.followedDocs.put(docTitle, currentVersion);
	}

	/**
	 * User unfollows a doc
	 * @param doc Title of document  user want to unfollow 
	 */
	public void unfollowDoc(String doc) {
		this.followedDocs.remove(doc);
	}
	
	/**
	 * Called when admin changes a category name
	 * @param oldCat is old category name
	 * @param newCat is new category name
	 */
	public void replaceUserCategory(String oldCat, String newCat) {
		if (this.userCategories != null && this.userCategories.contains(oldCat)) {
			this.userCategories.remove(oldCat);
			this.userCategories.add(newCat);
		}
	}
	
	/**
	 * @return notification list
	 */
	public java.util.List<String> getNotifications() {
		return notifications;
	}


	/**
	 * Adds a notification until there are 15 if them, then it deletes the older
	 * @param message Notification message
	 */
	public void addNotification(String message) {
		this.notifications.add(message);
		while (this.notifications.size() > 15) {
			this.notifications.remove(0);
		}
	}


}
