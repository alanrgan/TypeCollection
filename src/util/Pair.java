package util;

import java.io.Serializable;

public final class Pair<A,B> implements Serializable {
	private static final long serialVersionUID = 1L;
	
	public final A first;
	public final B second;
	
	private Pair(A first, B second) {
		this.first = first;
		this.second = second;
	}
	
	public static <A,B> Pair<A,B> of(A first, B second) {
		return new Pair<A,B>(first, second);
	}
}
