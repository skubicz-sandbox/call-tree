package org.squbich.calltree.model.calls;

import java.util.List;

import org.squbich.calltree.model.calls.ClassCaller;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(staticName = "of")
public class CallHierarchy {
    private List<ClassCaller> callers;

}