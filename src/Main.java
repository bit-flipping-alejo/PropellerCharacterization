import org.knowm.xchart.QuickChart;
import org.knowm.xchart.SwingWrapper;
import org.knowm.xchart.XYChart;

import dataContainers.AirfoilGeometry;

public class Main {

   public static void main(String[] args) {
      AirfoilGeometry ag = new AirfoilGeometry(1);
      
      ag.becomeNACA4Series(2,4,1,2);

      
      double x[] = new double [ag.getNumberOfPoints()];
      double y[] = new double [ag.getNumberOfPoints()];
      double thePts[][] = ag.getPoints();
      
      for (int i = 0; i < ag.getNumberOfPoints(); i++) {
         x[i] = thePts[i][0];
         y[i] = thePts[i][1];
      }
      
      XYChart chart = QuickChart.getChart("your airfoil", "cord location", "thickness", "airfoil points", x, y);
      new SwingWrapper(chart).displayChart();
      
      
   }

}
