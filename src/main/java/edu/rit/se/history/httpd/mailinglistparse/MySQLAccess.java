package edu.rit.se.history.httpd.mailinglistparse;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

public class MySQLAccess {
	private Connection connect = null;
	private Statement statement = null;
	private PreparedStatement preparedStatement = null;
	private boolean resultSet;
	String name ="";
	public void readDataBase() throws Exception {
		try {
			// This will load the MySQL driver, each DB has its own driver
			Class.forName("com.mysql.jdbc.Driver");
			// Setup the connection with the DB
			connect = DriverManager
					.getConnection("jdbc:mysql://127.0.0.1:3306/abhishek?"
							+ "user=root&password=root");

			// Statements allow to issue SQL queries to the database
			statement = connect.createStatement();
			// Result set get the result of the SQL query
			//resultSet = statement
				//	.executeQuery("select * from abhishek.table_1");
			name ="Suyash";
			resultSet = statement.execute("insert into abhishek.table_1 values (4,'"+name+"','province','1397')");
					
			System.out.println("Result: "+resultSet);
			/*System.out.println("The columns in the table are: ");

			System.out.println("Table: "
					+ resultSet.getMetaData().getTableName(1));

			for (int i = 1; i <= resultSet.getMetaData().getColumnCount(); i++) {
				System.out.println("Column " + i + " "
						+ resultSet.getMetaData().getColumnName(i));
			}
*/			
			
			/*while (resultSet.next()) {
			      // It is possible to get the columns via name
			      // also possible to get the columns via the column number
			      // which starts at 1
			      // e.g. resultSet.getSTring(2);
			      String id = resultSet.getString("id");
			      String name = resultSet.getString("name");
			      String address = resultSet.getString("address");
			      
			      String contact = resultSet.getString("contact_no");
			      System.out.println("\n ID: " + id);
			      System.out.println("\n Name: " + name);
			      System.out.println("\n Address: " + address);
			      System.out.println("\n Contact No: " + contact);
			      
			    }
			*/
			
			
			
			
			

		} catch (Exception e) {
			throw e;
		} finally {
			// close();
		}

	}

	public static void main(String[] args) throws Exception {
		MySQLAccess dao = new MySQLAccess();
		dao.readDataBase();
	}
}