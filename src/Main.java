import dataContainers.AirfoilGeometry;

public class Main {

   public static void main(String[] args) {
      AirfoilGeometry ag = new AirfoilGeometry(2.5);
      
      ag.becomeNACA4Series(1,2,3,4);

   }

}
