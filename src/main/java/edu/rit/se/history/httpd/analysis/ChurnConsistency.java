package edu.rit.se.history.httpd.analysis;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Scanner;
import org.chaoticbits.devactivity.DBUtil;
import com.mysql.jdbc.Statement;

public class ChurnConsistency {
	
	DecimalFormat pp = new DecimalFormat("0.0000");
	DecimalFormat p = new DecimalFormat("0.00");
	DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");	
	double REWRITE_THRESHOLD = 0.50; 	
	long interval1Day = 1000L*60*60*24;
	long interval30Days = 1000L*60*60*24*30;
	long interval60Days = 1000L*60*60*24*60;
	long interval90Days = 1000L*60*60*24*90;
	int NUM_OF_COMMITS_THRESHOLD = 1;	
	PrintWriter writer;
	Statement stmt;
	ArrayList<String> ignoreVCCFiles = new ArrayList<String>();
	
	public void histogram() throws Exception{		
		Scanner scan = new Scanner(new File("D:/Development/httpd-data/churn/30.txt")); //CHANGE THIS TO 30,60,90.txt
		PrintWriter fileWriter = new PrintWriter(new FileWriter(new File("D:/Development/httpd-data/churn/30his.txt")));
		ArrayList<String> temp = new ArrayList<String>();
		
		fileWriter.println("\"x\",\"y\"");
		while( scan.hasNextLine() ){
			temp.add(scan.nextLine());
		}
		
		// CHANGE THIS TO 30, 60, 90 for the respective interval set 
		for( int x = 0; x < 30; x++ ){
			int other = x;
			int pos = 0;
			int neg = 0;
			int cos = 0; 
			for( int i = 0; i < 64; i++ ){
				String test = temp.get(other);
				String[] regex = test.split(",");
				pos += Integer.parseInt(regex[0]);
				neg += Integer.parseInt(regex[1]);
				cos += Integer.parseInt(regex[2]);
				other += 30;
			} 	// CHANGE THIS TO 30, 60, 90 for the respective interval set
			
			fileWriter.println( pos + ",+ROC" ); 
			fileWriter.println( neg + ",-ROC" );
			fileWriter.println( cos + ",=ROC" );
			
		}
		fileWriter.close();
		
	}
	 
	public void locCalculation(DBUtil dbUtil) throws Exception{
		writer = new PrintWriter("D:\\Development\\httpd-data\\loc.txt", "UTF-8");
		Connection conn = dbUtil.getConnection();
		stmt = (Statement) conn.createStatement();
		String query =  "SELECT DISTINCT  `Filepath` FROM  `gitlogfiles`";
		ResultSet rs = 	stmt.executeQuery(query);
		ArrayList<String> fileNames = new ArrayList<String>();
		while( rs.next()){
			fileNames.add(rs.getString(1));
			writer.println(rs.getString(1));
		}
		writer.close();
	}
	
	public void parse(DBUtil dbUtil, File churnLog, File churnConsistency, File churnDir) throws Exception {
		/* GATHER NORMAL FILES FOR SENSITIVTY ANALYSIS */
		/*
		Connection connA = dbUtil.getConnection();
		ResultSet rsA = connA.createStatement().executeQuery("SELECT DISTINCT `Filepath` FROM CVEToGit ORDER BY `Filepath`");		
		ArrayList<String> removeFiles = new ArrayList<String>(); // vulnerable files, remove them for sensitivity
		ArrayList<String> allFiles  = new ArrayList<String>(); 
		ArrayList<String> senFiles = new ArrayList<String>();
		while(rsA.next()){
			removeFiles.add(rsA.getString(1));
		}
		
		ResultSet rsB = connA.createStatement().executeQuery("SELECT DISTINCT `Filepath` FROM gitlogfiles ORDER BY `Filepath`");
		while(rsB.next()){
			if( !removeFiles.contains(rsB.getString(1))){
				allFiles.add(rsB.getString(1));
			} 
		}
		for( int x = 0; x < 63; x++ ){
			int randomNum =  (int)(Math.random()*allFiles.size());
			String test = allFiles.get(randomNum);
			if( x < 55 ){
				while( test.contains(".h") ){	
					randomNum =  (int)(Math.random()*allFiles.size());
					test = allFiles.get(randomNum);
				}	
				senFiles.add(allFiles.get(randomNum));
			} else{
				while( !test.contains(".h")  ){
					randomNum =  (int)(Math.random()*allFiles.size());
					test = allFiles.get(randomNum);
				}	
				senFiles.add(allFiles.get(randomNum));
			}
			
		}
		connA.close();*/
		
		/* COMBINATION OF VULNERABLE FILES WHICH CONTAIN VCC'S ONLY DURING REWRITES AND/OR VCC'S THAT HAVE NO COMMIT
		 * CAUSED PROBLEMS FOR GRAPH GENERATION AND/OR WANT TO BE ANALYZED SEPARATELY, SO I WOULD COMMENT THEM IN/OUT  */
		/*		
		ignoreVCCFiles.add("include/http_core.h");
		ignoreVCCFiles.add("include/scoreboard.h");
		ignoreVCCFiles.add("modules/dav/fs/lock.c");
		ignoreVCCFiles.add("modules/generators/mod_info.c");
		ignoreVCCFiles.add("modules/http/http_filters.c");
		ignoreVCCFiles.add("modules/http/http_request.c");
		ignoreVCCFiles.add("modules/mappers/mod_alias.c");
		ignoreVCCFiles.add("modules/mappers/mod_imagemap.c");
		ignoreVCCFiles.add("modules/proxy/proxy_balancer.c");
		ignoreVCCFiles.add("modules/session/mod_session.c");
		ignoreVCCFiles.add("server/core_filters.c");
		ignoreVCCFiles.add("server/gen_test_char.c");
		ignoreVCCFiles.add("server/main.c");
		ignoreVCCFiles.add("modules/arch/win32/mod_isapi.c");
		ignoreVCCFiles.add("modules/experimental/mod_disk_cache.c");
		ignoreVCCFiles.add("modules/filters/mod_include.c");
		ignoreVCCFiles.add("modules/filters/mod_reqtimeout.c");
		ignoreVCCFiles.add("modules/generators/mod_status.c");
		ignoreVCCFiles.add("modules/http/byterange_filter.c");
		ignoreVCCFiles.add("modules/proxy/proxy_ftp.c");
		ignoreVCCFiles.add("modules/ssl/ssl_engine_init.c");
		ignoreVCCFiles.add("modules/ssl/ssl_engine_io.c");		
		ignoreVCCFiles.add("server/scoreboard.c");
		
		ignoreVCCFiles.add("modules/http/http_protocol.c");	
		ignoreVCCFiles.add("modules/experimental/cache_util.c");
		ignoreVCCFiles.add("modules/experimental/mod_cache.h");
		ignoreVCCFiles.add("include/ap_config.h");
		ignoreVCCFiles.add("include/httpd.h");
		ignoreVCCFiles.add("modules/proxy/mod_proxy_ftp.c");
		ignoreVCCFiles.add("include/ap_mmn.h");
		ignoreVCCFiles.add("include/http_log.h");
		ignoreVCCFiles.add("server/util.c");
		ignoreVCCFiles.add("server/protocol.c");*/
		
		writer = new PrintWriter(churnConsistency, "UTF-8");
		Connection conn = dbUtil.getConnection();
		stmt = (Statement) conn.createStatement();
		String query =  "SELECT DISTINCT  `Filepath` FROM  `gitlogfiles`";
		ResultSet rs = 	stmt.executeQuery(query);
		ArrayList<String> fileNames = new ArrayList<String>();
		while( rs.next()){
			fileNames.add(rs.getString(1));
		}
	
		ArrayList<String> vccFiles = new ArrayList<String>();
		query =  "SELECT DISTINCT  `Filepath` FROM  `cvetogit`";
		ResultSet rs4 = stmt.executeQuery(query);
		
		while( rs4.next() ){
			vccFiles.add(rs4.getString(1));
		}
		
		for( String file : fileNames ){
			
			int LOC = 0;
			int numCommits = 0;
			int totalChurn = 0;
			int totalChurnWOR = 0;
			long earliestCommit = 0;
			long lastCommit = 0;
			
			// MODIFY THIS IF-STATEMENT WHEN YOU WANT TO PROCESS CERTAIN FILES
			if( vccFiles.contains(file) &&  !ignoreVCCFiles.contains(file)){
			
			System.out.println("Processing file : " + file);
			String query3 = "SELECT `CommitIntroduced` FROM `cvetogit` WHERE `Filepath` = \"" + file + "\"";
			ResultSet rs3 = stmt.executeQuery(query3);
			ArrayList<String> vccs = new ArrayList<String>();
			while( rs3.next()){
				vccs.add(rs3.getString(1));
			}
				
			writer.println(file);
			String query2 = "SELECT COUNT(*) FROM `gitlogfiles` WHERE `Filepath` = \"" + file + "\"";
			ResultSet rs2 = stmt.executeQuery(query2);
			while( rs2.next() ){
				numCommits = Integer.parseInt(rs2.getString(1));
			}
			writer.println(numCommits);
			query = "SELECT `AuthorDate`, `LinesInserted`, `LinesDeleted`, `gitlogfiles`.`Commit` FROM `gitlogfiles` INNER JOIN `gitlog` ON `gitlogfiles`.`Commit` = `gitlog`.`Commit` WHERE `Filepath` = \"" + file + "\"ORDER BY `AuthorDate` ASC";
			rs = 	stmt.executeQuery(query);
			ArrayList<ChurnDate> churnDate = new ArrayList<ChurnDate>();

			if( rs.first() ){
				Date date = new SimpleDateFormat("yyyy-MM-dd").parse(rs.getString("AuthorDate"));
				earliestCommit = date.getTime(); 
				rs.beforeFirst();
			}
			while( rs.next()){
				boolean isRewrite = false;
				boolean vccCommit = false;
				Date date = new SimpleDateFormat("yyyy-MM-dd").parse(rs.getString("AuthorDate"));
				String dateF = dateFormat.format(date.getTime());
				int churnVal = Integer.parseInt(rs.getString(2))+Integer.parseInt(rs.getString(3));
				
				
				int temp = Integer.parseInt(rs.getString(2))-Integer.parseInt(rs.getString(3));
				//System.out.println(temp);
				LOC +=temp;
				if( LOC < 1 ){
					LOC -=temp;
				}
				churnDate.add(new ChurnDate(date.getTime(), churnVal));
				totalChurn += churnVal;
				totalChurnWOR += churnVal;
				if( (LOC * REWRITE_THRESHOLD) <= churnVal ){
					totalChurnWOR -= churnVal;
					isRewrite = true;
				}
				if( vccs.contains(rs.getString(4) )){
					vccCommit = true;
				}
				writer.println(rs.getString(4) + "," + dateF + "," + churnVal + "," + isRewrite + "," + vccCommit);
				
			}
			rs.last();
			Date date = new SimpleDateFormat("yyyy-MM-dd").parse(rs.getString("AuthorDate"));
			lastCommit = date.getTime(); 
	
			long difference = lastCommit - earliestCommit;
			int num30Periods = (int) ((difference/interval30Days)+1);		
			int num60Periods = (int) ((difference/interval60Days)+1);		
			int num90Periods = (int) ((difference/interval90Days)+1);	
						
			writer.println("Total Churn: " + totalChurn);
			writer.println("Earliest Commit: " + dateFormat.format(earliestCommit));
			writer.println("Last Commit: " + dateFormat.format(lastCommit));
			
			double rocThres = 0.75;
	
			//for( int i = 0; i < 30; i++ ){ // SENSTIVITY ANALYSIS OF ROC THRESHOLD, NEED TO HAVE ALL THE 
			// FILES PRINT TO THE SAME INTERVAL FILE, SO REMOVE "filename" for PrintWriters below			
				
			PrintWriter fileWriter;
			String fileName = file.replace('/', '+');
			fileWriter = new PrintWriter(new FileWriter(new File(churnDir+"\\"+fileName+"-30.txt"), true));
			writer.println("Number of 30 day periods: " + num30Periods);
			
			// CHANGE X to 30 FOR TIME SENSTIVITY ANALYSIS
			long earliestCommitSave = earliestCommit;
			for( int x = 0; x < 30; x++ ){
				churnInterval(earliestCommit, lastCommit, numCommits, churnDate, totalChurnWOR, interval30Days, LOC, file, churnDir, vccs, rocThres, fileWriter);
				writer.println(" ");
				earliestCommit = earliestCommit - interval1Day;
			}
			earliestCommit = earliestCommitSave; 
			fileWriter.close();
			fileWriter = new PrintWriter(new FileWriter(new File(churnDir+"\\"+fileName+"-60.txt"), true));
			writer.println("Number of 60 day periods: " + num60Periods);
			
			// CHANGE X to 60 FOR TIME SENSTIVITY ANALYSIS
			for( int x = 0; x < 1; x++ ){
				churnInterval(earliestCommit, lastCommit, numCommits, churnDate, totalChurnWOR, interval60Days, LOC, file, churnDir, vccs, rocThres, fileWriter);
				writer.println(" ");
				earliestCommit = earliestCommit - interval1Day;
				
			}
			fileWriter.close();
			earliestCommit = earliestCommitSave; 
		
			fileWriter = new PrintWriter(new FileWriter(new File(churnDir+"\\"+fileName+"-90.txt"), true));
			writer.println("Number of 90 day periods: " + num90Periods);
			
			// CHANGE X to 90 FOR TIME SENSTIVITY ANALYSIS
			for( int x = 0; x < 1; x++ ){
				churnInterval(earliestCommit, lastCommit, numCommits, churnDate, totalChurnWOR, interval90Days, LOC, file, churnDir, vccs, rocThres, fileWriter);
				writer.println(" ");
				earliestCommit = earliestCommit - interval1Day;
			}	
			writer.println();
			fileWriter.close();
		//	rocThres += (double)5/100; } // SENSTIVITY ANALYSIS OF ROC THRESHOLD
			}
		}
		writer.close();
		conn.close();
	}
	
	public void churnInterval( long earliestCommit, long lastCommit, int numCommits, ArrayList<ChurnDate> churnDate, int totalChurn, long timeInterval, int LOC, String file, File churnDir, ArrayList<String> vccs, double rocThres, PrintWriter fileWriter) throws Exception {		 
		long earliestCommitT = earliestCommit;		
		ArrayList<Interval> intervals = new ArrayList<Interval>(); 
		while( earliestCommitT <= lastCommit ){				
			Interval newRange = new Interval();
			long eCT = earliestCommitT + timeInterval;
			
			//SPECIAL CASE
			if( (dateFormat.parse("2008-11-5").getTime() > earliestCommitT && dateFormat.parse("2008-11-5").getTime() < eCT) && file.equals("modules/proxy/mod_proxy_ajp.c") ){
				newRange.setisVCC(true);
				int numVCC = newRange.getNumVCC();
				numVCC += 1;
				newRange.setNumVCC(numVCC);
			} 		
			int projectChurn = 0;
			
			if( numCommits > NUM_OF_COMMITS_THRESHOLD ){	
				String query = "SELECT `LinesInserted`, `LinesDeleted`, `gitlog`.`Commit` FROM `gitlogfiles` INNER JOIN `gitlog` on `gitlog`.`Commit`=`gitlogfiles`.`Commit` WHERE `AuthorDate` BETWEEN \"" + dateFormat.format(earliestCommitT) + "%\" AND \"" +  dateFormat.format(eCT) + "%\"";
				ResultSet rs3 = stmt.executeQuery(query);
				while( rs3.next() ){
					projectChurn +=	Integer.parseInt(rs3.getString(1)) + Integer.parseInt(rs3.getString(2));
				}	
				
				query = "SELECT `LinesInserted`, `LinesDeleted`, `gitlog`.`Commit`, `Filepath` FROM `gitlogfiles` INNER JOIN `gitlog` on `gitlog`.`Commit`=`gitlogfiles`.`Commit` WHERE `Filepath` = \"" + file + "\" AND `AuthorDate` BETWEEN \"" + dateFormat.format(earliestCommitT) + "%\" AND \"" +  dateFormat.format(eCT) + "%\"";
				ResultSet rs4 = stmt.executeQuery(query);
				while( rs4.next() ){
					if( vccs.contains(rs4.getString(3))){
						newRange.setisVCC(true);
						int numVCC = newRange.getNumVCC();
						// SPECIAL CASES
						if( rs4.getString(3).equals("1be2a4f4d27ce22dee4da56dfc21021a454b4253") && file.equals("modules/http/byterange_filter.c")){
							numVCC += 2;
						} else if( rs4.getString(3).equals("9de8a693d16e5ce76ff5b7f727311e783a51f34a") && file.equals("modules/mappers/mod_imagemap.c")){
							numVCC += 2;
						} else if( rs4.getString(3).equals("5d855a48777529f38b148c19c021a01685677f79") && file.equals("modules/mappers/mod_rewrite.c")){
							numVCC += 2;
						} else if( rs4.getString(3).equals("7e5c35962b46a8492323abedce2614f7f280a35a") && file.equals("modules/ssl/ssl_engine_kernel.c")){
							numVCC += 2;
						} else if( rs4.getString(3).equals("1f52b243020ac155bb26ee9b2a9ea960e6eefa06") && file.equals("modules/ssl/ssl_engine_kernel.c")){
							numVCC += 2;
						} else if( rs4.getString(3).equals("5430f8800f5fffd57e7421dee0ac9de8ca4f9573") && file.equals("server/log.c")){
							numVCC += 2;
						} else if( rs4.getString(3).equals("dfa894557bb5c0b2fac6d75b39acae65f66957fc") && file.equals("modules/ssl/ssl_engine_io.c")){
							numVCC += 2;
						} else if( rs4.getString(3).equals("3afccb5343dd2b9463d68b99202fb1c17b53989c") && file.equals("include/http_core.h")){
							numVCC += 2;
						}	
						else {
							numVCC += 1;
						}
						newRange.setNumVCC(numVCC);
					}
				}
			}			
			
			newRange.setStartDate(earliestCommitT);
			newRange.setEndDate(eCT);
			newRange.setProjectChurn(projectChurn);
			earliestCommitT = eCT;
			intervals.add(newRange);
		}
		for( ChurnDate churnEntity : churnDate ){
			for( int i = 0; i < intervals.size(); i++ ){
				Interval interval = intervals.get(i);
				if( churnEntity.getCommitDate() <= interval.getEndDate() && churnEntity.getCommitDate() >= interval.getStartDate() ){
	
					double churnVal = churnEntity.getChurn() + interval.getTotalChurn();
					double percentChurn = (churnVal/totalChurn)*100;
					interval.setTotalChurn((int)churnVal);
					
					if( interval.getProjectChurn() !=  0){
						double projectPercentChurn = (churnVal/interval.getProjectChurn());
						interval.setProjectPercentChurn(projectPercentChurn);
					} else{
						interval.setProjectPercentChurn(0);
					}
					if( (LOC * REWRITE_THRESHOLD) <= churnVal ){
						interval.setIsRewrite(true);
					}
					interval.setPercentChurn(percentChurn);					
				}
			}
		}
		ArrayList<Integer> remove = new ArrayList<Integer>();
		for( int x = 0; x < intervals.size(); x++ ){
			if( intervals.get(x).getisRewrite() ){
				remove.add(x);
			}
		}
		
		ArrayList<Interval> tempA = new ArrayList<Interval>();
		for( int x = 0; x < intervals.size(); x++ ){
			if( !remove.contains(x)){
				tempA.add(intervals.get(x));
			}
		}
		intervals = tempA;

		for( int x = 0; x < intervals.size(); x++ ){
			Interval newRange = intervals.get(x);
			if( x == 0 ){
				newRange.setRoc( newRange.getTotalChurn() );
			} else{
				double temp = (newRange.getTotalChurn() - intervals.get(x-1).getTotalChurn());
				newRange.setRoc(temp);
			}
		}
		
		ArrayList<Double> rocArray = new ArrayList<Double>();
		for( Interval interval : intervals){
			rocArray.add(interval.getRoC());
		}
		double aRoc = 0;
		for( Double d : rocArray ){
			aRoc += Math.abs(d);
		}
		aRoc = (aRoc / rocArray.size());		
		
		for( Interval interval : intervals){
			if( interval.getIsVCC()){
				writer.println("\t" + dateFormat.format(interval.getStartDate()) + "," + dateFormat.format(interval.getEndDate()) + "," + interval.getTotalChurn() + "," + p.format(interval.getPercentChurn()) + "," + pp.format(interval.getProjectPercentChurn()) + "," + interval.getisRewrite() + "," + p.format(interval.getRoC()) + "," + interval.getIsVCC());
			}
			else{
				writer.println("\t" + dateFormat.format(interval.getStartDate()) + "," + dateFormat.format(interval.getEndDate()) + "," + interval.getTotalChurn() + "," + p.format(interval.getPercentChurn()) + "," + pp.format(interval.getProjectPercentChurn()) + "," + interval.getisRewrite() + "," + p.format(interval.getRoC()));
			}
		}
		
		int i = 0;
		int posChurn = 0;
		int negChurn = 0;
		int neutralChurn = 0;
		double CHURN_THRES = aRoc * rocThres;
		for (Interval interval : intervals) {
			if (interval.getIsVCC()) {
				double intervalThreshold = Math.abs(interval.getTotalChurn());
				if (intervalThreshold > CHURN_THRES) {
					if (interval.getRoC() <= 0) {
						negChurn += interval.getNumVCC();
						int temp = interval.getNumVCC();
						temp *= -1;
						fileWriter.println(i + "," + dateFormat.format(interval.getStartDate()) + ","
								+ dateFormat.format(interval.getEndDate()) + "," + interval.getTotalChurn() + ","
								+ interval.getisRewrite() + "," + p.format(interval.getRoC()) + ","
								+ p.format(interval.getRoC()) + "," + temp);
					} else {
						fileWriter.println(i + "," + dateFormat.format(interval.getStartDate()) + ","
								+ dateFormat.format(interval.getEndDate()) + "," + interval.getTotalChurn() + ","
								+ interval.getisRewrite() + "," + p.format(interval.getRoC()) + ","
								+ p.format(interval.getRoC()) + "," + interval.getNumVCC());
						posChurn += interval.getNumVCC();
					}
				} else {
					int temp = interval.getNumVCC();
					temp *= -1;
					fileWriter.println(i + "," + dateFormat.format(interval.getStartDate()) + ","
							+ dateFormat.format(interval.getEndDate()) + "," + interval.getTotalChurn() + ","
							+ interval.getisRewrite() + "," + p.format(interval.getRoC()) + ","
							+ p.format(interval.getRoC()) + "," + temp);
					neutralChurn += interval.getNumVCC();
				}
			} else {
				fileWriter.println(i + "," + dateFormat.format(interval.getStartDate()) + ","
						+ dateFormat.format(interval.getEndDate()) + "," + interval.getTotalChurn() + ","
						+ interval.getisRewrite() + "," + p.format(interval.getRoC()));
			}
		//	fileWriter.println(posChurn + "," + negChurn + "," + neutralChurn);
			i++;
		}					
	}
	
	public void projectChurn(DBUtil dbUtil, File churnConsistency) throws Exception{
		PrintWriter writer = new PrintWriter(churnConsistency, "UTF-8");
		Connection conn = dbUtil.getConnection();
		Statement stmt = (Statement) conn.createStatement();
		String query = "SELECT `AuthorDate`, `LinesInserted`, `LinesDeletedSelf`, `gitlogfiles`.`Commit` FROM `gitlogfiles` INNER JOIN `gitlog` ON `gitlogfiles`.`Commit` = `gitlog`.`Commit` WHERE `Filepath` IN (SELECT DISTINCT  `Filepath` FROM  `gitlogfiles`) ORDER BY `AuthorDate` ASC";
		ResultSet rs = 	stmt.executeQuery(query);
		
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
			query = "SELECT `LinesInserted`, `LinesDeletedSelf` FROM `gitlogfiles` INNER JOIN `gitlog` on `gitlog`.`Commit`=`gitlogfiles`.`Commit` WHERE `AuthorDate` BETWEEN \"" + dateFormat.format(earliestCommitT) + "%\" AND \"" +  dateFormat.format(eCT) + "%\"";
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
			
			writer.println(i + "," + dateFormat.format(interval.getStartDate()) + "," + dateFormat.format(interval.getEndDate()) + "," + interval.getProjectChurn() );
			i++;
		}
		writer.println();
		writer.close();
	
	}
			
	private class Interval{
		private Long startDate;
		private Long endDate;
		private int totalChurn;
		private double percentChurn;
		private int projectChurn;
		private double projectPercentChurn;
		private boolean isRewrite = false;
		private double roc;
		private boolean isVCC = false;
		private int numVCC = 0;
		
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
		
		public boolean getisRewrite() {
			return isRewrite;
		}
		public void setIsRewrite(boolean isRewrite) {
			this.isRewrite = isRewrite;
		}	
		
		public double getRoC() {
			return roc;
		}
		public void setRoc(double roc) {
			this.roc = roc;
		}	
		
		public boolean getIsVCC() {
			return isVCC;
		}
		public void setisVCC(boolean isVCC) {
			this.isVCC = isVCC;
		}	
		
		public int getNumVCC(){
			return numVCC;
		}
		
		public void setNumVCC(int numVCC){
			this.numVCC = numVCC;
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
}
