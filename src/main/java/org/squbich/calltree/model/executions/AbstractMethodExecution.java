package org.squbich.calltree.model.executions;

import lombok.Builder;
import lombok.Getter;
import org.squbich.calltree.model.code.Method;

import java.util.List;

/**
 * Created by Szymon on 2017-07-29.
 */
@Getter
public class AbstractMethodExecution extends Execution {
    private List<? extends Execution> implementationExecutions;
    private Method method;

    @Builder
    protected AbstractMethodExecution(final String callExpression, final List<? extends Execution> implementationExecutions, final Method method) {
        super(callExpression,  method);
        this.implementationExecutions = implementationExecutions;
        this.method = method;
    }

//    public static class AbstractMethodExecutionBuilder extends MethodExecutionBuilder {
//        AbstractMethodExecutionBuilder() {
//            super();
//        }
//    }
}