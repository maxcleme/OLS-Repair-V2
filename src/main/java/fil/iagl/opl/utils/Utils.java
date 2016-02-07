package fil.iagl.opl.utils;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

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

public class Utils {

  private static final Class<?>[] parameters = new Class[] {URL.class};
  private static final Logger logger = Logger.getRootLogger();

  public static String getFormalName(CtMethod<?> method) {
    return method.getParent(CtClass.class).getQualifiedName() + "#" + method.getSimpleName();
  }

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

  public static String getDynamicClasspath(String pomPath) throws MavenInvocationException {
    ClasspathResolverInvocationOutputHandler ioh = new ClasspathResolverInvocationOutputHandler(false);
    runMavenGoal(pomPath, Arrays.asList("dependency:build-classpath"), ioh);
    return ioh.getClasspath();
  }

  public static boolean allTestPass(String pomPath, Map<String, List<Object>> map) throws MavenInvocationException {
    for (String test : map.keySet()) {
      if (runMavenGoal(pomPath, Arrays.asList("-Dtest=" + test, "test"), null) != 0)
        return false;
    }
    return true;
  }

  public static void addURL(URL u) {

    URLClassLoader sysloader = (URLClassLoader) ClassLoader.getSystemClassLoader();
    Class<?> sysclass = URLClassLoader.class;

    try {
      Method method = sysclass.getDeclaredMethod("addURL", parameters);
      method.setAccessible(true);
      method.invoke(sysloader, new Object[] {u});
    } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
      throw new RuntimeException("Exception occured during adding URL to system classloader", e);
    }

  }

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
