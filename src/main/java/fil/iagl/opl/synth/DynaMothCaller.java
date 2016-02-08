package fil.iagl.opl.synth;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import fil.iagl.opl.Constantes;
import fr.inria.lille.repair.nopol.SourceLocation;
import fr.inria.lille.spirals.repair.synthesizer.Synthesizer;
import fr.inria.lille.spirals.repair.synthesizer.SynthesizerImpl;
import spoon.Launcher;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.reference.CtTypeReference;

public class DynaMothCaller {

  private static final Logger logger = Logger.getRootLogger();

  private Launcher l;
  private String sourcePath;

  public DynaMothCaller(String inputPath, String outputPath) {
    this.l = new Launcher();
    this.l.addInputResource(inputPath);
    this.l.setSourceOutputDirectory(outputPath);
    this.l.buildModel();
    this.sourcePath = inputPath;
  }

  public void call(String currentMethod) {
    logger.debug("Trying to synth : " + currentMethod);

    // Declare and fill the oracle structure, only for the current method
    Map<String, Object[]> oracle = new HashMap<String, Object[]>();
    for (Entry<String, List<Object>> entry : Constantes.getCollectedValues().get(currentMethod).entrySet()) {
      oracle.put(entry.getKey(), entry.getValue().toArray(new Object[entry.getValue().size()]));
    }

    // Apply matcher
    Matcher m = Pattern.compile(Constantes.getPattern()).matcher(currentMethod);
    m.matches();

    // Get the currentMethod as CtMethod
    CtClass<?> clazz = l.getFactory().Class().get(m.group(1));
    List<CtTypeReference<?>> types = new ArrayList<CtTypeReference<?>>();
    for (String typeFromDocComment : m.group(3).split(",")) {
      types.add(l.getFactory().Type().createReference(typeFromDocComment));
    }
    CtMethod<?> synthMethod = clazz.getMethod(m.group(2), types.toArray(new CtTypeReference<?>[types.size()]));

    // Last statement of the method to be synth
    SourceLocation location = new SourceLocation(m.group(1), synthMethod.getBody().getLastStatement().getPosition().getLine());

    // Sources directory
    File sourceDir = new File(sourcePath);
    File[] files = {sourceDir};

    // DynaMoth constructor
    Synthesizer synthesizer = new SynthesizerImpl(
      files,
      location,
      Constantes.getClasspath(),
      oracle,
      oracle.keySet().toArray(new String[oracle.keySet().size()]),
      Constantes.getTimeOutCollection(),
      Constantes.getConstantsArray());

    // Call DynaMoth
    synthesizer.run(TimeUnit.MINUTES.toMillis(Constantes.getTimeOutDynaMoth()));

    // Try if location has been synthesize
    if (synthesizer.getValidExpressions().isEmpty()) {
      throw new NoSynthFoundException();
    }
    // Apply the patch
    synthMethod.getBody().getLastStatement().replace(l.getFactory().Code().createCodeSnippetStatement("return " + synthesizer.getValidExpressions().get(0).asPatch()));
    l.prettyprint();
  }

}
