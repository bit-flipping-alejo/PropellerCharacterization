package geometryContainers;

public class PropellerGeometry {
   
   private int numberOfBlades;
   private double startAngleRMT; // start angle for root mean twist
   private double endAngleRMT;   // end angle for root mean twist
   private double dp;            //Propeller Diameter
   private double hubDiameterPerc;
   private double omega;         // rotational speed of propeller
   private int numDescPoints;
   private final int DEFAULT_NUM_PTS = 10;
   private double[] rmtAngle;    // in rad
   
   public enum RMTTYPE{ LINEAR,LOG;}  
   private RMTTYPE rmtType;   
   private double chordLinePitch;
   public enum GEOMETRICWASHOUT{ RMT,PITCH_TO_DIAMETER;}  
   public GEOMETRICWASHOUT geometricWashoutDefinition;
   
   // blade properties
   private double hubChordLen;
   private double maxChordLen;
   private double maxChordPerc;
   private double tipChordLen;
   private double[] chords;
   private double[] radiusPoints;
   
   private AirfoilGeometry[] airfoils;
   
   
   public PropellerGeometry() {
      this.numDescPoints = this.DEFAULT_NUM_PTS;
      this.chords = new double[this.numDescPoints];
      this.radiusPoints = new double[this.numDescPoints];
      this.rmtType = RMTTYPE.LINEAR;
      this.airfoils = new AirfoilGeometry[this.numDescPoints];
   }
   
   public PropellerGeometry(int descPts) {
      this.numDescPoints = descPts;
      this.chords = new double[this.numDescPoints];
      this.radiusPoints = new double[this.numDescPoints];
      this.rmtType = RMTTYPE.LINEAR;
      this.airfoils = new AirfoilGeometry[this.numDescPoints];
   }
   
   public void setRadialParameters(double propDia, double hubDiaPerc) {
      this.dp = propDia;
      this.hubDiameterPerc = hubDiaPerc;
   }
   
   public void setChordParams(double hubChordLen,  double maxChordLen, 
         double maxChordPerc, double tipChordLen) {

      this.hubChordLen = hubChordLen;
      this.maxChordLen = maxChordLen;
      this.maxChordPerc = maxChordPerc;
      this.tipChordLen = tipChordLen;
      
   }
   
   public void generateChordLengths() {
      double radMinusHub = ((this.dp/2.0) - ((this.dp/2.0) * this.hubDiameterPerc));
      double incPerIter = radMinusHub/(this.numDescPoints );//- 1);
      double maxChordPoint = Math.floor((radMinusHub * this.maxChordPerc ) / incPerIter) ;
      
      for (int i = 0; i < this.numDescPoints; i++) {
         if ( i < maxChordPoint ) {            
            this.chords[i] = this.hubChordLen + ( (this.maxChordLen - this.hubChordLen) * (i / maxChordPoint) );            
         } else {
            this.chords[i] = this.maxChordLen - ( ( this.maxChordLen - this.tipChordLen ) * ( (i - maxChordPoint) / (this.numDescPoints - maxChordPoint - 1) ) );
         }  
         
      }
      //this.chords[ this.numDescPoints - 1] = this.tipChordLen;
   }

   // in degrees
   public void setRmtParametersDeg(double startAngle, double endAngle) {      
      this.startAngleRMT = startAngle * (Math.PI/180);
      this.endAngleRMT = endAngle * (Math.PI/180);
      this.generateRmtAngles();
   }
   
   public void generateRmtAngles() {
      this.rmtAngle = new double[this.numDescPoints];
            
      for (int i = 0; i < this.numDescPoints; i++) {
         this.rmtAngle[i] = this.startAngleRMT + ((this.endAngleRMT - this.startAngleRMT) * ( (double) i /  (double) (this.numDescPoints - 1)));
         //System.out.println(this.rmtAngle[i]);
      }
      
   }
   
   public void generateRadialPositions() {
      double hubRadius = ((this.dp/2.0) * this.hubDiameterPerc);
      double radMinusHub = ((this.dp/2.0) - hubRadius);
      double incPerIter = radMinusHub/(this.numDescPoints - 1);
      
      for (int i = 0; i < this.numDescPoints; i++) {
         this.radiusPoints[i] = hubRadius + ((double) i) * incPerIter;
         //System.out.println(this.radiusPoints[i]);
      }
   }
   
   public void setAirfoilsPerRadialPoint(int indexOfRadialPt, AirfoilGeometry af) {
      this.airfoils[indexOfRadialPt] = af;
   }
   
   public void setRadialPtsToSameAirfoil( AirfoilGeometry af) {
      for(int i = 0; i < this.numDescPoints; i++) {
         this.airfoils[i] = af;
      }
   }
   
   
   // getters and setters
   
   public double getStartAngleRMT() {
      return startAngleRMT;
   }
   public void setStartAngleRMT(double startAngleRMT) {
      this.startAngleRMT = startAngleRMT;
   }
   public double getEndAngleRMT() {
      return endAngleRMT;
   }
   public void setEndAngleRMT(double endAngleRMT) {
      this.endAngleRMT = endAngleRMT;
   }
   public double getDp() {
      return dp;
   }
   public void setDp(double dp) {
      this.dp = dp;
   }
   public double getOmega() {
      return omega;
   }
   public void setOmega(double omega) {
      this.omega = omega;
   }
   public int getNumDescPoints() {
      return numDescPoints;
   }
   public void setNumDescPoints(int numDescPoints) {
      this.numDescPoints = numDescPoints;
   }
   public double[] getRmtAngle() {
      return rmtAngle;
   }
   public void setRmtAngle(double[] rmtAngle) {
      this.rmtAngle = rmtAngle;
   }
   public double getHubDiameterPerc() {
      return hubDiameterPerc;
   }
   public void setHubDiameterPerc(double hubDiameterPerc) {
      this.hubDiameterPerc = hubDiameterPerc;
   }
   public double getHubChordLen() {
      return hubChordLen;
   }
   public void setHubChordLen(double hubChordLen) {
      this.hubChordLen = hubChordLen;
   }
   public double getMaxChordLen() {
      return maxChordLen;
   }
   public void setMaxChordLen(double maxChordLen) {
      this.maxChordLen = maxChordLen;
   }
   public double getMaxChordPerc() {
      return maxChordPerc;
   }
   public void setMaxChordPerc(double maxChordPerc) {
      this.maxChordPerc = maxChordPerc;
   }
   public double getTipChordLen() {
      return tipChordLen;
   }
   public void setTipChordLen(double tipChordLen) {
      this.tipChordLen = tipChordLen;
   }
   public double[] getChords() {
      return chords;
   }
  
   public double getChordsAtIndex(int index) {
      return chords[index];
   }
   public void setChords(double[] chords) {
      this.chords = chords;
   }
   public int getDEFAULT_NUM_PTS() {
      return DEFAULT_NUM_PTS;
   }
   public int getNumberOfBlades() {
      return numberOfBlades;
   }
   public void setNumberOfBlades(int numberOfBlades) {
      this.numberOfBlades = numberOfBlades;
   }
   public double[] getRadiusPoints() {
      return radiusPoints;
   }
  
   public double getRadiusPointIndex(int index) {
      return radiusPoints[index];
   }
   public void setRadiusPoints(double[] radiusPoints) {
      this.radiusPoints = radiusPoints;
   }
   public RMTTYPE getRmtType() {
      return rmtType;
   }
   public void setRmtType(RMTTYPE rmtType) {
      this.rmtType = rmtType;
   }
   public AirfoilGeometry[] getAirfoils() {
      return airfoils;
   }
   public AirfoilGeometry getAirfoilAtRadialIndex(int index) {
      return this.airfoils[index];
   }
   public void setAirfoils(AirfoilGeometry[] airfoils) {
      this.airfoils = airfoils;
   }

   public double getChordLinePitch() {
      return chordLinePitch;
   }

   public void setChordLinePitch(double chordLinePitch) {
      this.chordLinePitch = chordLinePitch;
   }

   public GEOMETRICWASHOUT getGeometricWashoutDefinition() {
      return geometricWashoutDefinition;
   }

   public void setGeometricWashoutDefinition(
         GEOMETRICWASHOUT geometricWashoutDefinition) {
      this.geometricWashoutDefinition = geometricWashoutDefinition;
   }

   
   public double getPitchToDiameterRatio() {
      return ( (this.chordLinePitch) / (this.dp) );
   }
   
   
   
   
   
}










