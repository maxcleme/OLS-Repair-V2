package _instrumenting;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Maxime CLEMENT
 * 
 * Start with _, will be injeted inside project.
 * This call contains all the output values collected during test execution.
 *  
 */
public class _Collector {

  /**
   * Structure containing all output value for each test executing a method to be synthesize
   */
  private static final Map<String, Map<String, List<Object>>> oracle = new HashMap<String, Map<String, List<Object>>>();

  /**
   * Add output value to the oracle structure
   * 
   * @param synthMethod method call inside test
   * @param testMethod test method
   * @param output output value
   */
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

  /**
   * Get the oracle structure
   * 
   * @return the oracle structure
   */
  public static Map<String, Map<String, List<Object>>> getAllCollectedValue() {
    return oracle;
  }

}
