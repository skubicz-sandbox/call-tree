package org.squbich.calltree.filter;

import java.util.function.Predicate;

import org.squbich.calltree.model.calls.MethodCall;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class FilterConfiguration {
    private FilterType type;
    private String value;

    public Predicate<MethodCall> getPredicate() {
        if (type == null || value == null) {
            return null;
        }
        return type.predicate(value);
    }
}