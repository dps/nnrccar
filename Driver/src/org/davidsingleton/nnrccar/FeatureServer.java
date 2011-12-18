/**
 * 
 */
package org.davidsingleton.nnrccar;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * A TCP server which listens on port 6666 for connections from the NNRCCar android app.
 * 
 * The server reads streams of brightness features from video frames sent from the NNRCCar app and
 * notifies the {@link FeatureCallback} listener initialized in its constructor each time a full
 * set of features is received.
 */
public class FeatureServer {
	
	class ServingThread extends Thread {
		
		private Socket sock;
		
		public ServingThread(Socket s) {
			sock = s;
		}
		
		@Override
		public void run() {
			try {
				DataInputStream dis = new DataInputStream(sock.getInputStream());
				while (true) {
					/*
					 * The "protocol" for each set of features
					 * [width]  - int
					 * [height] - int
					 * [A = # accelerometer features] - int
					 * (width x height) x [brightness value] - width x height bytes
					 * A x [accelerometerValue]  - A ints
					 */
					int width = dis.readInt();
					int height = dis.readInt();
					int accelerometerFeatureCount = dis.readInt();

					//System.out.println("[" + width + "x" + height + "]");
					int len = width * height;
					byte[] features = new byte[width * height];
					float[] accelerometerFeatures = new float[accelerometerFeatureCount];
					
					int read = 0;
					while (read < len) {
						read += dis.read(features, read, len - read);
					}
					for (int i = 0; i < accelerometerFeatureCount; i++) {
						accelerometerFeatures[i] = dis.readFloat();
					}
					//System.out.println(len + " " + ((byte)features[0]));
					listener.features(features, width, height, accelerometerFeatures);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	};
	
	ServerSocket ss;
	private FeatureCallback listener;
	
	/**
	 * Constructor for a new FeatureServer which will notify a {@link FeatureCallback} listener each time a full
	 * set of features is received.
	 * @param cb
	 */
	FeatureServer(FeatureCallback cb) {
		this.listener = cb;
		try {
			ss = new ServerSocket(6666);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Start listening for incoming TCP connections.  Starts the TCP server in its own new Thread.
	 * @throws IOException
	 */
	void startServing() throws IOException {
		while (true) {
			Socket s = ss.accept();
			ServingThread t = new ServingThread(s);
			t.start();
		}
	}

}
