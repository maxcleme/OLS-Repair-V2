package fil.iagl.opl.model;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import fil.iagl.opl.OLS_Repair;
import fr.inria.lille.repair.nopol.SourceLocation;
import fr.inria.lille.spirals.repair.commons.Candidates;
import fr.inria.lille.spirals.repair.synthesizer.Synthesizer;
import fr.inria.lille.spirals.repair.synthesizer.SynthesizerImpl;
import spoon.processing.AbstractProcessor;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.visitor.filter.TypeFilter;
import xxl.java.library.JavaLibrary;

public class ConstructModel extends AbstractProcessor<CtMethod<?>> {

  @Override
  public boolean isToBeProcessed(CtMethod<?> candidate) {
    return candidate.getAnnotation(org.junit.Test.class) != null && candidate.getParent(CtClass.class) != null;
  }

  @Override
  public void process(CtMethod<?> method) {
    Pattern p = Pattern.compile("@link((.*)#(.*)\\((.*?)\\))");
    Matcher m = p.matcher(method.getDocComment().trim().replaceAll(" ", ""));
    if (m.matches()) {
      String testedMethodSignature = m.group(1);
      Model.addTestedMethod(testedMethodSignature);
      // testedMethodsSignatures.putIfAbsent(testedMethodSignature, new HashMap<>());
      Model.addTestMethod(testedMethodSignature, method);
      // testedMethodsSignatures.get(testedMethodSignature).put(method, new ArrayList<>());

      List<CtInvocation<?>> invocations = method.getElements(new TypeFilter<>(CtInvocation.class));
      for (CtInvocation<?> invocation : invocations) {
        if (invocation.getTarget().getType().getQualifiedName().equals("org.junit.Assert")) {
          // TODO : Dynamic collect for expected value
          Model.addOutput(testedMethodSignature, method, Integer.parseInt(invocation.getArguments().get(0).toString()));
          // testedMethodsSignatures.get(testedMethodSignature).get(method).add(invocation.getArguments().get(0));
        }
      }
    } else {
      System.out.println("Missing @link");
    }

    // List<CtInvocation<?>> invocations = method.getElements(new TypeFilter<>(CtInvocation.class));
    // for (CtInvocation<?> invocation : invocations) {
    // if (invocation.getTarget().getType().getQualifiedName().equals("org.junit.Assert")) {
    // int nbArgs = invocation.getArguments().size();
    // CtInvocation<?> position = FinderFactory.getPositionFinder(invocation.getArguments().get(nbArgs -
    // 1)).getFrom(invocation.getArguments().get(nbArgs - 1));
    // if (position == null) {
    // System.out.println("Cannot find faulty position from this assert");
    // System.exit(0);
    // } else {
    // Model.addFailingMethods(position.getExecutable().getDeclaration());
    // if (Model.getFailingMethods().size() > 1) {
    // System.out.println("Cannot synthesis patch for multiple position");
    // System.exit(0);
    // }
    // Map<String, Object> testCollect = new HashMap<>();
    // List<CtExpression<?>> args = position.getArguments();
    // for (int i = 0; i < args.size(); i++) {
    // if (args.get(i) instanceof CtVariableAccess) {
    // CtVariableAccess<?> access = (CtVariableAccess<?>) args.get(i);
    // if (access.getVariable().getType().equals(getFactory().Type().STRING)) {
    // CtExpression<?> expr = access.getVariable().getDeclaration().getDefaultExpression();
    // int j = 0;
    // for (char c : expr.toString().replace("\"", "").toCharArray()) {
    // testCollect.put("((int)(" + position.getExecutable().getDeclaration().getParameters().get(i).getSimpleName() + ".charAt(" + j +
    // ")))", (int) c);
    // j++;
    // }
    // } else if (access.getVariable().getType().equals(getFactory().Type().createArrayReference(getFactory().Type().INTEGER_PRIMITIVE))) {
    // CtExpression<?> expr = access.getVariable().getDeclaration().getDefaultExpression();
    // int j = 0;
    // for (String s : expr.toString().replace("new int[]", "").replace("{", "").replace("}", "").split(",")) {
    // testCollect.put(position.getExecutable().getDeclaration().getParameters().get(i).getSimpleName() + "[" + j + "]",
    // Integer.parseInt(s.trim()));
    // j++;
    // }
    // } else {
    // System.out.println("Cannot handle defined type.");
    // System.exit(0);
    // }
    // }
    // }
    // if (invocation.getArguments().get(nbArgs - 2) instanceof CtVariableAccess) {
    // CtVariableAccess<?> expect = (CtVariableAccess<?>) invocation.getArguments().get(nbArgs - 2);
    // if (expect.getVariable().getType().equals(getFactory().Type().CHARACTER_PRIMITIVE)) {
    // Model
    // .addSpecification(
    // new Specification<Integer>(testCollect, (int) expect.getVariable().getDeclaration().getDefaultExpression().toString().replace("'",
    // "").charAt(0)));
    // } else if (expect.getVariable().getType().equals(getFactory().Type().INTEGER_PRIMITIVE)) {
    // Model
    // .addSpecification(
    // new Specification<Integer>(testCollect, Integer.parseInt(expect.getVariable().getDeclaration().getDefaultExpression().toString())));
    // } else {
    // System.out.println("Cannot handle defined type.");
    // System.exit(0);
    // }
    // }
    // }
    // }
    // }
  }

  @Override
  public void processingDone() {

    for (String methodToBeSynth : Model.getInstance().getModel().keySet()) {
      System.out.println("Trying to synth : " + methodToBeSynth);

      List<String> tests = new ArrayList<String>();
      Map<String, Object[]> oracle = new HashMap<String, Object[]>();

      for (Entry<CtMethod<?>, List<Object>> entry : Model.getInstance().getModel().get(methodToBeSynth).entrySet()) {
        tests.add(entry.getKey().getParent(CtClass.class).getQualifiedName() + "#" + entry.getKey().getSimpleName());
        oracle.put(entry.getKey().getParent(CtClass.class).getQualifiedName() + "#" + entry.getKey().getSimpleName(),
          entry.getValue().toArray(new Object[entry.getValue().size()]));
      }

      System.out.println(tests);
      System.out.println(oracle);

      Pattern p = Pattern.compile("(.*)#(.*)\\((.*?)\\)");
      Matcher m = p.matcher(methodToBeSynth);
      if (m.matches()) {
        CtClass<?> clazz = getFactory().Class().get(m.group(1));

        // TODO: Create array of collected output ( dynamicly collected )
        CtMethod<?> synthMethod = clazz.getMethod(m.group(2), getFactory().Type().INTEGER_PRIMITIVE, getFactory().Type().INTEGER_PRIMITIVE);

        SourceLocation location = new SourceLocation(m.group(1), synthMethod.getBody().getLastStatement().getPosition().getLine());

        File sourceDir = new File(OLS_Repair.PROJECT_PATH + "/src/main/java");
        File[] files = {sourceDir};

        // TODO: Dynamicly find classpath
        // TODO: mvn test -DskipTests on instrumented project to be sure /target is fill
        String classpath = OLS_Repair.JUNIT_JAR_PATH + File.pathSeparatorChar
          + "C:/Users/RMS/Documents/workspace-sts-3.7.0.RELEASE/DynaMoth/target/classes;C:/Users/RMS/Documents/workspace-sts-3.7.0.RELEASE/DynaMoth/target/test-classes";

        Synthesizer synthesizer = new SynthesizerImpl(files, location, JavaLibrary.classpathFrom(classpath), oracle, tests.toArray(new String[tests.size()]), 5);
        Candidates expression = synthesizer.run(TimeUnit.MINUTES.toMillis(15));
        System.out.println(expression);
      }
    }

    // Map<String, Integer> intConstants = new HashMap<>();
    // for (int constant : OLS_Repair.CONSTANTS_ARRAY) {
    // intConstants.put("" + constant, constant);
    // }
    //
    // ConstraintBasedSynthesis synthesis = new ConstraintBasedSynthesis(intConstants);
    // CodeGenesis genesis = synthesis.codesSynthesisedFrom(
    // (Integer.class), Model.getSpecs());
    // OLS_Repair.patch = genesis;

  }

}
