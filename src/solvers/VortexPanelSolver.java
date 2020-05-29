package solvers;


import dataContainers.GeometricIntegral;
import geometryContainers.AirfoilGeometry;




public class VortexPanelSolver {


   private AirfoilGeometry airfoil;
   private final double epsilon = .0000001; //max resolution
   
   private double Vinfinity;     // meters per second
   
   
   private double[] vortexStrengths;   
   private GeometricIntegral geometricIntegral;

   private double[] tangentialVeloc;
   private double[] coeffOfPressure;  
   private double[] beta;
   private double[] s;
   
   
   private double[] Cn;
   private double[] Ca;
   private double Cl;
   private double Cm;
   private double[] phi;
   
   //if there's time refine Cd using boundary layer eqns
   private double Cd;
   
   /* Constructors  */
   public VortexPanelSolver() {

   }
   public VortexPanelSolver (AirfoilGeometry airFoil) {
      this.airfoil = airFoil;

      if (! this.checkPanelOrientation() ) {
         this.flipPanelOrientation();
         System.out.println("VortexPanelSolver: Panel orientation is wrong");
      }
      this.tangentialVeloc = new double[this.airfoil.getNumberOfCtrlPoints()];
      this.coeffOfPressure = new double[this.airfoil.getNumberOfCtrlPoints()];
      this.beta = new double[this.airfoil.getNumberOfCtrlPoints()];

   }

   // prepare panels for VPM
   private Boolean checkPanelOrientation() {
      if (this.airfoil == null) {
         return false;
      }

      //double panelCoordSum[] = new double[this.airfoil.getNumberOfCtrlPoints()];
      double panelCoordSum = 0.0;
      
      for (int i = 0; i < this.airfoil.getNumberOfCtrlPoints(); i++) {
         double[] currPt = this.airfoil.getPointCoords(i);
         double[] ptPlusOne = this.airfoil.getPointCoords(i + 1);
         panelCoordSum += (ptPlusOne[0] - currPt[0]) * ( ptPlusOne[1] + currPt[1]);
      }

      //MatrixSolver ms = new MatrixSolver();
      //double res = ms.doSum(this.airfoil.getNumberOfCtrlPoints(), panelCoordSum);
      if (panelCoordSum  > 0 ) {
         return true;  
      }
      return false;

   }
   
   public void flipPanelOrientation() {

      double [][] afPts = this.airfoil.getPoints();
      int numPtsOverTwo = (int) Math.floor(this.airfoil.getNumberOfPoints() / 2);

      for (int i = 0; i < numPtsOverTwo; i++ ) {         
         double[] temp = this.airfoil.getPointCoords(i);          
         double [] bottom = this.airfoil.getPointCoords(this.airfoil.getNumberOfPoints() - 1 - i);

         this.airfoil.setPointCoords(i, bottom[0], bottom[1]);
         this.airfoil.setPointCoords(this.airfoil.getNumberOfPoints() - 1 - i, temp[0], temp[1]);         
      }

      this.airfoil.generateControlPoints();

   }


   // Solver
   public void runVPMSolver() {

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
         s[i] =  Math.sqrt( (Math.pow(dx, 2) + Math.pow(dy, 2)) );
         phi[i] = Math.atan2(dy, dx);

         if (phi[i] < 0) {
            phi[i] = phi[i] + 2 * Math.PI;
         }        

         beta[i] = phi[i] + ( Math.PI / 2 ) - this.airfoil.getangleOfAttackRad();         

         if (beta[i] > ( 2 * Math.PI ) ) {
            beta[i] = beta[i] - (2 * Math.PI);
         }
      }
      this.beta = beta;
      this.s = s;
      this.phi = phi;
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
      double [] VinfArray = this.calculateVinfinities(beta);

      this.geometricIntegral = geometricIntegral;

      //satisfy Kutta Condition in Normal Integ and Vinf array
      double[][] normalIntegWKuttaCond = geometricIntegral.getNormalIntegral();
      int index2Replace = this.airfoil.getNumberOfCtrlPoints() - 1;
      for (int i = 0; i < this.airfoil.getNumberOfCtrlPoints(); i++) {
         normalIntegWKuttaCond[index2Replace][i] = 0;
      }
      normalIntegWKuttaCond[index2Replace][0] = 1;
      normalIntegWKuttaCond[index2Replace][index2Replace] = 1;
      VinfArray[index2Replace] = 0;

      //use matrixSolver class here
      MatrixSolver matrixSolver = new MatrixSolver();
      matrixSolver.setNumRows(this.airfoil.getNumberOfCtrlPoints());
      matrixSolver.setNumCols(this.airfoil.getNumberOfCtrlPoints());
      matrixSolver.setA(normalIntegWKuttaCond);
      matrixSolver.setB(VinfArray);

      //matrixSolver.doGaussianEliminationNoPivot();
      matrixSolver.doGaussianEliminationPivot();

      this.vortexStrengths = matrixSolver.getX();
      
      //System.out.println("done with solver");
      
      this.solveForVtCpCnCaClCdCm();
      
   }

   public void solveForTangentialVelocAndCp(double[] gamma) {

      for (int i = 0; i < this.airfoil.getNumberOfCtrlPoints(); i++) {
         double rollingSum = 0;
         for (int j = 0; j < this.airfoil.getNumberOfCtrlPoints(); j++) {
            rollingSum += gamma[j]/(2*Math.PI*this.geometricIntegral.getNormalIntegralIndex(i, j));
         }   
         this.tangentialVeloc[i] = this.Vinfinity * Math.sin( this.beta[i] ) + rollingSum + (gamma[i]/2);
         this.coeffOfPressure[i] = 1 - Math.pow( (this.tangentialVeloc[i] / this.Vinfinity) , 2);
      }

   }
   
   private void solveForVtCpCnCaClCdCm() {

      this.Cn = new double[this.airfoil.getNumberOfCtrlPoints()];
      this.Ca = new double[this.airfoil.getNumberOfCtrlPoints()];
      this.tangentialVeloc = new double[this.airfoil.getNumberOfCtrlPoints()];
      this.coeffOfPressure = new double[this.airfoil.getNumberOfCtrlPoints()];
      
      for (int i = 0; i < this.airfoil.getNumberOfCtrlPoints(); i++) {
         double rollingSum = 0;
         for (int j = 0; j < this.airfoil.getNumberOfCtrlPoints(); j++) {
            rollingSum -= (this.vortexStrengths[j] / (2 * Math.PI)) * this.geometricIntegral.getTangentialIntegralIndex(i, j);
         }   
         this.tangentialVeloc[i] = this.Vinfinity * Math.sin( this.beta[i] ) + rollingSum + (this.vortexStrengths[i]/2);
         this.coeffOfPressure[i] = 1 - Math.pow( (this.tangentialVeloc[i] / this.Vinfinity) , 2);
         
         
         this.Cn[i] = -1 * this.coeffOfPressure[i] * this.s[i] * Math.sin(this.beta[i]);
         this.Ca[i] = -1 * this.coeffOfPressure[i] * this.s[i] * Math.cos(this.beta[i]);
        
         
      }
      
      MatrixSolver ms = new MatrixSolver();
      
      double[] cnCos = ms.doMatrixMultiplyByConst(this.airfoil.getNumberOfCtrlPoints(), this.Cn, Math.cos(this.airfoil.getangleOfAttackRad()) );
      double[] caSin = ms.doMatrixMultiplyByConst(this.airfoil.getNumberOfCtrlPoints(), this.Ca, Math.sin(this.airfoil.getangleOfAttackRad()) );
      
      double s1 = ms.doSum(this.airfoil.getNumberOfCtrlPoints(), cnCos);
      double s2 = ms.doSum(this.airfoil.getNumberOfCtrlPoints(), caSin);
      
      this.Cl = s1 - s2;
      
      double[] cnSin = ms.doMatrixMultiplyByConst(this.airfoil.getNumberOfCtrlPoints(), this.Cn, Math.sin(this.airfoil.getangleOfAttackRad()) );
      double[] caCos = ms.doMatrixMultiplyByConst(this.airfoil.getNumberOfCtrlPoints(), this.Ca, Math.cos(this.airfoil.getangleOfAttackRad()) );
      
      double s6 = ms.doSum(this.airfoil.getNumberOfCtrlPoints(), cnSin);
      double s7 = ms.doSum(this.airfoil.getNumberOfCtrlPoints(), caCos);
      
      this.Cd = s6 + s7;
      
      
      double[] offsetVal = ms.doMatrixAdditionByConst(this.airfoil.getNumberOfCtrlPoints(),this.airfoil.getAllCtrlPointX(), -0.25);
      double[] s3 = ms.doMatrixMultiply( this.airfoil.getNumberOfCtrlPoints() , offsetVal, this.coeffOfPressure);
      double[] s4 = ms.doMatrixMultiply( this.airfoil.getNumberOfCtrlPoints() , s3, this.s);
      double[] s5 = ms.doMatrixMultiplyCosOnMat2(this.airfoil.getNumberOfCtrlPoints(), s4, this.phi);
      
      this.Cm = ms.doSum(this.airfoil.getNumberOfCtrlPoints(), s5);
      
      
      
   }

   
   
   // Solver helper functions
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
       *    E  = (B - A^2) ^ 0.5
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
               geomInteg.setNormalIntegralIndex(i,j, 0);
               geomInteg.setTangentialIntegralIndex(i,j, 0);

            } else {

               double[] geometryPt_j = this.airfoil.getPointCoords(j);

               double xi_minus_Xj = controlPt_i[0] - geometryPt_j[0];
               double yi_minus_Yj = controlPt_i[1] - geometryPt_j[1];

               double A = (-1*xi_minus_Xj * Math.cos(phi[j])) - (yi_minus_Yj * Math.sin(phi[j]));
               double B = Math.pow( xi_minus_Xj , 2) + Math.pow( yi_minus_Yj , 2);               
               double Cn = -1 * Math.cos( phi[i] - phi[j] );                            
               double Dn = xi_minus_Xj * Math.cos(phi[i]) + yi_minus_Yj * Math.sin(phi[i]);
               double Ct = Math.sin( phi[j] - phi[i] );  
               double Dt = (xi_minus_Xj * Math.sin(phi[i])) - (yi_minus_Yj * Math.cos(phi[i]));               
               double E = Math.sqrt( (B - Math.pow(A, 2)) );

               if ( Double.isNaN(E) ) {
                  E = 0;
               }


               double leftHalf =  Math.log( ( (Math.pow(s[j], 2) + 2*A*s[j] + B) / B) );          

               double rightHalf_n = ((Dn - A*Cn)/E) * ( Math.atan2((s[j] + A), E) - Math.atan2(A, E) );                              
               double rightHalf_t = ((Dt - A*Ct)/E) * ( Math.atan2((s[j] + A), E) - Math.atan2(A, E) );               

               double normVal = ((Cn/2) *  leftHalf) + rightHalf_n  ;
               double tanVal =  ((Ct/2) *  leftHalf) + rightHalf_t  ;


               if ( Double.isNaN(normVal) ) {
                  normVal = 0;
               }
               
               if ( Double.isNaN(tanVal) ) {
                  tanVal = 0;
               }

               geomInteg.setNormalIntegralIndex(i,j, -1.0*normVal);
               geomInteg.setTangentialIntegralIndex(i,j, tanVal);
            }

            //System.out.println("Norm: " + geomInteg.getNormalIntegralIndex(i, j) + " | Tang: " + geomInteg.getTangentialIntegralIndex(i, j));

         }
         //double q = 0;
      }


      return geomInteg;
   }
   private double[] calculateVinfinities(double[] beta) {
      double[] VinfArray = new double[this.airfoil.getNumberOfCtrlPoints()];

      for (int i = 0; i < this.airfoil.getNumberOfCtrlPoints(); i++) {
         VinfArray[i] = -2.0 * Math.PI * this.Vinfinity * Math.cos(beta[i]);
         //System.out.println("Vinf[" + i + "]: " + VinfArray[i]);
      }

      return VinfArray;
   }

   
   /*Getters and Setters*/
   public AirfoilGeometry getAirfoil() {
      return airfoil;
   }
   public void setAirfoil(AirfoilGeometry airfoil) {
      this.airfoil = airfoil;
   }
   public double getVinfinity() {
      return Vinfinity;
   }
   public void setVinfinity(double vinfinity) {
      Vinfinity = vinfinity;
   }
   public double getEpsilon() {
      return epsilon;
   }
   public double[] getVortexStrengths() {
      return vortexStrengths;
   }
   public void setVortexStrengths(double[] vortexStrengths) {
      this.vortexStrengths = vortexStrengths;
   }
   public GeometricIntegral getGeometricIntegral() {
      return geometricIntegral;
   }
   public void setGeometricIntegral(GeometricIntegral geometricIntegral) {
      this.geometricIntegral = geometricIntegral;
   }
   public double[] getTangentialVeloc() {
      return tangentialVeloc;
   }
   public void setTangentialVeloc(double[] tangentialVeloc) {
      this.tangentialVeloc = tangentialVeloc;
   }
   public double[] getCoeffOfPressure() {
      return coeffOfPressure;
   }
   public void setCoeffOfPressure(double[] coeffOfPressure) {
      this.coeffOfPressure = coeffOfPressure;
   }
   public double[] getBeta() {
      return beta;
   }
   public void setBeta(double[] beta) {
      this.beta = beta;
   }
   
   public double[] getS() {
      return s;
   }
   public void setS(double[] s) {
      this.s = s;
   }
   
   public double getCl() {
      return Cl;
   }
   public void setCl(double cl) {
      Cl = cl;
   }
   public double getCm() {
      return Cm;
   }
   public void setCm(double cm) {
      Cm = cm;
   }
   public double[] getPhi() {
      return phi;
   }
   public void setPhi(double[] phi) {
      this.phi = phi;
   }
   public double[] getCn() {
      return Cn;
   }
   public void setCn(double[] cn) {
      Cn = cn;
   }
   public double[] getCa() {
      return Ca;
   }
   public void setCa(double[] ca) {
      Ca = ca;
   }
   public double getCd() {
      return Cd;
   }
   public void setCd(double cd) {
      Cd = cd;
   }


}
