package fil.iagl.opl.utils;

import java.util.HashSet;

import com.sanityinc.jargs.CmdLineParser;
import com.sanityinc.jargs.CmdLineParser.Option;

import fil.iagl.opl.Constantes;

/**
 * @author Maxime CLEMENT
 *
 * Parse arguments from user
 */
public class Params {

  /**
   * Parse args from user
   * 
   * @param args
   */
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

    Constantes.setProjectPath(parse.getOptionValue(sourcePathOption));
    Constantes.setMavenHomePath(parse.getOptionValue(mavenPathOption));
    Constantes.setOverride(parse.getOptionValue(overrideOption, false));
    Constantes.setVerbose(parse.getOptionValue(verboseOption, false));
    Constantes.setConstantsArray(new HashSet<Integer>(parse.getOptionValues(constantsOption)));
    Constantes.setTimeOutCollection(parse.getOptionValue(timeOutCollectionOption, 5));
    Constantes.setTimeOutDynaMoth(parse.getOptionValue(timeOutDynaMothOption, 15));

    // -s and -m are mandatory
    if (Constantes.getProjectPath() == null || Constantes.getMavenHomePath() == null) {
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

}
