#include <iostream>
#include <fstream>

#include "NeuralNetwork.h"

NeuralNetwork::NeuralNetwork(string theta1_filename, string theta2_filename) {
  theta1Transpose_ = read2DMatrixFromFile(theta1_filename);
  theta2Transpose_ = read2DMatrixFromFile(theta2_filename);
}

NeuralNetwork::~NeuralNetwork() {
  delete theta1Transpose_;
  delete theta2Transpose_;
}

double* read2DMatrixFromFile(string filename) {
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

  return NULL;
}
