package _instrumenting;

import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;

/**
 * @author Maxime CLEMENT
 *
 * Start with _, will be injeted inside project.
 * Serialize the oracle structure at the end of test.
 */
public class _CollectorRunNotifier extends RunListener {

  /*
   * (non-Javadoc)
   * 
   * @see org.junit.runner.notification.RunListener#testRunFinished(org.junit.runner.Result)
   */
  @Override
  public void testRunFinished(Result result) throws Exception {
    super.testRunFinished(result);

    List<String> failingTest = new ArrayList<>();
    List<String> noNeedToBeSynth = new ArrayList<>();

    // Add every test failure to 'failingTest'
    for (Failure faillure : result.getFailures()) {
      failingTest.add(faillure.getDescription().getClassName() + "#" + faillure.getDescription().getMethodName());
    }

    // If method inside oracle has no test failed, remove it.
    for (Entry<String, Map<String, List<Object>>> oracle : _Collector.getAllCollectedValue().entrySet()) {
      boolean allPass = true;
      for (Entry<String, List<Object>> test : oracle.getValue().entrySet()) {
        if (failingTest.contains(test.getKey())) {
          allPass = false;
        }
      }
      if (allPass) {
        noNeedToBeSynth.add(oracle.getKey());
      }
    }
    for (String synthMethod : noNeedToBeSynth) {
      _Collector.getAllCollectedValue().remove(synthMethod);
    }

    // Serialize oracle structure into file : collect.
    System.out.println("Serialize oracles...");
    FileOutputStream fout = new FileOutputStream("collect");
    ObjectOutputStream oos = new ObjectOutputStream(fout);
    oos.writeObject(_Collector.getAllCollectedValue());
    oos.close();
  }

}
