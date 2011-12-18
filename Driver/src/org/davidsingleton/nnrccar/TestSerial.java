/**
 * 
 */
package org.davidsingleton.nnrccar;

import gnu.io.CommPortIdentifier;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.UnsupportedCommOperationException;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.TooManyListenersException;

public class TestSerial {

  public static void main(String[] args) {
		String portName = "/dev/tty.usbmodemfd131";
		
		Enumeration ports = CommPortIdentifier.getPortIdentifiers();
		while (ports.hasMoreElements()) {
			CommPortIdentifier id = (CommPortIdentifier) ports.nextElement();
			System.out.println("Port: " + id.getName());
			if (id.getName().equals(portName)) {
				System.out.println("Found port");
				try {
					SerialPort port = (SerialPort) id.open("Driver", 1000);
					
					
					port.setSerialPortParams(9600, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
					port.addEventListener(new TestSerialEventListener());
					port.notifyOnOutputEmpty(true);
					port.notifyOnDataAvailable(true);
          OutputStream os = port.getOutputStream();
          Thread.sleep(1500);
          os.write('p');
					//PrintStream ps = new PrintStream(os);
					//ps.print('q');
					//ps.flush();
					os.flush();
					Thread.sleep(10000);
					//ps.close();
					os.close();
					port.close();
				} catch (PortInUseException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				} catch (UnsupportedCommOperationException e) {
					e.printStackTrace();
				} catch (InterruptedException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        } catch (TooManyListenersException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        } 
			}
		}
	}
}
