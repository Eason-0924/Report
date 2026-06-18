package system;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * @ClassName admin A model class of admin.
 **/
public class Admin {
	private String username;
	private String password;
	private String created_at;

	public Admin() {
	}

	public Admin(String username, String password) {
		super();
		this.username = username;
		this.password = password;

		// Get the current time.
		LocalDateTime nowDT = LocalDateTime.now();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
		String nowDTString = nowDT.format(formatter);
		System.out.println("Register Time: " + nowDTString);
		
		this.created_at = nowDTString;
	}

	/**
	 * insert() Insert a single admin into the admins table.
	 **/
	public void insert() {
		String sql = "INSERT INTO admins(username, password, created_at) VALUES (?, ?, ?)";
		// Execute SQL statement.
		try (PreparedStatement stmt = StartSystem.db.prepareStatement(sql)) {
			stmt.setString(1, this.getUsername());
			stmt.setString(2, this.getPassword());
			stmt.setString(3, this.getCreated_at());
			stmt.executeUpdate();

			System.out.println("The admin has been inserted.");

		} catch (SQLException se) {
			se.printStackTrace();
		}
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getCreated_at() {
		return created_at;
	}

	public void setCreated_at(String created_at) {
		this.created_at = created_at;
	}
}
