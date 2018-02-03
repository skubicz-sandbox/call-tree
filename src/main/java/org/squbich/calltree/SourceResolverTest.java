package org.squbich.calltree;

import com.google.common.collect.Lists;
import org.junit.Test;
import org.squbich.calltree.model.code.JavaFile;
import org.squbich.calltree.model.code.QualifiedName;
import org.squbich.calltree.resolver.CallHierarchy;
import org.squbich.calltree.resolver.SourceAggregate;
import org.squbich.calltree.resolver.SourceResolver;
import org.squbich.calltree.resolver.TypeResolver;

import java.io.File;
import java.util.List;

/**
 * Created by Szymon on 2017-07-27.
 */
public class SourceResolverTest {


    public static TypeResolver typeResolver = null;
    @Test
    public void test() {
        File file03 = new File("D:\\Programming\\workspace\\example-web\\src\\main\\java");

        SourceResolver sourceResolver = new SourceResolver();
 //       List<SourceAggregate> sourceAggregates = sourceResolver.solve(Lists.newArrayList(src, jar, jar02, jar03, jar04));
        List<SourceAggregate> sourceAggregates = sourceResolver.solve(Lists.newArrayList(file03));
        System.out.println(sourceAggregates);

      typeResolver = new TypeResolver(sourceAggregates, Lists.newArrayList());
//        System.out.println(typeResolver.findImplementations(tmp));

//                .build();
        JavaFile impl = JavaFile.builder().qualifiedName(QualifiedName.of("org.sk.example.exampleweb.TmpEndpoint"))
                .build();
        CallHierarchy callHierarchy = new CallHierarchy(typeResolver);
        Object out = callHierarchy.resolveHierarchy2(impl);
        System.out.println(out);

    }
}
