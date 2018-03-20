package org.squbich.calltree;

import com.google.common.collect.Lists;

import j2html.tags.ContainerTag;

import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.squbich.calltree.filter.CallFilters;
import org.squbich.calltree.model.calls.CallHierarchy;
import org.squbich.calltree.model.code.JavaFile;
import org.squbich.calltree.model.code.QualifiedName;
import org.squbich.calltree.model.calls.ClassCaller;
import org.squbich.calltree.resolver.CallHierarchyResolver;
import org.squbich.calltree.resolver.CompiledAggregate;
import org.squbich.calltree.resolver.SourceAggregate;
import org.squbich.calltree.resolver.SourceResolver;
import org.squbich.calltree.resolver.TypeResolver;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;

import static j2html.TagCreator.*;

/**
 * Created by Szymon on 2017-07-27.
 */
public class SourceResolverTest {

}
