

import java.io.FileReader;

import org.knowm.xchart.QuickChart;
import org.knowm.xchart.SwingWrapper;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYSeries;
import org.knowm.xchart.style.markers.SeriesMarkers;

import com.opencsv.CSVReader;

import geometryContainers.AirfoilGeometry;
import geometryContainers.PropellerGeometry;
import solvers.MatrixSolver;
import solvers.VortexPanelSolver;


public class Main {

   public static void main(String[] args) {
      airfoilAndVPMtests();
      //gvtTests();
   }

   
   /*GVT Tests*/
   public static void gvtTests() {
      //showChordProfile();
      
      calcRMTAngles();
   }
   
   public static void showChordProfile() {
      PropellerGeometry pg = new PropellerGeometry();
      pg.setRadialParameters(1.0, 0.1);
      pg.setBladeParams(2.0, 4.0, 0.4, 3.0);
      pg.generateChordLengths();
      
      double[] theChords = pg.getChords();
      double[] theRads = pg.getRadiusPoints();
      
      XYChart chart = QuickChart.getChart("Blade Profile", "Rad position", "Chord Len", "chord len", theRads, theChords);      

      new SwingWrapper(chart).displayChart();
   }
   
   public static void calcRMTAngles() {
      PropellerGeometry pg = new PropellerGeometry();
      pg.setRmtParametersDeg(10, 3);
      pg.setRadialParameters(1.0, 0.1);
      pg.generateRmtAngles();
      pg.generateRadialPositions();
      
      double[] theAngles = pg.getRmtAngle();
      double[] theRads = pg.getRadiusPoints();
      
      XYChart chart = QuickChart.getChart("RMT Angle", "Rad position", "RMT ()rad)", "RMT", theRads, theAngles);      

      new SwingWrapper(chart).displayChart();
   }
   
   /*VPM Tests*/
   public static void airfoilAndVPMtests() {
      //showAirfoil();

      //testAGaussElim();

      //testVPMSolverAirfoilFlip();

      //testVPMSolverReferencePoints();

      //testVPMSolver();
      
      testCamberline();
   }
   
   public static void showAirfoil() {


      String pathToCsv="/home/captain/sdb/CSUMB/Capstone/sw/ref/pythonVPMCode/pythonPoints.txt";

      double[][] pts = new double[99][2];
      try { 

         FileReader filereader = new FileReader(pathToCsv); 

         CSVReader csvReader = new CSVReader(filereader); 
         String[] nextRecord; 

         int ctr = 0;
         // we are going to read data line by line 
         while ((nextRecord = csvReader.readNext()) != null) { 
            Boolean xory = true;
            for (String cell : nextRecord) { 
               if (xory) {
                  pts[ctr][0] = Double.parseDouble(cell);
                  System.out.print("(" + Double.parseDouble(cell) + ","); 
               } else {
                  pts[ctr][1] = Double.parseDouble(cell);
                  System.out.print(Double.parseDouble(cell) + ")"); 
               }
               xory = ! xory;  

            } 
            ctr+=1;
            System.out.println(); 
         } 
      } 
      catch (Exception e) { 
         e.printStackTrace(); 
      } 



      AirfoilGeometry ag = new AirfoilGeometry(1, 100);
      AirfoilGeometry ag2 = new AirfoilGeometry(99, pts);

      ag.setangleOfAttackRad(5 * (Math.PI/180));
      ag2.setangleOfAttackRad(5 * (Math.PI/180));
      ag.becomeNACA4Series(2,4,1,2);
      ag.generateControlPoints();


      double x[] = new double [ag.getNumberOfPoints()];
      double y[] = new double [ag.getNumberOfPoints()];
      double camb[] = ag.getCamberLine();
      
      double xc[] = new double [ag.getNumberOfPoints()-1];
      double yc[] = new double [ag.getNumberOfPoints()-1];

      double thePts[][] = ag.getPoints();
      //double ctrlPts[][] = ag.getControlPoints();

      for (int i = 0; i < ag.getNumberOfPoints(); i++) {
         x[i] = thePts[i][0];
         y[i] = thePts[i][1];

         if (i != ag.getNumberOfPoints() - 1) {
            xc[i] = pts[i][0];
            yc[i] = pts[i][1];
         } 


      }
      //https://knowm.org/javadocs/xchart/index.html
      XYChart chart = QuickChart.getChart("your airfoil", "cord location", "thickness", "airfoil points", xc, yc);      

      XYSeries series = chart.addSeries("ControlPts", x, y);
      series.setMarker(SeriesMarkers.DIAMOND);

      XYSeries series2 = chart.addSeries("CamberLine", x, camb);
      
      new SwingWrapper(chart).displayChart();



   }

   public static void testAGaussElim() {
      double[][] testA = new double[3][3];
      double[] testB = new double [3];
      MatrixSolver ms = new MatrixSolver();

      ms.setNumRows(3);
      ms.setNumCols(3);

      testA[0][0] = 2;
      testA[0][1] = 1;
      testA[0][2] = -1;

      testA[1][0] = -3;
      testA[1][1] = -1;
      testA[1][2] = 2;

      testA[2][0] = -2;
      testA[2][1] = 1;
      testA[2][2] = 2;

      testB[0] = 8;
      testB[1] = -11;
      testB[2] = -3;

      ms.setA(testA);
      ms.setB(testB);

      ms.doGaussianEliminationNoPivot();

      double[][] postA = ms.getA();

      for (int i = 0; i < ms.getNumRows(); i++) {
         for (int j = 0; j < ms.getNumCols(); j++) {

            System.out.print(" " + postA[i][j] + " ");

         }  
         System.out.println();
      }


      double[] postX = ms.getX();

      System.out.println("-- SOLN --");
      for (int i = 0; i < ms.getNumRows(); i++) {
         System.out.println( i + " : " + postX[i]);
      }



   }

   public static void testVPMSolverAirfoilFlip() {
      AirfoilGeometry ag = new AirfoilGeometry(2.5);
      ag.setangleOfAttackRad(5 * (Math.PI/180));
      ag.becomeNACA4Series(2,4,1,2);
      ag.generateControlPoints();

      VortexPanelSolver vpm = new VortexPanelSolver(ag);
      vpm.setVinfinity(1);

      //vpm.runVPMSolver();

      AirfoilGeometry postFlip = vpm.getAirfoil();


      double x[] = new double [postFlip.getNumberOfPoints()];
      double y[] = new double [postFlip.getNumberOfPoints()];

      double xc[] = new double [postFlip.getNumberOfPoints() - 1];
      double yc[] = new double [postFlip.getNumberOfPoints() - 1];

      double thePts[][] = postFlip.getPoints();
      double ctrlPts[][] = postFlip.getControlPoints();

      for (int i = 0; i < postFlip.getNumberOfPoints(); i++) {
         x[i] = thePts[i][0];
         y[i] = thePts[i][1];

         if (i != postFlip.getNumberOfPoints() - 1) {
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

   public static void testVPMSolverReferencePoints() {

      String pathToCsv="/home/captain/sdb/CSUMB/Capstone/sw/ref/pythonVPMCode/pythonPoints.txt";

      double[][] pts = new double[20][2];
      try { 

         FileReader filereader = new FileReader(pathToCsv); 

         CSVReader csvReader = new CSVReader(filereader); 
         String[] nextRecord; 

         int ctr = 0;
         // we are going to read data line by line 
         while ((nextRecord = csvReader.readNext()) != null) { 
            Boolean xory = true;
            for (String cell : nextRecord) { 
               if (xory) {
                  pts[ctr][0] = Double.parseDouble(cell);
                  System.out.print("(" + Double.parseDouble(cell) + ","); 
               } else {
                  pts[ctr][1] = Double.parseDouble(cell);
                  System.out.print(Double.parseDouble(cell) + ")"); 
               }
               xory = ! xory;  

            } 
            ctr+=1;
            System.out.println(); 
         } 
      } 
      catch (Exception e) { 
         e.printStackTrace(); 
      } 


      AirfoilGeometry ag = new AirfoilGeometry(20, pts);

      ag.setangleOfAttackRad(5 * (Math.PI/180));

      //ag.becomeNACA4Series(2,4,1,2);
      ag.generateControlPoints();

      VortexPanelSolver vpm = new VortexPanelSolver(ag);
      vpm.setVinfinity(1);
      vpm.runVPMSolver();

      vpm.solveForVtCpCnCaClCdCm();

      double[] xHigh = new double[(int) Math.floor(ag.getNumberOfCtrlPoints()/2)];
      double[] xLow = new double[(int) Math.floor(ag.getNumberOfCtrlPoints()/2)];

      double[] cps = vpm.getCoeffOfPressure();
      double[] cpHigh = new double[(int) Math.floor(ag.getNumberOfCtrlPoints()/2)];
      double[] cpLow = new double[(int) Math.floor(ag.getNumberOfCtrlPoints()/2)];

      for (int i = 0; i < Math.floor(ag.getNumberOfCtrlPoints()/2); i++) {
         double [] hightPt = ag.getCtrlCoords(i);
         double [] lowPt = ag.getCtrlCoords( ag.getNumberOfCtrlPoints() - 1 - i );

         
         xHigh[i] = hightPt[0];
         xLow[i] = lowPt[0];
         cpHigh[i] = cps[i];
         cpLow[i] = cps[ag.getNumberOfCtrlPoints() - 1 - i ];

      }


      XYChart chart = QuickChart.getChart("Cp v X", "X", "Cp", "cpHigh", xHigh, cpHigh);   
      XYSeries series = chart.addSeries("cpLow", xLow, cpLow);
      series.setMarker(SeriesMarkers.DIAMOND);


      System.out.println("=== Results ===");
      System.out.println("Cl: " + vpm.getCl()  );
      System.out.println("Cm: " + vpm.getCm() );
      System.out.println(  );


      new SwingWrapper(chart).displayChart();



   }

   public static void testVPMSolver() {

      AirfoilGeometry ag = new AirfoilGeometry(1);

      ag.setangleOfAttackRad(5 * (Math.PI/180));

      ag.becomeNACA4Series(2,4,1,2);
      ag.generateControlPoints();

      VortexPanelSolver vpm = new VortexPanelSolver(ag);
      vpm.setVinfinity(1);
      
      vpm.runVPMSolver();

      vpm.solveForVtCpCnCaClCdCm();

      double[] xHigh = new double[(int) Math.floor(ag.getNumberOfCtrlPoints()/2)];
      double[] xLow = new double[(int) Math.floor(ag.getNumberOfCtrlPoints()/2)];

      double[] cps = vpm.getCoeffOfPressure();
      double[] cpHigh = new double[(int) Math.floor(ag.getNumberOfCtrlPoints()/2)];
      double[] cpLow = new double[(int) Math.floor(ag.getNumberOfCtrlPoints()/2)];

      for (int i = 0; i < Math.floor(ag.getNumberOfCtrlPoints()/2); i++) {
         double [] hightPt = ag.getCtrlCoords(i);
         double [] lowPt = ag.getCtrlCoords( ag.getNumberOfCtrlPoints() - 1 - i );

         xHigh[i] = hightPt[0];
         xLow[i] = lowPt[0];
         cpHigh[i] = cps[i];
         cpLow[i] = cps[ag.getNumberOfCtrlPoints() - 1 - i ];

      }


      XYChart chart = QuickChart.getChart("Cp v X", "X", "Cp", "cpHigh", xHigh, cpHigh);   
      XYSeries series = chart.addSeries("cpLow", xLow, cpLow);
      series.setMarker(SeriesMarkers.DIAMOND);


      System.out.println("=== Results ===");
      System.out.println("Cl: " + vpm.getCl()  );
      System.out.println("Cm: " + vpm.getCm() );
      System.out.println("Cd: " + vpm.getCd() );      
      System.out.println(  );


      new SwingWrapper(chart).displayChart();



   }

   public static void testCamberline(){
      AirfoilGeometry ag = new AirfoilGeometry();      
      ag.setangleOfAttackRad(5 * (Math.PI/180));
      ag.becomeNACA4Series(2, 4, 1, 2);
      double camb[] = ag.getCamberLine();
      double chord[] = ag.getCosChordPoints();
      
      ag.calcZeroLiftAlpha();
      
      System.out.println("=== Zero lift alpha ==");
      System.out.println( "Rad: " + ag.getZeroLiftAlpha() );
      System.out.println( "Deg: " + (ag.getZeroLiftAlpha() * (180/Math.PI) ) );
      
      
      XYChart chart = QuickChart.getChart("NACA 2412 Chord (0 -> pi -> 2pi)", "cord location", "thickness", "airfoil points", chord, camb);      
      new SwingWrapper(chart).displayChart();
      
   }

}




















