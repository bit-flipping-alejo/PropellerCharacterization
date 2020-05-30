package gui;



import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;

import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import solvers.GoldsteinVortexTheorySolver;
import solvers.VortexPanelSolver;

import java.util.regex.Pattern;

import geometryContainers.AirfoilGeometry;
import geometryContainers.PropellerGeometry;

public class GUI extends Application{


   private final double DEFAULT_HEIGHT = 600;
   private final double DEFAULT_WIDTH = DEFAULT_HEIGHT * 1.618;

   Scene mainScene;

   TabPane mainTabPane;
   Tab airfoilTab;
   Tab propellerTab;

   BorderPane airfoilPane;
   BorderPane propellerPane;

   //Airfoil tabs
   GridPane defaultAfPane;
   GridPane naca4SeriesPane;

   
   // GVT Tab Panes   
   GridPane defaultPropPane;
   GridPane threePtBladePane;
   
   GridPane defaultSetAirfoilPane;
   GridPane allSameAirfoilPane;
   GridPane perRadPtAirfoilPane;
   
   Button vpmCalculate ;

   private enum afInputType {
      NACA4SERIES,
      NONE;
   }

   afInputType aifoilType;

   /* notes:
    *    whole window is called the stage
    *    content in window is called scene
    *       > this is where buttons go
    * */
   public static void main(String[] args) {
      launch(args);
   }

   @Override
   public void start(Stage primaryStage) throws Exception {
      this.setStageParameters(primaryStage);

      primaryStage.show();
   }

   
   // --- Creation functions
   private void setStageParameters(Stage primaryStage) {
      primaryStage.setTitle("Propeller + Airfoil Solver");

      this.createTabs(primaryStage);
      this.populateAirfoilTabInputs(primaryStage);
      this.populatePropellerTabInputs(primaryStage);

      /*// When youre ready for external stylesheets use this
       * File f = new File("filecss.css");
         this.mainScene.getStylesheets().clear();
         this.mainScene.getStylesheets().add("file:///" + f.getAbsolutePath().replace("\\", "/"));
       * */

      primaryStage.setScene(this.mainScene);

   }

   private void createTabs(Stage primaryStage ) {
      this.mainTabPane = new TabPane();
      this.airfoilTab = new Tab("Airfoils");
      this.airfoilTab.setClosable(false);
      this.propellerTab = new Tab("Propellers");
      this.propellerTab.setClosable(false);
      this.mainTabPane.getTabs().add(this.airfoilTab);
      this.mainTabPane.getTabs().add(this.propellerTab);

      VBox tabVertBox = new VBox(this.mainTabPane);      
      this.mainScene = new Scene(tabVertBox, this.DEFAULT_WIDTH, this.DEFAULT_HEIGHT);
      //this.mainScene = new Scene(tabVertBox);
   }

   private void populateAirfoilTabInputs(Stage primaryStage) {

      this.airfoilPane = new BorderPane();
      this.airfoilPane.setPadding(new Insets(10, 20, 10, 20));

      Label title = new Label("Airfoil Solver [Vortex Panel Methods]");
      title.setFont(new Font(24.0));      

      this.airfoilPane.setTop(title);

      GridPane centerGrid = new GridPane();
      centerGrid.setHgap(10);
      centerGrid.setVgap(10);

      //
      // Center Grid Additions
      //    
      
      Label lblAirfoil = new Label("Airfoil Type: ");
      //centerGrid.add(lblAirfoil, 0, 0);

      ComboBox<String> cbAirfoilType = new ComboBox<String>();
      cbAirfoilType.getItems().add("Select One");
      cbAirfoilType.getItems().add("NACA 4 Series");
      //cbAirfoilType.getItems().add("TBD: NACA 5 Series");
      //cbAirfoilType.getItems().add("TBD: Points input");     
      cbAirfoilType.setOnAction(actionEvent -> {

         switch(cbAirfoilType.getValue()) {
         case "Select One":
            this.aifoilType = afInputType.NONE;
            this.showDefaultAFPanel();
            break;
         case "NACA 4 Series":            
            this.showNACA4SeriesPanel();
            this.aifoilType = afInputType.NACA4SERIES;
            break;         
         default:
            this.aifoilType = afInputType.NONE;
            this.showDefaultAFPanel();
            break;
         }

      });
      //centerGrid.add(cbAirfoilType, 2, 0);

      Button btnCalculateVPM = new Button("Calculate");
      btnCalculateVPM.setOnAction(actionEvent -> {
         this.runVPM();
      });
      //centerGrid.add(btnCalculateVPM, 0, 2);

      VBox nav = new VBox(lblAirfoil, cbAirfoilType, btnCalculateVPM);
      this.airfoilPane.setLeft(nav);
      this.createAfPanelOptions(centerGrid);


      this.airfoilPane.setCenter(centerGrid);
      this.airfoilTab.setContent(this.airfoilPane);

   }

   private void populatePropellerTabInputs(Stage primaryStage) {
      this.propellerPane = new BorderPane();
      this.propellerPane.setPadding(new Insets(10, 20, 10, 20));
      
      Label title = new Label("Propeller Solver [Goldsteins Vortex Theory]");
      title.setFont(new Font(24.0));
      TilePane topTile = new TilePane();
      topTile.getChildren().add(title);
      topTile.setAlignment(Pos.TOP_CENTER);
      this.propellerPane.setTop(title);

      GridPane leftGrid = new GridPane();
      leftGrid.setHgap(10);
      leftGrid.setVgap(10);

      //propeller radius
      Label lblRadValue = new Label("Propeller Radius (m): ");
      TextField tfRadValue = new TextField();
      leftGrid.add(lblRadValue, 0, 0);
      leftGrid.add(tfRadValue, 1, 0);

      //propeller % hub
      Label lblPercHub = new Label("Percent Hub: ");
      TextField tfPercHub = new TextField();
      leftGrid.add(lblPercHub, 0, 1);
      leftGrid.add(tfPercHub, 1, 1);

      //propeller, definitions combobox. default is 3 pt linear
      Label lblChordDef = new Label("Propeller chord definition");      
      ComboBox<String> cbChordDef = new ComboBox<String>();
      cbChordDef.getItems().add("Select One");
      cbChordDef.getItems().add("3 Pt");
      
      cbChordDef.setOnAction(eventHandler -> {
         switch(cbChordDef.getValue()) {
         case "Select One":
            this.showDefaultPropPanel();
            break;
         case "3 Pt":            
            this.show3PTPropPanel();
            break;         
         default:
            this.showDefaultPropPanel();
            break;
         }
      });
     
      leftGrid.add(lblChordDef, 0, 2);
      leftGrid.add(cbChordDef, 1, 2);      
      
      Label lblnumDescPts = new Label("Num Descritization pts: ");
      TextField tfnumDescPts = new TextField();
      tfnumDescPts.setUserData("tfnumDescPts");
      tfnumDescPts.setText("10");
      leftGrid.add(lblnumDescPts, 0, 3);
      leftGrid.add(tfnumDescPts, 1, 3);
      
      Label lblAfDef = new Label("Airfoil definition");      
      ComboBox<String> cbAfDef = new ComboBox<String>();
      cbAfDef.getItems().add("Select One");
      cbAfDef.getItems().add("All Airfoils Same");
      cbAfDef.getItems().add("Define Airfoil per radial pt");   
      
      cbAfDef.setOnAction(actionEvent ->{
         switch(cbAfDef.getValue()) {
         case "Select One":
            this.showDefaultBladeNacaPanel();
            break;
         case "All Airfoils Same":            
            this.showAllSameAirfoilPanel();
            break;
         case "Define Airfoil per radial pt":            
            this.showPerRadPtAirfoilPanel();
            break;
         default:
            break;
         }
      });
      
      leftGrid.add(lblAfDef, 0, 4);
      leftGrid.add(cbAfDef, 1, 4);
      
      
      Button btnCalculateGVT = new Button("Calculate");
      btnCalculateGVT.setOnAction(actionEvent -> {
         this.runGVT();
      });
      leftGrid.add(btnCalculateGVT, 0, 5);
      
      GridPane centerGrid = new GridPane();
      this.createGVTPanelOptions(centerGrid);
      
      this.propellerPane.setCenter(centerGrid);
      
      //this.propellerPane.setCenter(centerGrid);
      this.propellerPane.setLeft(leftGrid);
      this.propellerTab.setContent(this.propellerPane);
   }

   // --- Data Validation
   private boolean validateData() {
      
      if (this.airfoilTab.isSelected()) {
         
         switch (this.aifoilType) {
         case NONE:
            return false;
         case NACA4SERIES:
            Object tochk = this.getByUserData(this.naca4SeriesPane, "tf4SeriesInput");
            if (tochk == null) {
               return false;
            }
            TextField toCheck = (TextField) tochk;
            
            //validate data here 
            //NACA Number
            int len = toCheck.getText().length();
            if (len != 4) {
               return false;
            }
            
            String pattern = "\\d*[a-zA-Z]+";            
            Boolean isBad = Pattern.matches(pattern, toCheck.getText());
            if (isBad) {
               System.out.println("Is regex bad");
               return false;
            }
            
            int naca4val = Integer.parseInt( toCheck.getText() );
            if (! ( (naca4val > 0) && (naca4val < 9999) )) {               
               System.out.println("is out of numerical bounds");
               return false;
            }
            
            // AoA
            Object tochkAoA = this.getByUserData( this.naca4SeriesPane , "tfAOA");
            if (tochkAoA == null) {
               return false;
            }
            
            TextField toCheckAoA = (TextField) tochkAoA;            
            Boolean isBadAoA = Pattern.matches(pattern, toCheckAoA.getText());
            if (isBadAoA) {
               return false;
            }
            double aoaVal = (double) Double.parseDouble( toCheckAoA.getText() );
            if (Math.abs(aoaVal) > 12) {
               return false;
            }
            
            return true;
         default:
            return false;
         }
         
      } else if (this.propellerTab.isSelected()) {
         
      } else {
         
      }
      
      return false;
      
   }

   
   
   // --- GVT SubPanel Creation + Switching
   private void createGVTPanelOptions(GridPane centerGrid) {
      
      this.defaultPropPane = new GridPane();
      this.threePtBladePane = new GridPane();
      
      Label defPropLbl = new Label("Please select a Propeller Chord Definition to the left");
      this.defaultPropPane.add(defPropLbl, 0, 0);
      
      //this.threePtBladePane
      //propeller hub chord, max cord, max chord perc, end chord len
      Label lblHubChordLen = new Label("Hub Chord Len: ");
      TextField tfHubChordLen = new TextField();
      this.threePtBladePane.add(lblHubChordLen, 0, 0);
      this.threePtBladePane.add(tfHubChordLen, 1, 0);     

      Label lblMaxChordLen = new Label("Max Chord Len: ");
      TextField tfMaxChordLen = new TextField();
      this.threePtBladePane.add(lblMaxChordLen, 0, 1);
      this.threePtBladePane.add(tfMaxChordLen, 1, 1);

      Label lblMaxChordLocPerc = new Label("Max Chord Loc Perc: ");
      TextField tfMaxChordLocPerc = new TextField();
      this.threePtBladePane.add(lblMaxChordLocPerc, 0, 2);
      this.threePtBladePane.add(tfMaxChordLocPerc, 1, 2);

      Label lblTipChordLen = new Label("Tip Chord Len: ");
      TextField tfTipChordLen = new TextField();
      this.threePtBladePane.add(lblTipChordLen, 0, 3);
      this.threePtBladePane.add(tfTipChordLen, 1, 3);
      
      this.defaultPropPane.setVisible(false);
      this.threePtBladePane.setVisible(false);
      
      centerGrid.add(this.defaultPropPane, 0, 0);
      centerGrid.add(this.threePtBladePane, 0, 0);
      
      
      this.defaultSetAirfoilPane = new GridPane();
      this.allSameAirfoilPane = new GridPane();
      this.perRadPtAirfoilPane = new GridPane();
      
      Label lbldefSetAf = new Label("Please select how to define airfoils to the left");
      this.defaultSetAirfoilPane.add(lbldefSetAf, 0, 0);
      
      Label lblPropNACASeriesInput = new Label("NACA 4 Series Value");
      this.naca4SeriesPane.add(lblPropNACASeriesInput, 0, 0, 1, 1);

      TextField tfPropNACASeriesInput = new TextField();
      tfPropNACASeriesInput.setUserData("tf4SeriesInput");
      this.naca4SeriesPane.add(tfPropNACASeriesInput, 1, 0, 1, 1);
      
      // TODO: Do per radial point filling of NACA AF
      
      
      this.defaultSetAirfoilPane.setVisible(false);
      this.allSameAirfoilPane.setVisible(false);
      this.perRadPtAirfoilPane.setVisible(false);
      
      centerGrid.add(this.defaultSetAirfoilPane, 0,1);
      centerGrid.add(this.allSameAirfoilPane, 0,1);
      centerGrid.add(this.perRadPtAirfoilPane, 0,1);
      
   }
   
   // --- Blade panel switchers
   private void showDefaultPropPanel() {
      this.defaultPropPane.setVisible(true);
      this.threePtBladePane.setVisible(false);
   }
   
   private void show3PTPropPanel() {
      this.defaultPropPane.setVisible(false);
      this.threePtBladePane.setVisible(true);
   }
   
   // --- Blade af panel switchers
   private void showDefaultBladeNacaPanel() {
      this.defaultSetAirfoilPane.setVisible(true);
      this.allSameAirfoilPane.setVisible(false);
      this.perRadPtAirfoilPane.setVisible(false);
   }
   
   private void showAllSameAirfoilPanel() {
      this.defaultSetAirfoilPane.setVisible(false);
      this.allSameAirfoilPane.setVisible(true);
      this.perRadPtAirfoilPane.setVisible(false);
   }
   
   private void showPerRadPtAirfoilPanel() {
      this.defaultSetAirfoilPane.setVisible(false);
      this.allSameAirfoilPane.setVisible(false);
      this.perRadPtAirfoilPane.setVisible(true);
   }
   
   
   
   
   
   // --- Airfoil SubPanel Creation + Switching
   private void createAfPanelOptions(GridPane centerGrid) {
      this.defaultAfPane = new GridPane();
      this.naca4SeriesPane = new GridPane();
      //GridPane naca5SeriesPane = new GridPane();
      //GridPane pointsInputPane = new GridPane();

      Label defaultLabel = new Label("Please use Drop Down to left");
      this.defaultAfPane.add(defaultLabel, 0, 0);

      Label af4SeriesInput = new Label("NACA 4 Series Value");
      this.naca4SeriesPane.add(af4SeriesInput, 0, 0, 1, 1);

      TextField tf4SeriesInput = new TextField();
      tf4SeriesInput.setUserData("tf4SeriesInput");
      this.naca4SeriesPane.add(tf4SeriesInput, 1, 0, 1, 1);

      Label lblAOA = new Label("Angle of Attack (deg): ");
      this.naca4SeriesPane.add(lblAOA, 0, 1, 1, 1);

      TextField tfAOA = new TextField();
      tfAOA.setUserData("tfAOA");
      this.naca4SeriesPane.add(tfAOA, 1, 1, 1, 1);
      
      //centerGrid.add(pointsInputPane, 0, 8);      
      //centerGrid.add(naca5SeriesPane, 0, 8);

      this.naca4SeriesPane.setVisible(false);
      this.defaultAfPane.setVisible(false);
      centerGrid.add(this.naca4SeriesPane, 8, 0);
      centerGrid.add(this.defaultAfPane, 8, 0);
   }

   private void showDefaultAFPanel() {
      this.defaultAfPane.setVisible(true);
      this.naca4SeriesPane.setVisible(false);
   }

   private void showNACA4SeriesPanel() {
      this.defaultAfPane.setVisible(false);
      this.naca4SeriesPane.setVisible(true);
   }
   // --- End Panel Switching
   
   // --- utility functions
   private Node getByUserData(Parent parent, Object data) {
      for (Node n : parent.getChildrenUnmodifiable() ) {
         if (data.equals(n.getUserData())) {
            return n;
         }
      }
      
      return null;
   }

   
   
   
   
   // --- Running Functions   
   
   private void runVPM() {
      if (!this.validateData()) {
         System.out.println("data NOT validated");
         return;
      }
      
      AirfoilGeometry ag = new AirfoilGeometry(1);
      
      TextField nacaNum = (TextField) this.getByUserData( this.naca4SeriesPane , "tf4SeriesInput");
      ag.becomeNACA4Series(Integer.parseInt(Character.toString(nacaNum.getText().charAt(0))), 
            Integer.parseInt(Character.toString(nacaNum.getText().charAt(1))), 
            Integer.parseInt(Character.toString(nacaNum.getText().charAt(2))), 
            Integer.parseInt(Character.toString(nacaNum.getText().charAt(3))));
      
      TextField AoA = (TextField) this.getByUserData( this.naca4SeriesPane , "tfAOA");      
      ag.setangleOfAttackRad(Double.parseDouble(AoA.getText()) * (Math.PI/180));
      
      VortexPanelSolver vpm = new VortexPanelSolver(ag);
      vpm.setVinfinity(1);      
      vpm.runVPMSolver();
      
      // Add Labels
      Object testLblCL = this.getByUserData(this.naca4SeriesPane, "lblCL");
      if (!(testLblCL == null)) {
         //delete everything
         this.naca4SeriesPane.getChildren().remove((Label) testLblCL);
      }      
      Label lblCL = new Label("Coeff of Lift: " + vpm.getCl());
      lblCL.setUserData("lblCL");
      
      Object testLblCD = this.getByUserData(this.naca4SeriesPane, "lblCD");
      if (!(testLblCD == null)) {
         //delete everything
         this.naca4SeriesPane.getChildren().remove((Label) testLblCD);
      }
      Label lblCD = new Label("Coeff of Drag: " + vpm.getCd());
      lblCD.setUserData("lblCD");
      Object testLblCM = this.getByUserData(this.naca4SeriesPane, "lblCM");
      if (!(testLblCM == null)) {
         //delete everything
         this.naca4SeriesPane.getChildren().remove((Label) testLblCM);
      }
      Label lblCM = new Label("Coeff of Moment (quarter chord): " + vpm.getCm());
      lblCM.setUserData("lblCM");
      
      this.naca4SeriesPane.add(lblCL, 0, 3, Integer.MAX_VALUE, 1 );
      this.naca4SeriesPane.add(lblCD, 0, 4, Integer.MAX_VALUE, 1 );
      this.naca4SeriesPane.add(lblCM, 0, 5, Integer.MAX_VALUE, 1 );
      
      
      // plot AF points
      NumberAxis xAxisAF = new NumberAxis();
      xAxisAF.setLabel("Normalized Chord");
      
      NumberAxis yAxisAF = new NumberAxis();
      yAxisAF.setLabel("GeometryPosition");
      
      
      Object testChartaf = this.getByUserData(this.naca4SeriesPane, "afChart");
      if (!(testChartaf == null)) {
         //delete everything
         this.naca4SeriesPane.getChildren().remove((LineChart<Number, Number>) testChartaf);
      }
      LineChart<Number, Number> afChart = new LineChart<Number, Number>(xAxisAF, yAxisAF);
      afChart.setUserData("afChart");
      XYChart.Series<Number, Number> afSeries = new XYChart.Series<Number, Number>();
      
      double afPts[][] = ag.getPoints();
      for (int i = 0; i < ag.getNumberOfCtrlPoints(); i++) {         
         afSeries.getData().add(new XYChart.Data<Number, Number>( afPts[i][0] , afPts[i][1] ) );
      }
      afSeries.setName("Airfoil Points");
      afChart.getData().add(afSeries);
      afChart.setLegendVisible(false);
      this.naca4SeriesPane.add(afChart, 0, 6, 1, Integer.MAX_VALUE);
      
      // plot Pressure points
      NumberAxis xAxisP = new NumberAxis();
      xAxisAF.setLabel("Normalized Chord");
      
      NumberAxis yAxisP = new NumberAxis();
      yAxisAF.setLabel("Pressure");
      
      Object testChartP = this.getByUserData(this.naca4SeriesPane, "pChart");
      if (!(testChartP == null)) {
         //delete everything
         this.naca4SeriesPane.getChildren().remove((LineChart<Number, Number>) testChartP);
      }
      LineChart<Number, Number> pChart = new LineChart<Number, Number>(xAxisP, yAxisP);
      pChart.setUserData("pChart");
      XYChart.Series<Number, Number> pHiSeries = new XYChart.Series<Number, Number>();
      XYChart.Series<Number, Number> pLoSeries = new XYChart.Series<Number, Number>();
      
      double[] cps = vpm.getCoeffOfPressure();
      for (int i = 0; i < Math.ceil(ag.getNumberOfCtrlPoints()/2); i++) {   
         double [] hightPt = ag.getCtrlCoords(i);
         double [] lowPt = ag.getCtrlCoords( ag.getNumberOfCtrlPoints() - 1 - i );

         pHiSeries.getData().add(new XYChart.Data<Number, Number>( hightPt[0] , cps[i] ) );
         pLoSeries.getData().add(new XYChart.Data<Number, Number>( lowPt[0]   , cps[ag.getNumberOfCtrlPoints() - 1 - i ] ) ); 
      }
      pHiSeries.setName("High Pressure");
      pLoSeries.setName("Low Pressure");
      pChart.getData().add(pHiSeries);
      pChart.getData().add(pLoSeries);   
      pChart.setLegendSide(Side.BOTTOM);      
      afChart.setLegendVisible(true);
      this.naca4SeriesPane.add(pChart, 1, 6, 1, Integer.MAX_VALUE);
      
      
   }

   private void runGVT() {
      
      System.out.println("running GVT from func call");

   }

}











