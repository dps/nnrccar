#include "FeatureStreamer.h"
#include <iostream>

using namespace std;

Frame::Frame(int width, int height, int accel_features,
	     unsigned char* pixels, int* accels) {
  width_ = width;
  height_ = height;
  accel_features_ = accel_features;
  pixels_ = pixels;
  accels_ = accels;
}


FeatureStreamer::~FeatureStreamer() {
}

FeatureStreamer::FeatureStreamer(TCPSocket* socket,
				 queue<Frame* >* queue,
				 pthread_mutex_t mutex) {
  socket_ = socket;
  queue_ = queue;
  mutex_ = mutex;
}

void FeatureStreamer::Stream() {
  Frame* frame = ReceiveFeatureFrame();
  cout << frame->width_ << "\t" << frame->height_ << endl;
  delete frame;
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

  return new Frame(w, h, ac, pixels, accels);
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
