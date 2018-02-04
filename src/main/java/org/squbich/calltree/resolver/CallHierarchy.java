package org.squbich.calltree.resolver;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.resolution.types.ResolvedType;
import lombok.AllArgsConstructor;
import org.squbich.calltree.model.code.ClassDescriptor;
import org.squbich.calltree.model.code.JavaFile;
import org.squbich.calltree.model.code.Method;
import org.squbich.calltree.model.code.QualifiedName;
import org.squbich.calltree.model.executions.ClassRoot;
import org.squbich.calltree.model.executions.Execution;
import org.squbich.calltree.model.executions.MethodRoot;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Szymon on 2017-07-28.
 */
@AllArgsConstructor
public class CallHierarchy {
    private TypeResolver typeResolver;

    public Object resolveHierarchy(final JavaFile javaFile) {
        ClassOrInterfaceDeclaration declaration = typeResolver.toDeclaration(javaFile);
        List<MethodRoot> methods = new ArrayList<>();
        declaration.getMethods().forEach(bodyDeclaration -> {
            if (bodyDeclaration != null) {
                List<Execution> calls = bodyDeclaration.accept(new HierarchyVisitor(typeResolver), null);

                ResolvedType type = typeResolver.getJavaParserFacade().getTypeOfThisIn(declaration);

                ClassDescriptor parent = null;
                if (type.isReferenceType()) {
                    //   method.klass = type.asReferenceType().getQualifiedName();
                    QualifiedName className = QualifiedName.of(type.asReferenceType().getQualifiedName());
                    String comment = "";
                    ClassOrInterfaceDeclaration classOrInterfaceDeclaration = typeResolver.toClassOrInterfaceDeclaration(type.asReferenceType().getTypeDeclaration());
                    if (classOrInterfaceDeclaration != null) {
                        if (classOrInterfaceDeclaration.getComment().isPresent()) {
                            comment = classOrInterfaceDeclaration.getComment().get().getContent();
                        }
                    }
                    parent = ClassDescriptor.builder().qualifiedName(className).comment(comment).build();
                }

                Method method = Method.builder().name(bodyDeclaration.getNameAsString()).parentClass(parent).build();
                MethodRoot methodRoot = new MethodRoot(method, calls);
                methods.add(methodRoot);


            }
        });
        //List<?> calls = (List) declaration.getMembers().accept(this, null);

        ClassRoot classRoot = new ClassRoot(javaFile.getQualifiedName(), methods);
        System.out.println("-----------");
        System.out.println(classRoot.printTree(""));
        System.out.println("-----------");

        return classRoot;
    }
}
