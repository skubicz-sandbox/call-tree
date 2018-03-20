package org.squbich.calltree.model.calls;

import lombok.Builder;
import lombok.Getter;

import org.squbich.calltree.model.code.Method;

import java.util.List;

/**
 * Created by Szymon on 2017-07-29.
 */
@Getter
public class ImplementationOfAbstractMethodCall extends MethodCall {
    private Method implementedMethod;

    @Builder
    protected ImplementationOfAbstractMethodCall(final String expression, final Method method, final List<MethodCall> children,
            final Method implementedMethod) {
        super(expression, method, children);
        this.implementedMethod = implementedMethod;
    }

}