package fil.iagl.opl.repair;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.runner.RunWith;

import _instrumenting._CollectorRunner;
import fil.iagl.opl.utils.Utils;
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
  public void process(CtMethod<?> testMethod) {

    if (testMethod.getDocComment() == null) {
      return;
    }

    Pattern p = Pattern.compile("@link((.*)#(.*)\\((.*?)\\))");
    Matcher m = p.matcher(testMethod.getDocComment().trim().replaceAll(" ", ""));
    if (!m.matches()) {
      return;
    }

    CtClass<?> parentTestClass = testMethod.getParent(CtClass.class);

    if (parentTestClass.getAnnotation(RunWith.class) == null)
      getFactory().Annotation().annotate(parentTestClass, RunWith.class, "value", _CollectorRunner.class);

    List<CtInvocation<?>> invocations = testMethod.getElements(new TypeFilter<>(CtInvocation.class));
    for (CtInvocation<?> invocation : invocations) {
      if (invocation.getTarget().getType().getQualifiedName().equals("org.junit.Assert")) {
        CtTry ctTry = getFactory().Core().createTry();
        CtBlock<?> ctTryBlock = getFactory().Core().createBlock();
        ctTry.setParent(testMethod);
        ctTry.setBody(ctTryBlock);

        CtBlock<?> ctCatchBlock = getFactory().Core().createBlock();
        CtCatch ctCatch = getFactory().Code().createCtCatch("e", Exception.class, ctCatchBlock);

        ctTry.addCatcher(ctCatch);

        CtStatement assertion = getFactory().Code().createCodeSnippetStatement(invocation.toString());
        invocation.replace(ctTry);

        ctTryBlock.addStatement(getFactory().Code().createCodeSnippetStatement(
          "_instrumenting._Collector.addOutput(\"" + m.group(1) + "\",\"" + Utils.getFormalName(testMethod) + "\"," +
            invocation.getArguments().get(0)
            + ")"));
        ctTryBlock.addStatement(assertion);
      }
    }

  }

  @Override
  public void processingDone() {

  }

}
