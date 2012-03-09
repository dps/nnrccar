#include <iostream>
#include <fstream>
#include <math.h>

#include "NeuralNetwork.h"

NeuralNetwork::NeuralNetwork(string theta1_filename, string theta2_filename) {
  theta1_ = read2DMatrixFromFile(theta1_filename);
  theta2_ = read2DMatrixFromFile(theta2_filename);
}

NeuralNetwork::~NeuralNetwork() {
  delete theta1_.data;
  delete theta2_.data;
}

double* NeuralNetwork::predict(Frame* frame) {
  double* xs = new double[frame->width_ * frame->height_ + 1];
  xs[0] = 1.0;
  for (int i = 0; i < frame->width_ * frame->height_; i++) {
    xs[i + i] = (double) frame->pixels_[i];
  }

  // x = (1 x 25345)
  // tt = (25345 x 64)
  // res = (1 x 64)

  int pixels = frame->width_ * frame->height_ + 1;
  double* mul = new double[theta1_.rows];

  cblas_dgemm(CblasRowMajor, CblasNoTrans, CblasTrans,
	      1, theta1_.rows, pixels, 1.0,
	      xs, pixels, theta1_.data, theta1_.cols, 1.0, mul, theta1_.rows);

  //for (int i = 0; i < theta1_.rows; i++) {
  //  cout << mul[i] << endl;
  //}
  delete xs;

  double* h1 = new double[theta1_.rows + 1];
  h1[0] = 1.0;
  for (int j = 0; j < theta1_.rows; j++) {
    h1[j + 1] = sigmoid(mul[j]);
  }

  delete mul;

  // h1 = (1 x 65)
  // t2t = (65 x 4)
  // o = (1 x 4)
  double* res = new double[theta2_.rows];

  cblas_dgemm(CblasRowMajor, CblasNoTrans, CblasTrans,
	      1, theta2_.rows, theta2_.cols, 1.0,
	      h1, theta2_.cols, theta2_.data, theta2_.cols, 1.0,
	      res, theta2_.rows);

  for (int j = 0; j < theta2_.rows; j++) {
    res[j] = sigmoid(res[j]);
  }
  return res;	      
}

inline double sigmoid(double x) {
  return (1.0 / (1.0 + exp(-x)));
}

Matrix read2DMatrixFromFile(string filename) {
  /**
     # Created by Octave 3.4.0, Thu Dec 22 18:40:26 2011 GMT
     # name: Theta1
     # type: matrix
     # rows: 64
     # columns: 25345
     -0.08699511748607204 -0.0009880521168291463 ...
   */
  ifstream file(filename.c_str());
  string line;

  bool reading_header = true;
  int rows = -1;
  int cols = -1;
  int r = 0;
  int c = 0;

  double* ret = NULL;

  if (file.is_open()) {
    while (file.good()) {
      getline(file, line);

      if (reading_header) {
        if (line.find("# rows:") == 0) {
          rows = atoi(line.substr(7, line.length() - 7).c_str());
	} else if (line.find("# columns:") == 0) {
          cols = atoi(line.substr(11, line.length() - 11).c_str());
	  cout << "Reading " << rows << "x" << cols << " matrix" << endl;
          ret = new double[rows * cols];
          reading_header = false;
	}
      } else {
        int pos = 1;
        c = 0;
        while (c < cols) {
	  int end = line.find(' ', pos);
          if (end < 0) {
	    end = line.length() - 1;
	  }
	  ret[(r * cols) + c] = atof(line.substr(pos, end - pos).c_str());

	  pos = end + 1;
          c++;
	}
	cout << "r" << r << endl;
        r++;
      }

      if (r == rows) {
	break;
      }
    }
  }

  Matrix mat;
  mat.rows = rows;
  mat.cols = cols;
  mat.data = ret;
  // Caller takes ownership of the data
  return mat;
}
