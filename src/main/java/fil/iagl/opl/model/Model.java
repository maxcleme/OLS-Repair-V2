package fil.iagl.opl.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Model {

  private static final Model model = new Model();

  private Map<String, Map<String, List<Object>>> testedMethodsSignatures;

  private Model() {
    testedMethodsSignatures = new HashMap<>();
  }

  public static Model getInstance() {
    return model;
  }

  public Map<String, Map<String, List<Object>>> getModel() {
    return model.testedMethodsSignatures;
  }

  public static void addTestedMethod(String signature) {
    model.testedMethodsSignatures.putIfAbsent(signature, new HashMap<>());
  }

  public static void addTestMethod(String testedMethodSignature, String testMethod) {
    model.testedMethodsSignatures.get(testedMethodSignature).putIfAbsent(testMethod, new ArrayList<>());
  }

  public static void addOutput(String testedMethodSignature, String testMethod, List<Object> outputs) {
    addTestMethod(testedMethodSignature, testMethod);
    model.testedMethodsSignatures.get(testedMethodSignature).get(testMethod).addAll(outputs);
  }

}
