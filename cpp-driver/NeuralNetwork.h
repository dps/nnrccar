#ifndef __NEURALNETWORK_INCLUDED__
#define __NEURALNETWORK_INCLUDED__

#ifdef __APPLE__
#include <Accelerate/Accelerate.h>
#else
#include <cblas.h>
#endif

#include <string>
#include "Frame.h"

using namespace std;

struct Matrix {
  double* data;
  int rows;
  int cols;
};

class NeuralNetwork {
 public:
  NeuralNetwork(string theta1_filename, string theta2_filename);
  ~NeuralNetwork();

  double* predict(Frame* frame);
 private:
  Matrix theta1_;
  Matrix theta2_;
};

Matrix read2DMatrixFromFile(string filename);

double sigmoid(double x);

#endif
