

import java.io.FileReader;

import org.knowm.xchart.QuickChart;
import org.knowm.xchart.SwingWrapper;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYSeries;
import org.knowm.xchart.style.markers.SeriesMarkers;

import com.opencsv.CSVReader;

import geometryContainers.AirfoilGeometry;
import geometryContainers.PropellerGeometry;
import gui.GUI;
import solvers.GoldsteinVortexTheorySolver;
import solvers.MatrixSolver;
import solvers.VortexPanelSolver;
import tests.GVTTests;
import javafx.application.Application;

public class Main {

   public static void main(String[] args) {
      //airfoilAndVPMtests();
      //gvtTests();
      guiTests( args);
      
   }

   /*GUI*/
   public static void guiTests(String[] args) {
      startGUI( args);
   }
   
   public static void startGUI(String[] args) {
      Application.launch(GUI.class, args);

   }
 
   /*
   public static void gvtTests() {
      GVTTests.testRunGVT();
   }
   */

}




















