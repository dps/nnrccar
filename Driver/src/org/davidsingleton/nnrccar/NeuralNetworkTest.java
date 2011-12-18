package org.davidsingleton.nnrccar;

import junit.framework.TestCase;

import org.apache.commons.math.linear.RealMatrix;

public class NeuralNetworkTest extends TestCase {

  public void testPredict() {
    // Tests that network predictions made with trained parameters theta1 and
    // theta2
    // produce results that are within a small epsilon of the values computed by
    // octave implementation.
    RealMatrix theta1 = NeuralNetwork.loadMatrixFromOctaveDatFile("testdata/theta1.dat");
    RealMatrix theta2 = NeuralNetwork.loadMatrixFromOctaveDatFile("testdata/theta2.dat");
    // Test features
    RealMatrix xx = NeuralNetwork.loadMatrixFromOctaveDatFile("testdata/xx.dat");
    // Test predictions
    RealMatrix pp = NeuralNetwork.loadMatrixFromOctaveDatFile("testdata/pp.dat");

    NeuralNetwork nn = new NeuralNetwork(theta1, theta2);

    byte[] features = new byte[4 + 176 * 144];
    for (int i = 0; i < xx.getRowDimension(); i++) {
      for (int j = 0; j < 176 * 144; j++) {
        features[j] = (byte) ((int) xx.getEntry(i, j));
      }
      
      // The test data values have 4 extra features.
      features[176 * 144] = (byte) ((int)xx.getEntry(i, xx.getColumnDimension() - 4));
      features[1 + 176 * 144] = (byte) ((int)xx.getEntry(i, xx.getColumnDimension() - 3));
      features[2 + 176 * 144] = (byte) ((int)xx.getEntry(i, xx.getColumnDimension() - 2));
      features[3 + 176 * 144] = (byte) ((int)xx.getEntry(i, xx.getColumnDimension() - 1));

      double[] p = nn.predict(features);
      assert (Math.abs(pp.getEntry(i, 0) - p[0]) < 1e-9);
      assert (Math.abs(pp.getEntry(i, 1) - p[1]) < 1e-9);
      assert (Math.abs(pp.getEntry(i, 2) - p[2]) < 1e-9);
      assert (Math.abs(pp.getEntry(i, 3) - p[3]) < 1e-9);
    }
  }

}
