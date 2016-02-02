package _instrumenting;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class _Collector {

  Map<String, List<Object>> oracle = new HashMap<String, List<Object>>();

  public void addOutput(String method, Object output) {
    if (!oracle.containsKey(method)) {
      oracle.put(method, new ArrayList<>());
    }
    oracle.get(method).add(output);
  }

}
