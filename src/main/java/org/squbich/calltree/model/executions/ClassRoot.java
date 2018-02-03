package org.squbich.calltree.model.executions;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.squbich.calltree.model.code.QualifiedName;

import java.util.List;

/**
 * Created by Szymon on 2017-07-29.
 */
@Getter
@AllArgsConstructor
public class ClassRoot implements Executable {
    private QualifiedName className;
    private List<MethodRoot> methods;
}
