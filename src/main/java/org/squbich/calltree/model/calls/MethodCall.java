package org.squbich.calltree.model.calls;

import lombok.Getter;

import org.apache.commons.lang3.StringUtils;
import org.squbich.calltree.serialize.JsonFilters;
import org.squbich.calltree.model.code.Method;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonFilter;

/**
 * Created by Szymon on 2017-07-29.
 */
@Getter
@JsonFilter(JsonFilters.EXECUTION)
public abstract class MethodCall {
    private String expression;
    private Method method;
    private List<MethodCall> children;

    public MethodCall(String expression, Method method, List<MethodCall> children) {
        this.expression = expression;
        this.method = method;
        this.children = children;
    }

    public String printTree(String offset) {
        return offset + StringUtils.deleteWhitespace(expression);
    }

    @Override
    public String toString() {
        return expression;
    }
}