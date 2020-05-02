package solvers;

/* Pseudo code at bottom of page
 * https://en.wikipedia.org/wiki/Gaussian_elimination
 * 
 * */
public class LUDecompositionSolver {
   
   // of form AX = B
   private double [][] A;
   private double [] X;
   private double [] B;
   
   private int numRows;
   private int numCols;
   
   
   
   
   /* * * * * * * * * * * * * * * 
    * 
    * Solution provided by letting A
    * equal to LU where L is lower 
    * triangular and U is upper 
    * triangular
    * 
    * reframes problem as LUX = B
    * with a dummy variable substitution
    * of UX = Y you get LY = B , then you can
    * solve UX = Y for X which breaks it into 
    * 2 smaller problems instead of 1 bigger one
    * 
    * * * * * * * * * * * * * * */
   
   
   
   
}
