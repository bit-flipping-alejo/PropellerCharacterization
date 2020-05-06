package dataContainers;


/* * * * * * * * * * * * * * * 
 *  Generic container class for integrals
 * which need to be taken in normal and tangential
 * directions
 * * * * * * * * * * * * * * */
public class GeometricIntegral {

   private int numRows;
   private int numCols;

   private double[][] normalIntegral;
   private double[][] tangentialIntegral;

   private final int DEFAULT_NUMPTS = 100;

   public GeometricIntegral() {
      this.numRows = this.DEFAULT_NUMPTS;  
      this.numCols = this.DEFAULT_NUMPTS; 
      this.normalIntegral = new double[numRows][numCols];
      this.tangentialIntegral = new double[numRows][numCols];     
   }

   public GeometricIntegral(int squareMatrixNumRows) {
      this.numRows = squareMatrixNumRows;  
      this.numCols = squareMatrixNumRows; 
      this.normalIntegral = new double[this.numRows][this.numCols];
      this.tangentialIntegral = new double[this.numRows][this.numCols];     
   }

   public GeometricIntegral(int numRows, int numCols, double[][] normalIntegral,
         double[][] tangentialIntegral) {
      super();
      this.numRows = numRows;
      this.numCols = numCols;
      this.normalIntegral = normalIntegral;
      this.tangentialIntegral = tangentialIntegral;
   }

   /* Specific getters and setters*/
   public double getNormalIntegralIndex(int i, int j) {
      return normalIntegral[i][j];
   }

   public void setNormalIntegralIndex(int i, int j, double normalIntegralVal) {
      this.normalIntegral[i][j] = normalIntegralVal;
   }

   public double getTangentialIntegralIndex(int i, int j) {
      return tangentialIntegral[i][j];
   }

   public void setTangentialIntegralIndex(int i, int j, double tangentialIntegralVal) {
      this.tangentialIntegral[i][j] = tangentialIntegralVal;
   }





   /* Generic getters and setters*/
   public int getNumRows() {
      return numRows;
   }

   public void setNumRows(int numRows) {
      this.numRows = numRows;
   }

   public int getNumCols() {
      return numCols;
   }

   public void setNumCols(int numCols) {
      this.numCols = numCols;
   }

   public double[][] getNormalIntegral() {
      return normalIntegral;
   }

   public void setNormalIntegral(double[][] normalIntegral) {
      this.normalIntegral = normalIntegral;
   }

   public double[][] getTangentialIntegral() {
      return tangentialIntegral;
   }

   public void setTangentialIntegral(double[][] tangentialIntegral) {
      this.tangentialIntegral = tangentialIntegral;
   }




}
