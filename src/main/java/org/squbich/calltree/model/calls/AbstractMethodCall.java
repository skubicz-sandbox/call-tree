package org.squbich.calltree.model.calls;

import lombok.Builder;

import org.squbich.calltree.model.code.Method;

import java.util.List;

/**
 * Created by Szymon on 2017-07-29.
 */
//@Getter
public class AbstractMethodCall extends MethodCall {

    @Builder
    protected AbstractMethodCall(final String expression,
            final Method method, final List<MethodCall> implementationCalls) {
        super(expression, method, implementationCalls);
    }

    @Override
    public String printTree(String offset) {
        StringBuilder tree = new StringBuilder();
        //        tree.append(offset);
        //        tree.append(getExpression());
        //
        tree.append(offset + "[implementations]");
        if (getChildren() != null) {
            getChildren().forEach(execution -> {
                tree.append("\n");
                //   tree.append(offset);
                tree.append(execution.printTree(offset));
                //    tree.append("\n");
            });
        }
        return tree.toString();
    }

}