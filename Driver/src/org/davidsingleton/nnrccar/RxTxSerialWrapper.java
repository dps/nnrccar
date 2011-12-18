package org.davidsingleton.nnrccar;

import gnu.io.CommPortIdentifier;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;
import gnu.io.UnsupportedCommOperationException;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Enumeration;
import java.util.TooManyListenersException;

/**
 * An implementation of the SerialWrapper interface which uses the RXTX Serial library.
 */
public class RxTxSerialWrapper implements SerialWrapper, SerialPortEventListener {

  private SerialPort port;
  private PrintStream ps;

  RxTxSerialWrapper(String portName) {
    System.setProperty("gnu.io.rxtx.SerialPorts", portName);
    System.out.println("RxTxSerialWrapper init " + portName);
    Enumeration<CommPortIdentifier> ports = CommPortIdentifier.getPortIdentifiers();
    while (ports.hasMoreElements()) {
      CommPortIdentifier id = ports.nextElement();
      System.out.println(id.getName());
      if (id.getName().equals(portName)) {
        try {
          port = (SerialPort) id.open("Driver", 1000);
          OutputStream os = port.getOutputStream();
          
          port.setSerialPortParams(9600, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
          port.notifyOnOutputEmpty(true);
          port.addEventListener(this);
          
          ps = new PrintStream(os);
          
        } catch (PortInUseException e) {
          e.printStackTrace();
        } catch (IOException e) {
          e.printStackTrace();
        } catch (UnsupportedCommOperationException e) {
          e.printStackTrace();
        } catch (TooManyListenersException e) {
          e.printStackTrace();
        }
      }
    }
  }

  public void print(char c) {
  	if (ps != null) {
      ps.print(c);
  	}
  }

  public void close() {
    ps.flush();
    ps.close();
    port.close();
  }

  @Override
  public void serialEvent(SerialPortEvent event) {
    if (event.getEventType() == SerialPortEvent.OUTPUT_BUFFER_EMPTY) {
      System.out.println("Serial write complete");
    }
  }
  
}
