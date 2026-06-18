package system;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 *  @ClassName    borrowRecord
 *	A model class of borrow record.
 **/
public class BorrowRecord {
	private int user_id;
	private int book_id;
	private String borrow_date;
	private String due_date;
	private String return_date;
	private int borrow_days;
	private String created_at;
	
	public BorrowRecord() {}
	
	@JsonCreator
	public BorrowRecord(
			@JsonProperty("user_id") int user_id, 
			@JsonProperty("book_id") int book_id, 
			@JsonProperty("borrow_date") String borrow_date, 
			@JsonProperty("due_date") String due_date, 
			@JsonProperty("return_date") String return_date, 
			@JsonProperty("borrow_days") int borrow_days, 
			@JsonProperty("created_at") String created_at) {
		super();
		// Get the current time.
		LocalDateTime nowDT = LocalDateTime.now();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

		String[] borrowStrings = borrow_date.split(" ");
		String[] dueStrings= due_date.split(" ");
		String[] createdStrings = created_at.split(" ");
		
		this.user_id = user_id;
		this.book_id = book_id;
		this.borrow_date = nowDT.plusDays(Integer.parseInt(borrowStrings[0])).format(formatter);
		this.due_date = nowDT.plusDays(Integer.parseInt(dueStrings[0])).format(formatter);
		this.borrow_days = borrow_days;
		this.created_at = nowDT.plusDays(Integer.parseInt(createdStrings[0])).format(formatter);
		
		if (return_date != null) {
			String[] returnStrings = return_date.split(" ");
			this.return_date = nowDT.plusDays(Integer.parseInt(returnStrings[0])).format(formatter);
		} else {
			this.return_date = null;
		}
	}
	
	public BorrowRecord(int user_id, int book_id, String borrow_date, int borrow_days) {
		// Get the current time.
		LocalDateTime nowDT = LocalDateTime.now();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
		String nowDTString = nowDT.format(formatter);
		System.out.println("Borrow Record Time: " + nowDTString);
		
		// Compute due_date.
		LocalDateTime borrowDT = LocalDateTime.parse(borrow_date, formatter);
		LocalDateTime dueDT = borrowDT.plusDays(borrow_days);
		String dueDTString = dueDT.format(formatter);
		
		super();
		this.user_id = user_id;
		this.book_id = book_id;
		this.borrow_date = borrow_date;
		this.due_date = dueDTString;
		this.return_date = null;
		this.borrow_days = borrow_days;
		this.created_at = nowDTString;
	}
	
	public BorrowRecord(int user_id, int book_id, int borrow_days) {
		// Get the current time.
		LocalDateTime nowDT = LocalDateTime.now();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
		String nowDTString = nowDT.format(formatter);
		System.out.println("Borrow Record Time: " + nowDTString);
		
		// Compute due_date.
		LocalDateTime dueDT = nowDT.plusDays(borrow_days);
		String dueDTString = dueDT.format(formatter);
		
		super();
		this.user_id = user_id;
		this.book_id = book_id;
		this.borrow_date = nowDTString;
		this.due_date = dueDTString;
		this.return_date = null;
		this.borrow_days = borrow_days;
		this.created_at = nowDTString;
	}
	
	/**
	 * insert() Insert a single borrow record into the admins table.
	 **/
	public void insert() {
		String sql = "INSERT INTO borrow_records (user_id, book_id, borrow_date, due_date, return_date, borrow_days, created_at) VALUES (?, ?, ?, ?, ?, ?, ?)";
		// Execute SQL statement.
		try (PreparedStatement stmt = StartSystem.db.prepareStatement(sql)) {
			stmt.setString(1, String.valueOf(this.getUser_id()));
			stmt.setString(2, String.valueOf(this.getBook_id()));
			stmt.setString(3, this.getBorrow_date());
			stmt.setString(4, this.getDue_date());
			stmt.setString(5, this.getReturn_date());
			stmt.setString(6, String.valueOf(this.getBorrow_days()));
			stmt.setString(7, this.getCreated_at());
			stmt.executeUpdate();

			System.out.println("The borrow record has been inserted.");

		} catch (SQLException se) {
			se.printStackTrace();
		}
	}
	
	public int getUser_id() {return user_id;}
	
	public void setUser_id(int user_id) {this.user_id = user_id;}
	
	public int getBook_id() {return book_id;}
	
	public void setBook_id(int book_id) {this.book_id = book_id;}
	
	public String getBorrow_date() {return borrow_date;}
	
	public void setBorrow_date(String borrow_date) {this.borrow_date = borrow_date;}
	
	public String getDue_date() {return due_date;}
	
	public void setDue_date(String due_date) {this.due_date = due_date;}
	
	public String getReturn_date() {return return_date;}
	
	public void setReturn_date(String return_date) {this.return_date = return_date;}
	
	public int getBorrow_days() {return borrow_days;}
	
	public void setBorrow_days(int borrow_days) {this.borrow_days = borrow_days;}
	
	public String getCreated_at() {return created_at;}
	
	public void setCreated_at(String create_at) {this.created_at = create_at;}
	
}
