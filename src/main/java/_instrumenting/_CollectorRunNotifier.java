package _instrumenting;

import java.io.FileOutputStream;
import java.io.ObjectOutputStream;

import org.junit.runner.Result;
import org.junit.runner.notification.RunListener;

public class _CollectorRunNotifier extends RunListener {

  @Override
  public void testRunFinished(Result result) throws Exception {
    super.testRunFinished(result);

    FileOutputStream fout = new FileOutputStream("collect");
    ObjectOutputStream oos = new ObjectOutputStream(fout);
    oos.writeObject(_Collector.getAllCollectedValue());
    oos.close();
  }

}
