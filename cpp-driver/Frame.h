#ifndef __FRAME_INCLUDED__
#define __FRAME_INCLUDED__

using namespace std;

class Frame {
 public:
  ~Frame() {
    delete pixels_;
    delete accels_;
  };
 Frame(int width, int height, int accel_features,
       unsigned char* pixels, int* accels) : 
  width_(width), height_(height), accel_features_(accel_features),
    pixels_(pixels), accels_(accels) {};

 public:
  int width_;
  int height_;
  int accel_features_;
  unsigned char* pixels_;
  int* accels_;
};

#endif // __FRAME_INCLUDED__
