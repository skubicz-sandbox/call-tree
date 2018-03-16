package org.squbich.calltree.model.executions;

import lombok.Getter;

import org.apache.commons.lang3.StringUtils;
import org.squbich.calltree.browser.JsonFilters;
import org.squbich.calltree.model.code.Method;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonFilter;

/**
 * Created by Szymon on 2017-07-29.
 */
@Getter
@JsonFilter(JsonFilters.EXECUTION)
public abstract class Execution implements Executable {
    private String callExpression;
    private Method method;

    public Execution(String callExpression, Method method) {
        this.callExpression = callExpression;
        this.method = method;
    }

    public String printTree(String offset) {
        return offset + StringUtils.deleteWhitespace(callExpression);
    }

    public abstract List<Execution> getChildren();

    @Override
    public String toString() {
        return callExpression;
    }
}