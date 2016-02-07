package fil.iagl.opl.synth;

public class ClasspathResolverInvocationOutputHandler extends VerboseOutputHandler {

  private boolean next;
  private String classpath;

  public ClasspathResolverInvocationOutputHandler(boolean verbose) {
    super(verbose);
    this.next = false;
  }

  @Override
  public void consumeLine(String line) {
    super.consumeLine(line);
    if (next) {
      this.classpath = line;
    }
    this.next = line.equals("[INFO] Dependencies classpath:");
  }

  public String getClasspath() {
    return classpath;
  }

}
