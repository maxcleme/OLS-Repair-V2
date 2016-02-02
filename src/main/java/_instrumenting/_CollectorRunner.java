package _instrumenting;

import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.InitializationError;

public class _CollectorRunner extends BlockJUnit4ClassRunner {

  public _CollectorRunner(Class<?> klass) throws InitializationError {
    super(klass);
  }

  @Override
  public void run(RunNotifier notifier) {
    notifier.addListener(new _CollectorRunNotifier());
    super.run(notifier);
  }

}
