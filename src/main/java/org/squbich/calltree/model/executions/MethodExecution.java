package org.squbich.calltree.model.executions;

import lombok.Builder;
import lombok.Getter;
import org.squbich.calltree.model.code.Method;

import java.util.List;

/**
 * Created by Szymon on 2017-07-29.
 */
@Getter
public class MethodExecution extends Execution {
//    private String callExpression;
    private Method method;
    private List<Execution> executions;

    @Builder
    protected MethodExecution(final String callExpression, final List<Execution> executions, final Method method) {
        super(callExpression, method);
        this.executions = executions;
//        this.callExpression = callExpression;
        this.method = method;
    }

    @Override
    public String toString() {
        return getCallExpression();
    }

    @Override
    public String printTree(String offset) {
        StringBuilder tree = new StringBuilder();
        tree.append(offset);
        //    tree.append(method);
        tree.append(getCallExpression());
        if (getExecutions() != null) {
            getExecutions().forEach(execution -> {
                tree.append("\n");
            //    tree.append(offset);
                tree.append(execution.printTree(offset + ClassRoot.IDENT));
            //    tree.append("\n");
            });
        }
        return tree.toString();
    }
}
