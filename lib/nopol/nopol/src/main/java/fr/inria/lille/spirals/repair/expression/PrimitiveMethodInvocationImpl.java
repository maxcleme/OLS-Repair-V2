package fr.inria.lille.spirals.repair.expression;


import com.sun.jdi.Value;

import java.util.List;

/**
 *
 *
 *
 */

public class PrimitiveMethodInvocationImpl extends MethodInvocationImpl implements PrimitiveMethodInvocation {
    /**
     *
     */
    public PrimitiveMethodInvocationImpl(String method, String declaringType, Expression expression, List<Expression> parameters, Value jdiValue, Object value, Class type) {
        super(method, declaringType, expression, parameters, jdiValue, value, type);
    }
}

