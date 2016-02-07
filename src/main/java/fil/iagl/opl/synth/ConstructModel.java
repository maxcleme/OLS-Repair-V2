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
import spoon.processing.AbstractProcessor;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtPackage;
import spoon.reflect.reference.CtTypeReference;

public class ConstructModel extends AbstractProcessor<CtPackage> {

  private static final Logger logger = Logger.getRootLogger();

  @Override
  public void process(CtPackage method) {
  }

  @Override
  public void processingDone() {

    logger.debug("Trying to synth : " + Constantes.getCurrentMethod());

    Map<String, Object[]> oracle = new HashMap<String, Object[]>();

    for (Entry<String, List<Object>> entry : Constantes.getCollectedValues().get(Constantes.getCurrentMethod()).entrySet()) {
      oracle.put(entry.getKey(), entry.getValue().toArray(new Object[entry.getValue().size()]));
    }

    Matcher m = Pattern.compile(Constantes.getPattern()).matcher(Constantes.getCurrentMethod());
    m.matches();

    CtClass<?> clazz = getFactory().Class().get(m.group(1));
    List<CtTypeReference<?>> types = new ArrayList<CtTypeReference<?>>();
    for (String typeFromDocComment : m.group(3).split(",")) {
      types.add(getFactory().Type().createReference(typeFromDocComment));
    }
    CtMethod<?> synthMethod = clazz.getMethod(m.group(2), types.toArray(new CtTypeReference<?>[types.size()]));

    SourceLocation location = new SourceLocation(m.group(1), synthMethod.getBody().getLastStatement().getPosition().getLine());

    File sourceDir = new File(Constantes.getProjectPath() + "/src/main/java");
    File[] files = {sourceDir};

    Synthesizer synthesizer = new SynthesizerImpl(
      files,
      location,
      Constantes.getClasspath(),
      oracle,
      oracle.keySet().toArray(new String[oracle.keySet().size()]),
      Constantes.getTimeOutCollection(),
      Constantes.getConstantsArray());

    synthesizer.run(TimeUnit.MINUTES.toMillis(Constantes.getTimeOutDynaMoth()));

    if (synthesizer.getValidExpressions().isEmpty()) {
      throw new NoSynthFoundException();
    }
    synthMethod.getBody().getLastStatement().replace(getFactory().Code().createCodeSnippetStatement("return " + synthesizer.getValidExpressions().get(0).asPatch()));
  }

}
