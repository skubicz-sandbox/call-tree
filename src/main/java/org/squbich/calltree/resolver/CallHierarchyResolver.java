package org.squbich.calltree.resolver;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.google.common.collect.Lists;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.squbich.calltree.model.calls.CallHierarchy;
import org.squbich.calltree.model.calls.MethodCall;
import org.squbich.calltree.model.code.JavaFile;
import org.squbich.calltree.model.code.Method;
import org.squbich.calltree.model.calls.ClassCaller;
import org.squbich.calltree.model.calls.MethodCaller;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Created by Szymon on 2017-07-28.
 */
@AllArgsConstructor
@Slf4j
public class CallHierarchyResolver {
    private TypeResolver typeResolver;
    private String allowedPackagePattern;

    public CallHierarchy resolveHierarchy(final String packageName) {
        List<JavaFile> classes = typeResolver.findClassesInPackage(packageName);

        List<ClassCaller> classCallers = classes.stream().map(this::resolve).filter(Objects::nonNull).collect(Collectors.toList());

        return CallHierarchy.of(classCallers);
    }

    public CallHierarchy resolveHierarchy(final JavaFile javaFile) {
        return CallHierarchy.of(Lists.newArrayList(resolve(javaFile)));
    }

    private ClassCaller resolve(final JavaFile javaFile) {
        try {
            ClassOrInterfaceDeclaration declaration = typeResolver.toDeclaration(javaFile);
            List<MethodCaller> methods = new ArrayList<>();
            declaration.getMethods().forEach(bodyDeclaration -> {
                if (bodyDeclaration != null) {
                    List<MethodCall> calls = bodyDeclaration.accept(new HierarchyVisitor(typeResolver, allowedPackagePattern), null);

                    Method method = typeResolver.toMethod(bodyDeclaration);
                    MethodCaller methodCaller = new MethodCaller(method, calls);
                    methods.add(methodCaller);
                }
            });
            ClassCaller classCaller = new ClassCaller(javaFile.getQualifiedName(), methods);

            return classCaller;
        }
        catch (Exception e) {
            log.warn(e.getLocalizedMessage());
            return null;
        }
    }
}
