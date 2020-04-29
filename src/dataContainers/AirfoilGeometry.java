package dataContainers;

/* * * * * * * * * * * * * * * 
 *  Container for a single airfoil
 * 
 * gets geometry points, calculates the 
 * vortex points
 * * * * * * * * * * * * * * */
public class AirfoilGeometry {

   
   
   private int numberOfPoints;   
   private double cordLength;
   
   //index = point, [x][y]
   private double[][] points;   
   private double[][] controlPoints;

   private final int DEFAULTNUMPOINTS = 100;
   private final int NUMCOLUMNS = 2;
   
   
   


   public AirfoilGeometry() {
      this.numberOfPoints = this.DEFAULTNUMPOINTS;      
      this.controlPoints = new double[this.numberOfPoints - 1][this.NUMCOLUMNS];
      this.points = new double[this.numberOfPoints][this.NUMCOLUMNS];
      this.cordLength = 1;
      
   }
   
  
   public AirfoilGeometry(int numberOfPoints, double[][] points) {
      super();
      this.numberOfPoints = numberOfPoints;
      this.points = points;  
      this.cordLength = points[this.numberOfPoints - 1][1];
      
      this.controlPoints = new double[this.numberOfPoints - 1][this.NUMCOLUMNS];      
      this.generateControlPoints();
   }
   
   public AirfoilGeometry(double cordLen) {
      super();
      this.numberOfPoints = this.DEFAULTNUMPOINTS;        
      this.cordLength = cordLen; 
      
      this.controlPoints = new double[this.numberOfPoints - 1][this.NUMCOLUMNS];
      this.points = new double[this.numberOfPoints][this.NUMCOLUMNS];
   }
   
   
   /*Naca 4 + 5 Series eqns*/
   public void becomeNACA4Series(int nacaNumber1, int nacaNumber2, int nacaNumber3, int nacaNumber4 ) {
      //fill X coord with cos spaced points
      this.generateCosSpacing();
      
      double m = (double) nacaNumber1;
      //p is location of max camber
      double p =  this.cordLength * ((double) nacaNumber2) / 10.0 ;
      double yt = 0;
      double x_over_c = 0;
      
      for (int i = 0; i < this.numberOfPoints; i++) {
         
         x_over_c = (this.points[i][1]/this.cordLength);
         if (this.points[i][1] < p ) {
            // less than point of max camber                        
            yt = (m/ Math.pow(p, 2)) * (2*p*x_over_c - Math.pow(x_over_c, 2) );
         } else {
            // greater than point of max camber
            yt = ( m / Math.pow(( 1 - p ), 2) ) * (p - x_over_c);
         }
         
         
         
      }
      
      
   }
   
   
   
   // method for calculating Control points   
   public void generateControlPoints() {
      
      this.controlPoints = new double[this.numberOfPoints][2];
      
      for (int i = 0; i < this.numberOfPoints - 1; i++) {
         //x
         this.controlPoints[i][1] = this.points[i][1] + ( this.points[i + 1][1] -  this.points[i][1]) / 2;
         //y
         this.controlPoints[i][2] = this.points[i][2] + ( this.points[i + 1][2] -  this.points[i][2]) / 2;      
      }
      
   }
   
   /*Private Methods*/   
   private void generateCosSpacing() {
      
      //Set initial and final points
      
      
      //generate constants
      double numPtsOnX = Math.floor((this.numberOfPoints ) / 2);
      double midpt = this.cordLength / 2;      
      double deltaAngle = Math.PI / numPtsOnX;
      
      
      double currAngle = deltaAngle;
      for (int i = 0 ; i < numPtsOnX; i++) {         
         currAngle = deltaAngle * i; 
         this.points[i][1] = midpt * (1 - Math.cos(currAngle));
         this.points[ this.numberOfPoints - (i + 1) ][1] = this.points[i][1];
                 
      }
      
      for (int i = 0; i < this.numberOfPoints; i++) {
         System.out.println("Point: " + i + ", value: " + this.points[i][1] + ", currAngle: " + currAngle );
      }
      
   }
   
   
   
   /*Getters and Setters*/
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
   
   public double[][] getControlPoints() {
      return controlPoints;
   }
   
   public void setControlPoints(double[][] controlPoints) {
      this.controlPoints = controlPoints;
   }

   public double getCordLength() {
      return cordLength;
   }

   public void setCordLength(int cordLength) {
      this.cordLength = cordLength;
   }
   
}
