package org.squbich.calltree.model.executions;

import lombok.Builder;
import lombok.Getter;
import org.squbich.calltree.model.code.Method;

import java.util.List;

/**
 * Created by Szymon on 2017-07-29.
 */
@Getter
public class ImplementationOfAbstractMethodExecution extends MethodExecution {
    private Method implementedMethod;
  //  private Method method;

    @Builder
    protected ImplementationOfAbstractMethodExecution(final String callExpression, final List<Execution> executions, final Method method,
                                                      final Method implementedMethod) {
        super(callExpression, executions, method);
        this.implementedMethod = implementedMethod;
     //   this.method = method;
    }

    public static class ImplementationOfAbstractMethodExecutionBuilder extends MethodExecutionBuilder {
        ImplementationOfAbstractMethodExecutionBuilder() {
            super();
        }
    }
}