package org.squbich.calltree.resolver;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.comments.Comment;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.resolution.declarations.ResolvedMethodDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedReferenceTypeDeclaration;
import com.github.javaparser.resolution.types.ResolvedType;
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade;
import com.github.javaparser.symbolsolver.javaparsermodel.declarations.JavaParserClassDeclaration;
import com.github.javaparser.symbolsolver.javaparsermodel.declarations.JavaParserEnumDeclaration;
import com.github.javaparser.symbolsolver.javaparsermodel.declarations.JavaParserInterfaceDeclaration;
import com.github.javaparser.symbolsolver.model.typesystem.ReferenceTypeImpl;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JarTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;
import lombok.Getter;
import org.squbich.calltree.model.code.ClassDescriptor;
import org.squbich.calltree.model.code.JavaFile;
import org.squbich.calltree.model.code.Method;
import org.squbich.calltree.model.code.QualifiedName;
import org.squbich.calltree.model.executions.AbstractMethodExecution;
import org.squbich.calltree.model.executions.Execution;
import org.squbich.calltree.model.executions.ImplementationOfAbstractMethodExecution;
import org.squbich.calltree.model.executions.MethodRoot;
import org.squbich.calltree.tools.Assert;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


/**
 * Created by Szymon on 2017-07-27.
 */
public class TypeResolver {
    @Getter
    private CombinedTypeSolver combinedTypeSolver;
    @Getter
    private JavaParserFacade javaParserFacade;
    private List<JavaFile> sources;

    public TypeResolver(final List<SourceAggregate> sources, final List<CompiledAggregate> libraries) {
        Assert.notNull(sources, "Parameter sources must not be null.");
        Assert.notNull(libraries, "Parameter libraries must not be null.");

        this.combinedTypeSolver = new CombinedTypeSolver();
        this.combinedTypeSolver.add(new ReflectionTypeSolver());
        this.sources = new ArrayList<>();
        sources.forEach(sourceAggregate -> {
            if (sourceAggregate instanceof JarSourceAggregate) {
                this.combinedTypeSolver.add(new SourceJarTypeSolver(sourceAggregate.getName()));
            } else {
                this.combinedTypeSolver.add(new JavaParserTypeSolver(new File(sourceAggregate.getName())));
            }
            this.sources.addAll(sourceAggregate.getSources());
        });
        libraries.forEach(library -> {
            try {
                this.combinedTypeSolver.add(new JarTypeSolver(library.getName()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        this.javaParserFacade = JavaParserFacade.get(combinedTypeSolver);
    }

    public Method findMethod(final MethodCallExpr methodCall) {
        MethodDeclaration methodDeclaration = findMethodDeclaration(methodCall);
        if(methodDeclaration == null) {
            System.out.println("methodDeclaration null");
            return null;
        }
        ResolvedType resolvedReturnType = getJavaParserFacade().convertToUsage(methodDeclaration.getType());
        ResolvedReferenceTypeDeclaration callerType = findMethodCallerType(methodCall);
        ClassOrInterfaceDeclaration parent = toClassOrInterfaceDeclaration(callerType);
        String classComment = parent.getComment().map(Comment::getContent).orElse("");
        String methodComment = methodDeclaration.getComment().map(Comment::getContent).orElse("");

        ClassDescriptor parentClass = ClassDescriptor.builder().qualifiedName(QualifiedName.of(callerType.asReferenceType().getQualifiedName())).comment(classComment).build();
        List<QualifiedName> parameterTypes = resolveMethodParameterTypes(methodCall.getArguments());

        Method method = Method.builder()
                .parentClass(parentClass)
                .parameters(parameterTypes)
                .name(methodDeclaration.getNameAsString())
                .returnType(QualifiedName.of(resolvedReturnType.asReferenceType().getQualifiedName()))
                .comment(methodComment)
                .build();

        return method;
    }

    public Method toMethod(final MethodDeclaration methodDeclaration) {
        ResolvedType resolvedReturnType = getJavaParserFacade().convertToUsage(methodDeclaration.getType());
        ResolvedReferenceTypeDeclaration callerType = getJavaParserFacade().getTypeDeclaration(methodDeclaration.getParentNode().get());
        ClassOrInterfaceDeclaration parent = toClassOrInterfaceDeclaration(callerType);
        String classComment = parent.getComment().map(Comment::getContent).orElse("");
        String methodComment = methodDeclaration.getComment().map(Comment::getContent).orElse("");

        ClassDescriptor parentClass = ClassDescriptor.builder().qualifiedName(QualifiedName.of(callerType.asReferenceType().getQualifiedName())).comment(classComment).build();
        List<QualifiedName> parameterTypes = resolveMethodParameterTypes3(methodDeclaration.getParameters());

        Method method = Method.builder()
                .parentClass(parentClass)
                .parameters(parameterTypes)
                .name(methodDeclaration.getNameAsString())
                .returnType(QualifiedName.of(resolvedReturnType.asReferenceType().getQualifiedName()))
                .comment(methodComment)
                .build();

        return method;
    }

    public List<MethodDeclaration> findImplementationMethod(final Method method) {
        List<JavaFile> implementations = findImplementations(method.getParentClass().getQualifiedName());
        if (implementations == null) {
            return null;
        }

        List<MethodDeclaration> implementationMethod = new ArrayList<>();

        for (JavaFile javaFile : implementations) {
            ClassOrInterfaceDeclaration parentImplementation = toDeclaration(javaFile);

            MethodDeclaration methodDeclaration = findMethodDeclaration2(method.getName(), method.getParameters(), parentImplementation);
            if (methodDeclaration != null) {
                implementationMethod.add(methodDeclaration);
            }
        }
        return implementationMethod;
    }

    public MethodDeclaration findMethodDeclaration(final MethodCallExpr methodCall) {
        List<String> methodParamTypes = resolveMethodParameterTypes2(methodCall.getArguments());
        ResolvedReferenceTypeDeclaration callerType = findMethodCallerType(methodCall);

        ClassOrInterfaceDeclaration classOrInterfaceDeclaration = toClassOrInterfaceDeclaration(callerType);

        return findMethodDeclaration(methodCall.getNameAsString(), methodParamTypes, classOrInterfaceDeclaration);
    }


    public ClassOrInterfaceDeclaration toClassOrInterfaceDeclaration(final ResolvedReferenceTypeDeclaration referenceTypeDeclaration) {
        ClassOrInterfaceDeclaration classOrInterfaceDeclaration = null;
        if (referenceTypeDeclaration instanceof JavaParserClassDeclaration) {
            classOrInterfaceDeclaration = ((JavaParserClassDeclaration) referenceTypeDeclaration).getWrappedNode();
        } else if (referenceTypeDeclaration instanceof JavaParserEnumDeclaration) {
            classOrInterfaceDeclaration = ((JavaParserClassDeclaration) referenceTypeDeclaration).getWrappedNode();
        } else if (referenceTypeDeclaration instanceof JavaParserInterfaceDeclaration) {
            classOrInterfaceDeclaration = ((JavaParserInterfaceDeclaration) referenceTypeDeclaration).getWrappedNode();
        } else {
            System.out.println("Unsupported type: " + referenceTypeDeclaration);
            return null;
        }

        return classOrInterfaceDeclaration;
    }

    public MethodDeclaration findMethodDeclaration2(final String methodName, final List<QualifiedName>
            parameterTypes, final ClassOrInterfaceDeclaration parent) {
        return findMethodDeclaration(methodName, parameterTypes.stream().map(QualifiedName::toString).collect(Collectors.toList()), parent);
    }

    public MethodDeclaration findMethodDeclaration(final String methodName, final List<String>
            parameterTypes, final ClassOrInterfaceDeclaration parent) {
        if(parent == null) {
            System.out.println("Parent null");
            return null;
        }

        List<MethodDeclaration> methodDeclarations = parent
                .getMethodsBySignature(methodName, parameterTypes.toArray(new String[0]));

        if (methodDeclarations.isEmpty()) {
            System.out.println("Not found method " + methodName + "(" + parameterTypes + ")");
            return null;
        }

        return methodDeclarations.get(0);
    }

    public List<QualifiedName> resolveMethodParameterTypes3(final NodeList<Parameter> parametersExpresion) {
        if(parametersExpresion == null) {
            return null;
        }
        List<QualifiedName> nameList = parametersExpresion.stream().map(parameter -> {
            return QualifiedName.of(getJavaParserFacade().convertToUsage(parameter.getType()).asReferenceType().getQualifiedName());
        }).collect(Collectors.toList());

        return nameList;
    }

    public List<QualifiedName> resolveMethodParameterTypes(final NodeList<Expression> parametersExpresion) {
        List<String> parameters = resolveMethodParameterTypes2(parametersExpresion);
        if (parameters == null) {
            return null;
        }
        return parameters.stream().map(parameterName -> QualifiedName.of(parameterName)).collect(Collectors.toList());
    }

    public List<String> resolveMethodParameterTypes2(final NodeList<Expression> parameters) {
        List<String> methodParamTypes = new ArrayList<String>();

        if (parameters == null) {
            return methodParamTypes;
        }

        for (Expression argument : parameters) {
            try {
                ResolvedType type = this.javaParserFacade.getType(argument);
                if (type.isReferenceType()) {
                    String[] qualifiedName = type.asReferenceType().getQualifiedName().split("\\.");
                    methodParamTypes.add(qualifiedName[qualifiedName.length - 1]);
                } else if ((type.isPrimitive())) {
                    //String [] qualifiedName = type.asPrimitive().getQualifiedName().split("\\.");
                    methodParamTypes.add(type.asPrimitive().describe());
                } else {
                    System.out.println(type);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return methodParamTypes;
    }

    public ResolvedReferenceTypeDeclaration findMethodCallerType(final MethodCallExpr methodCall) {
        try {
            ResolvedMethodDeclaration methodDeclaration = this.javaParserFacade.solve(methodCall).getCorrespondingDeclaration();
            ResolvedReferenceTypeDeclaration callerType = methodDeclaration.declaringType();

            return callerType;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public ClassOrInterfaceDeclaration toDeclaration(JavaFile baseClass) {
        ResolvedReferenceTypeDeclaration referenceTypeDeclaration = combinedTypeSolver.solveType(baseClass.getQualifiedName().toString());

        ClassOrInterfaceDeclaration classOrInterfaceDeclaration = null;
        if (referenceTypeDeclaration instanceof JavaParserInterfaceDeclaration) {
            classOrInterfaceDeclaration = ((JavaParserInterfaceDeclaration) referenceTypeDeclaration).getWrappedNode();
        } else {
            classOrInterfaceDeclaration = ((JavaParserClassDeclaration) referenceTypeDeclaration).getWrappedNode();
        }
        return classOrInterfaceDeclaration;
    }

    public List<JavaFile> findImplementations(JavaFile baseClass) {
        return findImplementations(baseClass.getQualifiedName());
    }

    public List<JavaFile> findImplementations(QualifiedName qualifiedName) {
        List<JavaFile> implementationOfBaseClass = new ArrayList<>();
        ResolvedReferenceTypeDeclaration baseClassDeclaration = combinedTypeSolver.solveType(qualifiedName.toString());

        this.sources.forEach(source -> {
            ResolvedReferenceTypeDeclaration classDeclaration = combinedTypeSolver.solveType(source.getQualifiedName().toString());

            NodeList<ClassOrInterfaceType> classOrInterfaceTypes = null;
            if (baseClassDeclaration.isInterface()) {
                if (classDeclaration instanceof JavaParserInterfaceDeclaration) {
                    classOrInterfaceTypes = ((JavaParserInterfaceDeclaration) classDeclaration).getWrappedNode().getImplementedTypes();
                } else {
                    classOrInterfaceTypes = ((JavaParserClassDeclaration) classDeclaration).getWrappedNode().getImplementedTypes();
                }
            } else {
                if (classDeclaration instanceof JavaParserInterfaceDeclaration) {
                    classOrInterfaceTypes = ((JavaParserInterfaceDeclaration) classDeclaration).getWrappedNode().getExtendedTypes();
                } else {
                    classOrInterfaceTypes = ((JavaParserClassDeclaration) classDeclaration).getWrappedNode().getExtendedTypes();
                }
            }
            if (classOrInterfaceTypes != null) {
                classOrInterfaceTypes.forEach(classOrInterfaceType -> {
                    try {
                        ReferenceTypeImpl extendedType = (ReferenceTypeImpl) javaParserFacade.convertToUsage(classOrInterfaceType);

                        if (qualifiedName.toString().equals(extendedType.getQualifiedName())) {
                            implementationOfBaseClass.add(source);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            }
        });

        return implementationOfBaseClass;
    }

    public QualifiedName getQualifiedName(final CompilationUnit compilationUnit) {
        TypeDeclaration typeDeclaration = compilationUnit.getType(0);

        return getQualifiedName(typeDeclaration);
    }

    public QualifiedName getQualifiedName(final TypeDeclaration typeDeclaration) {
        ResolvedReferenceTypeDeclaration referenceBaseClass = javaParserFacade.getTypeDeclaration(typeDeclaration);

        return getQualifiedName(referenceBaseClass);
    }

    public QualifiedName getQualifiedName(final ResolvedReferenceTypeDeclaration referenceTypeDeclaration) {
        QualifiedName qualifiedName = QualifiedName.of(referenceTypeDeclaration.getQualifiedName());
        return qualifiedName;
    }

}
