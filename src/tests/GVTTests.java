package tests;

import org.knowm.xchart.QuickChart;
import org.knowm.xchart.SwingWrapper;
import org.knowm.xchart.XYChart;

import geometryContainers.AirfoilGeometry;
import geometryContainers.PropellerGeometry;
import solvers.GoldsteinVortexTheorySolver;

public class GVTTests {

   /*GVT Tests*/
   public GVTTests() {
      
   }
   
   public static void showChordProfile() {
      PropellerGeometry pg = new PropellerGeometry();
      pg.setRadialParameters(2.0, 0.1);
      pg.setChordParams(2.0, 4.0, 0.4, 3.0);
      pg.generateChordLengths();
      
      double[] theChords = pg.getChords();
      double[] theRads = pg.getRadiusPoints();
      
      XYChart chart = QuickChart.getChart("Blade Profile", "Rad position", "Chord Len", "chord len", theRads, theChords);      

      new SwingWrapper(chart).displayChart();
   }
   
   public static void calcRMTAngles() {
      PropellerGeometry pg = new PropellerGeometry();
      pg.setRmtParametersDeg(10, 3);
      pg.setRadialParameters(2.0, 0.1);
      pg.generateRadialPositions();
      
      double[] theAngles = pg.getRmtAngle();
      double[] theRads = pg.getRadiusPoints();
      
      XYChart chart = QuickChart.getChart("RMT Angle", "Rad position", "RMT ()rad)", "RMT", theRads, theAngles);      

      new SwingWrapper(chart).displayChart();
   }
   
   public static void testZeroLiftAlphaCalc() {
      AirfoilGeometry ag = new AirfoilGeometry(1,100);      
      ag.setangleOfAttackRad(5 * (Math.PI/180));
      ag.becomeNACA4Series(2, 4, 1, 2);
      ag.generateControlPoints();
      
      GoldsteinVortexTheorySolver gvt = new GoldsteinVortexTheorySolver();
      
      double zla = gvt.calculateZeroLiftAlpha(.0001, ag);
      
      System.out.println("=== Zero Lift Alpha ===");
      System.out.println("Alpha : " + (zla * 180 / Math.PI));
      
      
   }
   
   public static void testBetaCalc() {
      
      AirfoilGeometry af = new AirfoilGeometry();
      af.becomeNACA4Series(2, 4, 1, 2);
      
      PropellerGeometry pg = new PropellerGeometry();
      pg.setRadialParameters(2.0, 0.1);
      pg.setRmtParametersDeg(10, 5);
      pg.generateRadialPositions();
      pg.setRadialPtsToSameAirfoil(af);
      
      
      GoldsteinVortexTheorySolver gvt = new GoldsteinVortexTheorySolver();
      gvt.setPropeller(pg);
      gvt.calculateBeta_tip_rmt();
      
      double[] theBeta = gvt.getBeta_tip();
      double[] theLoc =pg.getRadiusPoints();
      
      XYChart chart = QuickChart.getChart("Beta tip Angles", "cord location", "Beta_tip", "Beta_tip",  theLoc, theBeta);      
      new SwingWrapper(chart).displayChart();
      
   }
   
   public static void testRunGVT() {
      AirfoilGeometry af = new AirfoilGeometry();
      af.becomeNACA4Series(2, 4, 1, 2);
      
      // https://www.amazon.com/DJI-Genuine-Release-Folding-Propellers/dp/B073JRWYKK
      // using this propeller
      
      PropellerGeometry pg = new PropellerGeometry();      
      pg.setGeometricWashoutDefinition(PropellerGeometry.GEOMETRICWASHOUT.PITCH_TO_DIAMETER);      
      pg.setRadialPtsToSameAirfoil(af);
      
      // ------------- DJI Spark Propeller -------------------
      // The below information taken from DJI Spark propeller
      // https://www.amazon.com/DJI-Genuine-Release-Folding-Propellers/dp/B073JRWYKK
      // Future analysis improvement: its more Thin Airfoil than 2412. consider camberline
      // of finite thickness for analysis instead of thick airfoil. could improve geometry
      // non linearities as well
      // --------------------------------
      pg.setOmega(700);                      // radians per second ~7k rpm      
      pg.setNumberOfBlades(2);
      pg.setChordLinePitch(.0762);
      pg.setRadialParameters(0.11938, 0.1);  // in meters
      pg.generateRadialPositions();
      pg.setChordParams(0.00994, .011928 , (1.0/3.0) , .00994);
      
      pg.generateChordLengths();
      
      GoldsteinVortexTheorySolver gvt = new GoldsteinVortexTheorySolver();
      gvt.setPropeller(pg);
      gvt.setVinf(0.5); // m/s
      //gvt.calculateBeta_tip();
      
      try {
         gvt.runGVT();
      } catch (Exception e) {
         // TODO Auto-generated catch block
         System.out.println("Exceeded max iterations");
         e.printStackTrace();
      }
      
      
      System.out.println("=== GVT Results ===");
      System.out.println("C_Thrust: " + gvt.getThrustCoefficient());
      System.out.println("C_Torque: " + gvt.getTorqueCoefficient());
      System.out.println("C_Power: " + gvt.getPowerCoefficient());
      
   }
   
   
}
