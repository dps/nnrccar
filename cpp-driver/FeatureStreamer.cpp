#include <iostream>
#include <time.h>

#include "FeatureStreamer.h"

using namespace std;

FeatureStreamer::~FeatureStreamer() {
}

FeatureStreamer::FeatureStreamer(TCPSocket* socket,
				 Mailbox<Frame>* mailbox) {
  socket_ = socket;
  mailbox_ = mailbox;
}

void FeatureStreamer::Stream() {
  time_t lap = time(NULL);
  int count = 0;
  while (true) {
    Frame* frame = ReceiveFeatureFrame();
    count++;
    mailbox_->post(frame);

    time_t now = time(NULL);
    if ((now - 10) >= lap) {
      cerr << "network: " << (count / (now - lap)) << " fps" << endl;
      count = 0;
      lap = time(NULL);
    }

  }
}

Frame* FeatureStreamer::ReceiveFeatureFrame() {
  int w = ReceiveInt();
  int h = ReceiveInt();
  int ac = ReceiveInt();
  unsigned char* pixels = new unsigned char[w * h];
  int* accels = new int[ac];

  int r = ReceiveBytes(pixels, w * h);
  if (r < w * h) {
    cerr << "Received an incomplete framebuffer" << endl;
  }

  for (int i = 0; i < ac; i++) {
    accels[i] = ReceiveInt();
  }

  // At present, we discard the accelerometer features for prediction
  // by setting ac=0
  return new Frame(w, h, 0, pixels, accels);
}

int FeatureStreamer::ReceiveInt() {
  unsigned char intBuffer[4];
  int recv = 0;
  while (recv < 4) {
    recv += socket_->recv(intBuffer + recv, 4 - recv);
  }
  return ((intBuffer[0] << 24) + (intBuffer[1] << 16) +
	  (intBuffer[2] << 8) + intBuffer[3]);
}

int FeatureStreamer::ReceiveBytes(unsigned char* buffer, int len) {
  int recv = 0;

  while (recv < len) {
    recv += socket_->recv(buffer + recv, len - recv);
  }
  return recv;
}
