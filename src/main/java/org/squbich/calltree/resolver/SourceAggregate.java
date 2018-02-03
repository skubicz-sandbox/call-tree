package org.squbich.calltree.resolver;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.squbich.calltree.model.code.JavaFile;

import java.util.List;

/**
 * Created by Szymon on 2017-07-26.
 */
@Getter
@AllArgsConstructor
public class SourceAggregate {
    private String name;
    private List<JavaFile> sources;
}