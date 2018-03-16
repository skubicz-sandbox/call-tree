package org.squbich.calltree.resolver;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.resolution.types.ResolvedType;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.squbich.calltree.model.code.ClassDescriptor;
import org.squbich.calltree.model.code.JavaFile;
import org.squbich.calltree.model.code.Method;
import org.squbich.calltree.model.code.QualifiedName;
import org.squbich.calltree.model.executions.ClassRoot;
import org.squbich.calltree.model.executions.Execution;
import org.squbich.calltree.model.executions.MethodRoot;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Created by Szymon on 2017-07-28.
 */
@AllArgsConstructor
@Slf4j
public class CallHierarchy {
    private TypeResolver typeResolver;
    private String allowedPackagePattern;

    public List<ClassRoot> resolveHierarchy(final String packageName) {
        List<JavaFile> classes = typeResolver.findClassesInPackage(packageName);

        List<ClassRoot> classRoots = classes.stream().map(this::resolveHierarchy).filter(Objects::nonNull).collect(Collectors.toList());

        return classRoots;
    }

    public ClassRoot resolveHierarchy(final JavaFile javaFile) {
        try {
            ClassOrInterfaceDeclaration declaration = typeResolver.toDeclaration(javaFile);
            List<MethodRoot> methods = new ArrayList<>();
            declaration.getMethods().forEach(bodyDeclaration -> {
                if (bodyDeclaration != null) {
                    List<Execution> calls = bodyDeclaration.accept(new HierarchyVisitor(typeResolver, allowedPackagePattern), null);

                    Method method = typeResolver.toMethod(bodyDeclaration);
                    MethodRoot methodRoot = new MethodRoot(method, calls);
                    methods.add(methodRoot);
                }
            });
            ClassRoot classRoot = new ClassRoot(javaFile.getQualifiedName(), methods);

            return classRoot;
        }
        catch (Exception e) {
            log.warn(e.getLocalizedMessage());
            return null;
        }
    }
}
