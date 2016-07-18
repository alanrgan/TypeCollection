package application;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class AuthorController implements Initializable {
	@FXML private Button okBtn;
	@FXML private Button cancelBtn;
	@FXML private TextField idInput;
	@FXML private TextField handednessInput;
	
	private MainController controller;
	
	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		controller = (MainController) Main.getLoader().getController();
		idInput.setPromptText(controller.getAuthorID());
		handednessInput.setPromptText(controller.getHandedness());
	}
	
	@FXML
	public void onAuthorInfoSet() {
		controller.setAuthorInfo(idInput.getText(), handednessInput.getText());
		Stage stage = (Stage) idInput.getScene().getWindow();
		stage.close();
	}

	@FXML
	public void close() {
		Stage stage = (Stage) idInput.getScene().getWindow();
		stage.close();
	}
	
}
