package application;
	
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;


public class Main extends Application {
	
	public static FXMLLoader fxmlloader;
	
	@Override
	public void start(Stage primaryStage) {
		try {
			fxmlloader = new FXMLLoader();
			Parent root = fxmlloader.load(getClass().getResource("MainWindow.fxml").openStream());
			Scene scene = new Scene(root,500,246);
			scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
			
			MainController mController = (MainController) fxmlloader.getController();
			
			primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
				@Override
				public void handle(WindowEvent arg0) {
					mController.close();
				}
			});
			
			primaryStage.setScene(scene);
			primaryStage.setResizable(false);
			primaryStage.show();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public static FXMLLoader getLoader() {
		return fxmlloader;
	}
	
	public static void main(String[] args) {
		launch(args);
	}
}
