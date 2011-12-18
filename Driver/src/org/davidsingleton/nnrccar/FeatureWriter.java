package org.davidsingleton.nnrccar;

import java.io.PrintStream;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * A class which queues feature frames for asynchronous writing to disk.
 * Should be run in its own Thread.
 */
class FeatureWriter implements Runnable {
  private long last;
  private PrintStream featuresOut;
  private Queue<FeatureFrame> toWrite = new ConcurrentLinkedQueue<FeatureFrame>();

  FeatureWriter(PrintStream featuresOut) {
    this.featuresOut = featuresOut;
  }

  @Override
  public void run() {

    while (true) {
      if (!toWrite.isEmpty()) {
        System.out.println("FeatureWriter " + toWrite.size() + " pending");
        FeatureFrame ff = toWrite.remove();
        writeFeatures(ff);
      }
    }
  }
  
  private void writeFeatures(FeatureFrame ff) {
    long s = System.currentTimeMillis();
    featuresOut.println(ff.getFeatures().length + " "
        + (ff.isLeft() ? "1" : "0") + " " + (ff.isRight() ? "1" : "0") + " "
        + (ff.isForward() ? "1" : "0") + " " + (ff.isReverse() ? "1" : "0")
        + " ");
    for (int i = 0; i < ff.getFeatures().length; i++) {
      byte[] features = ff.getFeatures();
      featuresOut.print(features[i] + " ");
    }
    featuresOut.print(ff.getLeftStateLenMs() + " ");
    featuresOut.print(ff.getRightStateLenMs() + " ");
    featuresOut.print(ff.getForwardStateLenMs() + " ");
    featuresOut.print(ff.getReverseStateLenMs() + " ");
    featuresOut.print(ff.getAccelerometerFeature(0) + " ");
    featuresOut.print(ff.getAccelerometerFeature(1) + " ");
    featuresOut.print(ff.getAccelerometerFeature(2) + " ");

    featuresOut.println();
    System.out.println("WF: " + (System.currentTimeMillis() - s));
    System.out.println("delta: " + (last - s));
    last = s;
  }
  
  public void enqueue(FeatureFrame ff) {
    toWrite.add(ff);
  }
  
}