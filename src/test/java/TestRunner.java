import org.junit.Test;
import org.junit.runner.RunWith;

import _instrumenting._CollectorRunner;
import junit.framework.Assert;

@RunWith(value = _CollectorRunner.class)
public class TestRunner {

  @Test
  public void testName() throws Exception {
    Assert.assertTrue(1 == 2);
  }

}
