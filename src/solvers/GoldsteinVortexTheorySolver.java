package solvers;

public class GoldsteinVortexTheorySolver {
   
   
   
   private double Vinf;       // forward speed of propeller disc
   private double[] beta;     // Aerodynamic pitch angle
   
   private double[] eps_i;    // advance angle, induced angle <-- target of this whole analysis
   private double[] eps_inf;  // total downwash angle
   private double[] eps_b;    // downwash angle
   
   private double rho;        // density of air 1.225 kg/m3 @ Sea level
   
   // things that depend on eps_i
   private double[] Vb;// fluid veloc relative to cross section
   private double[] Vi;// induced velocity
   private double[] f; // prandtl's tip loss factor
   
   // section properties (outputs of VPM)
   private VortexPanelSolver vpm;
   private double[] Cl;
   private double[] Cd;
   
   
   
   private GoldsteinVortexTheorySolver() {
      
   }
   
   
   
}
