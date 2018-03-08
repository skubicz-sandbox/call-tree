package org.squbich.calltree.model.executions;

import lombok.Getter;
import org.squbich.calltree.model.code.Method;

import java.util.List;

/**
 * Created by Szymon on 2017-07-29.
 */
@Getter
public abstract class Execution implements Executable {
    private String callExpression;
 //   private Method method;

    public Execution(String callExpression, Method method) {
        this.callExpression = callExpression;
      //  this.method = method;
    }

    public String printTree(String offset) {
        return offset + callExpression;
    }

    public abstract List<Execution> getChildren();

    @Override
    public String toString() {
        return callExpression;
    }
}