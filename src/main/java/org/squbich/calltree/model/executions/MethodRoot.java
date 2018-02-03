package org.squbich.calltree.model.executions;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.squbich.calltree.model.code.Method;

import java.util.List;

/**
 * Created by Szymon on 2017-07-29.
 */
@Getter
@AllArgsConstructor
public class MethodRoot implements Executable {
    private Method method;
    private List<Execution> executions;
}
