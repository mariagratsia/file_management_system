package dataManager;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map; // Added import

import org.json.JSONArray;
import org.json.JSONObject;

import docs.Category;
import docs.Document;
import users.Admin;
import users.Author;
import users.Reader;
import users.User;

public class ReadData {
	
	private static final String FOLDER = "medialab";
	private static final String USERS_PATH = "medialab/users.json";
	private static final String DOCS_PATH = "medialab/documents.json";
	private static final String CATS_PATH = "medialab/categories.json";
	
	/**
	 * Loads all data from json files when add starts
	 * + creates default admin if one not found
	 * @param users App's users
	 * @param docs App's docs
	 * @param cats App's categories
	 */
	public static void loadAll(List<User> users, List<Document> docs, List<Category> cats) {
		try {
			if (!Files.exists(Paths.get(FOLDER))) {
				System.out.println("Data folder not found. Starting fresh.");
				initializeDefaultAdmin(users);
				return;
			}

			if (Files.exists(Paths.get(CATS_PATH))) {
				String content = Files.readString(Paths.get(CATS_PATH));
				JSONArray jsonArray = new JSONArray(content);
				
				for (int i = 0; i < jsonArray.length(); i++) {
					String catName = jsonArray.getString(i);
					cats.add(new Category(i, catName)); 
				}
			}

			if (Files.exists(Paths.get(USERS_PATH))) {
				String content = Files.readString(Paths.get(USERS_PATH));
				JSONArray jsonArray = new JSONArray(content);

				for (int i = 0; i < jsonArray.length(); i++) {
					JSONObject obj = jsonArray.getJSONObject(i);

					int id = obj.optInt("userId", i);
					String type = obj.getString("userType");
					String username = obj.getString("username");
					String password = obj.getString("password");
					String firstName = obj.getString("userFirstName"); 
					String lastName = obj.getString("userLastName"); 

					List<String> allowed = new ArrayList<>();
					if(obj.has("allowedCategories")) {
						JSONArray allowedArr = obj.getJSONArray("allowedCategories");
						for (int k = 0; k < allowedArr.length(); k++) {
							allowed.add(allowedArr.getString(k));
						}
					}

					User u;
					if (type.equals("ADMIN")) {
						u = new Admin(id, firstName, lastName, username, password, allowed);
					} else if (type.equals("AUTHOR")) {
						u = new Author(id, firstName, lastName, username, password, allowed);
					} else {
						u = new Reader(id, firstName, lastName, username, password, allowed);
					}
					
					if (obj.has("followedDocs")) {
						JSONObject followedObj = obj.getJSONObject("followedDocs");
						for (String docTitle : followedObj.keySet()) {
							u.followDoc(docTitle, followedObj.getInt(docTitle));
						}
					}

					users.add(u);
				}
			} else {
				initializeDefaultAdmin(users);
			}

			if (Files.exists(Paths.get(DOCS_PATH))) {
				String content = Files.readString(Paths.get(DOCS_PATH));
				JSONArray jsonArray = new JSONArray(content);

				for (int i = 0; i < jsonArray.length(); i++) {
					JSONObject obj = jsonArray.getJSONObject(i);

					int id = obj.optInt("docID", i);
					String title = obj.getString("docTitle");
					String author = obj.getString("authorName");
					String category = obj.getString("category");
					String date = obj.getString("creationDate");
					String text = obj.getString("text");
					int version = obj.getInt("version");
					String lastMod = obj.optString("lastModified", date);

					Map<Integer, String> history = new HashMap<>();
					if (obj.has("docHistory")) {
						JSONObject histObj = obj.getJSONObject("docHistory");
						for (String key : histObj.keySet()) {
							history.put(Integer.parseInt(key), histObj.getString(key));
						}
					}

					Document d = new Document(id, title, author, category, date, text, version, history, lastMod);
					docs.add(d);
				}
			}

			System.out.println("Data loaded successfully.");

		} catch (IOException e) {
			System.err.println("Error reading files: " + e.getMessage());
			e.printStackTrace();
		}
	} 
	
	
	/**
	 * Called during app deploy to create system admin if one not found in user's list
	 * @param users list of all users
	 */
	private static void initializeDefaultAdmin(List<User> users) {
		System.out.println("No users found. Creating default 'medialab' admin.");
		
		List<String> allCats = new ArrayList<>(); 
		Admin admin = new Admin(0, "System", "Admin", "medialab", "medialab_2025", allCats);
		
		users.add(admin);
	}

}