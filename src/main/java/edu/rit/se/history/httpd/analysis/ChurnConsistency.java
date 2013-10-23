package edu.rit.se.history.httpd.analysis;

import java.awt.List;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.TreeMap;
import java.util.Vector;

import org.apache.commons.math3.util.MultidimensionalCounter.Iterator;
import org.chaoticbits.devactivity.DBUtil;

import com.mysql.jdbc.PreparedStatement;
import com.mysql.jdbc.Statement;
import com.mysql.jdbc.StatementImpl;



public class ChurnConsistency {
	
	DecimalFormat pp = new DecimalFormat("0.0000");
	DecimalFormat p = new DecimalFormat("0.00");
	DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
	long interval30Days = 1000L*60*60*24*30;
	long interval60Days = 1000L*60*60*24*60;
	
	public void parse(DBUtil dbUtil, File churnLog, File churnConsistency, File churnDir) throws Exception {
		PrintWriter writer = new PrintWriter(churnConsistency, "UTF-8");
		Connection conn = dbUtil.getConnection();
		Statement stmt = (Statement) conn.createStatement();
		String query =  "SELECT DISTINCT  `Filepath` FROM  `gitlogfiles`";
		ResultSet rs = 	stmt.executeQuery(query);
		ArrayList<String> fileNames = new ArrayList<String>();
		while( rs.next()){
			fileNames.add(rs.getString(1));
		}
		
		for( String file : fileNames ){
			int numCommits = 0;
			int totalChurn = 0;
			long earliestCommit = 0;
			long lastCommit = 0;
			writer.println(file);
			String query2 = "SELECT COUNT(*) FROM `gitlogfiles` WHERE `Filepath` = \"" + file + "\"";
			ResultSet rs2 = stmt.executeQuery(query2);
			while( rs2.next() ){
				numCommits = Integer.parseInt(rs2.getString(1));
			}
			writer.println(numCommits);
			query = "SELECT `AuthorDate`, `LinesInserted`, `LinesDeleted`, `gitlogfiles`.`Commit` FROM `gitlogfiles` INNER JOIN `gitlog` ON `gitlogfiles`.`Commit` = `gitlog`.`Commit` WHERE `Filepath` = \"" + file + "\"ORDER BY `AuthorDate` ASC";
			rs = 	stmt.executeQuery(query);
			
			ArrayList<Integer> churn = new ArrayList<Integer>();	
			ArrayList<ChurnDate> churnDate = new ArrayList<ChurnDate>();
			if( rs.first() ){
				Date date = new SimpleDateFormat("yyyy-MM-dd").parse(rs.getString("AuthorDate"));
				String dateF = dateFormat.format(date.getTime());
				earliestCommit = date.getTime(); 
				rs.beforeFirst();
			}
			while( rs.next()){
				Date date = new SimpleDateFormat("yyyy-MM-dd").parse(rs.getString("AuthorDate"));
				String dateF = dateFormat.format(date.getTime());
				int churnVal = Integer.parseInt(rs.getString(2))+Integer.parseInt(rs.getString(3));
				churnDate.add(new ChurnDate(date.getTime(), churnVal));
				totalChurn += churnVal;
				writer.println(rs.getString(4) + "," + dateF + "," + churnVal);
				
			}
			rs.last();
			Date date = new SimpleDateFormat("yyyy-MM-dd").parse(rs.getString("AuthorDate"));
			String dateF = dateFormat.format(date.getTime());
			lastCommit = date.getTime(); 
	
			long difference = lastCommit - earliestCommit;
			int num30Periods = (int) ((difference/interval30Days)+1);		
			int num60Periods = (int) ((difference/interval60Days)+1);		
			
			writer.println("Total Churn: " + totalChurn);
			writer.println("Earliest Commit: " + dateFormat.format(earliestCommit));
			writer.println("Last Commit: " + dateFormat.format(lastCommit));
			writer.println("Number of 30 day periods: " + num30Periods);
			
			long earliestCommitT = earliestCommit;			
			ArrayList<Interval> intervals30D = new ArrayList<Interval>(); 
			while( earliestCommitT <= lastCommit ){				
				Interval newRange = new Interval();
				long eCT = earliestCommitT + interval30Days;
				int projectChurn = 0;
				
				if( numCommits > 250 ){	
					query = "SELECT `LinesInserted`, `LinesDeleted` FROM `gitlogfiles` INNER JOIN `gitlog` on `gitlog`.`Commit`=`gitlogfiles`.`Commit` WHERE `AuthorDate` BETWEEN \"" + dateFormat.format(earliestCommitT) + "%\" AND \"" +  dateFormat.format(eCT) + "%\"";
					ResultSet rs3 = stmt.executeQuery(query);
					while( rs3.next() ){
						projectChurn +=	Integer.parseInt(rs3.getString(1)) + Integer.parseInt(rs3.getString(2));
					}	
				}
				newRange.setStartDate(earliestCommitT);
				newRange.setEndDate(eCT);
				newRange.setProjectChurn(projectChurn);
				earliestCommitT = eCT;
				intervals30D.add(newRange);
			}
			for( ChurnDate churnEntity : churnDate ){
				for( Interval interval : intervals30D){
					if( churnEntity.getCommitDate() <= interval.getEndDate() && churnEntity.getCommitDate() >= interval.getStartDate() ){
						double churnVal = churnEntity.getChurn() + interval.getTotalChurn();
						double percentChurn = (churnVal/totalChurn)*100;
						interval.setTotalChurn((int)churnVal);
						double projectPercentChurn = (churnVal/interval.getProjectChurn());
						interval.setProjectPercentChurn(projectPercentChurn);
						interval.setPercentChurn(percentChurn);
					}
				}
			}
			for( Interval interval : intervals30D){
				writer.println("\t" + dateFormat.format(interval.getStartDate()) + "," + dateFormat.format(interval.getEndDate()) + "," + interval.getTotalChurn() + "," + p.format(interval.getPercentChurn()) + "," + pp.format(interval.getProjectPercentChurn()));
			}
			
			if( numCommits > 250 ){
				String fileName = file.replace('/', '+');
				PrintWriter fileWriter = new PrintWriter(new File(churnDir+"\\"+fileName), "UTF-8");
				int i = 0;
				fileWriter.println("\"interval\",\"start.date\",\"end.date\",\"churn\"");
				for( Interval interval : intervals30D){
					fileWriter.println(i + "," + dateFormat.format(interval.getStartDate()) + "," + dateFormat.format(interval.getEndDate()) + "," + interval.getTotalChurn() );// + "," + p.format(interval.getPercentChurn()) + "," + pp.format(interval.getProjectPercentChurn()));
					i++;
				}
				fileWriter.close();
			}
			
			
			earliestCommitT = earliestCommit;
			writer.println("Number of 60 day periods: " + num60Periods);
			
			ArrayList<Interval> intervals60D = new ArrayList<Interval>(); 
			while( earliestCommitT <= lastCommit ){
				Interval newRange = new Interval();
				long eCT = earliestCommitT + interval60Days;
				int projectChurn = 0;
				//if( numCommits > 250 ){
					//query = "SELECT `LinesInserted`, `LinesDeleted` FROM `gitlogfiles` INNER JOIN `gitlog` on `gitlog`.`Commit`=`gitlogfiles`.`Commit` WHERE `AuthorDate` BETWEEN \"" + dateFormat.format(earliestCommitT) + "%\" AND \"" +  dateFormat.format(eCT) + "%\"";
				//	ResultSet rs3 = stmt.executeQuery(query);
					//while( rs3.next() ){
				//	/	projectChurn +=	Integer.parseInt(rs3.getString(1)) + Integer.parseInt(rs3.getString(2));
				//	}
				//}
				newRange.setStartDate(earliestCommitT);
				newRange.setEndDate(eCT);
				newRange.setProjectChurn(projectChurn);
				earliestCommitT = eCT;
				intervals60D.add(newRange);
			}
			
			for( ChurnDate churnEntity : churnDate ){
				for( Interval interval : intervals60D){
					if( churnEntity.getCommitDate() <= interval.getEndDate() && churnEntity.getCommitDate() >= interval.getStartDate() ){
						double churnVal = churnEntity.getChurn() + interval.getTotalChurn();
						double percentChurn = (churnVal/totalChurn)*100;
						interval.setTotalChurn((int)churnVal);
						double projectPercentChurn = (churnVal/interval.getProjectChurn());
						interval.setProjectPercentChurn(projectPercentChurn);
						interval.setPercentChurn(percentChurn);		
					}
				}
			}
			for( Interval interval : intervals60D){
				writer.println("\t" + dateFormat.format(interval.getStartDate()) + "," + dateFormat.format(interval.getEndDate()) + "," + interval.getTotalChurn() + "," + p.format(interval.getPercentChurn()) + "," + pp.format(interval.getProjectPercentChurn()));
			}
			writer.println();
		}
		
		
		conn.close();
		writer.close();
	
	}
	
	public void projectChurn(DBUtil dbUtil, File churnConsistency) throws Exception{
		PrintWriter writer = new PrintWriter(churnConsistency, "UTF-8");
		Connection conn = dbUtil.getConnection();
		Statement stmt = (Statement) conn.createStatement();
		String query = "SELECT `AuthorDate`, `LinesInserted`, `LinesDeleted`, `gitlogfiles`.`Commit` FROM `gitlogfiles` INNER JOIN `gitlog` ON `gitlogfiles`.`Commit` = `gitlog`.`Commit` WHERE `Filepath` IN (SELECT DISTINCT  `Filepath` FROM  `gitlogfiles`) ORDER BY `AuthorDate` ASC";
		ResultSet rs = 	stmt.executeQuery(query);
		
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		long earliestCommit = 0;
		long lastCommit = 0; 
		
		if( rs.first() ){
			Date date = new SimpleDateFormat("yyyy-MM-dd").parse(rs.getString("AuthorDate"));
			String dateF = dateFormat.format(date.getTime());
			earliestCommit = date.getTime(); 
		}
		if( rs.last() ){
			Date date = new SimpleDateFormat("yyyy-MM-dd").parse(rs.getString("AuthorDate"));
			String dateF = dateFormat.format(date.getTime());
			lastCommit = date.getTime(); 
		}
		
		long interval30Days = 1000L*60*60*24*30;
		long interval60Days = 1000L*60*60*24*60;
		long earliestCommitT = earliestCommit;	
		ArrayList<Interval> intervals30D = new ArrayList<Interval>(); 
		while( earliestCommitT <= lastCommit ){
			Interval newRange = new Interval();
			long eCT = earliestCommitT + interval30Days;
			int projectChurn = 0;
			query = "SELECT `LinesInserted`, `LinesDeleted` FROM `gitlogfiles` INNER JOIN `gitlog` on `gitlog`.`Commit`=`gitlogfiles`.`Commit` WHERE `AuthorDate` BETWEEN \"" + dateFormat.format(earliestCommitT) + "%\" AND \"" +  dateFormat.format(eCT) + "%\"";
			ResultSet rs2 = stmt.executeQuery(query);
			while( rs2.next() ){
				projectChurn +=	Integer.parseInt(rs2.getString(1)) + Integer.parseInt(rs2.getString(2));
			}
			newRange.setStartDate(earliestCommitT);
			newRange.setEndDate(eCT);
			newRange.setProjectChurn(projectChurn);
			earliestCommitT = eCT;
			intervals30D.add(newRange);
		}
		
		int i = 1;
		writer.println("\"interval\",\"start.date\",\"end.date\",\"churn\"");
		for( Interval interval : intervals30D){
			
			writer.println(i + "," + dateFormat.format(interval.getStartDate()) + "," + dateFormat.format(interval.getEndDate()) + "," + interval.getProjectChurn());
			i++;
		}
		writer.println();
		writer.close();
	
	}
		
	private Integer parseInt(String line, String... removes) {
		String str = " " + line;
		for (String remove : removes) {
			str = str.replaceAll(remove, "");
		}

		return Integer.valueOf(str.trim());
	}
		
	private boolean isCommitHash(String line) {
			return line.matches("[0-9a-f]{40}");
	}
	
	private class Interval{
		private Long startDate;
		private Long endDate;
		private int totalChurn;
		private double percentChurn;
		private int projectChurn;
		private double projectPercentChurn;
		
		public Long getStartDate() {
			return startDate;
		}
		public void setProjectPercentChurn(double projectPercentChurn) {
			this.projectPercentChurn = projectPercentChurn;			
		}
		public double getProjectPercentChurn(){
			return projectPercentChurn;
		}
		public void setStartDate(Long startDate) {
			this.startDate = startDate;
		}
		public Long getEndDate() {
			return endDate;
		}
		public void setEndDate(Long endDate) {
			this.endDate = endDate;
		}
		public int getTotalChurn() {
			return totalChurn;
		}
		public void setTotalChurn(int totalChurn) {
			this.totalChurn = totalChurn;
		}
		public int getProjectChurn() {
			return projectChurn;
		}
		public void setProjectChurn(int projectChurn) {
			this.projectChurn = projectChurn;
		}
		public double getPercentChurn() {
			return percentChurn;
		}
		public void setPercentChurn(double percentChurn) {
			this.percentChurn = percentChurn;
		}		
	}
	
	private class ChurnDate{
		private Long commitDate;
		private int churn;
		
		public ChurnDate(Long commitDate, int churn){
			this.commitDate = commitDate;
			this.churn = churn;
		}
		public Long getCommitDate() {
			return commitDate;
		}

		public int getChurn() {
			return churn;
		}

	}
	
	//HashMap<String, ArrayList<Integer>> map = new HashMap<String, ArrayList<Integer>>();
	//TreeMap<String, HashMap<String, Integer>> map = new TreeMap<String, HashMap<String,Integer>>();
	/*Scanner scanner = new Scanner(churnLog);
	while (scanner.hasNextLine()) {
		String commit = scanner.nextLine();
		String filepath = scanner.nextLine();
		Integer totalChurn = parseInt(scanner.nextLine(), "Total Churn:\t");
		for( int i = 0; i < 10; i++){
			scanner.nextLine();
		}
		
		
		filepath = filepath.trim();
		if( map.get(filepath) != null ){
			HashMap<String, Integer> temp = map.get(filepath);

			temp.put(commit, totalChurn);
		}
		else{
			HashMap<String, Integer> temp = new HashMap<String, Integer>();
			temp.put(commit, totalChurn);
			map.put(filepath, temp);
		}
	} 
	scanner.close();
	
	HashMap<String, Integer> totalValues = map.get("modules/ssl/ssl_engine_kernel.c");
	ArrayList<Integer> churn = new ArrayList<Integer>();
	for( Entry<String, Integer> entry : totalValues.entrySet() ){
		churn.add(entry.getValue());
	}
	
	HashMap<String, Integer> totalValues =  new HashMap<String, Integer>();
	for( Entry<String, HashMap<String, Integer>> entry : map.entrySet())
	{ 
		int total = 0;
		HashMap<String, Integer> temp = entry.getValue();
		for( Entry<String, Integer> val : temp.entrySet()){
			total = val.getValue();
		}
		if( temp.size() > 300){
			totalValues.put(entry.getKey(), temp.size());
		}
	}
	ArrayList<Integer> values = new ArrayList<Integer>();
	for( Entry<String, Integer> entry : totalValues.entrySet())
	{ 
		values.add(entry.getValue());			
		System.out.println( entry.getValue() + "\t\t" + entry.getKey());
	}
	Collections.sort(values);
	for( Integer i : values ){
		System.out.println( i);
	}*/
	
}
