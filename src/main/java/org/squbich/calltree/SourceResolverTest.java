package org.squbich.calltree;

import com.google.common.collect.Lists;

import j2html.tags.ContainerTag;

import org.junit.Test;
import org.squbich.calltree.browser.ExecutionBrowser;
import org.squbich.calltree.model.code.JavaFile;
import org.squbich.calltree.model.code.QualifiedName;
import org.squbich.calltree.model.executions.ClassRoot;
import org.squbich.calltree.model.executions.Execution;
import org.squbich.calltree.model.executions.MethodExecution;
import org.squbich.calltree.resolver.CallHierarchy;
import org.squbich.calltree.resolver.CompiledAggregate;
import org.squbich.calltree.resolver.SourceAggregate;
import org.squbich.calltree.resolver.SourceResolver;
import org.squbich.calltree.resolver.TypeResolver;

import java.io.File;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

import static j2html.TagCreator.*;

/**
 * Created by Szymon on 2017-07-27.
 */
public class SourceResolverTest {


    public static TypeResolver typeResolver = null;

    private Execution find(List<Execution> executions, String parentClassName) {
        if (executions == null) {
            return null;
        }
        for (Execution execution : executions) {
            if (execution instanceof MethodExecution) {
                try {
                    if (execution.getMethod().getParentClass().getQualifiedName().getNamePart().contains(parentClassName)) {
                        return execution;
                    }
                }
                catch (Exception e) {
                    return null;
                }
            }
            else {
                return find(execution.getChildren(), parentClassName);
            }
        }
        return null;
    }


    @Test
    public void test2() {
        File file01 = new File("D:\\BZWBK\\NCP\\workspace\\c2arm-stub-server\\src\\main\\java");
        File file02 = new File("D:\\BZWBK\\NCP\\workspace\\c2arm-demo-common\\src\\main\\java");
        //   File file02 = new File("D:\\Programming\\workspace\\example-integration\\src\\main\\java");

        SourceResolver sourceResolver = new SourceResolver();
        //       List<SourceAggregate> sourceAggregates = sourceResolver.solve(Lists.newArrayList(src, jar, jar02, jar03, jar04));
        List<SourceAggregate> sourceAggregates = sourceResolver.solve(Lists.newArrayList(file01, file02));
        System.out.println(sourceAggregates);

        typeResolver = new TypeResolver(sourceAggregates, Lists.newArrayList());
        CallHierarchy callHierarchy = new CallHierarchy(typeResolver, "com.squbich");
        List<ClassRoot> out = callHierarchy.resolveHierarchy("org.tmp.endpoints");


        System.out.println(out.get(0).printTree(""));

    }

    @Test
    public void test() {
        File file01 = new File("D:\\BZWBK\\NCP\\workspace\\c2arm-stub-server\\src\\main\\java");
        File file02 = new File("D:\\BZWBK\\NCP\\workspace\\c2arm-demo-common\\src\\main\\java");
        //   File file02 = new File("D:\\Programming\\workspace\\example-integration\\src\\main\\java");

        SourceResolver sourceResolver = new SourceResolver();
        //       List<SourceAggregate> sourceAggregates = sourceResolver.solve(Lists.newArrayList(src, jar, jar02, jar03, jar04));
        List<SourceAggregate> sourceAggregates = sourceResolver.solve(Lists.newArrayList(file01, file02));
        System.out.println(sourceAggregates);

        CompiledAggregate aggregate = new CompiledAggregate();
        aggregate.setName("D:\\BZWBK\\NCP\\repository\\com\\google\\guava\\guava\\19.0\\guava-19.0.jar");
        CompiledAggregate aggregate2 = new CompiledAggregate();
        aggregate2.setName("D:\\BZWBK\\NCP\\repository\\javax\\ws\\rs\\javax.ws.rs-api\\2.0.1\\javax.ws.rs-api-2.0.1.jar");
        typeResolver = new TypeResolver(sourceAggregates, Lists.newArrayList(aggregate, aggregate2));
        //        System.out.println(typeResolver.findImplementations(tmp));

        //                .build();
        JavaFile impl = JavaFile.builder().qualifiedName(QualifiedName.of("org.tmp.endpoints.events.EventsEndpoint"))
                .build();
        CallHierarchy callHierarchy = new CallHierarchy(typeResolver, "");
        ClassRoot out = callHierarchy.resolveHierarchy(impl);

        ExecutionBrowser.of(out.getMethods().get(1)).findByParentClass(".*Adapter");
        ExecutionBrowser.of(out.getMethods().get(1)).findByMethodName("getIfPresent");

        ContainerTag ul = ul();




        //        try {
        //          //  InetAddress addr = InetAddress.getByName("a");
        //            InetAddress addr = InetAddress.getByName("192.168.1.9");
        //            String host = addr.getHostName();
        //            System.out.println(addr.getCanonicalHostName());
        //            System.out.println(addr.getHostAddress());
        //        }catch (Exception e) {
        //            e.printStackTrace();
        //        }

    }
}
