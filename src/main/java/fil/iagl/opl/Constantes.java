package fil.iagl.opl;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
