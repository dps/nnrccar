//
//  main.cpp
//
//  Created by David Singleton on 06/03/2012.
//

#include "PracticalSocket.h"  // For Socket, ServerSocket, and SocketException
#include <iostream>           // For cout, cerr
#include <cstdlib>            // For atoi()  
#include <pthread.h>          // For POSIX threads  

#ifdef __APPLE__
#include <Accelerate/Accelerate.h>
#else
#include <cblas.h>
#endif

double m[] = {
    3, 1, 3,
    1, 5, 9,
    2, 6, 5
};

double x[] = {
    -1, -1, 1
};

double y[] = {
    0, 0, 0
};

int test_blas() {
  int i, j;
    
  for (i=0; i<3; ++i) {
    for (j=0; j<3; ++j) printf("%5.1f", m[i*3+j]);
    putchar('\n');
  }
    
  cblas_dgemv(CblasRowMajor, CblasNoTrans, 3, 3, 1.0, m, 3,
	      x, 1, 0.0, y, 1);
    
  for (i=0; i<3; ++i) {
    printf("%5.1f\n", y[i]);
  }

  return 0;
}


const int RCVBUFSIZE = 32;

void HandleTCPClient(TCPSocket *sock);     // TCP client handling function
void *ThreadMain(void *arg);               // Main program of a thread  

int main(int argc, char *argv[]) {
  test_blas();
  
  if (argc != 2) {                 // Test for correct number of arguments  
    cerr << "Usage: " << argv[0] << " <Server Port> " << endl;
    exit(1);
  }
    
  unsigned short echoServPort = atoi(argv[1]);    // First arg:  local port  
  
  try {
    TCPServerSocket servSock(echoServPort);   // Socket descriptor for server  
        
    for (;;) {      // Run forever  
      // Create separate memory for client argument  
      TCPSocket *clntSock = servSock.accept();
      
      // Create client thread  
      pthread_t threadID;              // Thread ID from pthread_create()  
      if (pthread_create(&threadID, NULL, ThreadMain, 
			 (void *) clntSock) != 0) {
	cerr << "Unable to create thread" << endl;
	exit(1);
      }
    }
  } catch (SocketException &e) {
    cerr << e.what() << endl;
    exit(1);
  }
  // NOT REACHED
    
  return 0;
}

// TCP client handling function
void HandleTCPClient(TCPSocket *sock) {
  cout << "Handling client ";
  try {
    cout << sock->getForeignAddress() << ":";
  } catch (SocketException &e) {
    cerr << "Unable to get foreign address" << endl;
  }
  try {
    cout << sock->getForeignPort();
  } catch (SocketException &e) {
    cerr << "Unable to get foreign port" << endl;
  }
  cout << " with thread " << pthread_self() << endl;
    
  // Send received string and receive again until the end of transmission
  char echoBuffer[RCVBUFSIZE];
  int recvMsgSize;
  while ((recvMsgSize = sock->recv(echoBuffer, RCVBUFSIZE)) > 0) { // Zero means
    // end of transmission
    // Echo message back to client
    sock->send(echoBuffer, recvMsgSize);
  }
  // Destructor closes socket
}

void *ThreadMain(void *clntSock) {
  // Guarantees that thread resources are deallocated upon return  
  pthread_detach(pthread_self()); 
  
  // Extract socket file descriptor from argument  
  HandleTCPClient((TCPSocket *) clntSock);
  
  delete (TCPSocket *) clntSock;
  return NULL;
}
