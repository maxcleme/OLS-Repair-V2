package fil.iagl.opl.synth;

import spoon.reflect.code.CtInvocation;
import spoon.reflect.visitor.filter.TypeFilter;

public class AssertionInvocationFilter extends TypeFilter<CtInvocation<?>> {

  public AssertionInvocationFilter() {
    super(CtInvocation.class);
  }

  public AssertionInvocationFilter(Class<? super CtInvocation<?>> type) {
    super(type);
  }

  @Override
  public boolean matches(CtInvocation<?> element) {
    return super.matches(element) && element.toString().startsWith("org.junit.Assert");
  }

}
