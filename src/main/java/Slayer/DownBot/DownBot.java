package Slayer.DownBot;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class DownBot extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("botGUI.fxml"));
        primaryStage.setTitle("Fakku Comic Downloader");
        primaryStage.setScene(new Scene(root, 270, 375));
        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }
}
