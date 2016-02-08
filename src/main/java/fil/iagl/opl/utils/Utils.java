package fil.iagl.opl.utils;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.apache.maven.shared.invoker.DefaultInvocationRequest;
import org.apache.maven.shared.invoker.DefaultInvoker;
import org.apache.maven.shared.invoker.InvocationOutputHandler;
import org.apache.maven.shared.invoker.InvocationRequest;
import org.apache.maven.shared.invoker.Invoker;
import org.apache.maven.shared.invoker.MavenInvocationException;

import com.google.common.base.Joiner;

import fil.iagl.opl.Constantes;
import fil.iagl.opl.synth.ClasspathResolverInvocationOutputHandler;
import fil.iagl.opl.synth.VerboseOutputHandler;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtMethod;

/**
 * @author Maxime CLEMENT
 *
 * Utility class
 */
public class Utils {

  private static final Logger logger = Logger.getRootLogger();

  /**
   * Restricted constructor
   */
  private Utils() {
  }

  /**
   * @param method the method concerned
   * @return the name of the method with the format : class.qualified.name#methodName(type.qualified.name...)
   */
  public static String getFormalName(CtMethod<?> method) {
    return method.getParent(CtClass.class).getQualifiedName() + "#" + method.getSimpleName();
  }

  /**
   * Run a set of Maven goals.
   * 
   * @param pomPath pom.xml location of the project
   * @param goals Sets of goals
   * @param ioh Outputhandler, if null the VerboseOutputHandler is used
   * @return status code
   * @throws MavenInvocationException exception throw by Maven Invoker API
   */
  public static int runMavenGoal(String pomPath, List<String> goals, InvocationOutputHandler ioh) throws MavenInvocationException {
    InvocationRequest request = new DefaultInvocationRequest();
    request.setPomFile(new File(pomPath));
    request.setGoals(goals);

    Invoker invoker = new DefaultInvoker();
    invoker.setMavenHome(new File(Constantes.getMavenHomePath()));

    if (ioh == null) {
      ioh = new VerboseOutputHandler(Constantes.getVerbose());
    }
    invoker.setOutputHandler(ioh);

    logger.info("> mvn " + Joiner.on(' ').join(goals));
    return invoker.execute(request).getExitCode();
  }

  /**
   * Compute classpath from pom.xml
   * 
   * @param pomPath pom.xml location
   * @return classpath
   * @throws MavenInvocationException exception throw by Maven Invoker API
   */
  public static String getDynamicClasspath(String pomPath) throws MavenInvocationException {
    ClasspathResolverInvocationOutputHandler ioh = new ClasspathResolverInvocationOutputHandler(false);
    runMavenGoal(pomPath, Arrays.asList("dependency:build-classpath"), ioh);
    return ioh.getClasspath();
  }

  /**
   * Check if all tests pass as parameter are passing
   * 
   * @param pomPath pom.xml location
   * @param tests lists of test qualified name classes
   * @return true if all tests pass, false otherwise
   * @throws MavenInvocationException exception throw by Maven Invoker API
   */
  public static boolean allTestPass(String pomPath, List<String> tests) throws MavenInvocationException {
    for (String test : tests) {
      if (runMavenGoal(pomPath, Arrays.asList("-Dtest=" + test, "test"), null) != 0)
        return false;
    }
    return true;
  }

  /**
   * Dynamically add url to current classpath
   * 
   * @param url Url to add
   */
  public static void addURL(URL url) {
    URLClassLoader sysloader = (URLClassLoader) ClassLoader.getSystemClassLoader();
    Class<?> sysclass = URLClassLoader.class;
    try {
      Method method = sysclass.getDeclaredMethod("addURL", new Class[] {URL.class});
      method.setAccessible(true);
      method.invoke(sysloader, new Object[] {url});
    } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
      throw new RuntimeException("Exception occured during adding URL to system classloader", e);
    }

  }

  /**
   * Delete folder if exists
   * 
   * @param dir Directory concerned
   */
  public static void deleteIfExist(File dir) {
    try {
      if (dir.exists()) {
        FileUtils.forceDelete(dir);
      }
    } catch (IOException e) {
      throw new RuntimeException("Exception occured during deleting folder.", e);
    }

  }
}
