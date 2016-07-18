package application;

import java.io.IOException;
import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.ResourceBundle;

import connection.CollectionRequest;
import connection.ConnectionManager;
import connection.CollectionRequest.RequestType;
import connection.InputListener;
import connection.Server;
import connection.ServerManager;
import connection.ServerManager.OnServerStartedHandler;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import util.Pair;
import util.Point3F;

public class MainController implements Initializable  {
	
	@FXML private Button nextBtn;
	@FXML private TextField sentenceInput;
	@FXML private Label statusLabel;
	@FXML private Label connStatusLabel;
	@FXML private AnchorPane ap;
	@FXML private HBox sentenceBox;
	
	private String authorID, authorHandedness;
	private HashMap<String, Long> keyToStartTime = new HashMap<>();
	
	private TrialManager trialManager = new TrialManager();
	private ServerManager mServerManager;
	
	private Thread checkConnectionThread = new Thread(new Runnable() {
		@Override
		public void run() {
			while(!Thread.currentThread().isInterrupted()) {
				try {
					mServerManager.sendRequest(new CollectionRequest(RequestType.CONNECTION_STATE));
				} catch (IOException e) {
					e.printStackTrace();
					updateConnectionLabel("Connection status: disconnected");
					checkConnectionThread.interrupt();
				}
			}
		}
	});
	
	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		Server server = new Server();
		mServerManager = new ServerManager(server);
		
		// When server starts, synchronize 
		mServerManager.setOnServerStartedHandler(new OnServerStartedHandler() {
			@Override
			public void handle() {
				sendRequest(mServerManager, new CollectionRequest()
						.addParameter("sync", System.currentTimeMillis()));
				updateConnectionLabel("Connection status: connected");
				checkConnectionThread.start();
			}
		});
		
		updateConnectionLabel("Connection status: waiting for clients to connect");
		
		mServerManager.startServer();
		mServerManager.registerListener(new InputListener() {
			@Override
			public void handle(CollectionRequest req) {
				Serializable errorParam = req.getParameter("errors");
				if(errorParam != null) {
					try {
						ArrayList<String> errors = (ArrayList<String>) errorParam;
						for(String error : errors) {
							System.out.println("Error: " + error);
						}
					} catch (ClassCastException e) {
					}
					return;
				}
				
				if(req.getParameter("imu") != null) {
					String keyName = (String)req.getParameter("key");
					trialManager.addIMUData(keyName, (HashMap<String, ArrayList<Pair<Long, Point3F>>>)req.getParameter("imu"));
				}
			}
		});
		
		nextBtn.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				requestNextSentence();
			}
		});
		
		sentenceInput.addEventFilter(KeyEvent.ANY, new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent event) {
				long timestamp = System.currentTimeMillis();
				if(event.getEventType() == KeyEvent.KEY_PRESSED) {
					if(event.getCode() == KeyCode.ENTER) {
						requestNextSentence();
					}
					
					keyToStartTime.put(event.getCode().getName(), timestamp);
					
				} else if(event.getEventType() == KeyEvent.KEY_RELEASED) {
					Long startInterval = keyToStartTime.get(event.getCode().getName());
					
					sendRequest(mServerManager, new CollectionRequest(RequestType.DATA)
							.addParameter("interval", Pair.of(startInterval, timestamp))
							.addParameter("key", event.getCode().getName()));
					
					Sentence sentence;
					if((sentence = trialManager.getCurrentSentence()) != null && event.getCode() != KeyCode.ENTER) {
						sentence.coloredCompare(sentenceInput.getText());
						sentenceBox.getChildren().clear();
						sentenceBox.getChildren().addAll(sentence.getComponents());
					}
				}
			}
		});
	}
	
	private void requestNextSentence() {
		sentenceInput.clear();
		sentenceBox.getChildren().clear();
		
		if(trialManager.hasNextSentence() && !trialManager.isTrialOver()) {
			Sentence sentence = trialManager.getNextSentence();
			sentenceBox.getChildren().addAll(sentence.getComponents());
		} else if(trialManager.isLoaded() && trialManager.isTrialOver()) {
			sentenceBox.getChildren().clear();
			statusLabel.setText("All Done!");
		} else {
			statusLabel.setText("Please load sentences");
		}
		sentenceInput.setText("");
	}
	
	@FXML
	public void loadSentences() {
		trialManager.loadSentences(ap);
		Sentence firstSentence;
		if((firstSentence = trialManager.getFirstSentence()) != null) {
			sentenceBox.getChildren().addAll(firstSentence.getComponents());
		}
	}
	
	@FXML
	public void setAuthorInfo() {
		Parent root;
		try {
			root = FXMLLoader.load(getClass().getResource("/application/EditAuthorDialog.fxml"));
			
			Stage stage = new Stage();
			
			stage.setTitle("Set Author Info");
			stage.setScene(new Scene(root, 289, 194));
			stage.setResizable(false);
			stage.show();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@FXML
	public void startNewTrial() {
		trialManager.startNewTrial();
		keyToStartTime.clear();
		sentenceInput.clear();
		sentenceBox.getChildren().clear();
		
		sendRequest(mServerManager, new CollectionRequest(RequestType.NEW_TRIAL));
		
		Sentence firstSentence;
		if((firstSentence = trialManager.getFirstSentence()) != null)
			sentenceBox.getChildren().addAll(firstSentence.getComponents());
	}
	
	@FXML
	public void exportData() {
		trialManager.exportData(ap);
		sendRequest(mServerManager, new CollectionRequest(RequestType.STOP));
	}
	
	public void sendRequest(ConnectionManager cm, CollectionRequest req) {
		try {
			cm.sendRequest(req);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public String getAuthorID() {
		return authorID;
	}
	
	public String getHandedness() {
		return authorHandedness;
	}
	
	public void setAuthorInfo(String id, String hand) {
		authorID = id;
		authorHandedness = hand;
	}
	
	private void updateConnectionLabel(final String text) {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				connStatusLabel.setText(text);
			}
		});
	}
	
	public void close() {
		try {
			checkConnectionThread.interrupt();
			mServerManager.closeConnection();
		} catch (IOException e) {
			e.printStackTrace();
			Platform.exit();
		}
	}
}
