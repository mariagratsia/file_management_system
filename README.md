# Document Management System (DMS)

A real-time Desktop Application built with JavaFX for managing documents, user roles, and category permissions. The system features a modern, responsive UI with smart notifications, document tracking, and secure Role-Based Access Control (RBAC).

## Key Features

* **Role-Based Access Control (RBAC):** Three distinct user tiers (Admin, Author, Reader) with dynamically shifting GUI permissions.
* **Live Document Tracking:** Users can "Follow" documents to track changes. The UI updates in real-time with visual "FOLLOWING" badges and live counter statistics.
* **Smart Notifications:** A custom, dark-mode "Toast" notification system that alerts users to updates or deletions of followed documents the moment they log in.
* **Cascading Updates:** Admin actions (like renaming or deleting a category) automatically cascade through the system, updating document tags and user permissions globally without breaking dependencies.
* **Modern UI/UX:** Features uniform styling, centered layouts, double-click-to-read functionality, and context-aware buttons that lock/unlock based on selection and user permissions.

## Tech Stack

* **Language:** Java 21
* **GUI Framework:** JavaFX 21
* **Data Serialization:** JSON
* **IDE:** Eclipse

## Data Persistence & Architecture

The backend completely separates logic from the GUI. Data is permanently serialized into three JSON files:

1. `users.json`: Stores credentials, roles, allowed categories, notification history, and followed document trackers.
2. `documents.json`: Stores document metadata (Title, Category, Author, Version, Content).
3. `categories.json`: Acts as the master dictionary for system categories.

Exit and save happens automatically when the app closes. All data saved successfully to the `medialab` folder.

## User Roles & Permissions

* **Admin:** Absolute control. Can read, edit, or delete *any* document in the system. Can manage user profiles (create other Admins, change passwords, assign category access) and manage system-wide categories.
* **Author:** Can create new documents and fully manage (edit/delete) their own documents. They have Read-Only access to documents created by other authors.
* **Reader:** Strictly Read-Only access. Can browse allowed categories, read documents, and utilize the Follow/Notification tracking system.

## How to Run the Project

1. **Clone the repository:**
```bash
git clone https://github.com/YourUsername/YourRepoName.git

```

2. **Import to Eclipse:**
* Open Eclipse IDE.
* Go to `File` > `Import` > `Existing Projects into Workspace`.
* Select the cloned repository folder. The included `.project` and `.classpath` files will automatically configure the workspace.

3. **Setup JavaFX:**
* Ensure JavaFX 21 is added to your module path/build path.
* If using VM arguments, ensure `--module-path` and `--add-modules javafx.controls,javafx.fxml` are configured.

4. **Launch:**
* Run `Main.java` to start the application.

---

*Developed as a University Multimedia Technologies Project.*

---

