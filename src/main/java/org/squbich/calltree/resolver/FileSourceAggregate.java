package org.squbich.calltree.resolver;

import com.google.common.collect.Lists;
import org.squbich.calltree.model.code.JavaFile;

/**
 * Created by Szymon on 2017-07-26.
 */
public class FileSourceAggregate extends SourceAggregate {
    public FileSourceAggregate(final String name, final JavaFile source) {
        super(name, Lists.newArrayList(source));
    }
}
