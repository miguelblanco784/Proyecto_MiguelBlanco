package application;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class Main extends Application {

    @Override
    public void start(Stage stage) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("MainGUI.fxml"));
            Parent root = (Parent) loader.load();

            Scene scene = new Scene(root, 800, 650);

            stage.setResizable(true);
            stage.setTitle("UNO");
            stage.setScene(scene);
            stage.setResizable(false);

            Controller controller = (Controller) loader.getController();
            controller.setStage(stage);
            controller.init();

            stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
                @Override
                public void handle(WindowEvent event) {
                }
            });

            stage.getIcons().add(new Image("images/icon.png"));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
