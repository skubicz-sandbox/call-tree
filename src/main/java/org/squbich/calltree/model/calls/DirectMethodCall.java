package org.squbich.calltree.model.calls;

import lombok.Builder;

import org.apache.commons.lang3.StringUtils;
import org.squbich.calltree.model.code.Method;

import java.util.List;

/**
 * Created by Szymon on 2017-07-29.
 */
//@Getter
public class DirectMethodCall extends MethodCall {

    @Builder
    protected DirectMethodCall(final String expression, final Method method, final List<MethodCall> children) {
        super(expression, method, children);
    }

    @Override
    public String toString() {
        return getExpression();
    }

    @Override
    public String printTree(String offset) {
        StringBuilder tree = new StringBuilder();
        tree.append(offset);
        //    tree.append(method);
        tree.append(StringUtils.deleteWhitespace(getExpression()));
        if (getChildren() != null) {
            getChildren().forEach(execution -> {
                tree.append("\n");
            //    tree.append(offset);
                tree.append(execution.printTree(offset + ClassCaller.IDENT));
            //    tree.append("\n");
            });
        }
        return tree.toString();
    }
}
