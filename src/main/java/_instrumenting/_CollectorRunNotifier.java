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

public class _CollectorRunNotifier extends RunListener {

  @Override
  public void testRunFinished(Result result) throws Exception {
    super.testRunFinished(result);

    List<String> failingTest = new ArrayList<>();
    List<String> noNeedToBeSynth = new ArrayList<>();

    for (Failure faillure : result.getFailures()) {
      failingTest.add(faillure.getDescription().getClassName() + "#" + faillure.getDescription().getMethodName());
    }

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

    FileOutputStream fout = new FileOutputStream("collect");
    ObjectOutputStream oos = new ObjectOutputStream(fout);
    oos.writeObject(_Collector.getAllCollectedValue());
    oos.close();
  }

}
