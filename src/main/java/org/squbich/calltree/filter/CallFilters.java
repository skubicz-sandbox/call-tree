package org.squbich.calltree.filter;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.squbich.calltree.model.calls.MethodCall;
import org.squbich.calltree.model.calls.MethodCaller;

public class CallFilters {
    private List<MethodCall> calls;

    private CallFilters(final List<MethodCall> calls) {
        this.calls = calls;
    }

    public static CallFilters of(final List<MethodCall> calls) {
        return new CallFilters(calls == null ? new ArrayList<>() : calls);
    }

    public static CallFilters of(final MethodCaller methodCaller) {
        return of(methodCaller.getCalls());
    }

    public List<MethodCall> byParentClass(String parentClassName) {
        List<MethodCall> result = filterCallsRecursive(FilterType.BY_PARENT_CLASS_NAME.predicate(parentClassName));
        return result;
    }

    public List<MethodCall> byParentClassRegexp(String parentClassNameRegexp) {
        List<MethodCall> result = filterCallsRecursive(FilterType.BY_PARENT_CLASS_NAME_REGEXP.predicate(parentClassNameRegexp));
        return result;
    }

    public List<MethodCall> byMethodName(String methodName) {
        List<MethodCall> result = filterCallsRecursive(FilterType.BY_METHOD.predicate(methodName));
        return result;
    }

    public List<MethodCall> byMethodAnnotation(String annotationType) {
        List<MethodCall> result = filterCallsRecursive(FilterType.BY_METHOD_ANNOTATION.predicate(annotationType));
        return result;
    }

    public List<MethodCall> filter(Predicate<MethodCall> predicate) {
        if (predicate == null) {
            return calls;
        }
        return filterCallsRecursive(calls, predicate);
    }

    private List<MethodCall> filterCallsRecursive(Predicate<MethodCall> predicate) {
        return filterCallsRecursive(calls, predicate);
    }

    private List<MethodCall> filterCallsRecursive(List<MethodCall> calls, Predicate<MethodCall> predicate) {
        List<MethodCall> foundCalls = calls.stream().filter(predicate).collect(Collectors.toList());

        List<MethodCall> allChildrenCalls = new ArrayList<>();
        calls.forEach(execution -> {
            if (execution.getChildren() != null) {
                List<MethodCall> childrenCalls = filterCallsRecursive(execution.getChildren(), predicate);
                allChildrenCalls.addAll(childrenCalls);

                execution.getChildren().clear();
                execution.getChildren().addAll(childrenCalls);
            }
        });

        if (foundCalls.isEmpty()) {
            return allChildrenCalls;
        }
        //  allFoundExecutions.addAll(childrenFoundExecutions);
        return foundCalls;
    }


}