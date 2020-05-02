package solvers;

import java.util.Collections;

import dataContainers.AirfoilGeometry;

/* http://www.joshtheengineer.com/2020/04/27/vortex-panel-method-airfoil/
 * https://www.youtube.com/watch?v=JL2fz-xTTT0
 *  look at phi[i] in geometric properties of airfoil
 *  
 * */
public class VortexPanelSolver {

   
   private AirfoilGeometry airfoil;
   
   public VortexPanelSolver() {
      
   }
   
   public VortexPanelSolver (AirfoilGeometry airFoil) {
      this.airfoil = airFoil;
      
      if (! this.checkPanelOrientation() ) {
         this.flipPanelOrientation();
         System.out.println("VortexPanelSolver: Panel orientation is wrong");
      }
      
   }
   
   public Boolean runVPMSolver() {
      
      double[] s = new double[this.airfoil.getNumberOfCtrlPoints()];
      double[] phi = new double[this.airfoil.getNumberOfCtrlPoints()];
      double[] beta = new double[this.airfoil.getNumberOfCtrlPoints()];
      
      for (int i = 0; i < this.airfoil.getNumberOfCtrlPoints(); i++) {
         double[] currPt = this.airfoil.getPointCoords(i);
         double[] ptPlusOne = this.airfoil.getPointCoords(i + 1);
         
         // Numerical integration variables
         // Calculate each panels: 
         //    x length, 
         //    y height, 
         //    s total length, 
         //    phi the angle the panel makes with the X axis
         //    beta, the angle the panel normal makes with freestream veloc9
         double dx = (ptPlusOne[0] - currPt[0]);
         double dy = (ptPlusOne[1] - currPt[1]);
         s[i] =  Math.pow( Math.pow(dx, 2) + Math.pow(dy, 2), 0.5);
         phi[i] = Math.atan2(dy, dx);
         
      }
      
      
      return true;
   }
   
   
   /*private functions*/
   private Boolean checkPanelOrientation() {
      if (this.airfoil == null) {
         return false;
      }
      
      double panelCoordSum = 0;
      for (int i = 0; i < this.airfoil.getNumberOfCtrlPoints(); i++) {
         double[] currPt = this.airfoil.getPointCoords(i);
         double[] ptPlusOne = this.airfoil.getPointCoords(i + 1);
         panelCoordSum += (currPt[0] - ptPlusOne[0]) * (currPt[1] - ptPlusOne[1]);
      }
      
      if (panelCoordSum > 0 ) {
       return true;  
      }
      return false;
      
   }
   
   //TODO finish panel flipping orientation
   private void flipPanelOrientation() {
      /*
      double [][] afPts = this.airfoil.getPoints();
      double [][] ctrlPts = this.airfoil.getControlPoints();
      
         
      for (int i = 0; i < (this.airfoil.getNumberOfPoints() / 2); i++ ) {
         Collections.swap(afPts, i,  this.airfoil.getNumberOfPoints() - 1 - i );
         Collections.swap(ctrlPts, i, this.airfoil.getNumberOfCtrlPoints() - 1 - i);
      }
      */
   }
   
   
   
   
   
   
}
