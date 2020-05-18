package solvers;

import geometryContainers.AirfoilGeometry;
import geometryContainers.PropellerGeometry;

public class GoldsteinVortexTheorySolver {

   private double Vinf;       // forward speed of propeller disc
   private double[] beta_tip;     // Aerodynamic pitch angle  
   private double[] beta_aero;
   private double[] eps_i;    // advance angle, induced angle <-- target of this whole analysis
   private double[] eps_inf;  // total downwash angle
   private double[] eps_b;    // downwash angle   
   private double rho;        // density of air 1.225 kg/m3 @ Sea level
   private double[] zeta;

   private double thrustCoefficient;
   private double torqueCoefficient;
   private double powerCoefficient;


   // things that depend on eps_i
   private double[] Vb;// fluid veloc relative to cross section
   private double[] Vi;// induced velocity
   //private double[] f; // prandtl's tip loss factor

   // internal propeller
   private PropellerGeometry propeller;

   // section properties (outputs of VPM)
   private VortexPanelSolver vpm;
   private double[] Cl;
   private double[] Cd;

   // internal airfoils   
   private double[] zeroLiftAlphas;   
   private final double resolutionEpsilon = .000001;

   // convergence variables
   private final int DEFAULT_MAX_ITERATIONS = 40;
   private int maxIterations;
   private int numIterations;
   private double[] epsiConvergenceData;


   public GoldsteinVortexTheorySolver() { 
      this.vpm = new VortexPanelSolver();
      this.maxIterations = this.DEFAULT_MAX_ITERATIONS;
   }

   public GoldsteinVortexTheorySolver(PropellerGeometry prop) { 
      this.propeller = prop;
      this.vpm = new VortexPanelSolver();
      this.maxIterations = this.DEFAULT_MAX_ITERATIONS;
   }

   public GoldsteinVortexTheorySolver(PropellerGeometry prop, double Vinfinity) { 
      this.propeller = prop;
      this.Vinf = Vinfinity;
      this.vpm = new VortexPanelSolver();
      this.maxIterations = this.DEFAULT_MAX_ITERATIONS;
   }

   public double[] calculateAllPropellerZeroLiftAlpha() {
      double[] zla = new double[this.propeller.getNumDescPoints()];
      String lastAirfoilType = "";
      double zeroLiftAngle = 0.0;
      for(int i = 0; i < this.propeller.getNumDescPoints(); i++) {

         AirfoilGeometry thisAf = this.propeller.getAirfoilAtRadialIndex(i);
         String thisAirfoilType = thisAf.getAirfoilType();

         if(thisAirfoilType.equalsIgnoreCase("custom")) {
            zeroLiftAngle = this.calculateZeroLiftAlpha(this.resolutionEpsilon, thisAf);

         } else if( !lastAirfoilType.equalsIgnoreCase(thisAirfoilType) ) {
            zeroLiftAngle = this.calculateZeroLiftAlpha(this.resolutionEpsilon, thisAf);
            lastAirfoilType = thisAf.getAirfoilType();
         } 


         zla[i] = zeroLiftAngle;

      }

      return zla;
   }

   public double calculateZeroLiftAlpha(double eps, AirfoilGeometry af) {
      //returns alpha
      double zeroLiftAlpha = 0.0;
      double prevAfAlpha = af.getangleOfAttackRad();

      VortexPanelSolver zvpm = new VortexPanelSolver();
      zvpm.setVinfinity(1);

      boolean inflectionDetect = true;
      boolean wasLastRoundPositive = true;

      double stepSize = 2.0 * (Math.PI / 180);

      while (true) {

         af.setangleOfAttackRad(zeroLiftAlpha);
         zvpm.setAirfoil(af);         
         zvpm.runVPMSolver();

         if(zvpm.getCl() > +0.0) {                 
            if(!wasLastRoundPositive) {
               stepSize /= 2;
            }            
            wasLastRoundPositive = true;
            zeroLiftAlpha -= stepSize;            
         } else {            
            if(wasLastRoundPositive) {
               stepSize /= 2;
            }            
            wasLastRoundPositive = false;
            zeroLiftAlpha += stepSize;            
         }

         //System.out.println("alpha(rad):" + zeroLiftAlpha + " | alpha(Deg):" + zeroLiftAlpha * (Math.PI/180) + 
         //      " | stepSize:" + stepSize + " | wasLastRoundPositive:" + wasLastRoundPositive + " | Cl:" + zvpm.getCl());

         if( Math.abs(zvpm.getCl()) < eps) {
            break;
         }

      }

      af.setangleOfAttackRad(prevAfAlpha);
      return zeroLiftAlpha;
   }

   public void calculateBeta_tip_rmt() {

      this.beta_tip = new double[this.propeller.getNumDescPoints()];

      double[] rmt = this.propeller.getRmtAngle();      
      String lastAirfoilType = "";
      double zeroLiftAngle = 0.0;

      for(int i = 0; i < this.propeller.getNumDescPoints(); i++) {
         AirfoilGeometry thisAf = this.propeller.getAirfoilAtRadialIndex(i);
         String thisAirfoilType = thisAf.getAirfoilType();

         if(thisAirfoilType.equalsIgnoreCase("custom")) {
            zeroLiftAngle = this.calculateZeroLiftAlpha(this.resolutionEpsilon, thisAf);

         } else if( !lastAirfoilType.equalsIgnoreCase(thisAirfoilType) ) {
            zeroLiftAngle = this.calculateZeroLiftAlpha(this.resolutionEpsilon, thisAf);
            lastAirfoilType = thisAf.getAirfoilType();
         } 

         this.beta_tip[i] = rmt[i] - zeroLiftAngle;
      }

   }



   public void runGVT() throws Exception {

      //-- zeta from eqn 2.3.37
      this.zeta = new double[this.propeller.getNumDescPoints()];
      for(int i = 0; i < this.propeller.getNumDescPoints(); i++) {
         this.zeta[i] = this.propeller.getRadiusPointIndex(i) / (this.propeller.getDp() / 2.0);
      }

      //-- Geometric Washout + Lambda + K calculation
      double[] k = new double[this.propeller.getNumDescPoints()];
      double[] lambda = new double[this.propeller.getNumDescPoints()];
      if (this.propeller.getGeometricWashoutDefinition() == PropellerGeometry.GEOMETRICWASHOUT.RMT) {

         //-- Lambda from eqn 2.2.2         
         for(int i = 0; i < this.propeller.getNumDescPoints(); i++) {
            //lambda[i] = 2 * Math.PI * (this.propeller.getDp()/2.0) * Math.tan(this.beta_tip[i]);
            lambda[i] = 2 * Math.PI * (this.propeller.getRadiusPointIndex(i)) * Math.tan(this.beta_tip[i]);         
         }

         //-- k from eqn 2.3.41         
         for(int i = 0; i < this.propeller.getNumDescPoints(); i++) {
            k[i] = lambda[i] / this.propeller.getDp();
         }   

      } else {

         double pToDRatio = this.propeller.getPitchToDiameterRatio();
         double[] zeroLiftAlphas = this.calculateAllPropellerZeroLiftAlpha();
         //-- k from eqn 2.2.3
         for(int i = 0; i < this.propeller.getNumDescPoints(); i++) {
            k[i] = (Math.PI * this.zeta[i]) * ( (pToDRatio - Math.PI * this.zeta[i] * Math.tan(zeroLiftAlphas[i]))  / (Math.PI*this.zeta[i] + pToDRatio*Math.tan(zeroLiftAlphas[i])) ); 
         }

      }

      //-- beta from eqn 2.3.41
      this.beta_aero = new double[this.propeller.getNumDescPoints()];
      for(int i = 0; i < this.propeller.getNumDescPoints(); i++) {
         beta_aero[i] = Math.atan2(k[i], (Math.PI * this.zeta[i]) );
      }

      //-- Advance ration [J] eqn 2.3.42
      double J = 0.0;
      J = (2 * Math.PI * this.Vinf) / (this.propeller.getDp() * this.propeller.getOmega());


      //-- total down wash angle per 2.3.40
      this.eps_inf = new double[this.propeller.getNumDescPoints()];
      for(int i = 0; i < this.propeller.getNumDescPoints(); i++) {
         this.eps_inf[i] = Math.atan2(J, (Math.PI * this.zeta[i]) );
      }

      //-- Chord len ratio
      double[] cbhat = new double[this.propeller.getNumDescPoints()];
      for(int i = 0; i < this.propeller.getNumDescPoints(); i++) {
         cbhat[i] = (this.propeller.getNumberOfBlades() * this.propeller.getChordsAtIndex(i)) / this.propeller.getDp();
      }




      // Secant method Root finder
      //    finding value of eps_i to solve:
      //    (cb_hat / (8*zeta) ) * C_L( alpha , zeta ) - acos( exp( -k(1-zeta)/(2*sin(beta_tip))))*tan(eps_i)*sin(eps_inf + eps_i) = 0
      //    
      //    notice in the acos term if zeta = 1, then the numerator of the exponent 0, so exp^0 equals 1, 
      //    and acos(1) == 0. which negates the contribution of any eps_i term. this means the value of the function is
      //    pinned to (cb_hat / (8*zeta) ) for all values of eps_i. the secant method then fails as it needs a difference
      //    of values to calculate the next steps. as such, it is the opinion of the developer that this is meant to happen
      //    for (N - 1) points.
      //
      // Convergence Monitoring
      //    in order to avoid infinite loops a max outer iteration limit is set. by default this is 40, about 5x greater than 
      //    has been needed in testing. however this is user editable. an array with convergence numbers is saved for display
      //    in post processing
      //
      this.eps_i = new double[this.propeller
                              .getNumDescPoints() - 1];
      this.epsiConvergenceData = new double[this.maxIterations]; 
      double magDelta = 100;     //initial large value 
      this.numIterations = 0;

      while ( magDelta > this.resolutionEpsilon ) {
         //System.out.println("=== OutCount:" + this.numIterations + " Begin ===");

         // enforce DEEP copy
         double[] oldEpsI = new double[this.propeller.getNumDescPoints() - 1];
         for(int p = 0; p < (this.propeller.getNumDescPoints() - 1); p++) {
            oldEpsI[p] = this.eps_i[p];
         }

         //Get section Cl and Cd
         this.calculateVPMParameters();

         for(int i = 0; i < (this.propeller.getNumDescPoints() - 1); i++) {
            double zeroGuess1 = this.beta_aero[0] / 3;
            double zeroGuess2 = this.beta_aero[this.propeller.getNumDescPoints() - 1] / 3;

            //System.out.println("\t OutCount:" + this.numIterations + " Iter#" + i + "  ===");
            int innerCounter = 0;
            while (true) {               
               double currG1Val = getInducedEpsilonEqnOutput(i , zeroGuess1, cbhat[i], this.zeta[i] );
               double currG2Val = getInducedEpsilonEqnOutput(i , zeroGuess2, cbhat[i], this.zeta[i] );
               if(Math.abs(currG1Val) < this.resolutionEpsilon ) {
                  //System.out.println("\t \t Epsilon: " + zeroGuess1 + " Val[N-1]: " + currG1Val );
                  break;
               }
               double lastGuess = zeroGuess1;
               zeroGuess1 = this.secantMethodNextGuess(zeroGuess1, zeroGuess2, currG1Val, currG2Val);
               zeroGuess2 = lastGuess;

               innerCounter++;
               if (innerCounter > this.maxIterations) {
                  throw new Exception("Secant Method exceeed Max Iterations");
               }
            } // end while
            this.eps_i[i] = zeroGuess1;
         }

         magDelta = this.calcMagDifference( (this.propeller.getNumDescPoints()-1) , oldEpsI, this.eps_i );   
         this.epsiConvergenceData[this.numIterations] = magDelta;
         this.numIterations++;
         //System.out.println("=== OutCount:" + this.numIterations + " Done. MagDelta: " + magDelta + " ===");

         if (this.numIterations > this.maxIterations) {
            throw new Exception("Induced Epsilon loop exceeed Max Iterations");
         }

      } // end eps_i while




      // calc dCT/dZeta
      double[] dCTdZeta = new double[this.propeller.getNumDescPoints() - 1];
      // calc Cl (l as in torque not lift)
      double[] dCldZeta = new double[this.propeller.getNumDescPoints() - 1];

      for(int i = 0; i < (this.propeller.getNumDescPoints() - 1); i++) {
         double cosPart = ( Math.pow( Math.cos(this.eps_i[i]), 2) / Math.pow(Math.cos(this.eps_inf[i]), 2) );

         dCTdZeta[i] = ( Math.pow(Math.PI,2.0) / 4 ) * Math.pow(this.zeta[i], 2.0) * cbhat[i] * cosPart * ( this.Cl[i] * Math.cos(this.eps_inf[i] + this.eps_i[i]) - this.Cd[i] * Math.sin(this.eps_inf[i] + this.eps_i[i])  );         
         dCldZeta[i] = ( Math.pow(Math.PI,2.0) / 8 ) * Math.pow(this.zeta[i], 3.0) * cbhat[i] * cosPart * ( this.Cd[i] * Math.cos(this.eps_inf[i] + this.eps_i[i]) + this.Cl[i] * Math.sin(this.eps_inf[i] + this.eps_i[i])  );
      }

      if ( (this.propeller.getNumDescPoints() - 1) % 2 == 1 ) {
         
         // is odd do trapezoidal Rule Integration
         double deltaXovr2 = (this.zeta[1] - this.zeta[0]) / 2.0;
         double rollSumCT = 0.0;
         double rollSumCl = 0.0;
         
         for(int i = 1; i < ((this.propeller.getNumDescPoints() - 1) - 1); i++) {
            rollSumCT += 2 * dCTdZeta[i];
            rollSumCl += 2 * dCldZeta[i];
         }
         
         this.thrustCoefficient = deltaXovr2 * ( dCTdZeta[0] + rollSumCT + dCTdZeta[this.propeller.getNumDescPoints() - 1 - 1] );         
         this.torqueCoefficient = deltaXovr2 * ( dCldZeta[0] + rollSumCl + dCldZeta[this.propeller.getNumDescPoints() - 1 - 1] );
         this.powerCoefficient = 2 * Math.PI * this.torqueCoefficient;
         
      } else {
         // is even do Simpsons Rule integration
         double deltaXovr3 = (this.zeta[1] - this.zeta[0]) / 3.0;
         boolean is4 = true;
         double rollSumCT = 0.0;
         double rollSumCl = 0.0;
         for(int i = 1; i < ((this.propeller.getNumDescPoints() - 1) - 1); i++) {

            if (is4) {
               rollSumCT += 4 * dCTdZeta[i];
               rollSumCl += 4 * dCldZeta[i];
            } else {
               rollSumCT += 2 * dCTdZeta[i];
               rollSumCl += 4 * dCldZeta[i];
            }
            is4 = !is4;

         }

         this.thrustCoefficient = deltaXovr3 * ( dCTdZeta[0] + rollSumCT + dCTdZeta[this.propeller.getNumDescPoints() - 1] );         
         this.torqueCoefficient = deltaXovr3 * ( dCldZeta[0] + rollSumCl + dCldZeta[this.propeller.getNumDescPoints() - 1] );
         this.powerCoefficient = 2 * Math.PI * this.torqueCoefficient;
      }

      System.out.println("-------------");
      System.out.println("Done with GVT");
      System.out.println("-------------");
      
   }


   private double getInducedEpsilonEqnOutput(int index, double epsIndValue, double cbhat, double zeta) {      

      double left = (cbhat/(8*zeta)) * this.Cl[index];      
      double right1 = Math.acos(   Math.exp((-1*this.propeller.getNumberOfBlades() * (1 - zeta)) / (2 * Math.sin(this.beta_aero[this.propeller.getNumDescPoints() - 1] + this.eps_inf[index]))   )  );      

      double right2 = Math.tan(epsIndValue) * Math.sin(this.eps_inf[index] + epsIndValue);      
      double result = left - (right1 * right2);

      return result;
   }

   // guess 1 = n-1, guess 2 = n-2
   private double secantMethodNextGuess( double guess1, double guess2, double val1, double val2) {

      return guess1 - val1 * ( (guess1 - guess2) / ( val1 - val2 ) );

   }


   //
   // Cl / Cd calculations
   //

   private void calculateVPMParameters() {
      this.Cl = new double [this.propeller.getNumDescPoints()];
      this.Cd = new double [this.propeller.getNumDescPoints()];

      double rad2deg = (180.0/Math.PI);
      for (int i = 0; i < this.propeller.getNumDescPoints() - 1; i++) {

         // set alpha_B
         this.propeller.getAirfoilAtRadialIndex(i).setangleOfAttackRad(this.beta_aero[i] - this.eps_inf[i] - this.eps_i[i]);
         AirfoilGeometry thisAirfoil = this.propeller.getAirfoilAtRadialIndex(i);
         this.vpm.setAirfoil(thisAirfoil );
         this.vpm.setVinfinity(1); // this is speed coming in, set to 1 for coeff
         this.vpm.runVPMSolver();

         //System.out.println("Beta Aero:" + (this.beta_aero[i]*rad2deg) + " | eps inf: " + 
         //(this.eps_inf[i]*rad2deg) + " | Cl:" + this.vpm.getCl() + " | Chord: " + this.propeller.getChordsAtIndex(i));

         this.Cl[i] = this.vpm.getCl() * this.propeller.getChordsAtIndex(i);
         this.Cd[i] = this.vpm.getCd() * this.propeller.getChordsAtIndex(i);

      }

   }

   private double calcMagDifference(int size, double[] ary1, double[] ary2) {
      double result = 0.0;
      for (int i = 0; i < size; i++) {
         result = result + Math.pow( (ary1[i] - ary2[i]) , 2 );
      }
      result = Math.sqrt(result);
      return result;
   }

   //
   // Getters and Setters
   //

   public double getVinf() {
      return Vinf;
   }

   public void setVinf(double vinf) {
      Vinf = vinf;
   }

   public double[] getBeta_tip() {
      return beta_tip;
   }

   public void setBeta_tip(double[] beta_tip) {
      this.beta_tip = beta_tip;
   }

   public double[] getEps_i() {
      return eps_i;
   }

   public void setEps_i(double[] eps_i) {
      this.eps_i = eps_i;
   }

   public double[] getEps_inf() {
      return eps_inf;
   }

   public void setEps_inf(double[] eps_inf) {
      this.eps_inf = eps_inf;
   }

   public double[] getEps_b() {
      return eps_b;
   }

   public void setEps_b(double[] eps_b) {
      this.eps_b = eps_b;
   }

   public double getRho() {
      return rho;
   }

   public void setRho(double rho) {
      this.rho = rho;
   }

   public double[] getVb() {
      return Vb;
   }

   public void setVb(double[] vb) {
      Vb = vb;
   }

   public double[] getVi() {
      return Vi;
   }

   public void setVi(double[] vi) {
      Vi = vi;
   }



   public PropellerGeometry getPropeller() {
      return propeller;
   }

   public void setPropeller(PropellerGeometry propeller) {
      this.propeller = propeller;
   }

   public VortexPanelSolver getVpm() {
      return vpm;
   }

   public void setVpm(VortexPanelSolver vpm) {
      this.vpm = vpm;
   }

   public double[] getCl() {
      return Cl;
   }

   public double[] getCd() {
      return Cd;
   }

   public double[] getZeroLiftAlphas() {
      return zeroLiftAlphas;
   }

   public void setZeroLiftAlphas(double[] zeroLiftAlphas) {
      this.zeroLiftAlphas = zeroLiftAlphas;
   }


   public double[] getZeta() {
      return zeta;
   }


   public double getResolutionEpsilon() {
      return resolutionEpsilon;
   }

   public int getMaxIterations() {
      return maxIterations;
   }

   public void setMaxIterations(int maxIterations) {
      this.maxIterations = maxIterations;
   }

   public int getNumIterations() {
      return numIterations;
   }

   public void setNumIterations(int numIterations) {
      this.numIterations = numIterations;
   }

   public double[] getEpsiConvergenceData() {
      return epsiConvergenceData;
   }

   public void setEpsiConvergenceData(double[] epsiConvergenceData) {
      this.epsiConvergenceData = epsiConvergenceData;
   }

   public double getThrustCoefficient() {
      return thrustCoefficient;
   }

   public double getTorqueCoefficient() {
      return torqueCoefficient;
   }

   public double getPowerCoefficient() {
      return powerCoefficient;
   }




}





