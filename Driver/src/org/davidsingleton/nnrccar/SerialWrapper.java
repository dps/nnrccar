package org.davidsingleton.nnrccar;

/**
 * Interface for a serial connection (allowing multiple different implementations).
 */
public interface SerialWrapper {

  public void print(char c);
  
  public void close();
  
}