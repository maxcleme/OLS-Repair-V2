package fil.iagl.opl.repair;

import org.apache.maven.shared.invoker.InvocationOutputHandler;

public class ClasspathResolverInvocationOutputHandler implements InvocationOutputHandler {

  private boolean next;
  private String classpath;

  public ClasspathResolverInvocationOutputHandler() {
    this.next = false;
  }

  @Override
  public void consumeLine(String line) {
    if (next) {
      this.classpath = line;
    }
    this.next = line.equals("[INFO] Dependencies classpath:");
  }

  public String getClasspath() {
    return classpath;
  }

}
