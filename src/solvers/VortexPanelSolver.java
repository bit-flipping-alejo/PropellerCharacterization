package solvers;

import java.util.Collections;

import dataContainers.GeometricIntegral;
import geometryContainers.AirfoilGeometry;



/* http://www.joshtheengineer.com/2020/04/27/vortex-panel-method-airfoil/
 * https://www.youtube.com/watch?v=JL2fz-xTTT0
 *  look at phi[i] in geometric properties of airfoil
 *  
 * */
public class VortexPanelSolver {


   private AirfoilGeometry airfoil;
   private final double epsilon = .0000001; //max resolution

   /* * * * * * * * * * * * * 
    * Constructors 
    * * * * * * * * * * * * */

   public VortexPanelSolver() {

   }

   public VortexPanelSolver (AirfoilGeometry airFoil) {
      this.airfoil = airFoil;

      if (! this.checkPanelOrientation() ) {
         this.flipPanelOrientation();
         System.out.println("VortexPanelSolver: Panel orientation is wrong");
      }

   }

   /* * * * * * * * * * * * * 
    * Solver 
    * * * * * * * * * * * * */
   public Boolean runVPMSolver() {

      // Numerical integration variables
      // Calculate each panels: 
      //    x length, 
      //    y height, 
      //    s total length, 
      //    phi the angle the panel makes with the X axis
      //    beta, the angle the panel normal makes with freestream veloc
      double[] s = new double[this.airfoil.getNumberOfCtrlPoints()];
      double[] phi = new double[this.airfoil.getNumberOfCtrlPoints()];
      double[] beta = new double[this.airfoil.getNumberOfCtrlPoints()];

      for (int i = 0; i < this.airfoil.getNumberOfCtrlPoints(); i++) {
         double[] currPt = this.airfoil.getPointCoords(i);
         double[] ptPlusOne = this.airfoil.getPointCoords(i + 1);

         double dx = (ptPlusOne[0] - currPt[0]);
         double dy = (ptPlusOne[1] - currPt[1]);
         s[i] =  Math.pow( Math.pow(dx, 2) + Math.pow(dy, 2), 0.5);
         phi[i] = Math.atan2(dy, dx);

         if (phi[i] < 0) {
            phi[i] = phi[i] + 2 * Math.PI;
         }        
         
         beta[i] = phi[i] + ( Math.PI / 2 ) - this.airfoil.getangleOfAttackRad();         
         
         if (beta[i] > ( 2 * Math.PI ) ) {
            beta[i] = beta[i] - (2 * Math.PI);
         }
      }
      
      
      /* * * * * * * * * * * * * * * * * * * * * * * * * * *
       * Geometric integral
       *    represents
       * 
       *    integ thru panel j of ( d(theta_ij)/d(n_i) ) ds_j
       * 
       *   where i loops thru the control points, and j 
       *   loops thru all panels i != j
       * * * * * * * * * * * * * * * * * * * * * * * * * * */
      GeometricIntegral geometricIntegral = this.calculateGeometricIntegral(s, phi);

      

      return true;
   }


   /* * * * * * * * * * * * * 
    * Private Functions 
    * * * * * * * * * * * * */
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
   

   private GeometricIntegral calculateGeometricIntegral(double[] s, double[] phi) {
      
      /* Calculation Convention
       *    i = index of control point
       *    j = loop over all panels != i, if j==i call it 0
       *    
       *    Components of solved integral
       *    A  = - (x_i - X_j) cos(phi_j) - (y_i - Y_j) sin(phi_j)
       *    B  = (x_i - X_j)^2 - (y_i - Y_j)^2
       *    Cn = - cos(phi_i - phi_j)
       *    Ct = sin (phi_j - phi_i)
       *    Dn = (x_i - X_j) cos(phi_i) + (y_i - Y_j) sin(phi_j)
       *    Dt = (x_i - X_j) sin(phi_i) - (y_i - Y_j) cos(phi_i)
       *    E  = (B - A) ^ 0.5
       *    
       *    geometricIntegral = (C_n,t / 2) * ( ln( s_j^2 + 2*A*s_j + B / B) + ((D_n,t - A*C) / E)(atan( (s_j+A) /E ) - atan(A/E) ) )  
       *    
       * * * * * * * * */
      GeometricIntegral geomInteg = new GeometricIntegral(this.airfoil.getNumberOfCtrlPoints());
      
      for (int i = 0; i < this.airfoil.getNumberOfCtrlPoints(); i++) {
         
         double[] controlPt_i = this.airfoil.getCtrlCoords(i);
         
         for (int j = 0; j < this.airfoil.getNumberOfCtrlPoints(); j++) {
            
            if (i == j) {
               // is zero
               geomInteg.setNormalIntegralIndex(i, 0);
               geomInteg.setTangentialIntegralIndex(i, 0);
               
            } else {
               
               double[] geometryPt_j = this.airfoil.getPointCoords(j);
               
               double xi_minus_Xj = controlPt_i[0] - geometryPt_j[0];
               double yi_minus_Yj = controlPt_i[1] - geometryPt_j[1];
               
               double A = xi_minus_Xj * Math.cos(phi[j]) - yi_minus_Yj * Math.sin(phi[j]);
               double B = Math.pow( xi_minus_Xj , 2) + Math.pow( yi_minus_Yj , 2);               
               double Cn = -1 * Math.cos( phi[i] - phi[j] );
               double Ct = Math.sin( phi[j] - phi[i] );               
               double Dn = xi_minus_Xj * Math.cos(phi[i]) + yi_minus_Yj * Math.sin(phi[j]);
               double Dt = xi_minus_Xj * Math.sin(phi[i]) - yi_minus_Yj * Math.cos(phi[i]);               
               double E = Math.sqrt(B - A);
               
               if ( Double.isNaN(E) ) {
                  E = this.epsilon;
               }
               

               double leftHalf =  Math.log( ( (Math.pow(s[j], 2) + 2*A*s[j] + B) / B) );          
               
               double rightHalf_n = ((Dn - A*Cn)/E) * ( Math.atan2(s[j] + A, E) - Math.atan2(A, E) );                              
               double rightHalf_t = ((Dt - A*Cn)/E) * ( Math.atan2(s[j] + A, E) - Math.atan2(A, E) );               
               
               double normVal = (Cn/2) * ( leftHalf + rightHalf_n ) ;
               double tanVal = (Ct/2) * ( leftHalf + rightHalf_t ) ;
               
               
               
               geomInteg.setNormalIntegralIndex(i, normVal);
               geomInteg.setTangentialIntegralIndex(i, tanVal);
            }
            
         }
         
      }
      
      
      return geomInteg;
   }




}
