package docs;

import java.util.HashMap;
import java.util.Map;

public class Document {

	private final int docID;
	private static int nextId = 1000;
	private final String docTitle;
	private String authorName;
	private String category;
	private final String creationDate;

	private int version;
	private String lastModified;
	private String text;

	private Map<Integer, String> docHistory;


	/**
	 * create new doc, version 1
	 * @param docTitle Document title
	 * @param authorName Author's username
	 * @param category Doc's category, set by author
	 * @param text the actual document 
	 */
	public Document(String docTitle, String authorName, String category, String text) {
		super();
		this.docID = nextId++; 
		this.docTitle = docTitle;
		this.authorName = authorName;
		this.category = category;
		this.text = text;

		String today = java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"));
		this.creationDate = today;
		this.lastModified = today;

		this.version = 1;
		this.docHistory = new HashMap<>();
	}

	/**
	 * load old doc from json during app initialize
	 * @param docID Doc's unique system id
	 * @param docTitle Document title
	 * @param authorName Author's username
	 * @param category Doc's category, set by author
	 * @param creationDate Date the doc was created
	 * @param text the actual document 
	 * @param version Doc's version
	 * @param docHistory Last two versions of the doc (if they exist) 
	 * @param lastModified When last got updated. If it's version 1, this is creation date
	 */
	public Document(int docID, String docTitle, String authorName, String category, String creationDate, String text,
			int version, Map<Integer, String> docHistory, String lastModified) {
		super();
		this.docID = docID;
		this.docTitle = docTitle;
		this.authorName = authorName;
		this.category = category;
		this.creationDate = creationDate;
		this.text = text;
		this.version = version;
		this.lastModified = lastModified;
		this.docHistory = docHistory;

		if (docID >= nextId) {
			nextId = docID + 1;
		}
	}


	/**
	 * @return document's system id
	 */
	public int getDocID() {
		return docID;
	}

	/**
	 * @return docTitle
	 */
	public String getDocTitle() {
		return docTitle;
	}
	/**
	 * @return authorName Author's username
	 */
	public String getAuthorName() {
		return authorName;
	}
	/**
	 * @return category the doc belongs to
	 */
	public String getCategory() {
		return category;
	}
	/**
	 * @return creation Date 
	 */
	public String getCreationDate() {
		return creationDate;
	}
	/**
	 * @return current version
	 */
	public int getVersion() {
		return version;
	}
	/**
	 * @return date the doc was last modified
	 */
	public String getLastModified() {
		return lastModified;
	}
	/**
	 * @return doc's text
	 */
	public String getText() {
		return text;
	}
	/**
	 * @return last two versions of the doc (if they exist) 
	 */
	public Map<Integer, String> getDocHistory() {
		return docHistory;
	}

	/**
	 * Doc update: save current version in history list
	 * + delete older versions if we have over 3
	 * + upadete doc's object text
	 * + increase version
	 * @param newText updated doc text
	 * @param date Date that got modified
	 */
	public void updateDoc(String newText, String date) {
		docHistory.put(this.version, this.text);

		if (docHistory.size() > 2) {
			docHistory.remove(this.version - 2); 
		}
		this.text = newText;
		this.version++;
		this.lastModified = date;
	}

	/**
	 * Pull older version from history
	 * @param oldVersion verison nummber
	 * @return doc object with the specific version
 	 */
	public String pullOldVersion(int oldVersion) {
		return docHistory.get(oldVersion);
	}
	/**
	 * Updates category name only when category's name is changed
	 * Doc's category cannot be modified by any user
	 * @param newName Category name
	 */
	public void setCategory(String newName) {
		this.category = newName;

	}

	/**
	 * Used only when an author user is deleted
	 * @param authorName usename with tag deleted
	 */
	public void setAuthorName(String authorName) {
		this.authorName = authorName;		
	}




}
