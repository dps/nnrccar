package org.davidsingleton.nnrccar;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;

import javax.swing.JFrame;
import javax.swing.JPanel;

import org.apache.commons.math.linear.RealMatrix;
import org.davidsingleton.nnrccar.Predictor.PredictionListener;

/**
 * Part of NNRCCar - a project to create a self driving radio controlled car.
 * 
 * Driver is an example AWT application which acts as both a TCP server,
 * receiving streamed image frames from a video streaming app on a phone and
 * a user interface allowing a human driver to control the car with the cursor
 * keys or mouse, sent to an RC unit over the serial interface.
 * 
 * RECORD MODE
 * 
 * In record mode, the video frames are saved to disk, labelled with the current control
 * input coming from the human driver. The neural network is trained using these
 * labelled frames in a separate environment on the computer. Trained parameters
 * are saved out to files which are in turn read by the Driver app...
 * 
 * AUTO MODE
 * 
 * Auto mode can feed incoming video frames directly to the neural network and
 * steer according to its predictions, by sending instructions over a serial
 * interface.
 */
public class Driver extends JFrame implements FeatureCallback, KeyListener,
    MouseMotionListener, PredictionListener {

	private DrawingArea view = new DrawingArea();
	private SerialWrapper serial = null;

	enum Mode {
		MANUAL, MANUAL_RECORD, AUTOMATIC
	};

	private Mode driveMode = Mode.MANUAL;
	private boolean left = false;
	private boolean right;
	private boolean forward;
	private boolean reverse;
	private long leftLastChangeMs = 1000;
	private long rightLastChangeMs = 1000;
	private long forwardLastChangeMs = 1000;
	private long reverseLastChangeMs = 1000;

	private PrintStream featuresOut;
	private FeatureWriter writer;

	private NeuralNetwork nn;
	private Point prevMousePoint;
	private Predictor predictor;

	public Driver(SerialWrapper serial, File featureOutFile, String theta1File,
	    String theta2File) {
		this.serial = serial;
		try {
			RealMatrix theta1 = NeuralNetwork
			    .loadMatrixFromOctaveDatFile("data/theta1.dat");
			RealMatrix theta2 = NeuralNetwork
			    .loadMatrixFromOctaveDatFile("data/theta2.dat");
			nn = new NeuralNetwork(theta1, theta2);
			this.featuresOut = new PrintStream(featureOutFile);
		} catch (FileNotFoundException e) {
		}
		setBounds(0, 0, 176 * 2, 144 * 2);
		view.setBackground(Color.white);

		JPanel content = new JPanel();
		content.setLayout(new BorderLayout(5, 5));
		content.add(view, BorderLayout.CENTER);

		setContentPane(content);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setTitle("Driver");
		setLocationRelativeTo(null);
		addKeyListener(this);
		addMouseMotionListener(this);
		pack();

		writer = new FeatureWriter(featuresOut);
		predictor = new Predictor(this, nn);
		Thread writeThread = new Thread(writer);
		writeThread.start();
	}

	public static void main(String[] args) {
		if (args.length != 1) {
			System.out.println("Driver\nSyntax:  Driver [serial port path]");
			return;
		}
		String serialPort = args[0];
		File out = null;
		try {
			out = File.createTempFile("nnrccar", "features");
			System.out.println("Features writing to: "
			    + out.getAbsoluteFile().toString());
		} catch (IOException e) {
			e.printStackTrace();
		}

		Driver window = new Driver(new RxTxSerialWrapper(serialPort), out,
		    "data/theta1.dat", "data/theta2.dat");
		FeatureServer fs = new FeatureServer(window);
		window.setVisible(true);
		try {
			fs.startServing();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void features(byte[] features, int width, int height,
	    float[] accelerometerFeatures) {
    // This method is called ever time new features have been received from
		// the camera via FeatureServer.
		if (driveMode == Mode.MANUAL_RECORD && featuresOut != null) {
			long now = System.currentTimeMillis();
			writeFeatures(new FeatureFrame(features, left, right, forward, reverse,
			    (int) (now - leftLastChangeMs), (int) (now - rightLastChangeMs),
			    (int) (now - forwardLastChangeMs), (int) (now - reverseLastChangeMs),
			    accelerometerFeatures));
		}
		if (driveMode == Mode.AUTOMATIC) {
			predictWithFeatures(features);
		}
		view.update(features, width, height, accelerometerFeatures);
	}

	private void predictWithFeatures(byte[] features) {
		predictor.queuePredict(features);
	}

	@Override
	public void onPrediction(double[] pred, boolean nowLeft, boolean nowRight,
	    boolean nowFwd, boolean nowReverse) {

		long now = System.currentTimeMillis();
		boolean sendControls = false;
		if (nowLeft != left) {
			leftLastChangeMs = now;
			sendControls = true;
		}
		if (nowRight != right) {
			rightLastChangeMs = now;
			sendControls = true;
		}
		if (nowFwd != forward) {
			forwardLastChangeMs = now;
			sendControls = true;
		}
		if (nowReverse != reverse) {
			reverseLastChangeMs = now;
			sendControls = true;
		}

		left = nowLeft;
		right = nowRight;
		forward = nowFwd;
		reverse = nowReverse;
		view.update(pred);
		view.update(left, right, forward, reverse, driveMode);
		if (sendControls) {
			sendControls(left, right, forward, reverse);
		}
	}

	private void writeFeatures(FeatureFrame ff) {
		writer.enqueue(ff);
	}

	void sendControls(boolean left, boolean right, boolean forward,
	    boolean reverse) {
		char outCh = 'p';
		if (left && right) {
			left = right = false;
		}
		if (left) {
			outCh |= 0x01;
		}
		if (right) {
			outCh |= 0x02;
		}
		if (forward) {
			outCh |= 0x04;
		}
		if (reverse) {
			outCh |= 0x08;
		}
		System.out.println("Sending " + outCh);
		if (serial != null) {
			serial.print(outCh);
		}
	}

	@Override
	public void keyPressed(KeyEvent e) {
		int keyCode = e.getKeyCode();
		System.out.println("keyPressed " + keyCode + " " + e.getWhen());

		if (keyCode == KeyEvent.VK_LEFT) {
			left = true;
			leftLastChangeMs = System.currentTimeMillis();
		} else if (keyCode == KeyEvent.VK_RIGHT) {
			right = true;
			rightLastChangeMs = System.currentTimeMillis();
		} else if (keyCode == KeyEvent.VK_UP) {
			forward = true;
			forwardLastChangeMs = System.currentTimeMillis();
		} else if (keyCode == KeyEvent.VK_DOWN) {
			reverse = true;
			reverseLastChangeMs = System.currentTimeMillis();
		}

		e.consume();
		sendControls(left, right, forward, reverse);
		view.update(left, right, forward, reverse, driveMode);
	}

	@Override
	public void keyReleased(KeyEvent e) {
		int keyCode = e.getKeyCode();
		System.out.println("keyReleased " + keyCode);

		if (keyCode == KeyEvent.VK_LEFT) {
			left = false;
			leftLastChangeMs = System.currentTimeMillis();
		} else if (keyCode == KeyEvent.VK_RIGHT) {
			right = false;
			rightLastChangeMs = System.currentTimeMillis();
		} else if (keyCode == KeyEvent.VK_UP) {
			forward = false;
			forwardLastChangeMs = System.currentTimeMillis();
		} else if (keyCode == KeyEvent.VK_DOWN) {
			reverse = false;
			reverseLastChangeMs = System.currentTimeMillis();
		} else if (keyCode == KeyEvent.VK_M) {
			driveMode = Mode.MANUAL;
		} else if (keyCode == KeyEvent.VK_R) {
			driveMode = driveMode == Mode.MANUAL_RECORD ? Mode.MANUAL
			    : Mode.MANUAL_RECORD;
			if (driveMode == Mode.MANUAL_RECORD) {
				forward = true;
			} else {
				forward = false;
			}
			forwardLastChangeMs = System.currentTimeMillis();
		} else if (keyCode == KeyEvent.VK_A) {
			driveMode = Mode.AUTOMATIC;
		} else if (keyCode == KeyEvent.VK_F) {
			forward = !forward;
			forwardLastChangeMs = System.currentTimeMillis();
		}

		sendControls(left, right, forward, reverse);
		view.update(left, right, forward, reverse, driveMode);
	}

	@Override
	public void keyTyped(KeyEvent e) {
	}

	@Override
	public void mouseDragged(MouseEvent arg0) {
	}

	@Override
	public void mouseMoved(MouseEvent event) {
		if (prevMousePoint != null) {
			double diff = event.getPoint().getX() - prevMousePoint.getX();
			if (driveMode != Mode.AUTOMATIC) {
				if (diff > 0) {
					if (left) {
						left = false;
						leftLastChangeMs = System.currentTimeMillis();
					} else if (System.currentTimeMillis() - leftLastChangeMs > 250) {
						right = true;
						rightLastChangeMs = System.currentTimeMillis();
					}
				}
				if (diff < 0) {
					if (right) {
						right = false;
						rightLastChangeMs = System.currentTimeMillis();
					} else if (System.currentTimeMillis() - rightLastChangeMs > 250) {
						left = true;
						leftLastChangeMs = System.currentTimeMillis();
					}
				}
			}
			sendControls(left, right, forward, reverse);
			view.update(left, right, forward, reverse, driveMode);
		}
		prevMousePoint = event.getPoint();
	}

}
