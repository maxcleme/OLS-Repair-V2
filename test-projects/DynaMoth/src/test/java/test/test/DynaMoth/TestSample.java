package test.test.DynaMoth;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TestSample {
  private Sample sample;

  @Before
  public void setUp() {
    this.sample = new Sample();
  }

  /**
  * @link test.test.DynaMoth.Sample#isEven(int)
  */
  @Test
  public void testIsEven() throws Exception {
    Assert.assertEquals(false, this.sample.isEven(3));
    Assert.assertEquals(true, this.sample.isEven(10));
    Assert.assertEquals(false, this.sample.isEven(151));
    Assert.assertEquals(true, this.sample.isEven(-1147586));
  }

  /**
  * @link test.test.DynaMoth.Sample#isOdd(int)
  */
  @Test
  public void testIsOdd() throws Exception {
    Assert.assertEquals(true, this.sample.isOdd(3));
    Assert.assertEquals(false, this.sample.isOdd(10));
    Assert.assertEquals(true, this.sample.isOdd(151));
    Assert.assertEquals(false, this.sample.isOdd(-1147586));
  }

  /**
  * @link test.test.DynaMoth.Sample#add(int, int)
  */
  @Test
  public void testAdd() throws Exception {
    Assert.assertEquals(3, this.sample.add(2, 1));
    Assert.assertEquals(4, this.sample.add(2, 2));
    Assert.assertEquals(8, this.sample.add(4, 4));
    Assert.assertEquals(0, this.sample.add(0, 0));
    Assert.assertEquals(20, this.sample.add(10, 10));
    Assert.assertEquals(25, this.sample.add(12, 13));
  }

  /**
  * @link test.test.DynaMoth.Sample#minus(int, int)
  */
  @Test
  public void testMinus() throws Exception {
    Assert.assertEquals(1, this.sample.minus(2, 1));
    Assert.assertEquals(0, this.sample.minus(2, 2));
    Assert.assertEquals(0, this.sample.minus(4, 4));
    Assert.assertEquals(0, this.sample.minus(0, 0));
    Assert.assertEquals(0, this.sample.minus(10, 10));
    Assert.assertEquals(-1, this.sample.minus(12, 13));
  }

  /**
  * @link test.test.DynaMoth.Sample#times(int, int)
  */
  @Test
  public void testTimes() throws Exception {
    Assert.assertEquals(2, this.sample.times(2, 1));
    Assert.assertEquals(4, this.sample.times(2, 2));
    Assert.assertEquals(-16, this.sample.times(-4, 4));
    Assert.assertEquals(0, this.sample.times(0, 0));
    Assert.assertEquals(-100, this.sample.times(10, -10));
    Assert.assertEquals(156, this.sample.times(12, 13));
  }

  /**
  * @link test.test.DynaMoth.Sample#square(int)
  */
  @Test
  public void testSquare() throws Exception {
    Assert.assertEquals(4, this.sample.square(2));
    Assert.assertEquals(4, this.sample.square(-2));
    Assert.assertEquals(16, this.sample.square(-4));
    Assert.assertEquals(0, this.sample.square(0));
    Assert.assertEquals(100, this.sample.square(10));
    Assert.assertEquals(144, this.sample.square(12));
  }

}
