package _instrumenting;

import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.InitializationError;

/**
 * @author Maxime CLEMENT
 *
 * Start with _, will be injeted inside project.
 * Custom JUnit runner. Use to serialize oracle structure at the end of test.
 */
public class _CollectorRunner extends BlockJUnit4ClassRunner {

  /**
   * @param klass
   * @throws InitializationError
   */
  public _CollectorRunner(Class<?> klass) throws InitializationError {
    super(klass);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.junit.runners.ParentRunner#run(org.junit.runner.notification.RunNotifier)
   */
  @Override
  public void run(RunNotifier notifier) {
    notifier.addListener(new _CollectorRunNotifier());
    super.run(notifier);
  }

}
