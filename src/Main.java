
import org.knowm.xchart.QuickChart;
import org.knowm.xchart.SwingWrapper;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYSeries;
import org.knowm.xchart.style.markers.SeriesMarkers;

import geometryContainers.AirfoilGeometry;


public class Main {

   public static void main(String[] args) {
      //showAirfoil();
      
      testAGaussElim();
      

   }

   public static void showAirfoil() {
      AirfoilGeometry ag = new AirfoilGeometry(2.5);

      ag.setangleOfAttackRad(5 * (Math.PI/180));

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

         /*
         if (i > ag.getNumberOfPoints() / 4) {
            break;
         }
          */ 
      }
      //https://knowm.org/javadocs/xchart/index.html
      XYChart chart = QuickChart.getChart("your airfoil", "cord location", "thickness", "airfoil points", x, y);      
      XYSeries series = chart.addSeries("ControlPts", xc, yc);
      series.setMarker(SeriesMarkers.DIAMOND);

      new SwingWrapper(chart).displayChart();
   }

   public static void testAGaussElim() {
      double[][] testA = new double[3][3];
      double[] testB = new double [3];
      
      testA[0][1] = 2;
      testA[0][2] = 1;
      testA[0][3] = -1;
      
      testA[1][1] = -3;
      testA[1][2] = -1;
      testA[1][3] = 2;
      
      testA[2][1] = -2;
      testA[2][2] = 1;
      testA[2][3] = 2;
      
      testB[0] = 8;
      testB[1] = -11;
      testB[2] = -3;
      
      
   }
}
