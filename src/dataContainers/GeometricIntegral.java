package dataContainers;


/* * * * * * * * * * * * * * * 
 *  Generic container class for integrals
 * which need to be taken in normal and tangential
 * directions
 * * * * * * * * * * * * * * */
public class GeometricIntegral {

   private int numPts;
   private double[] normalIntegral;
   private double[] tangentialIntegral;
   
   private final int DEFAULT_NUMPTS = 100;
   
   public GeometricIntegral() {
      this.numPts = this.DEFAULT_NUMPTS;  
      this.normalIntegral = new double[this.numPts];
      this.tangentialIntegral = new double[this.numPts];
      
   }
   
   public GeometricIntegral(int numPts) {
      this.numPts = numPts;
      this.normalIntegral = new double[this.numPts];
      this.tangentialIntegral = new double[this.numPts];
   }
   
   public GeometricIntegral(double[] normalIntegral,
         double[] tangentialIntegral, int numPts) {
      super();
      this.normalIntegral = normalIntegral;
      this.tangentialIntegral = tangentialIntegral;
      this.numPts = numPts;
   }

   /* Specific getters and setters*/
   public double getNormalIntegralIndex(int index) {
      return normalIntegral[index];
   }

   public void setNormalIntegralIndex(int index, double normalIntegralVal) {
      this.normalIntegral[index] = normalIntegralVal;
   }

   public double getTangentialIntegralIndex(int index) {
      return tangentialIntegral[index];
   }

   public void setTangentialIntegralIndex(int index, double tangentialIntegralVal) {
      this.tangentialIntegral[index] = tangentialIntegralVal;
   }
      
   
   /* Generic getters and setters*/
   public double[] getNormalIntegral() {
      return normalIntegral;
   }

   public void setNormalIntegral(double[] normalIntegral) {
      this.normalIntegral = normalIntegral;
   }

   public double[] getTangentialIntegral() {
      return tangentialIntegral;
   }

   public void setTangentialIntegral(double[] tangentialIntegral) {
      this.tangentialIntegral = tangentialIntegral;
   }
   
   
   
   
}
