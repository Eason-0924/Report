package system;


import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 *  @ClassName    book
 *	A model class of book.
 **/
public class Book {
	// Use @JsonProperty to match the Chinese words in Jackson.
	@JsonProperty("題名")
	private String title;
	@JsonProperty("作者")
	private List<String> authors;
	@JsonProperty("主題")
	private List<String> subjects;
	@JsonProperty("出版者")
	private String publisher;
	@JsonProperty("出版年")
	private String publish_year;
	@JsonProperty("版本")
	private String edition;
	@JsonProperty("格式")
	private String formate_desc;
	@JsonProperty("資料來源")
	private String source;
	@JsonProperty("識別號")
	private List<String> isbn;
	@JsonProperty("附註")
	private String note;
	
	public Book() {}
	
	public Book(String title, List<String> authors, List<String> subjects, 
				String publisher, String publish_year,String edition, 
				String formate_desc, String source, List<String> isbn, String note) {
		super();
		this.title = title;
		this.authors = authors;
		this.subjects = subjects;
		this.publisher = publisher;
		this.publish_year = publish_year;
		this.edition = edition;
		this.formate_desc = formate_desc;
		this.source = source;
		this.isbn = isbn;
		this.note = note;
	}
	
	/**
	 *  Insert()
	 *  Insert a single book into the books table.
	 **/
	public void insert() {
		String sql = "INSERT INTO books(title, authors, subjects, publisher, publish_year, edition, format_desc, source, isbn, note) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

		// Execute SQL statement.
		try (PreparedStatement stmt = StartSystem.db.prepareStatement(sql)) {
			stmt.setString(1, this.getTitle());
			// Convert List to String.
			stmt.setString(2, String.join(",", this.getAuthors()));
			stmt.setString(3, String.join(",", this.getSubjects()));
			stmt.setString(4, this.getPublisher());
			stmt.setString(5, this.getPublish_year());
			stmt.setString(6, this.getEdition());
			stmt.setString(7, this.getFormate_desc());
			stmt.setString(8, this.getSource());
			stmt.setString(9, String.join(",", this.getIsbn()));
			stmt.setString(10, this.getNote());
			stmt.executeUpdate();
			
			System.out.println("The book has been inserted.");

		} catch (SQLException se) {
			se.printStackTrace();
		} 
	}
	

	public String getTitle() {return title;}

	public void setTitle(String title) {this.title = title;}

	public List<String> getAuthors() {return authors;}

	public void setAuthors(List<String> authors) {this.authors = authors;}

	public List<String> getSubjects() {return subjects;}

	public void setSubjects(List<String> subjects) {this.subjects = subjects;}

	public String getPublisher() {return publisher;}

	public void setPublisher(String publisher) {this.publisher = publisher;}

	public String getPublish_year() {return publish_year;}

	public void setPublish_year(String publish_year) {this.publish_year = publish_year;}

	public String getEdition() {return edition;}

	public void setEdition(String edition) {this.edition = edition;}

	public String getFormate_desc() {return formate_desc;}

	public void setFormate_desc(String formate_desc) {this.formate_desc = formate_desc;}

	public String getSource() {return source;}

	public void setSource(String source) {this.source = source;}

	public List<String> getIsbn() {return isbn;}

	public void setIsbn(List<String> isbn) {this.isbn = isbn;}

	public String getNote() {return note;}

	public void setNote(String note) {this.note = note;}
}
