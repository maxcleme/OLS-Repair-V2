package fil.iagl.opl.repair;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.runner.RunWith;

import _instrumenting._CollectorRunner;
import spoon.processing.AbstractProcessor;
import spoon.reflect.code.CtBlock;
import spoon.reflect.code.CtCatch;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtStatement;
import spoon.reflect.code.CtTry;
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

    if (method.getDocComment() == null) {
      return;
    }

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
        CtTry ctTry = getFactory().Core().createTry();
        CtBlock<?> ctTryBlock = getFactory().Core().createBlock();
        ctTry.setParent(method);
        ctTry.setBody(ctTryBlock);

        CtBlock<?> ctCatchBlock = getFactory().Core().createBlock();
        CtCatch ctCatch = getFactory().Code().createCtCatch("e", AssertionError.class, ctCatchBlock);

        ctTry.addCatcher(ctCatch);

        CtStatement assertion = getFactory().Code().createCodeSnippetStatement(invocation.toString());
        invocation.replace(ctTry);

        ctTryBlock.addStatement(getFactory().Code().createCodeSnippetStatement(
          "_instrumenting._Collector.addOutput(\"" + parentClass.getQualifiedName() + "#" + method.getSimpleName() + "\"," +
            invocation.getArguments().get(0)
            + ")"));
        ctTryBlock.addStatement(assertion);
      }
    }

  }

}
