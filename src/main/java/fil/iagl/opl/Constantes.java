package fil.iagl.opl;

import java.io.File;
import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.sanityinc.jargs.CmdLineParser;
import com.sanityinc.jargs.CmdLineParser.Option;

public class Constantes {
  private static String pattern = "(.*)#(.*)\\((.*?)\\)";
  private static String spoonedFolderName = "spooned";
  private static File spoonedDir = new File(spoonedFolderName);

  private static String projectPath;
  private static String mavenHomePath;
  private static String currentMethod;
  private static Boolean override;
  private static URL[] classpath;
  private static Set<Integer> constantsArray;
  private static Map<String, Map<String, List<Object>>> collectedValues;

  private static Integer timeOutCollection;
  private static Integer timeOutDynaMoth;

  private static Boolean verbose;

  public static void handleArgs(String[] args) {
    CmdLineParser parse = new CmdLineParser();

    Option<String> sourcePathOption = parse.addStringOption('s', "source-path");
    Option<String> mavenPathOption = parse.addStringOption('m', "maven-home-path");
    Option<Integer> constantsOption = parse.addIntegerOption('c', "add-constant");
    Option<Integer> timeOutCollectionOption = parse.addIntegerOption('t', "time-out-collection");
    Option<Integer> timeOutDynaMothOption = parse.addIntegerOption('d', "time-out-dynamoth");
    Option<Boolean> overrideOption = parse.addBooleanOption('o', "override");
    Option<Boolean> verboseOption = parse.addBooleanOption('v', "verbose");

    try {
      parse.parse(args);
    } catch (CmdLineParser.OptionException e) {
      System.err.println(e.getMessage());
      printUsage();
      System.exit(2);
    }

    projectPath = parse.getOptionValue(sourcePathOption);
    mavenHomePath = parse.getOptionValue(mavenPathOption);
    override = parse.getOptionValue(overrideOption, false);
    verbose = parse.getOptionValue(verboseOption, false);
    constantsArray = new HashSet<Integer>(parse.getOptionValues(constantsOption));
    timeOutCollection = parse.getOptionValue(timeOutCollectionOption, 5);
    timeOutDynaMoth = parse.getOptionValue(timeOutDynaMothOption, 15);

    if (projectPath == null) {
      printUsage();
      System.exit(2);
    }

  }

  private static void printUsage() {
    System.err.println("Usage: OLS_Repair\n"
      + " -s, --source-path\t\tpath_program\n"
      + " -m, --maven-home-path\t\tpath_maven_home\n"
      + " [-c, --constant\t\tone_constant_to_add]*\n"
      + " [-t, --time-out-collection\ttime in second]\n"
      + " [-d, --time-out-dynamoth\ttime in second]\n"
      + " [-o, --override]\n"
      + " [-v, --verbose]\n");
  }

  public static String getProjectPath() {
    return projectPath;
  }

  public static void setProjectPath(String projectPath) {
    Constantes.projectPath = projectPath;
  }

  public static String getMavenHomePath() {
    return mavenHomePath;
  }

  public static void setMavenHomePath(String mavenHomePath) {
    Constantes.mavenHomePath = mavenHomePath;
  }

  public static Boolean getOverride() {
    return override;
  }

  public static void setOverride(Boolean override) {
    Constantes.override = override;
  }

  public static Set<Integer> getConstantsArray() {
    return constantsArray;
  }

  public static void setConstantsArray(Set<Integer> constantsArray) {
    Constantes.constantsArray = constantsArray;
  }

  public static Map<String, Map<String, List<Object>>> getCollectedValues() {
    return collectedValues;
  }

  public static void setCollectedValues(Map<String, Map<String, List<Object>>> collectedValues) {
    Constantes.collectedValues = collectedValues;
  }

  public static String getCurrentMethod() {
    return currentMethod;
  }

  public static void setCurrentMethod(String currentMethod) {
    Constantes.currentMethod = currentMethod;
  }

  public static String getSpoonedFolderName() {
    return spoonedFolderName;
  }

  public static void setSpoonedFolderName(String spoonedFolderName) {
    Constantes.spoonedFolderName = spoonedFolderName;
  }

  public static File getSpoonedDir() {
    return spoonedDir;
  }

  public static void setSpoonedDir(File spoonedDir) {
    Constantes.spoonedDir = spoonedDir;
  }

  public static URL[] getClasspath() {
    return classpath;
  }

  public static void setClasspath(URL[] classpath) {
    Constantes.classpath = classpath;
  }

  public static String getPattern() {
    return pattern;
  }

  public static void setPattern(String pattern) {
    Constantes.pattern = pattern;
  }

  public static Integer getTimeOutCollection() {
    return timeOutCollection;
  }

  public static void setTimeOutCollection(Integer timeOutCollection) {
    Constantes.timeOutCollection = timeOutCollection;
  }

  public static Integer getTimeOutDynaMoth() {
    return timeOutDynaMoth;
  }

  public static void setTimeOutDynaMoth(Integer timeOutDynaMoth) {
    Constantes.timeOutDynaMoth = timeOutDynaMoth;
  }

  public static Boolean getVerbose() {
    return verbose;
  }

  public static void setVerbose(Boolean verbose) {
    Constantes.verbose = verbose;
  }

}
