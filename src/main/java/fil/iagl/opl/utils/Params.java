package fil.iagl.opl.utils;

import java.util.HashSet;
import java.util.Set;

import com.sanityinc.jargs.CmdLineParser;
import com.sanityinc.jargs.CmdLineParser.Option;

/**
 * @author Maxime CLEMENT
 *
 * Parse arguments from user
 */
public class Params {

  /**
   * time out for dynamoth collection in seconds
   */
  private Integer timeOutCollection;
  /**
   * global time out for dynamoth in seconds
   */
  private Integer timeOutDynaMoth;
  /**
   * project path containing methods to be synthesize
   */
  private String projectPath;
  /**
   * path to user maven home
   */
  private String mavenHomePath;
  /**
   * override original project ?
   */
  private Boolean override;
  /**
   * print more things
   */
  private Boolean verbose;
  /**
   * primitive int constants to add to dynamoth
   */
  private Set<Integer> constantsArray;

  public Params(String[] args) {
    handleArgs(args);
  }

  /**
   * Parse args from user
   * 
   * @param args
   */
  private void handleArgs(String[] args) {
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

    this.projectPath = parse.getOptionValue(sourcePathOption);
    this.mavenHomePath = parse.getOptionValue(mavenPathOption);
    this.override = parse.getOptionValue(overrideOption, false);
    this.verbose = parse.getOptionValue(verboseOption, false);
    this.constantsArray = new HashSet<Integer>(parse.getOptionValues(constantsOption));
    this.timeOutCollection = parse.getOptionValue(timeOutCollectionOption, 10);
    this.timeOutDynaMoth = parse.getOptionValue(timeOutDynaMothOption, 30);

    // -s and -m are mandatory
    if (this.projectPath == null || this.mavenHomePath == null) {
      printUsage();
      System.exit(2);
    }

  }

  /**
   * Print usage
   */
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

  public String getProjectPath() {
    return projectPath;
  }

  public void setProjectPath(String projectPath) {
    this.projectPath = projectPath;
  }

  public String getMavenHomePath() {
    return mavenHomePath;
  }

  public void setMavenHomePath(String mavenHomePath) {
    this.mavenHomePath = mavenHomePath;
  }

  public Boolean getOverride() {
    return override;
  }

  public void setOverride(Boolean override) {
    this.override = override;
  }

  public Set<Integer> getConstantsArray() {
    return constantsArray;
  }

  public void setConstantsArray(Set<Integer> constantsArray) {
    this.constantsArray = constantsArray;
  }

  public Integer getTimeOutCollection() {
    return timeOutCollection;
  }

  public void setTimeOutCollection(Integer timeOutCollection) {
    this.timeOutCollection = timeOutCollection;
  }

  public Integer getTimeOutDynaMoth() {
    return timeOutDynaMoth;
  }

  public void setTimeOutDynaMoth(Integer timeOutDynaMoth) {
    this.timeOutDynaMoth = timeOutDynaMoth;
  }

  public Boolean getVerbose() {
    return verbose;
  }

  public void setVerbose(Boolean verbose) {
    this.verbose = verbose;
  }

}
