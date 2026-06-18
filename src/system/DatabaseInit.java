package system;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public class DatabaseInit {
	// Imformations of the database.
	public static final String URL = "jdbc:mysql://localhost:3306/data?serverTimezone=Asia/Taipei";
	public static final String USER = "root"; // TODO 改為自己設定的使用者名稱及密碼
	public static final String PASSWORD = "eason940924";

	public static void main(String[] args) {
		Connection db;
		try {
			// Load driver.
			System.out.print("Loading driver... ");
			Class.forName("com.mysql.cj.jdbc.Driver");
			System.setProperty("jdbs.drivers", "com.mysql.jdbc.Driver");
			System.out.println("Succeed!");

			// Connect database.
			System.out.print("Establishing connection... ");
			db = DriverManager.getConnection(URL, USER, PASSWORD);
			System.out.println("Succeed!");

			usersInit(db);
			adminsInit(db);
			booksInit(db);
			borrowRecordsInit(db);

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * Initialize the users table.
	 **/
	public static void usersInit(Connection db) {
		try {
			// Use mapper to read the Users.json file.
			ObjectMapper mapper = new ObjectMapper();
			List<User> usersList = mapper.readValue(new File("lib/使用者資料/Users.json"), new TypeReference<List<User>>() {
			});

			// Create users table.
			String create = "CREATE TABLE users(" 
					+ "user_id INT PRIMARY KEY AUTO_INCREMENT,\n"
					+ "student_no VARCHAR(20),\n" 
					+ "name VARCHAR(50),\n" 
					+ "password VARCHAR(255),\n"
					+ "role_level ENUM(\"NORMAL\", \"VIP\"),\n" 
					+ "created_at DATETIME,\n"
					+ "status ENUM(\"ACTIVE\", \"SUSPENDED\"),\n"
					+ "suspended_until DATE DEFAULT NULL)";
			try (Statement stmt = db.createStatement()) {
				stmt.executeUpdate(create);
				System.out.println("Create users table");
			}

			// Insert each user.
			String sql = "INSERT INTO users(student_no, name, password, role_level, created_at, status) VALUES (?, ?, ?, ?, ?, ?)";
			try (PreparedStatement stmt = StartSystem.db.prepareStatement(sql)) {
				int count = 0;
				for (User u : usersList) {
					stmt.setString(1, u.getStudent_no());
					stmt.setString(2, u.getName());
					stmt.setString(3, u.getPassword());
					stmt.setString(4, u.getRole_level().name());
					stmt.setString(5, u.getcreated_at());
					stmt.setString(6, u.getStatus().name());
					stmt.executeUpdate();
					count++;
				}
				System.out.println("All " + count + " users are inserted.");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void adminsInit(Connection db) {
		try {
			// Create admins table.
			String create = "CREATE TABLE admins("
					+ "admin_id INT PRIMARY KEY AUTO_INCREMENT,\n"
					+ "username VARCHAR(50),\n"
					+ "password VARCHAR(255),\n"
					+ "created_at DATETIME)";
			try (Statement stmt = db.createStatement()) {
				stmt.executeUpdate(create);
				System.out.println("Create users table");
			}

			// Clear admins table except default.
			String delete = "DELETE FROM admins WHERE username != 'default'";
			try (Statement stmt = StartSystem.db.createStatement()) {
				stmt.executeUpdate(delete);
				System.out.println("Clear admins table.");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * Initialize the books table.
	 **/
	public static void booksInit(Connection db) {
		try {
			// Use mapper to read the Books.json file.
			ObjectMapper mapper = new ObjectMapper();
			List<Book> booksList = mapper.readValue(new File("lib/書籍資料/Books.json"), new TypeReference<List<Book>>() {
			});

			// Create admins table.
			String create = "CREATE TABLE books("
					+ "book_id INT PRIMARY KEY AUTO_INCREMENT,\n"
					+ "title VARCHAR(255),\n"
					+ "authors VARCHAR(255),\n"
					+ "subjects VARCHAR(255),\n"
					+ "publisher VARCHAR(255),\n"
					+ "publish_year VARCHAR(10),\n"
					+ "edition VARCHAR(50),\n"
					+ "format_desc VARCHAR(100),\n"
					+ "source VARCHAR(100),\n"
					+ "isbn VARCHAR(255),\n"
					+ "note TEXT)";
			try (Statement stmt = db.createStatement()) {
				stmt.executeUpdate(create);
				System.out.println("Create books table");
			}

			// Insert each user.
			String sql = "INSERT INTO books(title, authors, subjects, publisher, publish_year, edition, format_desc, source, isbn, note) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
			try (PreparedStatement stmt = StartSystem.db.prepareStatement(sql)) {
				int count = 0;
				for (Book b : booksList) {
					stmt.setString(1, b.getTitle());
					// Convert List to String.
					stmt.setString(2, String.join(",", b.getAuthors()));
					stmt.setString(3, String.join(",", b.getSubjects()));
					stmt.setString(4, b.getPublisher());
					stmt.setString(5, b.getPublish_year());
					stmt.setString(6, b.getEdition());
					stmt.setString(7, b.getFormate_desc());
					stmt.setString(8, b.getSource());
					stmt.setString(9, String.join(",", b.getIsbn()));
					stmt.setString(10, b.getNote());

					stmt.executeUpdate();
					count++;
				}
				System.out.println("All " + count + " books are inserted.");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Initialize the borrow_records table.
	 **/
	public static void borrowRecordsInit(Connection db) {
		try {
			// Use mapper to read the Books.json file.
			ObjectMapper mapper = new ObjectMapper();
			List<BorrowRecord> borrowRecordsList = mapper.readValue(new File("lib/借還紀錄資料/Borrow_records.json"),
					new TypeReference<List<BorrowRecord>>() {
					});

			// Create books_records table.
			String create = "CREATE TABLE borrow_records("
					+ "record_id INT AUTO_INCREMENT PRIMARY KEY,\n"
					+ "user_id INT,\n"
					+ "book_id INT,\n"
					+ "borrow_date DATETIME, \n"
					+ "due_date DATETIME,\n"
					+ "return_date DATETIME,\n"
					+ "borrow_days INT,\n"
					+ "created_at DATETIME,\n"
					+ "FOREIGN KEY (user_id) REFERENCES users(user_id),\n"
					+ "FOREIGN KEY (book_id) REFERENCES books(book_id))";
			try (Statement stmt = db.createStatement()) {
				stmt.executeUpdate(create);
				System.out.println("Create books table");
			}

			
			// Insert each borrow record.
			String sql = "INSERT INTO borrow_records(user_id, book_id, borrow_date, due_date, return_date, borrow_days, created_at) VALUES (?, ?, ?, ?, ?, ?, ?)";
			try (PreparedStatement stmt = StartSystem.db.prepareStatement(sql)) {
				int count = 0;
				for (BorrowRecord b : borrowRecordsList) {
					stmt.setString(1, String.valueOf(b.getUser_id()));
					stmt.setString(2, String.valueOf(b.getBook_id()));
					stmt.setString(3, b.getBorrow_date());
					stmt.setString(4, b.getDue_date());
					stmt.setString(5, b.getReturn_date());
					stmt.setString(6, String.valueOf(b.getBorrow_days()));
					stmt.setString(7, b.getCreated_at());
					stmt.executeUpdate();
					count++;
				}
				System.out.println("All " + count + " borrow records are inserted.");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
