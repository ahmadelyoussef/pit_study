package pitexample;

import org.junit.Test;
import static org.junit.Assert.*;

public class MyClassTest {

  @Test
  public void testMe1() {
    //Yes, this should be multiple test cases...
    MyClass sut = new MyClass();
    assertTrue(sut.myMethod(1, true));
    assertTrue(sut.myMethod(2, true));
    assertTrue(sut.myMethod(1, false));
    //assertTrue(sut.myMethod(2, false));
    //assertFalse(sut.myMethod(0, false));
    //assertTrue(sut.myMethod(0, true));
  }

  @Test
  public void testMe2() {
    MyClass sut = new MyClass();
    assertTrue(sut.myMethod(2, false));
    assertFalse(sut.myMethod(0, false));
    assertTrue(sut.myMethod(0, true));
  }
}
