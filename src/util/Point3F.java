package util;

import java.io.Serializable;

public class Point3F implements Serializable {
	private static final long serialVersionUID = 4887284704831950688L;
	
	private float x, y, z;
	
	public Point3F() {
		x = y = z = 0;
	}
	
	public Point3F(float x, float y, float z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	public float getX() {
		return x;
	}
	
	public float getY() {
		return y;
	}
	
	public float getZ() {
		return z;
	}
	
	public void get(float[] t) {
		t = new float[3];
		t[0] = x;
		t[1] = y;
		t[2] = z;
	}
}
