NNRCCar - build a Neural Network Controlled self-driving car!
=============================================================

```android/ - An android app which streams video frames to the Driver app running on a PC.
arduino/ - The arduino sketch allowing Driver to send commands to the car via Serial interface.
Driver/  - Java applet which acts as both a TCP server, receiving streamed image frames from a video streaming app on a phone
      and a user interface allowing a human driver to control the car with the cursor keys or mouse
train/   - Octave code to train the neural network.
```
BUILD / QUICKSTART
------------------

To build and install the android app (using your default Android SDK and device):

```cd android/
ant install
```

Flash the Arduino sketch to your arduino by opening the serialrccar sketch in the Arduino IDE and clicking Upload.  Note the USB serial port your Arduino board is connected to (available in Tools -> Serial Port) as you will need to know it later.

Driver depends on the RxTx Serial library and Apache Commons Math library - see the README in Driver/ for more details.  Once the dependencies are provided:

Build the Driver app

```cd Driver/
ant
```

Run the Driver app

```ant Driver [serial port path]
```

* [serial port path] is the path to the serial port connected to your Arduino board - it's the same
  as the one used by the Arduino IDE so the value noted earlier is what you need.

Run the Android app (with phone and computer on the same wifi network).  You'll be prompted to enter the IP address of the computer running the Driver app, once connected, you should start to see incoming video frames from the phone.  You will also be able to note the location of the training data being written in  record mode on standard out e.g.:

```Driver:
     [java] Features writing to: /tmp/nnrccar4119787618703988036features
     [java] RxTxSerialWrapper init /dev/tty.usbmodemfa131
```

Driver starts in Manual mode - confirm that you can drive the car using the arrow keys or mouse (Note that 'F' toggles forward, up arrow produces a momentary forward pulse).

Once you've gotten used to driving the car, you're ready to record examples for the neural network to learn from.  Press 'R' to enter record mode and drive the car in the same environment you'd like it to drive itself.  You can return to manual mode by pressing 'M' and re-enter record at any time by pressing 'R' again.  When done, press 'M' and wait for the queue of samples to be written to disk.  You're now ready to train the network.

Training:

Pre-process the recorded data into octave matrices:
* change into the train/ directory and copy the temporary features file above to nnrccar.features
* run the parse.py script which reads the features and writes two files X.dat and y.dat.  X.dat is an octave matrix containing the input frames and y.dat is a matrix containing the labels corresponding to how you drove the car.

Train:

Follow the instructions in train/README to get set up with a solution to Ex4, then Run octave and start the nnrccar script:
```octave-3.4.0:4> cd train/
octave-3.4.0:5> nnrccar

Elapsed time is 233.526 seconds.
Training Neural Network... 
```

It will take ~4 mins for octave to load the training data, after which you'll see the status output from fmincfg showing the progress of training the network.  By default, 100 iterations of training will occur.

Once trained, the script will write out the parameters for the two sets of connections in Theta1.dat and Theta2.dat.

Copy these files to the Driver/data directory - note that they need to be renamed theta1.dat and theta2.dat [different capitalization].

Drive!

Run Driver again, and press 'A' to enter auto mode.  Hey presto - you have a self driving car!
