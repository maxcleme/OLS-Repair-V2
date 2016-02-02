package _instrumenting;

import org.junit.runner.Result;
import org.junit.runner.notification.RunListener;

public class _CollectorRunNotifier extends RunListener {

  @Override
  public void testRunFinished(Result result) throws Exception {
    super.testRunFinished(result);
    System.out.println("YOLOOO");
  }

}
