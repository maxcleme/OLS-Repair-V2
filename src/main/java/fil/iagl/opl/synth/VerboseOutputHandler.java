package fil.iagl.opl.synth;

import org.apache.log4j.Logger;
import org.apache.maven.shared.invoker.InvocationOutputHandler;

public class VerboseOutputHandler implements InvocationOutputHandler {

  private static final Logger logger = Logger.getRootLogger();

  private boolean verbose;

  public VerboseOutputHandler(boolean verbose) {
    this.verbose = verbose;
  }

  @Override
  public void consumeLine(String line) {
    if (verbose)
      System.out.println(line);
  }

}
