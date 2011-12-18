package org.davidsingleton.nnrccar;

/**
 * A class to represent all the features and control labels sampled at a particular
 * moment in time.
 */
public class FeatureFrame {

  byte[] features;
  float[] accelerometerFeatures;
  boolean left;
  boolean right;
  boolean forward;
  boolean reverse;
  int leftStateLenMs;
  int rightStateLenMs;
  int forwardStateLenMs;
  int reverseStateLenMs;

  public int getLeftStateLenMs() {
    return leftStateLenMs;
  }

  public int getRightStateLenMs() {
    return rightStateLenMs;
  }

  public int getForwardStateLenMs() {
    return forwardStateLenMs;
  }

  public int getReverseStateLenMs() {
    return reverseStateLenMs;
  }
  

  public FeatureFrame(byte[] features, boolean left, boolean right,
	    boolean forward, boolean reverse, int leftTime, int rightTime,
	    int forwardTime, int reverseTime, float[] accelerometerFeatures) {
    super();
    this.features = features;
    this.left = left;
    this.right = right;
    this.forward = forward;
    this.reverse = reverse;
    this.leftStateLenMs = leftTime;
    this.rightStateLenMs = rightTime;
    this.forwardStateLenMs = forwardTime;
    this.reverseStateLenMs = reverseTime;
    this.accelerometerFeatures = accelerometerFeatures;
  }

  public byte[] getFeatures() {
    return features;
  }
  public boolean isLeft() {
    return left;
  }
  public boolean isRight() {
    return right;
  }
  public boolean isForward() {
    return forward;
  }
  public boolean isReverse() {
    return reverse;
  }

	public float getAccelerometerFeature(int i) {
	  return accelerometerFeatures[i];
  }
  
}
