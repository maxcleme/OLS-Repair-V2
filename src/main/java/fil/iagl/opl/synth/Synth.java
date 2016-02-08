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

/**
 * @author Maxime CLEMENT
 * 
 * Synthesizer call. Contains the workflow of the application.
 */
public class Synth {

  private static final Logger logger = Logger.getRootLogger();

  /**
   * Args for spoon the instrument tests
   */
  private String[] spoonArgsCollecting;
  /**
   * Args for spoon to apply patch
   */
  private String[] spoonArgsSynth;

  public Synth() {
    this.spoonArgsCollecting = new String[] {
      "-i", Constantes.getProjectPath() + "/src/main/java" + File.pathSeparatorChar + Constantes.getProjectPath() + "/src/test/java" + File.pathSeparatorChar
        + "src/main/java/_instrumenting",
      "-o", Constantes.getSpoonedFolderName() + "/src/test/java",
      "-p", OutputCollector.class.getName(),
      "-x"
    };

  }

  /**
   * Start the workflow.
   */
  public void start() {
    // The project pass as parameter
    File projectDir = new File(Constantes.getProjectPath());

    // Remove old execution result
    try {
      if (Constantes.getSpoonedDir().exists()) {
        FileUtils.forceDelete(Constantes.getSpoonedDir());
      }
      FileUtils.copyDirectory(projectDir, Constantes.getSpoonedDir());
    } catch (IOException e) {
      throw new RuntimeException("Error during copyping project to temporary folder", e);
    }

    // Instrument test
    try {
      logger.info("Intrumenting tests to further collect...");
      Launcher.main(spoonArgsCollecting);
    } catch (Exception e) {
      throw new RuntimeException("Error during collecting output values.", e);
    }

    // Run instrumented test
    try {
      logger.info("Running instrumented tests to collect output values...");
      Utils.runMavenGoal(Constantes.getSpoonedFolderName(), Arrays.asList("clean", "test"), null);
    } catch (MavenInvocationException e) {
      throw new RuntimeException("Error occured during dynamic output collect.", e);
    }

    // mvn test -DskipTests on instrumented project to be sure /target is fill ( mvn compile does not compile tests )
    try {
      logger.info("Compiling...");
      Utils.runMavenGoal(Constantes.getProjectPath(), Arrays.asList("clean", "test", "-DskipTests"), null);
    } catch (MavenInvocationException e) {
      throw new RuntimeException("Error occured during compiling.", e);
    }

    // Compute classpath from pom.xml
    logger.info("Computing projet classpath from pom.xml...");
    String classpathAsString;
    URL[] classpath;
    try {
      classpathAsString = Utils.getDynamicClasspath(Constantes.getProjectPath());
      classpathAsString += File.pathSeparatorChar + Constantes.getProjectPath() + File.separatorChar + "target" + File.separatorChar + "classes";
      classpathAsString += File.pathSeparatorChar + Constantes.getSpoonedDir().getAbsolutePath() + File.separatorChar + "target" + File.separatorChar + "test-classes";
      classpath = JavaLibrary.classpathFrom(classpathAsString);
      Constantes.setClasspath(classpath);
    } catch (MavenInvocationException e) {
      throw new RuntimeException("Error occured during classpath evaluation.", e);
    }

    // Dynamically add classpath to current classpath
    for (URL url : classpath) {
      Utils.addURL(url);
    }

    // Retrieved oracle structure serialized in /collect file
    try {
      FileInputStream fin = new FileInputStream(Constantes.getSpoonedFolderName() + "/collect");
      ObjectInputStream ois = new ObjectInputStream(fin);
      Constantes.setCollectedValues((Map<String, Map<String, List<Object>>>) ois.readObject());
      ois.close();
    } catch (IOException | ClassNotFoundException e) {
      throw new RuntimeException("Error occured when reading collected value.", e);
    }

    // Main loop
    String sourcePath = Constantes.getProjectPath() + "/src/main/java";
    DynaMothCaller caller = new DynaMothCaller(sourcePath, sourcePath);
    List<String> successfulSynth = new ArrayList<>();
    boolean synthNeed = successfulSynth.size() != Constantes.getCollectedValues().keySet().size();
    boolean hasSynth = false;

    // While method could be synth
    while (synthNeed) {
      hasSynth = false;
      // For all method with @link and valid pattern
      for (String methodToBeSynth : Constantes.getCollectedValues().keySet()) {
        if (successfulSynth.contains(methodToBeSynth)) {
          // Already synth this method
          continue;
        }

        // Set the current method
        Constantes.setCurrentMethod(methodToBeSynth);

        // mvn test -DskipTests on instrumented project to be sure /target is fill ( mvn compile does not compile tests )
        try {
          logger.info("Compiling...");
          Utils.runMavenGoal(Constantes.getProjectPath(), Arrays.asList("clean", "test", "-DskipTests"), null);
        } catch (MavenInvocationException e) {
          throw new RuntimeException("Error occured during compiling.", e);
        }

        // Start DynaMoth and apply patch if found
        try {
          // Launcher.main(spoonArgsSynth);
          caller.call(methodToBeSynth);
          successfulSynth.add(methodToBeSynth);
          hasSynth = true; // One method has been synth
        } catch (NoSynthFoundException e) {
          logger.info("Cannot synthesize " + methodToBeSynth + " for now.");
        } catch (Exception e) {
          throw new RuntimeException("Error during synthetizing.", e);
        }
      }

      // If no method could be synthesize during the entire loop, the configuration will never found new synthesize.
      if (!hasSynth) {
        logger.info("Can no synthesize more methods. Maybe you can increase timeout or write more tests.");
        System.exit(0); // Kill remaining threads
      }
      // Check all method has been synthesized
      synthNeed = successfulSynth.size() != Constantes.getCollectedValues().keySet().size();
    }
    logger.info("Successfully synthesize " + successfulSynth.size() + " methods.");
  }

}
