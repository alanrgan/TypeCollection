package connection;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;

public abstract class InputListener extends Thread {
	private volatile boolean close = false;
	private InputStream mInputStream = null;
	
	public void setInputStream(InputStream is, ConnectionManager.Key key) {
		key.hashCode();
		mInputStream = is;
	}
	
	public void start() throws IllegalStateException {
		super.start();
		if(mInputStream == null) {
			this.interrupt();
			close = true;
			throw new IllegalStateException("Cannot start thread without an input stream");
		}
	}
	
	public void run() {
		while(!Thread.currentThread().isInterrupted() && !close) {
			try {
				if(mInputStream.available() > 0) {
					CollectionRequest req;
					try {
						synchronized(mInputStream) {
							ObjectInputStream in = new ObjectInputStream(mInputStream);
							req = (CollectionRequest) in.readObject();
							handle(req);
						}
					} catch (ClassNotFoundException e) {
						e.printStackTrace();
						close = true;
						return;
					}
				}
			} catch (Exception e) {
				onInterrupted(e);
				close = true;
			}
		}
	}
	
	public abstract void handle(CollectionRequest req);
	public abstract void onInterrupted(Exception e);
	
	public void close(ConnectionManager.Key key) throws IOException {
		key.hashCode();
		close = true;
		mInputStream.close();
	}
}
