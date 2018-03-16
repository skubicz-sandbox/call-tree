package org.squbich.calltree.model.executions;

import lombok.AllArgsConstructor;
import lombok.Getter;

import org.squbich.calltree.browser.ExecutionBrowser;
import org.squbich.calltree.browser.JsonFilters;
import org.squbich.calltree.model.code.Method;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonFilter;

/**
 * Created by Szymon on 2017-07-29.
 */
@Getter
@AllArgsConstructor
@JsonFilter(JsonFilters.METHOD_ROOT)
public class MethodRoot {
    private Method method;
    private List<Execution> executions;

    public ExecutionBrowser browser() {
        return ExecutionBrowser.of(executions);
    }

    public String printTree(String offset) {
        StringBuilder tree = new StringBuilder();
        tree.append(offset);
        tree.append(method);
        if (executions != null) {
            executions.forEach(execution -> {
                tree.append("\n");
             //   tree.append(offset);
                tree.append(execution.printTree(offset + ClassRoot.IDENT));
             //   tree.append("\n");
            });
        }
        tree.append("\n");
        return tree.toString();
    }
}
