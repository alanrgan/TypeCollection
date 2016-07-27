package application;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javafx.scene.Parent;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import util.Pair;
import util.Point3F;

public class TrialManager {
	private List<Sentence> sentences = new ArrayList<Sentence>();
	private int cSentIdx = 0;
	private boolean loaded;
	private MainController mController;
	
	private ArrayList<KeyIMUData> dataPoints = new ArrayList<>();
	
	public TrialManager(MainController mc) {
		mController = mc;
	}
	
	public void loadSentences(Parent ap) {
		clearSentences();
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Open Sentence File");
		fileChooser.getExtensionFilters().addAll(new ExtensionFilter("Text Files", "*.txt"));
		File selectedFile = fileChooser.showOpenDialog(ap.getScene().getWindow());
		
		if(selectedFile != null) {
			BufferedReader in ;
			try {
				in = new BufferedReader(new FileReader(selectedFile));
				String line;
				while((line = in.readLine()) != null) {
					String[] parts = line.split(",");
					if(parts.length == 2)
						sentences.add(new Sentence(parts[1], Integer.parseInt(parts[0])));
				}
				in.close();
				loaded = true;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	public boolean hasNextSentence() {
		if(sentences.size() == 0)
			return false;
		Sentence sent = sentences.get(cSentIdx).decrement();
		return cSentIdx < sentences.size() && (cSentIdx+1 < sentences.size() || sent.getIterationsLeft() > 0);
	}
	
	public Sentence getNextSentence() {
		if(sentences.get(cSentIdx).getIterationsLeft() <= 0)
			cSentIdx++;
		Sentence sent = sentences.get(cSentIdx);
		sent.clearFormatting();
		return sent;
	}
	
	public Sentence getFirstSentence() {
		if(sentences.size() != 0) 
			return sentences.get(0);
		return null;
	}
	
	public Sentence getCurrentSentence() {
		if(cSentIdx >= sentences.size())
			return null;
		return sentences.get(cSentIdx);
	}
	
	public boolean isLoaded() {
		return loaded;
	}
	
	public boolean isTrialOver() {
		return cSentIdx >= sentences.size();
	}
	
	public void startNewTrial() {
		cSentIdx = 0;
		dataPoints.clear();
		KeyIMUData.resetCount();
		
		for (Sentence sent : sentences) {
			sent.resetIterations();
			sent.clearFormatting();
		}
	}
	
	private void clearSentences() {
		sentences.clear();
		cSentIdx = 0;
		dataPoints.clear();
	}
	
	public void addIMUData(String keyName, HashMap<String, ArrayList<Pair<Long, Point3F>>> map) {
		MainController mController = (MainController) Main.getLoader().getController();
		dataPoints.add(new KeyIMUData(keyName, map.get("accelerometer_data"), map.get("gyroscope_data"),
				mController.getAuthorID(), mController.getHandedness()));
		if (map.containsKey("negative_accel")) {
			dataPoints.add(new KeyIMUData("negative", map.get("negative_accel"), map.get("negative_gyro"),
					mController.getAuthorID(), mController.getHandedness()));
		}
	}
	
	public void exportData(Parent ap) {
		System.out.println("in exportData in TrialManager");
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Save File");
		fileChooser.getExtensionFilters().addAll(new ExtensionFilter("CSV Files", "*.csv"));
		File selectedFile = fileChooser.showSaveDialog(ap.getScene().getWindow());
		String fname = selectedFile.getName().replaceFirst("[.][^.]+$","");
		
		// serialize imu data in case program crashes while writing
		serializeIMU(fname);
		
		System.out.println("selectedFile is null: " + (selectedFile == null) + " dataPoints empty: " + dataPoints.isEmpty());
		if(selectedFile != null && !dataPoints.isEmpty()) {
			try {
				FileWriter writer = new FileWriter(selectedFile, true);
				
				writer.write("SequenceID,Time,x_ddot,y_ddot,z_ddot,theta_dot,phi_dot,psi_dot,label,author-handedness,author-id\r\n");
				
				int counter = 1;
				for(KeyIMUData pt : dataPoints) {
					writer.write(pt.asCSV());
					mController.updateStatusLabel(String.format("%.2f%% written%n",counter/(double)dataPoints.size()*100));
					counter++;
				}
				
				writer.flush();
				writer.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	private void serializeIMU(String fname) {
		File dataSerFile = new File(fname+".ser");
		try {
			FileOutputStream fOut = new FileOutputStream(dataSerFile);
			ObjectOutputStream out = new ObjectOutputStream(fOut);
			out.writeObject(dataPoints);
			fOut.close();
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
