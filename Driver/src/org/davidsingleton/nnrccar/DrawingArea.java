package org.davidsingleton.nnrccar;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.nio.channels.SelectableChannel;

import javax.swing.JPanel;

import org.davidsingleton.nnrccar.Driver.Mode;

/**
 * A simple panel which renders a set of brightness values (features) from video frames
 * received by a FeatureStreamer.
 */
class DrawingArea extends JPanel {

  private static final long serialVersionUID = 3345306744146300918L;

  byte[] features;
  float[] accelerometerFeatures = new float[3];
  int width;
  int height;

  boolean left;
  boolean right;
  boolean forward;
  boolean reverse;

  private Mode mode = Mode.MANUAL;

  private String pred;

  public DrawingArea() {
    setPreferredSize(new Dimension(144 * 2, 176 * 2));
  }

  public synchronized void paintComponent(Graphics g) {
    super.paintComponent(g);

    int i = 0;

    if (width != 0) {
      for (int y = 0; y < height; y++) {
        for (int x = 0; x < width; x++) {
          int v = ((byte) features[i]);
          if (v < 0)
            v = 256 + v;
          if (v > 255)
            System.out.println(v);
          g.setColor(new Color(v, v, v));
          g.fillRect((height - y - 1) * 2, x * 2, 2, 2);
          i++;
        }
      }
    }
    g.setColor(Color.RED);
    String disp = "";
    switch (mode) {
    case MANUAL:
      disp = "M ";
      break;
    case MANUAL_RECORD:
      disp = "R ";
      break;
    case AUTOMATIC:
      disp = "A ";
      break;
    }
    if (left) {
      disp = disp + "\u2190";
    }
    if (right) {
      disp = disp + "\u2192";
    }
    if (forward) {
      disp = disp + "\u2191";
    }
    if (reverse) {
      disp = disp + "\u2193";
    }
    g.drawChars(disp.toCharArray(), 0, disp.length(), 0, 20);
    if (pred != null) {
      g.setColor(Color.YELLOW);
      g.drawChars(pred.toCharArray(), 0, pred.length(), 0, 340);
    } else if (accelerometerFeatures != null){
    	g.setColor(Color.GREEN);
			String accel = String.format("%f %f %f", accelerometerFeatures[0],
			    accelerometerFeatures[1], accelerometerFeatures[2]);
			g.drawChars(accel.toCharArray(), 0, accel.length(), 0, 340);
    }
  }

  public void update(byte[] features, int width, int height, float[] accelerometerFeatures) {
    this.features = features;
    this.width = width;
    this.height = height;
    this.accelerometerFeatures = accelerometerFeatures;
    repaint();
  }

  public void update(boolean left, boolean right, boolean forward,
      boolean reverse, Mode driveMode) {
    this.left = left;
    this.right = right;
    this.forward = forward;
    this.reverse = reverse;
    this.mode = driveMode;
    repaint();
  }

  public void update(double[] pred) {
    if (pred.length != 4) {
      return;
    }
    this.pred = String.format("%2f %2f %2f %2f", pred[0], pred[1], pred[2], pred[3]);
  }
}