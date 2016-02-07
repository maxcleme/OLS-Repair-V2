package fil.iagl.opl.synth;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.apache.maven.shared.invoker.MavenInvocationException;

import fil.iagl.opl.Constantes;
import fil.iagl.opl.utils.Utils;
import spoon.Launcher;
import xxl.java.library.JavaLibrary;

public class Synth {

  private static final Logger logger = Logger.getRootLogger();

  public void start() {
    File projectDir = new File(Constantes.getProjectPath());

    String[] spoonArgsCollecting = {
      "-i",
      Constantes.getProjectPath() + File.pathSeparatorChar + "src/main/java/_instrumenting",
      "-o",
      Constantes.getSpoonedFolderName() + "/src/test/java",
      "-p",
      "fil.iagl.opl.synth.OutputCollector",
      "-x"
    };

    String[] spoonArgsSynth = {
      "-i",
      Constantes.getProjectPath() + "/src/main/java",
      "-o",
      Constantes.getProjectPath() + "/src/main/java",
      "-p",
      "fil.iagl.opl.synth.ConstructModel",
      "-x"
    };

    try {
      if (Constantes.getSpoonedDir().exists()) {
        FileUtils.forceDelete(Constantes.getSpoonedDir());
      }
      FileUtils.copyDirectory(projectDir, Constantes.getSpoonedDir());
    } catch (IOException e) {
      throw new RuntimeException("Error during copyping project to temporary folder", e);
    }

    try {
      logger.info("Intrumenting tests to further collect...");
      Launcher.main(spoonArgsCollecting);
    } catch (Exception e) {
      throw new RuntimeException("Error during collecting output values.", e);
    }
    try {
      logger.info("Running instrumented tests to collect output values...");
      Utils.runMavenGoal(Constantes.getSpoonedFolderName(), Arrays.asList("clean", "test"), null);
    } catch (MavenInvocationException e) {
      throw new RuntimeException("Error occured during dynamic output collect.", e);
    }

    logger.info("Computing projet classpath from pom.xml...");
    String classpathAsString;
    try {
      classpathAsString = Utils.getDynamicClasspath(Constantes.getProjectPath());
      classpathAsString += File.pathSeparatorChar + Constantes.getProjectPath() + File.separatorChar + "target" + File.separatorChar + "classes";
      classpathAsString += File.pathSeparatorChar + Constantes.getSpoonedDir().getAbsolutePath() + File.separatorChar + "target" + File.separatorChar + "test-classes";
    } catch (MavenInvocationException e) {
      throw new RuntimeException("Error occured during classpath evaluation.", e);
    }

    URL[] classpath = JavaLibrary.classpathFrom(classpathAsString);
    for (URL url : classpath) {
      Utils.addURL(url);
    }
    Constantes.setClasspath(classpath);

    try {
      FileInputStream fin = new FileInputStream(Constantes.getSpoonedFolderName() + "/collect");
      ObjectInputStream ois = new ObjectInputStream(fin);
      Constantes.setCollectedValues((Map<String, Map<String, List<Object>>>) ois.readObject());
      ois.close();
    } catch (IOException | ClassNotFoundException e) {
      throw new RuntimeException("Error occured when reading collected value.", e);
    }

    List<String> successfulSynth = new ArrayList<>();
    boolean synthNeed = successfulSynth.size() != Constantes.getCollectedValues().keySet().size();
    boolean hasSynth = false;
    while (synthNeed) {
      hasSynth = false;
      for (String methodToBeSynth : Constantes.getCollectedValues().keySet()) {
        if (successfulSynth.contains(methodToBeSynth)) {
          // ALREADY SYNTH
          continue;
        }

        Constantes.setCurrentMethod(methodToBeSynth);
        try {
          // mvn test -DskipTests on instrumented project to be sure /target is fill ( mvn compile does not compile tests )
          logger.info("Compiling...");
          Utils.runMavenGoal(Constantes.getProjectPath(), Arrays.asList("clean", "test", "-DskipTests"), null);
        } catch (MavenInvocationException e) {
          throw new RuntimeException("Error occured during compiling.", e);
        }
        try {
          Launcher.main(spoonArgsSynth);
          successfulSynth.add(methodToBeSynth);
          hasSynth = true;
        } catch (NoSynthFoundException e) {
          logger.info("Cannot synthesize " + methodToBeSynth + " for now.");
        } catch (Exception e) {
          throw new RuntimeException("Error during synthetizing.", e);
        }
      }
      if (!hasSynth) {
        logger.info("Can no synthesize more methods. Maybe you can increase timeout or write more tests.");
        System.exit(0);
      }
      synthNeed = successfulSynth.size() != Constantes.getCollectedValues().keySet().size();
    }
    logger.info("Successfully synthesize " + successfulSynth.size() + " methods.");
  }

}
