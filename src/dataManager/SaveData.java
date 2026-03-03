package dataManager;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import docs.Category;
import docs.Document;
import users.User;

public class SaveData {

	private static final String FOLDER = "medialab";
	private static final String USERS_PATH = "medialab/users.json";
	private static final String DOCS_PATH = "medialab/documents.json";
	private static final String CATS_PATH = "medialab/categories.json";

	/**
	 * Saves all app data when app is closed and updates all json files
	 * @param users App's users
	 * @param docs App's docs
	 * @param cats App's categories
	 */
	public static void saveAll(List<User> users, List<Document> docs, List<Category> cats) {
		try {
			if (!Files.exists(Paths.get(FOLDER))) {
				Files.createDirectories(Paths.get(FOLDER));
			}

			JSONArray catsArray = new JSONArray();
			for (Category cat : cats) {
				catsArray.put(cat.getCatName());
			}
			Files.writeString(Paths.get(CATS_PATH), catsArray.toString(4));


			JSONArray usersArray = new JSONArray();
			for (User u : users) {
				JSONObject userObj = new JSONObject();
				
				userObj.put("userId", u.getUserId());
				userObj.put("userType", u.getUserType()); 
				userObj.put("username", u.getUsername());
				userObj.put("password", u.getPassword());
				userObj.put("userFirstName", u.getUserFirstName());
				userObj.put("userLastName", u.getUserLastName());

				JSONArray allowedCats = new JSONArray();
				for (String cat : u.getUserCategories()) {
					allowedCats.put(cat);
				}
				userObj.put("allowedCategories", allowedCats);

				JSONObject followedDocs = new JSONObject();
				for (Map.Entry<String, Integer> entry : u.getFollowedDocs().entrySet()) {
					followedDocs.put(entry.getKey(), entry.getValue());
				}
				userObj.put("followedDocs", followedDocs);

				usersArray.put(userObj);
			}
			Files.writeString(Paths.get(USERS_PATH), usersArray.toString(4));


			JSONArray docsArray = new JSONArray();
			for (Document d : docs) {
				JSONObject docObj = new JSONObject();
				
				docObj.put("docID", d.getDocID());
				docObj.put("docTitle", d.getDocTitle());
				docObj.put("authorName", d.getAuthorName());
				docObj.put("category", d.getCategory());
				docObj.put("creationDate", d.getCreationDate());
				docObj.put("text", d.getText());
				docObj.put("version", d.getVersion());
				docObj.put("lastModified", d.getLastModified());

				JSONObject historyObj = new JSONObject();
				for (Map.Entry<Integer, String> entry : d.getDocHistory().entrySet()) {
					historyObj.put(String.valueOf(entry.getKey()), entry.getValue());
				}
				docObj.put("docHistory", historyObj);

				docsArray.put(docObj);
			}
			Files.writeString(Paths.get(DOCS_PATH), docsArray.toString(4));

			System.out.println("All data saved successfully to the medialab folder.");

		} catch (IOException e) {
			System.err.println("Critical Error saving data: " + e.getMessage());
			e.printStackTrace();
		}
	}
}