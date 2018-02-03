package org.squbich.calltree.resolver;

import org.squbich.calltree.model.code.JavaFile;

import java.util.List;

/**
 * Created by Szymon on 2017-07-26.
 */
public class JarSourceAggregate extends SourceAggregate {
    public JarSourceAggregate(final String name, final List<JavaFile> sources) {
        super(name, sources);
    }
}
