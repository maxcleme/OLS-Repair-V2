package _instrumenting;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class _Collector {

  private static final Map<String, List<Object>> oracle = new HashMap<String, List<Object>>();

  public static void addOutput(String method, Object output) {
    System.out.println(method + " : " + output);
    if (!oracle.containsKey(method)) {
      oracle.put(method, new ArrayList<>());
    }
    oracle.get(method).add(output);
  }

  public static Map<String, List<Object>> getAllCollectedValue() {
    return oracle;
  }

}
