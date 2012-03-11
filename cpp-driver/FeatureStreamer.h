#ifndef __FEATURESTREAMER_INCLUDED__
#define __FEATURESTREAMER_INCLUDED__

#include <string>            // For string
#include <exception>         // For exception class
#include <pthread.h>

#include "Frame.h"
#include "Mailbox.h"
#include "PracticalSocket.h"

using namespace std;

/**
 *   Feature Streamer
 */
class FeatureStreamer {
public:
  ~FeatureStreamer();
  FeatureStreamer(TCPSocket* socket, Mailbox<Frame>* mailbox);

  void Stream();
  Frame* ReceiveFeatureFrame();

  int ReceiveInt();
  int ReceiveBytes(unsigned char* buffer, int len);

protected:
  TCPSocket* socket_;       // not owned
  Mailbox<Frame>* mailbox_; // not owned

};

#endif
