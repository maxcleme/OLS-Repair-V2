package fil.iagl.opl.synth;

import java.io.File;
import java.net.URL;
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
import fil.iagl.opl.utils.Params;
import fr.inria.lille.repair.common.config.Config;
import fr.inria.lille.repair.nopol.SourceLocation;
import fr.inria.lille.spirals.repair.synthesizer.Synthesizer;
import fr.inria.lille.spirals.repair.synthesizer.SynthesizerImpl;
import spoon.Launcher;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.reference.CtTypeReference;

/**
 * @author Maxime CLEMENT
 *
 * DynaMoth interface, get everything needed and call dynamoth. If patch found, it write it.
 */
public class DynaMothCaller {

  private static final Logger logger = Logger.getRootLogger();

  /**
   * Spoon launcher
   */
  private Launcher l;
  /**
   * Where are the sources
   */
  private String sourcePath;
  /**
   * User parameters
   */
  private Params params;

  /**
   * @param inputPath where the sources are
   * @param outputPath where patches will be written
   * @param params user parameters
   */
  public DynaMothCaller(String inputPath, String outputPath, Params params) {
    this.l = new Launcher();
    this.l.addInputResource(inputPath);
    this.l.setSourceOutputDirectory(outputPath);
    this.l.buildModel();
    this.sourcePath = inputPath;
    this.params = params;

    // DynaMoth Conf
    Config.INSTANCE.setCollectOnlyUsedMethod(false);
    Config.INSTANCE.setCollectStaticMethods(true);
    // Config.INSTANCE.setSynthesisDepth(5);
  }

  /**
   * Call dynamoth to try method synthesis
   * 
   * @param currentMethod the method to be synthesize
   * @param allOracles oracles collected during collect phaze
   * @param classpath classpath from pom.xml source project
   */
  public void call(String currentMethod, Map<String, Map<String, List<Object>>> allOracles, URL[] classpath) {
    logger.info("Trying to synth : " + currentMethod);

    // Declare and fill the oracle structure, only for the current method
    Map<String, Object[]> oracle = new HashMap<String, Object[]>();
    for (Entry<String, List<Object>> entry : allOracles.get(currentMethod).entrySet()) {
      oracle.put(entry.getKey(), entry.getValue().toArray(new Object[entry.getValue().size()]));
    }

    // Apply matcher
    Matcher m = Pattern.compile(Constantes.PATTERN).matcher(currentMethod);
    m.matches();

    // Get the currentMethod as CtMethod
    CtClass<?> clazz = l.getFactory().Class().get(m.group(1));
    List<CtTypeReference<?>> types = new ArrayList<CtTypeReference<?>>();
    for (String typeFromDocComment : m.group(3).split(",")) {
      if (typeFromDocComment.endsWith("...") || typeFromDocComment.endsWith("[]")) {
        types.add(l.getFactory().Type().createArrayReference(typeFromDocComment.replace("...", "").replace("[]", "")));
      } else {
        types.add(l.getFactory().Type().createReference(typeFromDocComment));
      }
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
      classpath,
      oracle,
      oracle.keySet().toArray(new String[oracle.keySet().size()]),
      params.getTimeOutCollection(),
      params.getConstantsArray());

    // Call DynaMoth
    synthesizer.run(TimeUnit.SECONDS.toMillis(params.getTimeOutDynaMoth()));

    // Try if location has been synthesize
    if (synthesizer.getValidExpressions() == null || synthesizer.getValidExpressions().isEmpty()) {
      throw new NoSynthFoundException();
    }
    // Apply the patch
    synthMethod.getBody().getLastStatement().replace(l.getFactory().Code().createCodeSnippetStatement("return " + synthesizer.getValidExpressions().get(0).asPatch()));
    l.prettyprint();
  }

}
