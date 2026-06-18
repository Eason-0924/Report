package system;

import login.Login;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import librarySystem.LibrarySystemAdmin;
import librarySystem.LibrarySystemUser;

/**
 *  @ClassName    start
 *	Start the Library management system.
 **/
public class StartSystem {
	
	// A feild that shares a connection to the database.
	public static Connection db = null;
	
	public static void main(String[] args) {
		start();

		//jsonToSQL.usersInit();
		//jsonToSQL.adminsInit();
		JsonToSQL.reservationsReset();
		JsonToSQL.reviewsReset();
		JsonToSQL.borrowRecordsReset();
		JsonToSQL.userReset();
		JsonToSQL.booksInit();
		JsonToSQL.borrowRecordsInit();
		
		/* login loginPage = */new Login();
		//close();
	}

	/**
	 *  Connect to the database.
	 *
	 *  @return The Connection to database
	 **/
	public static void start() {
		// Imformations of the database.
		String URL = "jdbc:mysql://localhost:3306/data?serverTimezone=Asia/Taipei";
		String USER = "root";
		String PASSWORD = "eason940924";
		
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
			AppSettings.initialize();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 *  Close the connection if it exists.
	 *
	 *  @param db The connection to the database.
	 **/
	public static void close() {
		if (StartSystem.db != null) {
			try {
				db.close();
				System.out.println("Closed connection to data.");
			} catch (SQLException se) {
				se.printStackTrace();
			}
		}
	}
	
	public static void launchSystemUser(int user_id) {
		new LibrarySystemUser(user_id);

	}

	public static void launchSystemAdmin(int admin_id) {
		new LibrarySystemAdmin(admin_id);
	}
}
