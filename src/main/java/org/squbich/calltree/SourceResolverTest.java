package org.squbich.calltree;

import com.google.common.collect.Lists;
import j2html.tags.ContainerTag;
import org.junit.Test;
import org.squbich.calltree.model.code.JavaFile;
import org.squbich.calltree.model.code.QualifiedName;
import org.squbich.calltree.model.executions.ClassRoot;
import org.squbich.calltree.model.executions.Execution;
import org.squbich.calltree.model.executions.MethodExecution;
import org.squbich.calltree.resolver.CallHierarchy;
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
        if(executions == null) {
            return null;
        }
        for(Execution execution : executions) {
            if(execution instanceof MethodExecution) {
                System.out.println(((MethodExecution)execution).getMethod().getParentClass().getQualifiedName().getNamePart());
                if(((MethodExecution)execution).getMethod().getParentClass().getQualifiedName().getNamePart().contains(parentClassName)) {
                    return execution;
                }
            }
            else {
                return find(execution.getChildren(), parentClassName);
            }
        }
        return null;
    }

    @Test
    public void test() {
        File file01 = new File("D:\\Programming\\workspace\\example-web\\src\\main\\java");
        File file02 = new File("D:\\Programming\\workspace\\example-integration\\src\\main\\java");

        SourceResolver sourceResolver = new SourceResolver();
 //       List<SourceAggregate> sourceAggregates = sourceResolver.solve(Lists.newArrayList(src, jar, jar02, jar03, jar04));
        List<SourceAggregate> sourceAggregates = sourceResolver.solve(Lists.newArrayList(file01, file02));
        System.out.println(sourceAggregates);

      typeResolver = new TypeResolver(sourceAggregates, Lists.newArrayList());
//        System.out.println(typeResolver.findImplementations(tmp));

//                .build();
        JavaFile impl = JavaFile.builder().qualifiedName(QualifiedName.of("org.sk.example.exampleweb.TmpEndpoint"))
                .build();
        CallHierarchy callHierarchy = new CallHierarchy(typeResolver);
        ClassRoot out = callHierarchy.resolveHierarchy(impl);

        ContainerTag ul = ul();

        List<ContainerTag> liList = new ArrayList<>();
        out.getMethods().forEach(methodRoot -> {
            ContainerTag p = p(methodRoot.getMethod().getComment());

            Execution execution = find(methodRoot.getExecutions(), "Adapter");

            ContainerTag p1 = null;
            if(execution != null) {
                p1 = p(((MethodExecution)execution).getMethod().getComment());
            }

            liList.add(li(p, p1));
        });
        System.out.println(out);


        System.out.println(html(body(liList.toArray(new ContainerTag[]{}))).renderFormatted());




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
