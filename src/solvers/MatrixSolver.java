package solvers;

public class MatrixSolver {


   // of form AX = B, you solve for x
   private double [][] A;
   private double [] X;
   private double [] B;

   private double[] m1;
   private double[] m2;
   private int[] ordering;
   
   private double[][] augmentedMatrix;
   
   private int numRows;
   private int numCols;

   public MatrixSolver() {}
   
   public MatrixSolver(double[][] a, double[] b, int numRows, int numCols) {
      super();
      A = a;
      B = b;
      this.numRows = numRows;
      this.numCols = numCols; 
      this.augmentedMatrix = new double[numRows + 1][numCols]; 
      this.populateAugmentedMatrix();
   }

   
   
   
   
   
   //Gaussian elimination chosen since each Geometric
   // integral is calculated per point O(n^2) where as
   // LU Decomposition is performed in O(n^3) but it allows
   // solutions in O(N^2)
   // Augmented matrix is regular A matrix, 
   // but with n+1 columns, in the n+1th column 
   // the B matrix resides, this allows all 
   // elementary operations to be performed
   // at once


   public void doGaussianEliminationPivot() {
      
      this.makeAugmentedMatrix();
      this.populateAugmentedMatrix();
      
      this.ordering = new int[this.numRows];
      
      for (int m = 0; m < this.numRows; m++) {
         this.ordering[m] = m;
      }
      
      int h = 0; 
      int k = 0;
      
      while ( (h < this.numRows) && (k < this.numCols) ) {
         
         //identify max row in the current col
         //store off as maxRowNotH
         int maxRowNotH = h;
         for(int rowctr = h; rowctr < this.numRows; rowctr++) {            
            if (Math.abs(this.augmentedMatrix[rowctr][k]) > Math.abs(this.augmentedMatrix[maxRowNotH][k]) ) {
               maxRowNotH = rowctr;
            }            
         }
         
         if ( this.augmentedMatrix[maxRowNotH][k] == 0 ) {
            k++;
         } else {
            // do swap
            this.swapAugmentedRows(h, maxRowNotH);
            this.swapRows(h, maxRowNotH, this.ordering);
            
            for (int i = h + 1; i < this.numRows; i++) {
               double ratio = this.augmentedMatrix[i][k] / this.augmentedMatrix[h][k];
               this.augmentedMatrix[i][k] = 0;
               
               for (int j = k + 1; j < this.numCols + 1; j++) {
                  this.augmentedMatrix[i][j] = this.augmentedMatrix[i][j] - (this.augmentedMatrix[h][j] * ratio);
               }
               
            }
            h++;
            k++;
         } // end if          
      } // end while
      
      this.doBackwardsSubstitutionPivot();
      
   }

   private void swapAugmentedRows(int row1, int row2) {
      
      for (int i = 0; i < this.numCols + 1; i++) {
         double tempVal = this.augmentedMatrix[row1][i];
         this.augmentedMatrix[row1][i] = this.augmentedMatrix[row2][i];
         this.augmentedMatrix[row2][i] = tempVal;
      }
      
   }

   private void swapRows(int row1, int row2, int[]matrix) {
      int tempVal = matrix[row1];
      matrix[row1] = matrix[row2];
      matrix[row2] = tempVal;
   }

   
   private void doBackwardsSubstitutionPivot() {
      //https://algowiki-project.org/en/Backward_substitution#General_description_of_the_algorithm
      this.X = new double[this.numRows];
      this.B = new double[this.numRows];
      
      //populate B
      for (int i = 0; i < this.numRows; i++) {
         B[i] = this.augmentedMatrix[i][this.numCols ];
      }
      
      
      for (int i = (this.numRows - 1); i >= 0; i-- ) {
         this.X[i] = B[i];
         for (int j = i + 1; j < this.numCols; j++) {
            this.X[i] = this.X[i] - ( this.augmentedMatrix[i][j] * this.X[j] );
         }
         this.X[i] =   this.X[i] /  this.augmentedMatrix[i][i];
      }
      
      this.reOrderDueToPivots();
      
   }   
   
   private void reOrderDueToPivots() {
      
      double[] newX = new double[this.numRows];
      
      for (int i = 0; i < this.numRows; i++) {
         newX[ this.ordering[i] ] = this.X[this.ordering[i]];
      }
      this.X = newX;
   }
   
   private void makeAugmentedMatrix() {
      this.augmentedMatrix = new double[numRows][numCols + 1]; 
   }
   
   private void populateAugmentedMatrix() {
      for (int i = 0; i < this.numRows; i++) {   
         
         for (int j = 0; j < this.numCols + 1; j++) {
         
            if(j == this.numCols) {
               this.augmentedMatrix[i][j] = this.B[i];
            } else {
               this.augmentedMatrix[i][j] = this.A[i][j];
            }            
         
         } // end j      
         
      } // end i      
   }

   
   
   
   public void doGaussianEliminationNoPivot() {
      for (int i = 0; i < this.numRows-1; i++) {
         
         
         
         for (int j = i+1; j < this.numCols; j++) {
            
            double ratio = this.A[j][i]/this.A[i][i];
            //this.A[j][i] = 0;
            
            for (int k = i; k < this.numCols; k++) {
               this.A[j][k] -= (this.A[i][k] * ratio);               
            }
            this.B[j]-= (ratio * this.B[i]);
            
         }
         
      }
      
      
      this.doBackwardsSubstitutionNoPivot();
      
   }
   
   private void doBackwardsSubstitutionNoPivot() {
      //https://algowiki-project.org/en/Backward_substitution#General_description_of_the_algorithm
      this.X = new double[this.numRows];
      
      
      for (int i = (this.numRows - 1); i >= 0; i-- ) {
         this.X[i] = this.B[i];
         for (int j = i + 1; j < this.numCols; j++) {
            this.X[i] = this.X[i] - ( this.A[i][j] * this.X[j] );
         }
         this.X[i] =   this.X[i] /  this.A[i][i];
      }

   }
   
   
   
   

   //LU Decomposition 
   public void doLUDecomposition() {
      for(int i = 0; i < this.numRows; i++) {
      
         for(int j = 0; j < i; j++) {
            
            for(int k = 0; k < j; k++) {
               
            }
            
         }
         
         for(int j = i; j < this.numCols; j++) {

         }
         
      }
      
      
   }

   
   
   
   
   
   
   
   
   
   
   
   
   
   
   /*Matrix Multiplication*/
   public double[] doMatrixMultiply(int size) {
      double[] res = new double[size];
      
      for (int i = 0; i < size; i++) {
         res[i] = this.m1[i] * this.m2[i];
      }
      
      return res;
   }
   
   public double[] doMatrixMultiply(int size, double[] mat1, double[] mat2) {
      double[] res = new double[size];
      
      for (int i = 0; i < size; i++) {
         res[i] = mat1[i] * mat2[i];
      }
      
      return res;
   }

   public double[] doMatrixMultiplyCosOnMat2(int size, double[] mat1, double[] mat2) {
      double[] res = new double[size];
      
      for (int i = 0; i < size; i++) {
         res[i] = mat1[i] * Math.cos(mat2[i]);
      }
      
      return res;
   }

   public double[] doMatrixMultiplySinOnMat2(int size, double[] mat1, double[] mat2) {
      double[] res = new double[size];
      
      for (int i = 0; i < size; i++) {
         res[i] = mat1[i] * Math.sin(mat2[i]);
      }
      
      return res;
   }
   
   public double doSum(int size, double[] mat) {
      double res = 0;
      for(int i = 0; i < size; i++) {
         res += mat[i];
      }
      return res;
   }

   public double[] doMatrixMultiplyByConst(int size, double[] mat1, double theVal) {
      double[] res = new double[size];

      for (int i = 0; i < size; i++) {
         res[i] = mat1[i] * theVal;
      }

      return res;
   }
   
   public double[] doMatrixAdditionByConst(int size, double[] mat1, double theVal) {
      double[] res = new double[size];

      for (int i = 0; i < size; i++) {
         res[i] = mat1[i] + theVal;
      }

      return res;
   }
   
   
   
   /*Getters and setters*/
   
   public double[][] getA() {
      return A;
   }

   public void setA(double[][] a) {
      A = a;
   }

   public double[] getB() {
      return B;
   }

   public void setB(double[] b) {
      B = b;
   }

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

   public double[] getX() {
      return X;
   }

   public double[][] getAugmentedMatrix() {
      return augmentedMatrix;
   }

   public double[] getM1() {
      return m1;
   }

   public void setM1(double[] m1) {
      this.m1 = m1;
   }

   public double[] getM2() {
      return m2;
   }

   public void setM2(double[] m2) {
      this.m2 = m2;
   }
   
   
}

