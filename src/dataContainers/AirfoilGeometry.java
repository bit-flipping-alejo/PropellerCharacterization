package dataContainers;

/* * * * * * * * * * * * * * * 
 *  Container for a single airfoil
 * 
 * gets geometry points, calculates the 
 * vortex points
 * * * * * * * * * * * * * * */
public class AirfoilGeometry {

   
   
   private int numberOfPoints;   
   //index = point, [x][y]
   private double[][] points;   
   private double[][] vortexPoints;

   
   
   
   public AirfoilGeometry() {}
   
   public AirfoilGeometry(int numberOfPoints, double[][] points,
         double[][] vortexPoints) {
      super();
      this.numberOfPoints = numberOfPoints;
      this.points = points;
      this.vortexPoints = vortexPoints;
   }

   public AirfoilGeometry(int numberOfPoints, double[][] points) {
      super();
      this.numberOfPoints = numberOfPoints;
      this.points = points;  
      
      this.generateVortexPoints();
   }
   
   // method for calculating vortex points
   
   public void generateVortexPoints() {
      
      this.vortexPoints = new double[this.numberOfPoints][2];
      
      for (int i = 0; i < this.numberOfPoints - 1; i++) {
         //x
         this.vortexPoints[i][1] = this.points[i][1] + ( this.points[i + 1][1] -  this.points[i][1]) / 2;
         //y
         this.vortexPoints[i][2] = this.points[i][2] + ( this.points[i + 1][2] -  this.points[i][2]) / 2;      
      }
      
   }
   
   
   
   public int getNumberOfPoints() {
      return numberOfPoints;
   }

   public void setNumberOfPoints(int numberOfPoints) {
      this.numberOfPoints = numberOfPoints;
   }

   public double[][] getPoints() {
      return points;
   }

   public void setPoints(double[][] points) {
      this.points = points;
   }

   public double[][] getVortexPoints() {
      return vortexPoints;
   }

   public void setVortexPoints(double[][] vortexPoints) {
      this.vortexPoints = vortexPoints;
   }
   
   
   
   
   
   
}
