package br.unifor.kubow;

/**
 * @author Carlos Mendes (cmendesce@gmail.com)
 */
public class KubowException extends RuntimeException {

  public KubowException(String msg) {
    super(msg);
  }

  public KubowException(String msg, Throwable e) {
    super(msg, e);
  }
}
