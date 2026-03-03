package docs;

public class Category {
	
	private String catName;
	
	public String getCatName() {
		return catName;
	}

	/**
	 * Constrcuts new category object
	 * @param catName Category's name
	 */
	public Category(int catID, String catName) {
		super();
		this.catName = catName;
	}
	
	/**
	 * Changes category's name
	 * @param catName New category name
	 */
	public void changeCatName(String catName) {
		this.catName = catName;
	}

}
