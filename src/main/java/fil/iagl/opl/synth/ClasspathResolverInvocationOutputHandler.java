package fil.iagl.opl.synth;

/**
 * @author Maxime CLEMENT
 *
 * Use to retrieve classpath during maven execution.
 */
public class ClasspathResolverInvocationOutputHandler extends VerboseOutputHandler {

  /**
   * Does the next line is the classpath
   */
  private boolean next;
  /**
   * The classpath string
   */
  private String classpath;

  /**
   * @param verbose print Maven execution
   */
  public ClasspathResolverInvocationOutputHandler(boolean verbose) {
    super(verbose);
    this.next = false;
  }

  /*
   * (non-Javadoc)
   * 
   * @see fil.iagl.opl.synth.VerboseOutputHandler#consumeLine(java.lang.String)
   */
  @Override
  public void consumeLine(String line) {
    super.consumeLine(line);
    if (next) {
      this.classpath = line;
    }
    this.next = line.equals("[INFO] Dependencies classpath:");
  }

  /**
   * @return the classpath printed during maven execution
   */
  public String getClasspath() {
    return classpath;
  }

}
