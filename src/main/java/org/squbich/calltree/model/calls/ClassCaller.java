package org.squbich.calltree.model.calls;

import lombok.AllArgsConstructor;
import lombok.Getter;

import org.squbich.calltree.serialize.JsonFilters;
import org.squbich.calltree.model.code.QualifiedName;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonFilter;

/**
 * Created by Szymon on 2017-07-29.
 */
@Getter
@AllArgsConstructor
@JsonFilter(JsonFilters.CLASS_ROOT)
public class ClassCaller {
    public static final String IDENT = "    ";
    private QualifiedName className;
    private List<MethodCaller> methods;

    public String printTree(String offset) {
        StringBuilder tree = new StringBuilder();
        tree.append(offset);
        tree.append(className);
        tree.append("\n");
        if (methods != null) {
            methods.forEach(methodCaller -> {
            //    tree.append(offset);
                tree.append(methodCaller.printTree(offset + IDENT));
             //   tree.append("\n");
            });
        }
        return tree.toString();
    }
}