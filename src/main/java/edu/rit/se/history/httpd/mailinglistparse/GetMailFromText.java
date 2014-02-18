package edu.rit.se.history.httpd.mailinglistparse;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.Statement;

public class GetMailFromText {
	private static Connection connect = null;
	private static Statement statement = null;
	private static PreparedStatement preparedStatement = null;
	private static boolean resultSet;
	static String subject ="";
	static int i=0;
	public static void main(String[] args) throws IOException
	{
		String fileName = "/Users/abhishekdale/Documents/Mailing List/200201.txt";
		BufferedReader br= null;
		
		try
		{
			FileInputStream fis = new FileInputStream(fileName);
			br= new BufferedReader(new InputStreamReader(fis,"UTF-8"));
			String sCurrentLine;
			while ((sCurrentLine = br.readLine())!=null)
			{
				if(sCurrentLine.startsWith("Subject:"))
				{	i++;
					processLine(sCurrentLine,i);
				}
				
			
			}
			
		}
		catch(Exception e)
		{} finally{ if (br!=null) br.close();}
		
	}
	
	public static void processLine(String Line,int id)
	{
		System.out.println(Line);
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
			subject =Line;
			
			
			
			resultSet = statement.execute("insert into abhishek.email values ("+
											id+
											","+
											"'"+
											subject+
											"'"+
											")");		
			
			//System.out.println("insert into abhishek.email values ("+id+","+"'"+subject+"'"+")");
		
		} catch (Exception e) {
			System.out.println(e.getMessage());
		} finally {
			// close();
		}
		
	}

}
