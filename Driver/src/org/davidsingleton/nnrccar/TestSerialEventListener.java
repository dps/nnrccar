/**
 * 
 */
package org.davidsingleton.nnrccar;

import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;

class TestSerialEventListener implements SerialPortEventListener {
  TestSerialEventListener() {
    
  }

  @Override
  public void serialEvent(SerialPortEvent e) {
    System.out.println(e  + " " + e.getEventType());
    
  }
}