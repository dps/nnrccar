#ifndef __FEATURESTREAMER_INCLUDED__
#define __FEATURESTREAMER_INCLUDED__

#include <string>            // For string
#include <exception>         // For exception class
#include <queue>
#include <pthread.h>

#include "PracticalSocket.h"

using namespace std;

class Frame {
 public:
  ~Frame() {
    delete pixels_;
    delete accels_;
  };
  Frame(int width, int height, int accel_features,
	unsigned char* pixels, int* accels);

 public:
  int width_;
  int height_;
  int accel_features_;
  unsigned char* pixels_;
  int* accels_;
};

/**
 *   Feature Streamer
 */
class FeatureStreamer {
public:
  ~FeatureStreamer();
  FeatureStreamer(TCPSocket* socket, queue<Frame* >* queue,
		  pthread_mutex_t mutex);

  void Stream();
  Frame* ReceiveFeatureFrame();

  int ReceiveInt();
  int ReceiveBytes(unsigned char* buffer, int len);

protected:
  TCPSocket* socket_;    // not owned
  queue<Frame* >* queue_; // not owned
  pthread_mutex_t mutex_;
};

#endif
