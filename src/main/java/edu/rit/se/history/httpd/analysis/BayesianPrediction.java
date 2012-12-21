package edu.rit.se.history.httpd.analysis;

import java.sql.SQLException;

import org.chaoticbits.devactivity.DBUtil;

import weka.classifiers.bayes.NaiveBayes;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;
import weka.experiment.InstanceQuery;

import com.mysql.jdbc.Connection;

public class BayesianPrediction {

	private DBUtil dbUtil;

	public BayesianPrediction(DBUtil dbUtil) {
		this.dbUtil = dbUtil;
	}

	public void run() throws Exception {
		// Check out this: http://weka.wikispaces.com/Generating+cross-validation+folds+%28Java+approach%29
		// http://weka.wikispaces.com/Use+Weka+in+your+Java+code
		// InstanceQuery query = new InstanceQuery();
		// query.setUsername(dbUtil.getUser());
		// query.setPassword(dbUtil.getPassword());
		// query.setDatabaseURL(dbUtil.getUrl());
		// query.connectToDatabase();
		// query.setQuery("SELECT * FROM GitLogFiles");
		// Instances instances = query.retrieveInstances();
		// Instances instances2 = new Instances("Name", attInfo, capacity)
//		train(instances);
	}

	private void train(Instances instances) throws Exception {
		// NaiveBayes bayes = new NaiveBayes();
		// bayes.buildClassifier(instances);
	}

}
