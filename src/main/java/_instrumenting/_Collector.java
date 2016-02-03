package _instrumenting;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class _Collector {

  private static final Map<String, Map<String, List<Object>>> oracle = new HashMap<String, Map<String, List<Object>>>();

  public static void addOutput(String synthMethod, String testMethod, Object output) {
    System.out.println(testMethod + " : " + output);
    if (!oracle.containsKey(synthMethod)) {
      oracle.put(synthMethod, new HashMap<String, List<Object>>());
    }
    if (!oracle.get(synthMethod).containsKey(testMethod)) {
      oracle.get(synthMethod).put(testMethod, new ArrayList<Object>());
    }
    oracle.get(synthMethod).get(testMethod).add(output);
  }

  public static Map<String, Map<String, List<Object>>> getAllCollectedValue() {
    return oracle;
  }

}
