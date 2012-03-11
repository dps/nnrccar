#include <cppunit/TestFixture.h>
#include <cppunit/extensions/HelperMacros.h>
#include <cppunit/CompilerOutputter.h>
#include <cppunit/extensions/TestFactoryRegistry.h>
#include <cppunit/TestResult.h>
#include <cppunit/TestResultCollector.h>
#include <cppunit/TestRunner.h>
#include <cppunit/BriefTestProgressListener.h>

#include <cmath>
#include <iostream>
#include <time.h>

#include "NeuralNetwork.h"

using namespace std;

class NeuralNetworkTest : public CPPUNIT_NS :: TestFixture {
  CPPUNIT_TEST_SUITE(NeuralNetworkTest);
  CPPUNIT_TEST(predictTest);
  CPPUNIT_TEST_SUITE_END();

public:
  void setUp();
  void tearDown();

protected:
  void predictTest();

private:
  NeuralNetwork* nn_;
};

CPPUNIT_TEST_SUITE_REGISTRATION(NeuralNetworkTest);

void NeuralNetworkTest::setUp() {
  nn_ = new NeuralNetwork("testdata/theta1.dat", "testdata/theta2.dat");
}

void NeuralNetworkTest::tearDown() {
  delete nn_;
}


void NeuralNetworkTest::predictTest() {
  Matrix xx = read2DMatrixFromFile("testdata/xx.dat");
  Matrix pp = read2DMatrixFromFile("testdata/pp.dat");
  CPPUNIT_ASSERT(xx.data[0] == 10);

  int used = 0;
  int po = 0;

  while (used < xx.rows * xx.cols) {
    unsigned char* features = new unsigned char[xx.cols];
    for (int i = 0; i < xx.cols; i++) {
      features[i] = ((unsigned char)((int)xx.data[used + i]));
    }
    used = used + xx.cols;

    Frame* f = new Frame(176, 144, 4, features, NULL);
    double* res = nn_->predict(f);

    CPPUNIT_ASSERT(abs(res[0] - pp.data[po]) < 1e-5);
    CPPUNIT_ASSERT(abs(res[1] - pp.data[po + 1]) < 1e-5);
    CPPUNIT_ASSERT(abs(res[2] - pp.data[po + 2]) < 1e-5);
    CPPUNIT_ASSERT(abs(res[3] - pp.data[po + 3]) < 1e-5);

    delete res;
    delete f; // also deletes features
    po+=4;
  }


  delete xx.data;
  delete pp.data;
}



int main(int argc, char* argv[]) {
  // informs test-listener about testresults
  CPPUNIT_NS::TestResult testresult;
  
  // register listener for collecting the test-results
  CPPUNIT_NS::TestResultCollector collectedresults;
  testresult.addListener(&collectedresults);
  
  // register listener for per-test progress output
  CPPUNIT_NS::BriefTestProgressListener progress;
  testresult.addListener(&progress);
  
  // insert test-suite at test-runner by registry
  CPPUNIT_NS::TestRunner testrunner;
  testrunner.addTest(CPPUNIT_NS::TestFactoryRegistry::getRegistry().makeTest());
  testrunner.run(testresult);
  
  // output results in compiler-format
  CPPUNIT_NS::CompilerOutputter compileroutputter (&collectedresults, std::cerr);
  compileroutputter.write();
  
  // return 0 if tests were successful
  return collectedresults.wasSuccessful () ? 0 : 1;
}
