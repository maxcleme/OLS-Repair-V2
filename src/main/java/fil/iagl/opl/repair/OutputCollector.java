package fil.iagl.opl.repair;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.runner.RunWith;

import _instrumenting._CollectorRunner;
import spoon.processing.AbstractProcessor;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.visitor.filter.TypeFilter;

public class OutputCollector extends AbstractProcessor<CtMethod<?>> {

  @Override
  public boolean isToBeProcessed(CtMethod<?> candidate) {
    return candidate.getAnnotation(org.junit.Test.class) != null && candidate.getParent(CtClass.class) != null;
  }

  @Override
  public void process(CtMethod<?> method) {

    Pattern p = Pattern.compile("@link((.*)#(.*)\\((.*?)\\))");
    Matcher m = p.matcher(method.getDocComment().trim().replaceAll(" ", ""));
    if (!m.matches()) {
      return;
    }

    CtClass<?> parentClass = method.getParent(CtClass.class);

    if (parentClass.getAnnotation(RunWith.class) == null)
      getFactory().Annotation().annotate(parentClass, RunWith.class, "value", _CollectorRunner.class);

    List<CtInvocation<?>> invocations = method.getElements(new TypeFilter<>(CtInvocation.class));
    for (CtInvocation<?> invocation : invocations) {
      if (invocation.getTarget().getType().getQualifiedName().equals("org.junit.Assert")) {
        // TODO : Dynamic collect for expected value
        // Model.addOutput(testedMethodSignature, method, Integer.parseInt(invocation.getArguments().get(0).toString()));
        // testedMethodsSignatures.get(testedMethodSignature).get(method).add(invocation.getArguments().get(0));
        invocation.insertBefore(getFactory().Code().createCodeSnippetStatement(
          "_instrumenting._Collector.addOutput(\"" + parentClass.getQualifiedName() + "#" + method.getSimpleName() + "\"," + invocation.getArguments().get(0)
            + ")"));
      }
    }

  }

}
