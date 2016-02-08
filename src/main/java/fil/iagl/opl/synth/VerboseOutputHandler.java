package fil.iagl.opl.synth;

import org.apache.log4j.Logger;
import org.apache.maven.shared.invoker.InvocationOutputHandler;

/**
 * @author Maxime CLEMENT
 *
 * Custom OutputHandler to handle a 'verbose' variable.
 */
public class VerboseOutputHandler implements InvocationOutputHandler {

  private static final Logger logger = Logger.getRootLogger();

  private boolean verbose;

  /**
   * @param verbose print Maven execution
   */
  public VerboseOutputHandler(boolean verbose) {
    this.verbose = verbose;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.codehaus.plexus.util.cli.StreamConsumer#consumeLine(java.lang.String)
   */
  @Override
  public void consumeLine(String line) {
    // Print line only if 'verbose' is set to true
    if (verbose)
      System.out.println(line);
  }

}
