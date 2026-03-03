package users;

import java.util.List;

public class Admin extends User {

	/**
	 * @param userId User's unique Id, defined by the system
	 * @param userFirstName User's first name
	 * @param userLastName User's last name
	 * @param username User's username, this is gonna appear publically in the app
	 * @param password User's password set by the Admin
	 * @param userCategories Categories assigned to user by Admin during sign up
	 */
	public Admin(int userId, String userFirstName, String userLastName, String username, String password, List<String> userCategories) {
		super(userId, userFirstName, userLastName, username, password ,"ADMIN", userCategories);
		// TODO Auto-generated constructor stub
	}

}
