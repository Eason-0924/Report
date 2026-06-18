package system;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * @ClassName user 
 * A model class of user.
 **/
public class User {

	private String student_no;
	private String name;
	private String password;
	private Role_level role_level;
	private String created_at;
	private Status status;
	private String suspended_until;

	public User() {}
	
	public User(String student_no, String name, String password, String level) {
		// Get the current time.
		LocalDateTime now = LocalDateTime.now();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
		String formatTime = now.format(formatter);
		System.out.println("Register Time: " + formatTime);
		
		this.student_no = student_no;
		this.name = name;
		this.password = password;
		this.role_level = Role_level.valueOf(level);
		this.created_at = formatTime;
		this.status = Status.ACTIVE;
		this.suspended_until = null;
	}

	public User(String student_no, String name, String password, Role_level role_level, String created_at, Status status) {
		this(student_no, name, password, role_level, created_at, status, null);
	}

	public User(
			String student_no,
			String name,
			String password,
			Role_level role_level,
			String created_at,
			Status status,
			String suspended_until) {
		super();
		this.student_no = student_no;
		this.name = name;
		this.password = password;
		this.role_level = role_level;
		this.created_at = created_at;
		this.status = status;
		this.suspended_until = suspended_until;
	}

	/**
	 *  insert()
	 *  Insert a single user into the users table.
	 **/
	public void insert() {
		String sql = "INSERT INTO users(student_no, name, password, role_level, created_at, status) VALUES (?, ?, ?, ?, ?, ?)";
		// Execute SQL statement.
		try (PreparedStatement stmt = StartSystem.db.prepareStatement(sql)) {
			stmt.setString(1, this.getStudent_no());
			stmt.setString(2, this.getName());
			stmt.setString(3, this.getPassword());
			stmt.setString(4, this.getRole_level().name());
			stmt.setString(5, this.getcreated_at());
			stmt.setString(6, this.getStatus().name());
			stmt.executeUpdate();
			
			System.out.println("The user has been inserted.");

		} catch (SQLException se) {
			se.printStackTrace();
		}
	}
	
	public String getStudent_no() {return student_no;}
	
	public void setStudent_no(String student_no) {this.student_no = student_no;}

	public String getName() {return name;}

	public void setName(String name) {this.name = name;}

	public String getPassword() {return password;}

	public void setPassword(String password) {this.password = password;}

	public Role_level getRole_level() {return role_level;}

	public void setRole_level(Role_level role_level) {this.role_level = role_level;}

	public String getcreated_at() {return created_at;}

	public void setcreated_at(String created_at) {this.created_at = created_at;}

	public Status getStatus() {return status;}

	public void setStatus(Status status) {this.status = status;}

	public String getSuspended_until() {return suspended_until;}

	public void setSuspended_until(String suspended_until) {
		this.suspended_until = suspended_until;
	}
	
	public enum Role_level {
		NORMAL, VIP
	}
	
	public enum Status {
		ACTIVE, SUSPENDED
	}
}
