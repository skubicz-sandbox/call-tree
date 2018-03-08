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

    @Override
    public  List<Execution> getChildren() {
        return (List<Execution>) implementationExecutions;
    }
    @Override
    public String printTree(String offset) {
        StringBuilder tree = new StringBuilder();
//        tree.append(offset);
//        tree.append(getCallExpression());
//
        tree.append(offset + "[implementations]");
        if (implementationExecutions != null) {
            implementationExecutions.forEach(execution -> {
                tree.append("\n");
             //   tree.append(offset);
                tree.append(execution.printTree(offset));
            //    tree.append("\n");
            });
        }
        return tree.toString();
    }

    //    public static class AbstractMethodExecutionBuilder extends MethodExecutionBuilder {
//        AbstractMethodExecutionBuilder() {
//            super();
//        }
//    }
}