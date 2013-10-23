package edu.rit.se.history.httpd.parse;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.List;
import java.util.ArrayList;

import javax.swing.JFrame;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

public class XYPlotSample extends JFrame {
  
  private static final long serialVersionUID = 1L;

  public static void main(String[] args) {
    XYPlotSample frame = new XYPlotSample();

    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.setBounds(10, 10, 500, 500);
    frame.setTitle("Sample graph");
    frame.setVisible(true);
  }

  public XYPlotSample() {
    JFreeChart chart = createChart();
    
    try {
      //ChartUtilities.saveChartAsPNG(new File("test.png"), chart, 300, 300);
    } catch (Exception e) {
      e.printStackTrace();
    }
    
    ChartPanel cpanel = new ChartPanel(chart);
    getContentPane().add(cpanel, BorderLayout.CENTER);
  }
  
  private JFreeChart createChart() {

    ArrayList<Dot> dots = new ArrayList<Dot>();
    double xdata1[] = {0.05, 0.20, 0.34, 0.45, 0.5, 0.7, 0.9, 1.0};
    double ydata1[] = {0.94, 0.80, 0.69, 0.44, 0.31, 0.25, 0.01, 0.0};
    for ( int i=0; i<xdata1.length; i++ ) {
      Dot d = new Dot();
      d.x = xdata1[i];
      d.y = ydata1[i];
      dots.add(d);
    }

    XYDataset data = (XYDataset)createData("component", dots);
    JFreeChart chart = 
      ChartFactory.createXYLineChart("P-R curve", "Recall", "Precision",
                                     data, PlotOrientation.VERTICAL, 
                                     true, true, false);
    XYPlot plot = chart.getXYPlot();
//    StandardXYItemRenderer renderer = (StandardXYItemRenderer) plot.getRenderer();
    Font font  = new Font( "Meiryo", Font.PLAIN, 12);
    Font font2 = new Font( "Meiryo", Font.PLAIN, 8);
    chart.getLegend().setItemFont(font);
    chart.getTitle().setFont(font);
    XYPlot xyp = chart.getXYPlot();
    xyp.getDomainAxis().setLabelFont(font); // X
    xyp.getRangeAxis().setLabelFont(font); // Y
    xyp.getDomainAxis().setRange(-610,610);
    xyp.getRangeAxis().setRange(-610,610);
    xyp.getDomainAxis().setTickLabelFont(font2);
    xyp.getRangeAxis().setTickLabelFont(font2);
    xyp.getDomainAxis().setVerticalTickLabels(true);
    xyp.getDomainAxis().setFixedAutoRange(100);
    xyp.getRangeAxis().setFixedAutoRange(100);
    
    // fill and outline
    XYLineAndShapeRenderer r = (XYLineAndShapeRenderer)plot.getRenderer();
    r.setSeriesOutlinePaint(0, Color.RED);
    r.setSeriesOutlinePaint(1, Color.BLUE);
    r.setSeriesShapesFilled(0, false);
    r.setSeriesShapesFilled(1, false);
    
    return chart;
  }
  
  private XYSeriesCollection createData(String componentId, ArrayList<Dot> dots){
    XYSeriesCollection data = new XYSeriesCollection();

    XYSeries series1 = new XYSeries(componentId);

    for (int i = 0 ; i < dots.size() ; i++){
      series1.add(dots.get(i).y, dots.get(i).x);
    }

    data.addSeries(series1);

    return data;
  }
  
  static class Dot {
    double x;
    double y;
  }
}