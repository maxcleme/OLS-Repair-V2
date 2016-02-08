package fil.iagl.opl.synth;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.runner.RunWith;

import _instrumenting._CollectorRunner;
import fil.iagl.opl.Constantes;
import fil.iagl.opl.utils.Utils;
import spoon.processing.AbstractProcessor;
import spoon.reflect.code.CtBlock;
import spoon.reflect.code.CtCatch;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtStatement;
import spoon.reflect.code.CtTry;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtMethod;

/**
 * @author Maxime CLEMENT
 *
 * Processor to instrument tests, generate call to _Collector
 * Also surround assertions with try/catch to force all test execution.
 * If one assertion fails, throw exception at the end to preserve original behavior.
 */
public class OutputCollector extends AbstractProcessor<CtMethod<?>> {

  /**
   * Name of boolean variable to be insert
   */
  private String booleanName;
  /**
   * Initialization of the boolean
   */
  private CtStatement variableInit;
  /**
   * Change to value of the variable to false
   */
  private CtStatement variableToFalse;
  /**
   * The inserted assertion to preserved original behavior
   */
  private CtStatement finalAssert;

  /**
   * TODO : Should use Spoon Element instead of createCodeSnippetStatement()
   */
  @Override
  public void init() {
    // Random name, limit the collision with other variable
    booleanName = RandomStringUtils.randomAlphabetic(15);
    // Init the variable to true
    variableInit = getFactory().Code().createCodeSnippetStatement("boolean " + booleanName + " = true");
    // Change value of the variable to false
    variableToFalse = getFactory().Code().createCodeSnippetStatement(booleanName + "=false");
    // Test that no assertions has fail
    finalAssert = getFactory().Code().createCodeSnippetStatement("org.junit.Assert.assertTrue(" + booleanName + ")");
  }

  /*
   * (non-Javadoc)
   * 
   * @see spoon.processing.AbstractProcessor#isToBeProcessed(spoon.reflect.declaration.CtElement)
   */
  @Override
  public boolean isToBeProcessed(CtMethod<?> candidate) {
    // Filter method with @Test and method with a parent class to add @RunWith.
    return candidate.getAnnotation(org.junit.Test.class) != null && candidate.getParent(CtClass.class) != null;
  }

  @Override
  public void process(CtMethod<?> testMethod) {
    // Method need to have javadoc
    if (testMethod.getDocComment() == null) {
      return;
    }

    // If no @link qualifiedName#method(type...) pattern, do not instrument it.
    Pattern p = Pattern.compile("@see(" + Constantes.getPattern() + ")");
    Matcher m = p.matcher(testMethod.getDocComment().trim().replaceAll(" ", ""));
    if (!m.matches()) {
      return;
    }

    // Add boolean initialization at the beginning of the test
    testMethod.getBody().getStatements().add(0, variableInit);

    // If Class doesn't have @RunWith yet, add @RunWith(_CollectorRunner.class)
    CtClass<?> parentTestClass = testMethod.getParent(CtClass.class);
    if (parentTestClass.getAnnotation(RunWith.class) == null) {
      getFactory().Annotation().annotate(parentTestClass, RunWith.class, "value", _CollectorRunner.class);
    }

    // For all invocation
    List<CtInvocation<?>> invocations = testMethod.getElements(new AssertionInvocationFilter());
    for (CtInvocation<?> invocation : invocations) {
      // Build tryBlock
      CtTry ctTry = getFactory().Core().createTry();
      CtBlock<?> ctTryBlock = getFactory().Core().createBlock();
      ctTry.setParent(testMethod);
      ctTry.setBody(ctTryBlock);

      // Build catchBlock
      CtBlock<?> ctCatchBlock = getFactory().Core().createBlock();
      CtCatch ctCatch = getFactory().Code().createCtCatch("e", Exception.class, ctCatchBlock);
      ctCatch.getBody().addStatement(variableToFalse);

      // Build try/catch
      ctTry.addCatcher(ctCatch);

      // Surround assertion with try/catch
      CtStatement assertion = getFactory().Code().createCodeSnippetStatement(invocation.toString());
      invocation.replace(ctTry);

      // Add _Collector invocation before the assertion
      ctTryBlock.addStatement(getFactory().Code().createCodeSnippetStatement(
        "_instrumenting._Collector.addOutput(\"" + m.group(1) + "\",\"" + Utils.getFormalName(testMethod) + "\"," +
          invocation.getArguments().get(0)
          + ")"));
      ctTryBlock.addStatement(assertion);

    }

    // Add final assertion to the test method
    testMethod.getBody().addStatement(finalAssert);

  }

  @Override
  public void processingDone() {

  }

}
