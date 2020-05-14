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
   
   // things that depend on eps_i
   private double[] Vb;// fluid veloc relative to cross section
   private double[] Vi;// induced velocity
   private double[] f; // prandtl's tip loss factor
   
   // internal propeller
   private PropellerGeometry propeller;
   
   // section properties (outputs of VPM)
   private VortexPanelSolver vpm;
   private double[] Cl;
   private double[] Cd;
   
   // internal airfoils   
   private double[] zeroLiftAlphas;   
   private final double epsilon = .001;
   
   
   
   
   
   public GoldsteinVortexTheorySolver() { 
      this.vpm = new VortexPanelSolver();
   }
   
   public GoldsteinVortexTheorySolver(PropellerGeometry prop) { 
      this.propeller = prop;
      this.vpm = new VortexPanelSolver();
   }
   
   public GoldsteinVortexTheorySolver(PropellerGeometry prop, double Vinfinity) { 
      this.propeller = prop;
      this.Vinf = Vinfinity;
      this.vpm = new VortexPanelSolver();
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
   
   public void calculateBeta_tip() {
      
      this.beta_tip = new double[this.propeller.getNumDescPoints()];
      
      double[] rmt = this.propeller.getRmtAngle();      
      String lastAirfoilType = "";
      double zeroLiftAngle = 0.0;
      
      for(int i = 0; i < this.propeller.getNumDescPoints(); i++) {
         AirfoilGeometry thisAf = this.propeller.getAirfoilAtRadialIndex(i);
         String thisAirfoilType = thisAf.getAirfoilType();
         
         if(thisAirfoilType.equalsIgnoreCase("custom")) {
            zeroLiftAngle = this.calculateZeroLiftAlpha(this.epsilon, thisAf);
            
         } else if( !lastAirfoilType.equalsIgnoreCase(thisAirfoilType) ) {
            zeroLiftAngle = this.calculateZeroLiftAlpha(this.epsilon, thisAf);
            lastAirfoilType = thisAf.getAirfoilType();
         } 
         
         this.beta_tip[i] = rmt[i] - zeroLiftAngle;
      }
      
   }

   
   public void runGVT() {

      //-- Lambda from eqn 2.2.2
      double[] lambda = new double[this.propeller.getNumDescPoints()];
      for(int i = 0; i < this.propeller.getNumDescPoints(); i++) {
         //lambda[i] = 2 * Math.PI * (this.propeller.getDp()/2.0) * Math.tan(this.beta_tip[i]);
         lambda[i] = 2 * Math.PI * (this.propeller.getRadiusPointIndex(i)) * Math.tan(this.beta_tip[i]);         
      }

      //-- k from eqn 2.3.41
      double[] k = new double[this.propeller.getNumDescPoints()];
      for(int i = 0; i < this.propeller.getNumDescPoints(); i++) {
         k[i] = lambda[i] / this.propeller.getDp();
      }
      
      //-- zeta from eqn 2.3.37
      double[] zeta = new double[this.propeller.getNumDescPoints()];
      for(int i = 0; i < this.propeller.getNumDescPoints(); i++) {
         zeta[i] = this.propeller.getRadiusPointIndex(i) / (this.propeller.getDp() / 2.0);
      }
      
      //-- beta from eqn 2.3.41
      this.beta_aero = new double[this.propeller.getNumDescPoints()];
      for(int i = 0; i < this.propeller.getNumDescPoints(); i++) {
         beta_aero[i] = Math.atan2(k[i], (Math.PI * zeta[i]) );
      }
      
      //-- Advance ration [J] eqn 2.3.42
      double J = 0.0;
      J = (2 * Math.PI * this.Vinf) / (this.propeller.getDp() * this.propeller.getOmega());
      
      
      //-- total down wash angle per 2.3.40
      this.eps_inf = new double[this.propeller.getNumDescPoints()];
      for(int i = 0; i < this.propeller.getNumDescPoints(); i++) {
         this.eps_inf[i] = Math.atan2(J, (Math.PI * zeta[i]) );
      }
      
      //-- Chord len ratio
      double[] cbhat = new double[this.propeller.getNumDescPoints()];
      for(int i = 0; i < this.propeller.getNumDescPoints(); i++) {
         cbhat[i] = (this.propeller.getNumberOfBlades() * this.propeller.getChordsAtIndex(i)) / this.propeller.getDp();
      }
      
      //Get section Cl and Cd
      this.calculateVPMParameters();
      
      
      
      for(int i = 0; i < this.propeller.getNumDescPoints(); i++) {
         
         double zeroGuess1 = this.propeller.getStartAngleRMT() / 3;
         double zeroGuess2 = this.propeller.getEndAngleRMT() / 3;
         
         while (true) {
            double currG1Val = getInducedEpsilonEqnOutput(i , zeroGuess1, cbhat[i], zeta[i] );
            double currG2Val = getInducedEpsilonEqnOutput(i , zeroGuess2, cbhat[i], zeta[i] );
            
            if(Math.abs(currG1Val) < this.epsilon ) {
               break;
            }
            
            double lastGuess = zeroGuess1;
            zeroGuess1 = this.secantMethodNextGuess(zeroGuess1, zeroGuess2, currG1Val, currG2Val);
            zeroGuess2 = lastGuess;
            
            
            
         } // end while
         
         this.eps_i[i] = zeroGuess1;
         
      }
   }
   
   
   private double getInducedEpsilonEqnOutput(int index, double epsIndValue, double cbhat, double zeta) {      
      
      double left = (cbhat/(8*zeta)) * this.Cl[index];      
      double right1 = Math.acos(   Math.exp( (this.propeller.getNumberOfBlades() * (1 - zeta) ) / (2 * Math.sin(this.beta_tip[this.propeller.getNumDescPoints() - 1]))   )  );      
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
   
   // eventually eps_i (induced eps) since alpha_b is a function of that as well. 
   private void calculateVPMParameters() {
      
      for (int i = 0; i < this.propeller.getNumDescPoints(); i++) {
         
         // set alpha_B
         this.propeller.getAirfoilAtRadialIndex(i).setangleOfAttackRad(this.beta_aero[i] - this.eps_inf[i]);
         AirfoilGeometry thisAirfoil = this.propeller.getAirfoilAtRadialIndex(i);
         this.vpm.setAirfoil(thisAirfoil );
         this.vpm.setVinfinity(1); // this is speed coming in, set to 1 for coeff
         this.vpm.runVPMSolver();
         
         this.Cl[i] = this.vpm.getCl() * this.propeller.getChordsAtIndex(i);
         this.Cd[i] = this.vpm.getCd() * this.propeller.getChordsAtIndex(i);
         
      }
      
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

   public double[] getF() {
      return f;
   }

   public void setF(double[] f) {
      this.f = f;
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

   public void setCl(double[] cl) {
      Cl = cl;
   }

   public double[] getCd() {
      return Cd;
   }

   public void setCd(double[] cd) {
      Cd = cd;
   }

   public double[] getZeroLiftAlphas() {
      return zeroLiftAlphas;
   }

   public void setZeroLiftAlphas(double[] zeroLiftAlphas) {
      this.zeroLiftAlphas = zeroLiftAlphas;
   }

   public double getEpsilon() {
      return epsilon;
   }
   
   
}





