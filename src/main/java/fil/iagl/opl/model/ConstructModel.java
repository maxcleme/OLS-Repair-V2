package fil.iagl.opl.model;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
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
import fr.inria.lille.spirals.repair.synthesizer.Synthesizer;
import fr.inria.lille.spirals.repair.synthesizer.SynthesizerImpl;
import spoon.processing.AbstractProcessor;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.reference.CtTypeReference;
import xxl.java.library.JavaLibrary;

public class ConstructModel extends AbstractProcessor<CtMethod<?>> {

  private Map<String, Map<String, List<Object>>> collectedValues;

  @Override
  public void init() {
    super.init();
    try {
      FileInputStream fin = new FileInputStream("spooned/collect");
      ObjectInputStream ois = new ObjectInputStream(fin);
      this.collectedValues = (Map<String, Map<String, List<Object>>>) ois.readObject();
      ois.close();
    } catch (IOException | ClassNotFoundException e) {
      throw new RuntimeException("Error occured when reading collected value.", e);
    }
  }

  @Override
  public void process(CtMethod<?> method) {

  }

  @Override
  public void processingDone() {
    for (String methodToBeSynth : collectedValues.keySet()) {

      System.out.println("Trying to synth : " + methodToBeSynth);

      Map<String, Object[]> oracle = new HashMap<String, Object[]>();

      for (Entry<String, List<Object>> entry : this.collectedValues.get(methodToBeSynth).entrySet()) {
        oracle.put(entry.getKey(), entry.getValue().toArray(new Object[entry.getValue().size()]));
      }

      oracle.entrySet().forEach(entry -> {
        System.out.println(entry.getKey());
        System.out.println("\t" + Arrays.toString(entry.getValue()));
      });

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

        String classpath;
        try {
          classpath = Utils.getDynamicClasspath(OLS_Repair.PROJECT_PATH, OLS_Repair.MAVEN_HOME_PATH);
          classpath += File.pathSeparatorChar + OLS_Repair.PROJECT_PATH + File.separatorChar + "target" + File.separatorChar + "classes";
          classpath += File.pathSeparatorChar + OLS_Repair.PROJECT_PATH + File.separatorChar + "target" + File.separatorChar + "test-classes";
        } catch (MavenInvocationException e) {
          throw new RuntimeException("Error occured during classpath evaluation.", e);
        }

        try {
          // mvn test -DskipTests on instrumented project to be sure /target is fill ( mvn compile does not compile tests )
          Utils.runMavenGoal(OLS_Repair.PROJECT_PATH, OLS_Repair.MAVEN_HOME_PATH, Arrays.asList("test", "-DskipTests"), Optional.empty());
        } catch (MavenInvocationException e) {
          throw new RuntimeException("Error occured during compiling.", e);
        }

        Synthesizer synthesizer = new SynthesizerImpl(files, location, JavaLibrary.classpathFrom(classpath), oracle, oracle.keySet().toArray(new String[oracle.keySet().size()]),
          30);
        synthesizer.run(TimeUnit.MINUTES.toMillis(180));

        synthMethod.getBody().getLastStatement().replace(getFactory().Code().createCodeSnippetStatement("return " + synthesizer.getValidExpressions().get(0).asPatch()));
      }
    }
  }

}
