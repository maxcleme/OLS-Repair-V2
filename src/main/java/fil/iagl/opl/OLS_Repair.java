package fil.iagl.opl;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.maven.shared.invoker.MavenInvocationException;

import com.sanityinc.jargs.CmdLineParser;
import com.sanityinc.jargs.CmdLineParser.Option;

import fil.iagl.opl.repair.NoSynthFoundException;
import fil.iagl.opl.utils.Utils;
import fr.inria.lille.commons.synthesis.CodeGenesis;
import fr.inria.lille.commons.synthesis.smt.solver.SolverFactory;
import fr.inria.lille.repair.common.config.Config;
import spoon.Launcher;

public class OLS_Repair {
  public static final String MAVEN_HOME_PATH = "C:/Users/RMS/Downloads/apache-maven-3.3.3-bin/apache-maven-3.3.3";

  public static String PROJECT_PATH;
  public static String JUNIT_JAR_PATH;
  public static String Z3_PATH;
  public static Boolean USE_BLACK_BOX;
  public static Boolean OVERRIDE;
  public static Collection<Integer> CONSTANTS_ARRAY;

  public static CodeGenesis patch;

  public static Map<String, Map<String, List<Object>>> collectedValues;
  public static String currentMethod;

  public static void main(String[] args) throws IOException {
    handleArgs(args);
    Config.INSTANCE.setSolverPath(Z3_PATH);
    Config.INSTANCE.setCollectOnlyUsedMethod(false);
    SolverFactory.setSolver(Config.INSTANCE.getSolver(), Config.INSTANCE.getSolverPath());

    if (!OVERRIDE) {
      String workingDirPath = PROJECT_PATH + "_synth";
      File workingDir = new File(workingDirPath);
      File projectDir = new File(PROJECT_PATH);
      try {
        if (workingDir.exists()) {
          FileUtils.forceDelete(workingDir);
        }
        FileUtils.copyDirectory(projectDir, workingDir);
        PROJECT_PATH = workingDirPath;
      } catch (IOException e) {
        throw new RuntimeException("Error occured when creating temporary directory", e);
      }
    }

    File projectDir = new File(PROJECT_PATH);
    File spoonedDir = new File("spooned");

    String[] spoonArgsCollecting = {
      "-i",
      PROJECT_PATH + File.pathSeparatorChar + "src/main/java/_instrumenting",
      "-o",
      "spooned/src/test/java",
      "-p",
      "fil.iagl.opl.repair.OutputCollector",
      "-x"
    };

    String[] spoonArgsSynth = {
      "-i",
      PROJECT_PATH + "/src/main/java",
      "-o",
      PROJECT_PATH + "/src/main/java",
      "-p",
      "fil.iagl.opl.model.ConstructModel",
      "-x"
    };

    if (spoonedDir.exists()) {
      FileUtils.forceDelete(spoonedDir);
    }
    FileUtils.copyDirectory(projectDir, spoonedDir);
    try {
      Launcher.main(spoonArgsCollecting);
    } catch (Exception e) {
      throw new RuntimeException("Error during collecting output values.", e);
    }
    try {
      Utils.runMavenGoal("spooned", OLS_Repair.MAVEN_HOME_PATH, Arrays.asList("clean", "test"), null);
    } catch (MavenInvocationException e) {
      throw new RuntimeException("Error occured during dynamic output collect.", e);
    }

    try {
      FileInputStream fin = new FileInputStream("spooned/collect");
      ObjectInputStream ois = new ObjectInputStream(fin);
      collectedValues = (Map<String, Map<String, List<Object>>>) ois.readObject();
      ois.close();
    } catch (IOException | ClassNotFoundException e) {
      throw new RuntimeException("Error occured when reading collected value.", e);
    }

    List<String> successfulSynth = new ArrayList<>();
    boolean synthNeed = successfulSynth.size() != collectedValues.keySet().size();
    boolean hasSynth = false;
    while (synthNeed) {
      System.out.println("SYNTH LOOP");
      hasSynth = false;
      for (String methodToBeSynth : collectedValues.keySet()) {
        if (successfulSynth.contains(methodToBeSynth)) {
          // ALREADY SYNTH
          continue;
        }

        currentMethod = methodToBeSynth;
        try {
          // mvn test -DskipTests on instrumented project to be sure /target is fill ( mvn compile does not compile tests )
          Utils.runMavenGoal(OLS_Repair.PROJECT_PATH, OLS_Repair.MAVEN_HOME_PATH, Arrays.asList("clean", "test", "-DskipTests"), null);
        } catch (MavenInvocationException e) {
          throw new RuntimeException("Error occured during compiling.", e);
        }
        try {
          Launcher.main(spoonArgsSynth);
          successfulSynth.add(methodToBeSynth);
          hasSynth = true;
        } catch (NoSynthFoundException e) {
          // WILL TRY LATER WITH NEXT SYNTH
        } catch (Exception e) {
          throw new RuntimeException("Error during synthetizing.", e);
        }
      }
      if (!hasSynth) {
        System.out.println("NO SYNTH DURING ENTIRE LOOP");
        System.exit(0);
      }
      synthNeed = successfulSynth.size() != collectedValues.keySet().size();
    }
    System.out.println("END OF OLS");
  }

  private static void handleArgs(String[] args) {
    CmdLineParser parse = new CmdLineParser();

    Option<String> sourcePath = parse.addStringOption('s', "source-path");
    Option<String> junitPath = parse.addStringOption('j', "junit-path");
    Option<String> z3Path = parse.addStringOption('z', "z3-path");
    Option<Integer> constants = parse.addIntegerOption('c', "add-constant");
    Option<Boolean> override = parse.addBooleanOption('o', "override");
    Option<Boolean> useBlackbox = parse.addBooleanOption('u', "use-blackbox");

    try {
      parse.parse(args);
    } catch (CmdLineParser.OptionException e) {
      System.err.println(e.getMessage());
      printUsage();
      System.exit(2);
    }

    PROJECT_PATH = parse.getOptionValue(sourcePath);
    JUNIT_JAR_PATH = parse.getOptionValue(junitPath);
    Z3_PATH = parse.getOptionValue(z3Path);
    OVERRIDE = parse.getOptionValue(override, false);
    USE_BLACK_BOX = parse.getOptionValue(useBlackbox, false);
    CONSTANTS_ARRAY = parse.getOptionValues(constants);

    if (PROJECT_PATH == null || JUNIT_JAR_PATH == null || Z3_PATH == null) {
      printUsage();
      System.exit(2);
    }

  }

  private static void printUsage() {
    System.err.println("Usage: OLS_Repair\n"
      + " -s, --source-path path_buggy_program\n"
      + " -j, --junit-path path_junit_jar\n"
      + " -z, --z3-path path_z3_executable\n"
      + " [-c, --constant one_constant_to_add]*\n"
      + " [-o, --override]\n"
      + " [-u, --use-blackbox]");
  }

}
