package gui;

import java.awt.Font;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class GUI extends Application{
   
   
   private final double DEFAULT_HEIGHT = 600;
   private final double DEFAULT_WIDTH = DEFAULT_HEIGHT * 1.618;
   
   Scene mainScene;
   
   TabPane mainTabPane;
   Tab airfoilTab;
   Tab propellerTab;
   
   BorderPane airfoilPane;
   BorderPane propellerPane;
   
   Button vpmCalculate ;
   
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
   
   private void setStageParameters(Stage primaryStage) {
      primaryStage.setTitle("Propeller + Airfoil Solver");
      
      this.createTabs(primaryStage);
      this.populateAirfoilTab(primaryStage);
      
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
      this.propellerTab = new Tab("Propellers");
      
      this.mainTabPane.getTabs().add(this.airfoilTab);
      this.mainTabPane.getTabs().add(this.propellerTab);
      
      VBox tabVertBox = new VBox(this.mainTabPane);      
      this.mainScene = new Scene(tabVertBox, this.DEFAULT_WIDTH, this.DEFAULT_HEIGHT);  
   }
   
   private void populateAirfoilTab(Stage primaryStage) {

      this.airfoilPane = new BorderPane();
      this.airfoilPane.setPadding(new Insets(10, 20, 10, 20));
      
      
      Label title = new Label("Vortex Panel Solver");
      TilePane topTile = new TilePane();
      topTile.getChildren().add(title);
      topTile.setAlignment(Pos.TOP_CENTER);
      this.airfoilPane.setTop(title);
      
      GridPane centerGrid = new GridPane();
      centerGrid.setHgap(10);
      centerGrid.setVgap(10);
      /*Add Center grid buttons etc here*/
      
      
      this.airfoilPane.setCenter(centerGrid);
      
      
      
      this.airfoilTab.setContent(this.airfoilPane);
      
   }
   
   
   
   
}











