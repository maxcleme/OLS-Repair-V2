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

import org.apache.maven.shared.invoker.MavenInvocationException;

import fil.iagl.opl.OLS_Repair;
import fil.iagl.opl.repair.NoSynthFoundException;
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

  @Override
  public void process(CtMethod<?> method) {

  }

  @Override
  public void processingDone() {

    System.out.println("Trying to synth : " + OLS_Repair.currentMethod);

    Map<String, Object[]> oracle = new HashMap<String, Object[]>();

    for (Entry<String, List<Object>> entry : OLS_Repair.collectedValues.get(OLS_Repair.currentMethod).entrySet()) {
      oracle.put(entry.getKey(), entry.getValue().toArray(new Object[entry.getValue().size()]));
    }

    // oracle.entrySet().forEach(entry -> {
    // System.out.println(entry.getKey());
    // System.out.println("\t" + Arrays.toString(entry.getValue()));
    // });

    Pattern p = Pattern.compile("(.*)#(.*)\\((.*?)\\)");
    Matcher m = p.matcher(OLS_Repair.currentMethod);
    m.matches();

    CtClass<?> clazz = getFactory().Class().get(m.group(1));
    List<CtTypeReference<?>> types = new ArrayList<CtTypeReference<?>>();
    for (String typeFromDocComment : m.group(3).split(",")) {
      types.add(getFactory().Type().createReference(typeFromDocComment));
    }
    CtMethod<?> synthMethod = clazz.getMethod(m.group(2), types.toArray(new CtTypeReference<?>[types.size()]));

    SourceLocation location = new SourceLocation(m.group(1), synthMethod.getBody().getLastStatement().getPosition().getLine());

    File sourceDir = new File(OLS_Repair.PROJECT_PATH + "/src/main/java");
    File[] files = {sourceDir};

    String classpath;
    try {
      classpath = Utils.getDynamicClasspath(OLS_Repair.PROJECT_PATH, OLS_Repair.MAVEN_HOME_PATH);
      classpath += File.pathSeparatorChar + OLS_Repair.PROJECT_PATH + File.separatorChar + "target" + File.separatorChar + "classes";
      classpath += File.pathSeparatorChar + new File("spooned").getAbsolutePath() + File.separatorChar + "target" + File.separatorChar + "test-classes";
    } catch (MavenInvocationException e) {
      throw new RuntimeException("Error occured during classpath evaluation.", e);
    }

    // TODO: Synth in another method in order to prettyPrint code after each synth
    Synthesizer synthesizer = new SynthesizerImpl(files, location, JavaLibrary.classpathFrom(classpath), oracle, oracle.keySet().toArray(new String[oracle.keySet().size()]),
      5);
    synthesizer.run(TimeUnit.MINUTES.toMillis(15));

    if (synthesizer.getValidExpressions().isEmpty()) {
      throw new NoSynthFoundException();
    }
    synthMethod.getBody().getLastStatement().replace(getFactory().Code().createCodeSnippetStatement("return " + synthesizer.getValidExpressions().get(0).asPatch()));
  }

}
