package solvers;

import geometryContainers.AirfoilGeometry;
import geometryContainers.PropellerGeometry;

public class GoldsteinVortexTheorySolver {
   
   
   
   private double Vinf;       // forward speed of propeller disc
   private double[] beta_tip;     // Aerodynamic pitch angle   
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
   
   
   
   public GoldsteinVortexTheorySolver() { }
   
   public GoldsteinVortexTheorySolver(PropellerGeometry prop) { 
      this.propeller = prop;
   }
   
   public GoldsteinVortexTheorySolver(PropellerGeometry prop, double Vinfinity) { 
      this.propeller = prop;
      this.Vinf = Vinfinity;
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
         lambda[i] = 2 * Math.PI * this.propeller.getDp() * Math.tan(this.beta_tip[i]);
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
      double[] beta_aero = new double[this.propeller.getNumDescPoints()];
      for(int i = 0; i < this.propeller.getNumDescPoints(); i++) {
         beta_aero[i] = Math.atan2(k[i], (Math.PI * zeta[i]) );
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





