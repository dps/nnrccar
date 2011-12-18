Driver README
-------------

Introduction
------------

Part of NNRCCar - a project to create a self driving radio controlled car.

Driver is an example AWT application which acts as both a TCP server,
receiving streamed image frames from a video streaming app on a phone and
a user interface allowing a human driver to control the car with the cursor
keys or mouse, sent to an RC unit over the serial interface.

RECORD MODE

In record mode, the video frames are saved to disk, labelled with the current control
input coming from the human driver. The neural network is trained using these
labelled frames in a separate environment on the computer. Trained parameters
are saved out to files which are in turn read by the Driver app...

AUTO MODE

Auto mode can feed incoming video frames directly to the neural network and
steer according to its predictions, by sending instructions over a serial
interface.


Dependencies
------------

Driver depends on:

The RxTx Serial library  http://rxtx.qbang.org/wiki/index.php/Main_Page
Apache Commons Math library  http://commons.apache.org/math/
  docs: http://commons.apache.org/math/userguide/linear.html


Build and Run
-------------

Using the build.xml file in the root of the Driver source:

ant
ant Driver

License
-------

Driver is released under the Simplified BSD License:

Copyright (c) 2011, David Singleton
All rights reserved.

Redistribution and use in source and binary forms, with or without modification, are
permitted provided that the following conditions are met:

   1. Redistributions of source code must retain the above copyright notice, this list of
      conditions and the following disclaimer.

   2. Redistributions in binary form must reproduce the above copyright notice, this list
      of conditions and the following disclaimer in the documentation and/or other materials
      provided with the distribution.

THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR IMPLIED
WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE AUTHOR OR
CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.