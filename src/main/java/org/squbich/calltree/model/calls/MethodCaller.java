package org.squbich.calltree.model.calls;

import lombok.AllArgsConstructor;
import lombok.Getter;

import org.squbich.calltree.filter.CallFilters;
import org.squbich.calltree.serialize.JsonFilters;
import org.squbich.calltree.model.code.Method;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonFilter;

/**
 * Created by Szymon on 2017-07-29.
 */
@Getter
@AllArgsConstructor
@JsonFilter(JsonFilters.METHOD_ROOT)
public class MethodCaller {
    private Method method;
    private List<MethodCall> calls;

    public CallFilters browser() {
        return CallFilters.of(calls);
    }

    public String printTree(String offset) {
        StringBuilder tree = new StringBuilder();
        tree.append(offset);
        tree.append(method);
        if (calls != null) {
            calls.forEach(execution -> {
                tree.append("\n");
             //   tree.append(offset);
                tree.append(execution.printTree(offset + ClassCaller.IDENT));
             //   tree.append("\n");
            });
        }
        tree.append("\n");
        return tree.toString();
    }
}
