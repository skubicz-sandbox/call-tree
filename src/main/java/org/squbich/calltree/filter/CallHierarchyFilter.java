package org.squbich.calltree.filter;

import java.util.ArrayList;
import java.util.List;

import org.squbich.calltree.model.calls.MethodCall;
import org.squbich.calltree.model.calls.CallHierarchy;
import org.squbich.calltree.model.calls.ClassCaller;
import org.squbich.calltree.model.calls.MethodCaller;

import lombok.AllArgsConstructor;

@AllArgsConstructor(staticName = "of")
public class CallHierarchyFilter {
    private FilterConfiguration configuration;

    public CallHierarchy filter(CallHierarchy callHierarchy) {
        if (configuration == null || callHierarchy == null || callHierarchy.getCallers() == null) {
            return callHierarchy;
        }
        List<ClassCaller> filtered = new ArrayList<>();

        callHierarchy.getCallers().forEach(classRoot -> {
            List<MethodCaller> methods = new ArrayList<>();
            classRoot.getMethods().forEach(methodRoot -> {
                CallFilters executionBrowser = CallFilters.of(methodRoot);

                List<MethodCall> filteredExecutions = executionBrowser.filter(configuration.getPredicate());

                methods.add(new MethodCaller(methodRoot.getMethod(), filteredExecutions));
            });

            filtered.add(new ClassCaller(classRoot.getClassName(), methods));
        });

        return CallHierarchy.of(filtered);
    }
}