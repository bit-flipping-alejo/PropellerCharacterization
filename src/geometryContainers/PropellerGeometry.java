package geometryContainers;

public class PropellerGeometry {
   
   private int numberOfBlades;
   private double startAngleRMT; // start angle for root mean twist
   private double endAngleRMT;   // end angle for root mean twist
   private double dp;            //Propeller Diameter
   private double omega;         // rotational speed of propeller
   private int numDescPoints;
   private final int DEFAULT_NUM_PTS = 10;
   private double[] rmtAngle;    // in rad
   
   enum RMTTYPE{
      LINEAR,
      LOG;
   }
   
   private RMTTYPE rmtType;
   
   
   private double hubDiameterPerc;
   
   // blade properties
   private double hubChordLen;
   private double maxChordLen;
   private double maxChordPerc;
   private double tipChordLen;
   private double[] chords;
   private double[] radiusPoints;
   
   
   
   public PropellerGeometry() {
      this.numDescPoints = this.DEFAULT_NUM_PTS;
      this.chords = new double[this.numDescPoints];
      this.radiusPoints = new double[this.numDescPoints];
      this.rmtType = RMTTYPE.LINEAR;
      
   }
   
   public PropellerGeometry(int descPts) {
      this.numDescPoints = descPts;
      this.chords = new double[this.numDescPoints];
      this.radiusPoints = new double[this.numDescPoints];
      this.rmtType = RMTTYPE.LINEAR;
      
   }
   
   public void setRadialParameters(double propDia, double hubDiaPerc) {
      this.dp = propDia;
      this.hubDiameterPerc = hubDiaPerc;
   }
   
   public void setBladeParams(double hubChordLen,  double maxChordLen, 
         double maxChordPerc, double tipChordLen) {

      this.hubChordLen = hubChordLen;
      this.maxChordLen = maxChordLen;
      this.maxChordPerc = maxChordPerc;
      this.tipChordLen = tipChordLen;
      
   }
   
   public void generateChordLengths() {
      double diaMinusHub = (this.dp - (this.dp * this.hubDiameterPerc));
      double incPerIter = diaMinusHub/(this.numDescPoints - 1);
      double maxChordPoint = Math.floor((diaMinusHub * this.maxChordPerc ) / incPerIter) ;
      
      for (int i = 0; i < this.numDescPoints; i++) {
         if ( i < maxChordPoint ) {            
            this.chords[i] = this.hubChordLen + ( (this.maxChordLen - this.hubChordLen) * (i / maxChordPoint) );            
         } else {
            this.chords[i] = this.maxChordLen - ( ( this.maxChordLen - this.tipChordLen ) * ( (i - maxChordPoint) / (this.numDescPoints - maxChordPoint) ) );
         }  
         this.radiusPoints[i] = i * incPerIter;
      }
      
   }

   // in degrees
   public void setRmtParametersDeg(double startAngle, double endAngle) {      
      this.startAngleRMT = startAngle * (Math.PI/180);
      this.endAngleRMT = endAngle * (Math.PI/180);
   }
   
   public void generateRmtAngles() {
      this.rmtAngle = new double[this.numDescPoints];
            
      for (int i = 0; i < this.numDescPoints; i++) {
         this.rmtAngle[i] = this.startAngleRMT + ((this.endAngleRMT - this.startAngleRMT) * ( (double) i /  (double) (this.numDescPoints - 1)));
         //System.out.println(this.rmtAngle[i]);
      }
      
   }
   
   
   public void generateRadialPositions() {
      double hubRadius = (this.dp * this.hubDiameterPerc);
      double diaMinusHub = (this.dp - hubRadius);
      double incPerIter = diaMinusHub/(this.numDescPoints - 1);
      
      for (int i = 0; i < this.numDescPoints; i++) {
         this.radiusPoints[i] = hubRadius + ((double) i) * incPerIter;
         System.out.println(this.radiusPoints[i]);
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

   public void setRadiusPoints(double[] radiusPoints) {
      this.radiusPoints = radiusPoints;
   }

   public RMTTYPE getRmtType() {
      return rmtType;
   }

   public void setRmtType(RMTTYPE rmtType) {
      this.rmtType = rmtType;
   }
   
   
   
   
}










