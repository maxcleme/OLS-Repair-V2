package fil.iagl.opl.model;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.maven.shared.invoker.MavenInvocationException;

import fil.iagl.opl.OLS_Repair;
import fil.iagl.opl.utils.Utils;
import fr.inria.lille.repair.nopol.SourceLocation;
import fr.inria.lille.spirals.repair.commons.Candidates;
import fr.inria.lille.spirals.repair.synthesizer.Synthesizer;
import fr.inria.lille.spirals.repair.synthesizer.SynthesizerImpl;
import spoon.processing.AbstractProcessor;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.reference.CtTypeReference;
import xxl.java.library.JavaLibrary;

public class ConstructModel extends AbstractProcessor<CtMethod<?>> {

  private Map<String, List<Object>> oracle;

  @Override
  public void init() {
    super.init();
    try {
      FileInputStream fin = new FileInputStream("spooned/collect");
      ObjectInputStream ois = new ObjectInputStream(fin);
      this.oracle = (Map<String, List<Object>>) ois.readObject();
      ois.close();
    } catch (IOException | ClassNotFoundException e) {
      throw new RuntimeException("Error occured when reading collected value.", e);
    }
  }

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
      String testMethodName = Utils.getFormalName(method);
      Model.addTestedMethod(testedMethodSignature);
      Model.addTestMethod(testedMethodSignature, testMethodName);
      Model.addOutput(testedMethodSignature, testMethodName, oracle.get(testMethodName));
    } else {
      System.out.println("Missing @link");
    }
  }

  @Override
  public void processingDone() {
    for (String methodToBeSynth : Model.getInstance().getModel().keySet()) {

      System.out.println("Trying to synth : " + methodToBeSynth);

      List<String> tests = new ArrayList<String>();
      Map<String, Object[]> oracle = new HashMap<String, Object[]>();

      for (Entry<String, List<Object>> entry : Model.getInstance().getModel().get(methodToBeSynth).entrySet()) {
        tests.add(entry.getKey());
        oracle.put(entry.getKey(), entry.getValue().toArray(new Object[entry.getValue().size()]));
      }

      System.out.println(tests);
      System.out.println(oracle);
      Pattern p = Pattern.compile("(.*)#(.*)\\((.*?)\\)");
      Matcher m = p.matcher(methodToBeSynth);
      if (m.matches()) {
        CtClass<?> clazz = getFactory().Class().get(m.group(1));
        List<CtTypeReference<?>> types = Arrays.stream(m.group(3).split(",")).map(typeFromDocComment -> getFactory().Type().createReference(typeFromDocComment))
          .collect(Collectors.toList());
        CtMethod<?> synthMethod = clazz.getMethod(m.group(2), types.toArray(new CtTypeReference<?>[types.size()]));

        SourceLocation location = new SourceLocation(m.group(1), synthMethod.getBody().getLastStatement().getPosition().getLine());

        File sourceDir = new File(OLS_Repair.PROJECT_PATH + "/src/main/java");
        File[] files = {sourceDir};

        try {
          String classpath = Utils.getDynamicClasspath(OLS_Repair.PROJECT_PATH, OLS_Repair.MAVEN_HOME_PATH);
          classpath += File.pathSeparatorChar + OLS_Repair.PROJECT_PATH + File.separatorChar + "target" + File.separatorChar + "classes";
          classpath += File.pathSeparatorChar + OLS_Repair.PROJECT_PATH + File.separatorChar + "target" + File.separatorChar + "test-classes";

          // mvn test -DskipTests on instrumented project to be sure /target is fill
          Utils.runMavenGoal(OLS_Repair.PROJECT_PATH, OLS_Repair.MAVEN_HOME_PATH, Arrays.asList("test", "-DskipTests"), Optional.empty());

          Synthesizer synthesizer = new SynthesizerImpl(files, location, JavaLibrary.classpathFrom(classpath), oracle, tests.toArray(new String[tests.size()]), 5);
          Candidates expression = synthesizer.run(TimeUnit.MINUTES.toMillis(15));
          System.out.println(expression);
        } catch (MavenInvocationException e) {
          throw new RuntimeException("Error occured during classpath evaluation.", e);
        }
      }
    }
  }

}
