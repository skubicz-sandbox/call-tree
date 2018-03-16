package org.squbich.calltree.browser;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.squbich.calltree.model.code.QualifiedName;
import org.squbich.calltree.model.executions.Execution;
import org.squbich.calltree.model.executions.MethodRoot;

public class ExecutionBrowser {
    private List<Execution> executions;

    private ExecutionBrowser(final List<Execution> executions) {
        this.executions = executions;
    }

    public static ExecutionBrowser of(final List<Execution> executions) {
        return new ExecutionBrowser(executions == null ? new ArrayList<>() : executions);
    }

    public static ExecutionBrowser of(final MethodRoot methodRoot) {
        return of(methodRoot.getExecutions());
    }

    public List<Execution> findByParentClass(QualifiedName parentClassName) {
        List<Execution> result = filterExecutionsRecursive(byParentClassNamePredicate(parentClassName));
        return result;
    }

    public List<Execution> findByParentClass(String parentClassNameRegexp) {
        List<Execution> result = filterExecutionsRecursive(byParentClassNameRegexpPredicate(parentClassNameRegexp));
        return result;
    }

    public List<Execution> findByMethodName(String methodName) {
        List<Execution> result = filterExecutionsRecursive(byMethodNamePredicate(methodName));
        return result;
    }

    public List<Execution> findByMethodAnnotation(QualifiedName annotationType) {
        List<Execution> result = filterExecutionsRecursive(byMethodAnnotationPredicate(annotationType));
        return result;
    }

    private List<Execution> filterExecutionsRecursive(Predicate<Execution> predicate) {
        return filterExecutionsRecursive(executions, predicate);
    }

    private List<Execution> filterExecutionsRecursive(List<Execution> executions, Predicate<Execution> predicate) {
        List<Execution> childrenFoundExecutions = new ArrayList<>();
        executions.forEach(execution -> {
            if (execution.getChildren() != null) {
                childrenFoundExecutions.addAll(filterExecutionsRecursive(execution.getChildren(), predicate));
            }
        });

        List<Execution> allFoundExecutions = executions.stream().filter(predicate).collect(Collectors.toList());
        allFoundExecutions.addAll(childrenFoundExecutions);
        return allFoundExecutions;
    }

    private Predicate<Execution> byMethodAnnotationPredicate(QualifiedName annotationType) {
        return (Execution execution) -> {
            if (execution.getMethod().getAnnotations() == null) {
                return false;
            }
            return execution.getMethod().getAnnotations().stream().filter(type -> type.equals(annotationType)).findFirst().isPresent();
        };
    }

    private Predicate<Execution> byMethodNamePredicate(String methodName) {
        return (Execution execution) -> execution.getMethod().getName().equals(methodName);
    }

    private Predicate<Execution> byParentClassNameRegexpPredicate(String parentClassNameRegexp) {
        return (Execution execution) -> execution.getMethod().getParentClass().getQualifiedName().toString().matches(parentClassNameRegexp);
    }

    private Predicate<Execution> byParentClassNamePredicate(QualifiedName param) {
        return (Execution execution) -> execution.getMethod().getParentClass().isEquals(param);
    }

}