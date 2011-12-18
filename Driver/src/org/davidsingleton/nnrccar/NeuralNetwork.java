package org.davidsingleton.nnrccar;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;

import org.apache.commons.math.linear.Array2DRowRealMatrix;
import org.apache.commons.math.linear.RealMatrix;

/**
 * Implementation of a Neural Network with 1 hidden layer. Layer sizes are
 * arbitrary and are determined by the dimensions of the trained weight matrices
 * theta1 and theta2 used to construct the index.
 */
public class NeuralNetwork {

	private RealMatrix theta1Transpose;
	private RealMatrix theta2Transpose;

	/**
	 * Constructor for a new NeuralNetwork.
	 * @param theta1 - a RealMatrix containing weights for connections between the input layer and hidden layer.
	 * @param theta2 - a RealMatrix containing weights for connections between the hidden layer and output layer.
	 */
	NeuralNetwork(RealMatrix theta1, RealMatrix theta2) {
		this.theta1Transpose = theta1.transpose();
		this.theta2Transpose = theta2.transpose();
	}

	double[] predict(byte[] features) {
		double[] xs = new double[features.length + 1];
		xs[0] = 1.0;
		for (int i = 0; i < features.length; i++) {
			xs[i + 1] = features[i] < 0 ? 256 + (double) features[i]
			    : (double) features[i];
		}

		RealMatrix x = new Array2DRowRealMatrix(1, features.length + 1);
		x.setRow(0, xs);

		RealMatrix h1 = sigmoidAddOnes(x.multiply(theta1Transpose));
		RealMatrix h2 = sigmoid(h1.multiply(theta2Transpose));
		double[] out = new double[h2.getColumnDimension()];
		for (int z = 0; z < h2.getColumnDimension(); z++) {
			out[z] = h2.getEntry(0, z);
		}
		return out;
	}

	RealMatrix sigmoid(RealMatrix z) {
		// g = 1.0 ./ (1.0 + exp(-z));
		RealMatrix m = z.copy();
		for (int i = 0; i < m.getRowDimension(); i++) {
			for (int j = 0; j < m.getColumnDimension(); j++) {
				double y = m.getEntry(i, j);
				double g = 1.0 / (1.0 + Math.exp(-y));
				m.setEntry(i, j, g);
			}
		}
		return m;
	}

	RealMatrix sigmoidAddOnes(RealMatrix z) {
		// g = 1.0 ./ (1.0 + exp(-z));
		RealMatrix m = new Array2DRowRealMatrix(z.getRowDimension(),
		    z.getColumnDimension() + 1);
		for (int i = 0; i < m.getRowDimension(); i++) {
			for (int j = 0; j < m.getColumnDimension(); j++) {
				double g = 1.0;
				if (j != 0) {
					double y = z.getEntry(i, j - 1);
					g = 1.0 / (1.0 + Math.exp(-y));
				}
				m.setEntry(i, j, g);
			}
		}
		return m;
	}

	public static RealMatrix loadMatrixFromOctaveDatFile(String filename) {
		/*
		 * Example file format: # Created by Octave 3.4.0, Mon Nov 21 21:47:56 2011
		 * GMT <foo@foo> # name: Theta1 # type: matrix # rows: 8 # columns: 25349
		 * -0.1138064731504729 -0.0001669091035754908 3.817117905771809e-05 ...
		 */
		RealMatrix result = null;
		LineNumberReader lnr = null;
		try {
			FileReader fr = new FileReader(filename);
			lnr = new LineNumberReader(fr);
			int rows = -1;
			int cols = -1;
			boolean created = false;
			String line;
			int rowc = 0;
			int colc = 0;
			while (true) {
				line = lnr.readLine();
				if (line == null) {
					break;
				}
				if (line.startsWith("#")) {
					String[] tokens = line.split(" ");
					if (tokens[1].equals("rows:")) {
						rows = Integer.parseInt(tokens[2]);
					}
					if (tokens[1].equals("columns:")) {
						cols = Integer.parseInt(tokens[2]);
					}

					if (rows > 0 && cols > 0 && !created) {
						result = new Array2DRowRealMatrix(rows, cols);
						created = true;
					}
				} else {
					if (!created) {
						System.err.println("Unexpected non-header read at line "
						    + lnr.getLineNumber() + ":" + filename);
						throw new IOException("Invalid file format");
					}
					String[] tokens = line.split(" ");
					for (String token : tokens) {
						if (token.equals("")) {
							continue;
						}
						double d = Double.parseDouble(token);
						result.setEntry(rowc, colc, d);
						colc++;
					}
					rowc++;
					colc = 0;
				}
			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			// End of file
		} catch (NumberFormatException e) {
			System.err.println("NumberFormatException reading line "
			    + lnr.getLineNumber() + " of " + filename);
			e.printStackTrace();
		}
		return result;
	}
}
