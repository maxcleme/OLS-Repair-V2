package fil.iagl.opl.utils;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.maven.shared.invoker.DefaultInvocationRequest;
import org.apache.maven.shared.invoker.DefaultInvoker;
import org.apache.maven.shared.invoker.InvocationOutputHandler;
import org.apache.maven.shared.invoker.InvocationRequest;
import org.apache.maven.shared.invoker.Invoker;
import org.apache.maven.shared.invoker.MavenInvocationException;

import com.google.common.base.Joiner;

import fil.iagl.opl.repair.ClasspathResolverInvocationOutputHandler;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtMethod;

public class Utils {

  public static String getFormalName(CtMethod<?> method) {
    return method.getParent(CtClass.class).getQualifiedName() + "#" + method.getSimpleName();
  }

  public static int runMavenGoal(String pomPath, String mavenHomePath, List<String> goals, InvocationOutputHandler ioh) throws MavenInvocationException {
    InvocationRequest request = new DefaultInvocationRequest();
    request.setPomFile(new File(pomPath));
    request.setGoals(goals);

    Invoker invoker = new DefaultInvoker();
    invoker.setMavenHome(new File(mavenHomePath));
    if (ioh != null) {
      invoker.setOutputHandler(ioh);
    }
    System.out.println("mvn " + Joiner.on(' ').join(goals));
    return invoker.execute(request).getExitCode();
  }

  public static String getDynamicClasspath(String pomPath, String mavenHomePath) throws MavenInvocationException {
    ClasspathResolverInvocationOutputHandler ioh = new ClasspathResolverInvocationOutputHandler();
    runMavenGoal(pomPath, mavenHomePath, Arrays.asList("dependency:build-classpath"), ioh);
    return ioh.getClasspath();
  }

  public static boolean allTestPass(String pomPath, String mavenHomePath, Map<String, List<Object>> map) throws MavenInvocationException {
    for (String test : map.keySet()) {
      if (runMavenGoal(pomPath, mavenHomePath, Arrays.asList("-Dtest=" + test, "test"), null) != 0)
        return false;
    }
    return true;
  }
}
