#include <cppunit/TestFixture.h>
#include <cppunit/extensions/HelperMacros.h>
#include <cppunit/CompilerOutputter.h>
#include <cppunit/extensions/TestFactoryRegistry.h>
#include <cppunit/TestResult.h>
#include <cppunit/TestResultCollector.h>
#include <cppunit/TestRunner.h>
#include <cppunit/BriefTestProgressListener.h>

#include <iostream>
#include <time.h>

#include "Mailbox.h"

using namespace std;

class DeletionChecker {
public:
  DeletionChecker(int value, int* iod) {
    value_ = value;
    inc_on_delete_ = iod;
  }
  ~DeletionChecker() {
    (*inc_on_delete_)++;
  }
public:
  int value_;
  int* inc_on_delete_;
};

class MailboxTest : public CPPUNIT_NS :: TestFixture {
  CPPUNIT_TEST_SUITE(MailboxTest);
  CPPUNIT_TEST(multiThreadTest);
  CPPUNIT_TEST_SUITE_END();

public:
  void setUp();
  void tearDown();

protected:
  void multiThreadTest();

private:
  Mailbox<DeletionChecker> box_;
};

CPPUNIT_TEST_SUITE_REGISTRATION(MailboxTest);

void MailboxTest::setUp() {
}

void MailboxTest::tearDown() {
}


void* ProducerThreadMain(void* boxPtr) {
  pthread_detach(pthread_self());

  int* to_inc = new int;
  (*to_inc) = 0;

  Mailbox<DeletionChecker>* box = (Mailbox<DeletionChecker>*) boxPtr;
  for (int i = 0; i < 100; i++) {
    DeletionChecker* obj = new DeletionChecker(i, to_inc);
    box->post(obj);
    sleep(0);
  }

  cerr << "ti: " << *to_inc << endl;

  while (*to_inc < 100) {
    // spin - every one should get deleted, some in the other thread so
    // need to wait until then before deleting to_inc.
  }

  delete to_inc;

  return NULL;
}

void MailboxTest::multiThreadTest() {
  pthread_t producerId;

  CPPUNIT_ASSERT_EQUAL(0,
		       pthread_create(&producerId, NULL,
				      ProducerThreadMain, (void *) &box_));

  int lastRead = -1;
  int totRead = 0;
  while(lastRead != 99) {
    DeletionChecker* dc = box_.blockingFetch();
    lastRead = dc->value_;
    delete dc;
    totRead++;
  }

  CPPUNIT_ASSERT(totRead > 0);
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
