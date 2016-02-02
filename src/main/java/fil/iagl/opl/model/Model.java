package fil.iagl.opl.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import spoon.reflect.declaration.CtMethod;

public class Model {

  private static final Model model = new Model();

  private Map<String, Map<CtMethod<?>, List<Object>>> testedMethodsSignatures;

  private Model() {
    testedMethodsSignatures = new HashMap<>();
  }

  public static Model getInstance() {
    return model;
  }

  public Map<String, Map<CtMethod<?>, List<Object>>> getModel() {
    return model.testedMethodsSignatures;
  }

  public static void addTestedMethod(String signature) {
    model.testedMethodsSignatures.putIfAbsent(signature, new HashMap<>());
  }

  public static void addTestMethod(String testedMethodSignature, CtMethod<?> testMethod) {
    model.testedMethodsSignatures.get(testedMethodSignature).putIfAbsent(testMethod, new ArrayList<>());
  }

  public static void addOutput(String testedMethodSignature, CtMethod<?> testMethod, Object output) {
    model.testedMethodsSignatures.get(testedMethodSignature).get(testMethod).add(output);
  }

}
