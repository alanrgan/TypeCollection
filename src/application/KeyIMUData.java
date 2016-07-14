package application;

import java.util.ArrayList;

import util.Pair;
import util.Point3F;

public class KeyIMUData {
	private static int count = 0;
	
	private String key;
	private ArrayList<Pair<Long, Point3F>> accelValues, gyroValues;
	private int id;
	private String authID, handedness;
	
	public KeyIMUData(String keyName, ArrayList<Pair<Long, Point3F>> accel, ArrayList<Pair<Long, Point3F>> gyro, String authID, String hand) {
		this.key = keyName;
		this.accelValues = accel;
		this.gyroValues = gyro;
		this.authID = authID;
		this.handedness = hand;
		id = ++count;
	}
	
	public String asCSV() {
		String result = "";
		int endIdx = Math.min(accelValues.size(), gyroValues.size());
		for(int i = 0; i < endIdx; i++) {
			Point3F accelPt = accelValues.get(i).second;
			Point3F gyroPt = gyroValues.get(i).second;
			long time = gyroValues.get(i).first.longValue();
			
			result += String.format("%d,%d,%3.12f,%3.12f,%3.12f,%3.12f,%3.12f,%3.12f,%s,%s,%s%n",
					id, time, accelPt.getX(), accelPt.getY(), accelPt.getZ(), gyroPt.getX(), gyroPt.getY(),
					gyroPt.getZ(), key, authID, handedness);
		}
		
		return result;
	}
	
}
