#ifndef __NEURALNETWORK_INCLUDED__
#define __NEURALNETWORK_INCLUDED__

#ifdef __APPLE__
#include <Accelerate/Accelerate.h>
#else
#include <cblas.h>
#endif

#include <string>

using namespace std;

class NeuralNetwork {
 public:
  NeuralNetwork(string theta1_filename, string theta2_filename);
  ~NeuralNetwork();
 private:
  double* theta1Transpose_;
  double* theta2Transpose_;
};

double* read2DMatrixFromFile(string filename);

#endif
