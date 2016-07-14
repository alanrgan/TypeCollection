package application;

import java.util.ArrayList;
import java.util.List;

import javafx.scene.paint.Color;
import javafx.scene.text.Text;

public class Sentence {
	private int numIterations;
	private int iterationsLeft;
	private List<Text> letters = new ArrayList<Text>();
	
	public Sentence(String text, int iterations) {
		char[] charArray = text.toCharArray();
		for(int i = 0; i < charArray.length; i++) {
			Text letter = new Text(Character.toString(charArray[i]));
			letter.getStyleClass().add("letter");
			letters.add(letter);
		}
		this.numIterations = iterations;
		this.iterationsLeft = iterations;
	}
	
	public List<Text> getComponents() {
		return letters;
	}
	
	public void coloredCompare(String text) {
		if(text.length() > letters.size()) {
			for(int i = 0; i < letters.size(); i++)
				letters.get(i).setFill(Color.RED);
		} else {
			for(int i = 0; i < text.length(); i++) {
				Text letterText = letters.get(i);
				char letter = letterText.getText().toCharArray()[0];
				if(text.charAt(i) != letter)
					break;
				letterText.setFill(Color.GREEN);
			}
		}
	}
	
	public void clearFormatting() {
		for(int i = 0; i < letters.size(); i++) {
			Text letterText = letters.get(i);
			letterText.setFill(Color.BLACK);
		}
	}
	
	public boolean isEmpty() {
		return letters.isEmpty();
	}
	
	public int getIterationsLeft() {
		return iterationsLeft;
	}
	
	public void resetIterations() {
		iterationsLeft = numIterations;
	}
	
	public Sentence decrement() {
		iterationsLeft--;
		return this;
	}
	
}
