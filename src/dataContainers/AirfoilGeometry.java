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
      //https://en.wikipedia.org/wiki/NACA_airfoil#Equation_for_a_symmetrical_4-digit_NACA_airfoil
      //http://www.aerospaceweb.org/question/airfoils/q0100.shtml
      //http://www.aerospaceweb.org/question/airfoils/q0041.shtml
      this.generateCosSpacing();
      
      double m = (double) nacaNumber1 / 100.0;
      double t = (nacaNumber3 * 10) + nacaNumber4;
      t = t / 100.0;
      
      //p is location of max camber
      double p =  ((double) nacaNumber2) / 10.0 ;
      double yc = 0;
      double yt = 0;
      double x_over_c = 0;
      double dy_dx = 0;
      double delineationBtnTopAndBtm = Math.floor((this.numberOfPoints ) / 2);
      double theta = 0;
      
      for (int i = 0; i < this.numberOfPoints; i++) {
         
         x_over_c = (this.points[i][0]/this.cordLength);
         
         yt = 5.0 * t * ( 0.2969*Math.sqrt(x_over_c) - 0.126*x_over_c 
               - 0.3516*Math.pow(x_over_c, 2) + 0.2843*Math.pow(x_over_c, 3) 
               - 0.1015*Math.pow(x_over_c, 4) );
               
         if (this.points[i][1] < this.cordLength * p ) {
            // less than point of max camber                        
            yc = (m/ Math.pow(p, 2)) * (2*p*x_over_c - Math.pow(x_over_c, 2) );         
            dy_dx = ( (2 * m) / Math.pow(p, 2) ) * (p - x_over_c);   
            
         } else {
            // greater than point of max camber
            yc = ( m / Math.pow(( 1 - p ), 2) ) * ( (1 - 2*p) + 2*p*x_over_c - Math.pow(x_over_c,  2)   );     
            dy_dx = ( (2 * m) / Math.pow((1 - p), 2) ) * (p - x_over_c);
            
         }
         
         theta = Math.atan(dy_dx);
         
         if (i < delineationBtnTopAndBtm) {
            //xUpper
            this.points[i][0] = this.points[i][0] - yt * Math.sin(theta);             
            //yUpper
            this.points[i][1] = yc + yt * Math.cos(theta);
            
         } else {
            // xLower
            this.points[i][0] = this.points[i][0] + yt * Math.sin(theta);             
            //yLower
            this.points[i][1] = yc - yt * Math.cos(theta);                        
         }
           
      }
      
      // Ensure midpoints are equal for closed airfoil
      int midPt = (int) Math.floor((this.numberOfPoints ) / 2);      
      this.points[midPt + 1][1] = this.points[midPt][1 ];
     
   }
   
   public double[] getPointCoords(int index){
      double[] retVal = new double[2];
      retVal[0] = this.points[index][0];
      retVal[1] = this.points[index][1];
      return retVal;      
   }
   
   public double[] getCtrlCoords(int index){
      double[] retVal = new double[2];
      retVal[0] = this.controlPoints[index][0];
      retVal[1] = this.controlPoints[index][1];
      return retVal;      
   }
   
   // method for calculating Control points   
   public void generateControlPoints() {      
      this.controlPoints = new double[this.numberOfPoints][2];      
      for (int i = 0; i < this.numberOfPoints - 1; i++) {
         //x
         this.controlPoints[i][0] = this.points[i][0] + ( this.points[i + 1][0] -  this.points[i][0]) / 2;
         //y
         this.controlPoints[i][1] = this.points[i][1] + ( this.points[i + 1][1] -  this.points[i][1]) / 2;      
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
         this.points[i][0] = midpt * (1 - Math.cos(currAngle));
         this.points[ this.numberOfPoints - (i + 1) ][0] = this.points[i][0];
                 
      }
      /*
      for (int i = 0; i < this.numberOfPoints; i++) {
         System.out.println("Point: " + i + ", value: " + this.points[i][1] + ", currAngle: " + currAngle );
      }*/
      
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
