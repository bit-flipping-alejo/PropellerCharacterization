
import org.knowm.xchart.QuickChart;
import org.knowm.xchart.SwingWrapper;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYSeries;
import org.knowm.xchart.style.markers.SeriesMarkers;

import dataContainers.AirfoilGeometry;

public class Main {

   public static void main(String[] args) {
      AirfoilGeometry ag = new AirfoilGeometry(2.5);
      
      ag.becomeNACA4Series(2,4,1,2);
      ag.generateControlPoints();
      
      
      double x[] = new double [ag.getNumberOfPoints()];
      double y[] = new double [ag.getNumberOfPoints()];
      
      double xc[] = new double [ag.getNumberOfPoints() - 1];
      double yc[] = new double [ag.getNumberOfPoints() - 1];
      
      double thePts[][] = ag.getPoints();
      double ctrlPts[][] = ag.getControlPoints();
      
      for (int i = 0; i < ag.getNumberOfPoints(); i++) {
         x[i] = thePts[i][0];
         y[i] = thePts[i][1];
         
         if (i != ag.getNumberOfPoints() - 1) {
            xc[i] = ctrlPts[i][0];
            yc[i] = ctrlPts[i][1];
         } 
      }
      //https://knowm.org/javadocs/xchart/index.html
      XYChart chart = QuickChart.getChart("your airfoil", "cord location", "thickness", "airfoil points", x, y);      
      XYSeries series = chart.addSeries("ControlPts", xc, yc);
      series.setMarker(SeriesMarkers.DIAMOND);
      
      new SwingWrapper(chart).displayChart();
      
      
   }

}
