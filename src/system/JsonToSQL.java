package system;

import java.io.File;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @ClassName jsonToSQL Import .json files to initialize table in MySQL.
 **/
public class JsonToSQL {
	// Imformations of the database.
	public static final String URL = "jdbc:mysql://localhost:3306/data?serverTimezone=Asia/Taipei";
	public static final String USER = "root";
	public static final String PASSWORD = "eason940924";

	/**
	 * Initialize the users table.
	 **/
	public static void usersInit() {
		try {
			// Use mapper to read the Users.json file.
			ObjectMapper mapper = new ObjectMapper();
			List<User> usersList = mapper.readValue(new File("lib/使用者資料/Users.json"), new TypeReference<List<User>>() {
			});

			// Clear users table.
			String clear = "DELETE FROM users";
			try (Statement stmt = StartSystem.db.createStatement()) {
				stmt.executeUpdate(clear);
				System.out.println("Clear users table");
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

	public static void adminsReset() {
		// Clear admins table except default.
		String delete = "DELETE FROM admins WHERE username != 'default'";
		try (Statement stmt = StartSystem.db.createStatement()) {
			stmt.executeUpdate(delete);
			System.out.println("Clear admins table.");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Initialize the books table.
	 **/
	public static void booksInit() {
		try {
			// Use mapper to read the Books.json file.
			ObjectMapper mapper = new ObjectMapper();
			List<Book> booksList = mapper.readValue(new File("lib/書籍資料/Books.json"), new TypeReference<List<Book>>() {
			});

				// Clear books table.
				String clear = "DELETE FROM books";
				try (Statement stmt = StartSystem.db.createStatement()) {
					stmt.executeUpdate(clear);
					System.out.println("Clear books table");
				}
				
				String reset = "ALTER TABLE books AUTO_INCREMENT = 1";
				try (Statement stmt = StartSystem.db.createStatement()) {
					stmt.executeUpdate(reset);
					System.out.println("Reset auto increment in books table.");
				}
	
				// Insert each book.
				String sql = "INSERT INTO books(title, authors, subjects, publisher, publish_year, edition, format_desc, source, isbn, note) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
			try (PreparedStatement stmt = StartSystem.db.prepareStatement(sql)) {
				int count = 0;
				for (Book b : booksList) {
					stmt.setString(1, b.getTitle());
					// Convert List to String.
					stmt.setString(2, String.join(", ", b.getAuthors()));
					stmt.setString(3, String.join(", ", b.getSubjects()));
					stmt.setString(4, b.getPublisher());
					stmt.setString(5, b.getPublish_year());
					stmt.setString(6, b.getEdition());
					stmt.setString(7, b.getFormate_desc());
					stmt.setString(8, b.getSource());
					stmt.setString(9, String.join(", ", b.getIsbn()));
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
	
	public static void booksUpdate() {
		try {
			// Use mapper to read the Books.json file.
			ObjectMapper mapper = new ObjectMapper();
			List<Book> booksList = mapper.readValue(new File("lib/書籍資料/Books.json"), new TypeReference<List<Book>>() {
			});
			
			// Update each user.
			String sql = "UPDATE books SET authors = ?, subjects = ?, isbn = ? WHERE title = ?";
			try (PreparedStatement stmt = StartSystem.db.prepareStatement(sql)) {
				int count = 0;
				for (Book b : booksList) {
					// Convert List to String.
					stmt.setString(1, String.join(", ", b.getAuthors()));
					stmt.setString(2, String.join(", ", b.getSubjects()));
					stmt.setString(3, String.join(", ", b.getIsbn()));
					stmt.setString(4, b.getTitle());
					
					stmt.executeUpdate();
					count++;
				}
				System.out.println("All " + count + " books are updated.");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Initialize the borrow_records table.
	 **/
	public static void borrowRecordsInit() {
		try {
			// Use mapper to read the Books.json file.
			ObjectMapper mapper = new ObjectMapper();
			List<BorrowRecord> borrowRecordsList = mapper.readValue(new File("lib/借還紀錄資料/Borrow_records.json"),
					new TypeReference<List<BorrowRecord>>() {
					});

			// Clear borrow_records table.
			String clear = "DELETE FROM borrow_records";
			try (Statement stmt = StartSystem.db.createStatement()) {
				stmt.executeUpdate(clear);
				System.out.println("Clear borrow_records table");
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

	public static void borrowRecordsReset() {
		resetTable("borrow_records", "borrow_records");
	}
	
	public static void userReset() {
		// Delete new inserted users.
		String delete = "DELETE FROM users WHERE user_id > 20";
		try (Statement stmt = StartSystem.db.createStatement()) {
			stmt.executeUpdate(delete);
			System.out.println("Reset newly registered users.");
			
			String reset = "ALTER TABLE users AUTO_INCREMENT = 21";
			try (Statement stmt2 = StartSystem.db.createStatement()) {
				stmt2.execute(reset);
				System.out.println("Reset auto increment in users table.");
			}
			
			String resetSus = "UPDATE users SET suspended_until = NULL WHERE 1=1";
			try (Statement stmt3 = StartSystem.db.createStatement()) {
				stmt3.execute(resetSus);
				System.out.println("Reset suspended end dates for all users.");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void reservationsReset() {
		resetTable("reservations", "reservations");
	}

	public static void reviewsReset() {
		resetTable("reviews", "reviews");
	}

	private static void resetTable(String tableName, String displayName) {
		String sql = "DELETE FROM " + tableName;
		try (Statement statement = StartSystem.db.createStatement()) {
			statement.executeUpdate(sql);
			System.out.println("Reset " + displayName + " table.");
		} catch (Exception exception) {
			exception.printStackTrace();
		}
	}
}
