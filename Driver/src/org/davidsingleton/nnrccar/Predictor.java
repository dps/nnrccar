package org.davidsingleton.nnrccar;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;


/**
 * A class which makes neural network predictions from the most recent set of features added to its
 * (concurrent) queue.  Should be run in its own Thread.
 */
public class Predictor implements Runnable {
	
  private static final double NN_CONFIDENCE_THRESHOLD = 0.4;

	public interface PredictionListener {
		void onPrediction(double[] pred, boolean left, boolean right, boolean forward, boolean reverse);
	}

  private Queue<byte[]> predictQueue = new ConcurrentLinkedQueue<byte[]>();
  
  private Thread predictThread;
	private PredictionListener listener;
	private NeuralNetwork nn;

  Predictor(PredictionListener listener, NeuralNetwork nn) {
  	this.listener = listener;
  	this.nn = nn;
  	predictThread = new Thread(this);
  	predictThread.start();
  }

	@Override
  public void run() {
		while (true) {
		  if (!predictQueue.isEmpty()) {
	  	  byte[] features = null;

		  	synchronized (predictQueue) {
		  	  int dropped = -1;
		  	  while (!predictQueue.isEmpty()) {
		  	  	// Take the freshest features in the queue, dropping the rest
		  	  	features = predictQueue.remove();
		  	  	dropped = dropped + 1;
		  	  }
		  	  if (dropped > 0) {
		  	  	System.out.println(dropped + " frames dropped from predict queue");
		  	  }
		  	}
		  	predict(features);
		  } else {
		  	try {
	        Thread.sleep(10);
        } catch (InterruptedException e) {
        }
		  }
		}
  }

	public void queuePredict(byte[] features) {
		synchronized (predictQueue) {
			predictQueue.add(features);
		}
	}
	
	private void predict(byte[] features) {
    double[] pred = nn.predict(features);

    boolean left = pred[0] > NN_CONFIDENCE_THRESHOLD;
    boolean right = pred[1] > NN_CONFIDENCE_THRESHOLD;
    boolean forward = pred[2] > NN_CONFIDENCE_THRESHOLD;
    boolean reverse = pred[3] > NN_CONFIDENCE_THRESHOLD;

    listener.onPrediction(pred, left, right, forward, reverse);
  }
}
