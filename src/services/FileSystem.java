package services;

import java.util.ArrayList;
import java.util.List;

import docs.Category;
import docs.Document;
import users.User;

// holds app state will running
public class FileSystem {

	// this is current state of the app
	private List<User> Users;
	private List<Category> Categories;
	private List<Document> Documents;

	/**
	 * Default constructor
	 */
	public FileSystem() {
		this.Users = new ArrayList<>();
		this.Documents = new ArrayList<>();
		this.Categories = new ArrayList<>();
	}

	/**
	 * @return list of all users
	 */
	public List<User> getUsers() {
		return Users;
	}
	/**
	 * Used during load data, populates app's user's list
	 * @param users All app users
	 */
	public void setUsers(List<User> users) {
		Users = users;
	}
	/**
	 * @return list of all categories
	 */
	public List<Category> getCategories() {
		return Categories;
	}
	/**
	 * Used during load data, populates app's categories's list
	 * @param categories All categories
	 */
	public void setCategories(List<Category> categories) {
		Categories = categories;
	}
	/**
	 * @return list of all docs
	 */
	public List<Document> getDocuments() {
		return Documents;
	}
	/**
	 * Used during load data, populates app's user's list
	 * @param documents list of all docs
	 */
	public void setDocuments(List<Document> documents) {
		Documents = documents;
	}

	// functions needed to run the files system logic
	

	/**
	 * Adds new user to app's user list
	 * @param newUser New user object 
	 */
	public void addUser(User newUser) {
		this.Users.add(newUser);
	}

	/**
	 * Removes user + adds deleted tag to former user's docs 
	 * @param user User object to be deleted
	 */
	public void removeUser(User user) {
		String deletedUsername = user.getUsername();
		this.Users.remove(user);
		for (Document doc : Documents) {
			if (doc.getAuthorName().equals(deletedUsername)) {
				doc.setAuthorName(deletedUsername + " (Deleted Account)");
			}
		}
	}

	
	/**
	 * Add new category 
	 * @param newCategory New category's name
	 */
	public void addCategory(Category newCategory) {
		this.Categories.add(newCategory);
	}



	/**
	 * Deletes a category from category list 
	 * + deletes all docs belonging to that category 
	 * + deletes docs from user's follow list 
	 * + sends a notification for deleted docs
	 * @param category Category to be deleted
	 */
	public void deleteCategory(Category category) {
		String catName = category.getCatName();

		this.Categories.remove(category);

		List<Document> docsToDelete = new java.util.ArrayList<>();
		for (Document doc : Documents) {
			if (doc.getCategory().equals(catName)) {
				docsToDelete.add(doc);
			}
		}
		this.Documents.removeAll(docsToDelete);
		String today = java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"));
		for (User user : Users) {
			user.getUserCategories().remove(catName);
			for (Document deletedDoc : docsToDelete) {
				String docTitle = deletedDoc.getDocTitle();
				if (user.getFollowedDocs().containsKey(docTitle)) {
					user.addNotification("[" + today + "] Category Deleted: '" + docTitle + "' was removed because its category (" + catName + ") was deleted.");
					user.getFollowedDocs().remove(docTitle);
				}
			}
		}
	}


	/**
	 * @param doc Document object
	 */
	public void addDocument(Document doc) {
		this.Documents.add(doc);
	}

	/**
	 * Deletes doc + removes it from user's following list + sends notification
	 * @param doc Document object to be deleted
	 */
	public void deleteDocument(Document doc) {
		String title = doc.getDocTitle();
		String today = java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"));

		this.Documents.remove(doc);

		for (User user : Users) {
			if (user.getFollowedDocs().containsKey(title)) {
				user.addNotification("[" + today + "] Document Deleted: The document '" + title + "' you were following was removed.");
				user.getFollowedDocs().remove(title);
			}
		}
	}

	/**
	 * renames a category and updates all documents and users attached to it
	 * @param category old category name
	 * @param newName new category name
	 */

	public void modifyCategoryName(Category category, String newName) {
		String oldName = category.getCatName();
		
		category.changeCatName(newName);
		
		for (Document doc : Documents) {
			if (doc.getCategory().trim().equalsIgnoreCase(oldName.trim())) {
				doc.setCategory(newName);
			}
		}
		
		for (User user : Users) {
			List<String> userCats = user.getUserCategories();
			if (userCats != null) {
				for (int i = 0; i < userCats.size(); i++) {
					if (userCats.get(i).trim().equalsIgnoreCase(oldName.trim())) {
						userCats.set(i, newName);
					}
				}
			}
		}
	}
}
