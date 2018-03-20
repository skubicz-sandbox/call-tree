package org.squbich.calltree.resolver;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.squbich.calltree.model.code.ClassDescriptor;
import org.squbich.calltree.model.code.JavaFile;
import org.squbich.calltree.model.code.Method;
import org.squbich.calltree.model.code.QualifiedName;
import org.squbich.calltree.tools.Assert;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.comments.Comment;
import com.github.javaparser.ast.comments.JavadocComment;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.resolution.declarations.ResolvedMethodDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedParameterDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedReferenceTypeDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedTypeDeclaration;
import com.github.javaparser.resolution.types.ResolvedType;
import com.github.javaparser.symbolsolver.core.resolution.Context;
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade;
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFactory;
import com.github.javaparser.symbolsolver.javaparsermodel.declarations.JavaParserClassDeclaration;
import com.github.javaparser.symbolsolver.javaparsermodel.declarations.JavaParserEnumDeclaration;
import com.github.javaparser.symbolsolver.javaparsermodel.declarations.JavaParserInterfaceDeclaration;
import com.github.javaparser.symbolsolver.javassistmodel.JavassistInterfaceDeclaration;
import com.github.javaparser.symbolsolver.model.resolution.SymbolReference;
import com.github.javaparser.symbolsolver.model.typesystem.ReferenceTypeImpl;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JarTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;


/**
 * Created by Szymon on 2017-07-27.
 */
@Slf4j
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
            }
            else {
                this.combinedTypeSolver.add(new JavaParserTypeSolver(new File(sourceAggregate.getName())));
            }
            this.sources.addAll(sourceAggregate.getSources());
        });
        libraries.forEach(library -> {
            try {
                this.combinedTypeSolver.add(new JarTypeSolver(library.getName()));
            }
            catch (IOException e) {
                log.warn(e.getLocalizedMessage());
            }
        });
        this.javaParserFacade = JavaParserFacade.get(combinedTypeSolver);
    }

    public Method findMethod(final MethodCallExpr methodCall) {
        MethodDeclaration methodDeclaration = findMethodDeclaration(methodCall);
        try {
            ResolvedReferenceTypeDeclaration callerType = findMethodCallerType(methodCall);

            if (methodDeclaration == null) {
                if (callerType != null) {
                    ClassDescriptor parentClass = ClassDescriptor.builder().qualifiedName(QualifiedName.of(callerType.asReferenceType()
                            .getQualifiedName()))
                            .build();

                    List<QualifiedName> methodParamTypes = resolveMethodParameterTypesFromExpresions(methodCall.getArguments());

                    ResolvedType resolvedType = findReturnType(callerType, methodCall.getNameAsString(), methodParamTypes);
                    return Method.builder().parentClass(parentClass).parameters(methodParamTypes).name(methodCall.getNameAsString())
                            .returnType(resolvedType == null ? null : QualifiedName.of(resolvedType.describe())).build();
                }
                return null;
            }
            return toMethod(methodDeclaration);
        }
        catch (Exception e) {
            log.warn("findMethod: " + e.getLocalizedMessage());
            return Method.builder().parentClass(null).name(methodDeclaration.getNameAsString()).build();
        }
    }

    private ResolvedType findReturnType(ResolvedReferenceTypeDeclaration callerType, String methodName,
            List<QualifiedName> methodParamTypes) {
        for (ResolvedMethodDeclaration resolvedMethodDeclaration : callerType.getDeclaredMethods()) {
            if (methodName.equals(resolvedMethodDeclaration.getName())) {
                int numberOfParams = resolvedMethodDeclaration.getNumberOfParams();
                if (numberOfParams == 0 && methodParamTypes.size() == 0) {
                    return resolvedMethodDeclaration.getReturnType();
                }
                else if (numberOfParams > 0 && methodParamTypes.size() == numberOfParams) {
                    boolean paramsFit = false;
                    for (int i = 0; i < numberOfParams; i++) {
                        ResolvedParameterDeclaration resolvedParameterDeclaration = resolvedMethodDeclaration.getParam(i);
                        QualifiedName expectedParam = methodParamTypes.get(i);
                        if (expectedParam.toString().equals(resolvedParameterDeclaration.describeType())) {
                            paramsFit = true;
                        }
                        else {
                            paramsFit = false;
                            try {
                                Class<?> expectedParamType = Class.forName(expectedParam.toString());
                                Class<?> foundParamType = Class.forName(resolvedParameterDeclaration.describeType());
                                // unsupported collections yet
                                if (foundParamType.isAssignableFrom(expectedParamType)) {
                                    paramsFit = true;
                                }
                            }
                            catch (Exception e) {
                                log.warn("findReturnType: " + e.getLocalizedMessage());
                            }
                        }
                        if (paramsFit) {
                            return resolvedMethodDeclaration.getReturnType();
                        }
                    }
                }
            }
        }
        return null;
    }

    public Method toMethod(final MethodDeclaration methodDeclaration) {
        try {
            ResolvedType resolvedReturnType = getJavaParserFacade().convertToUsage(methodDeclaration.getType());
            ResolvedReferenceTypeDeclaration callerType = getJavaParserFacade().getTypeDeclaration(methodDeclaration.getParentNode().get());
            ClassOrInterfaceDeclaration parent = toClassOrInterfaceDeclaration(callerType);
            String classComment = commentAsText(parent.getComment());
            String methodComment = commentAsText(methodDeclaration.getComment());

            ClassDescriptor parentClass = ClassDescriptor.builder()
                    .qualifiedName(QualifiedName.of(callerType.asReferenceType().getQualifiedName()))
                    .annotations(findAnnotationTypes(parent.getAnnotations())).comment(classComment).build();
            List<QualifiedName> parameterTypes = resolveMethodParameterTypes(methodDeclaration.getParameters());

            Method method = Method.builder().parentClass(parentClass).annotations(findAnnotationTypes(methodDeclaration.getAnnotations()))
                    .parameters(parameterTypes).name(methodDeclaration.getNameAsString())
                    .returnType(QualifiedName.of(resolvedReturnType.describe())).comment(methodComment).build();

            return method;
        }
        catch (Exception e) {
            log.warn("toMethod: " + e.getLocalizedMessage());
            return Method.builder().parentClass(null).name(methodDeclaration.getNameAsString()).build();
        }
    }

    public String commentAsText(Optional<Comment> optionalComment) {
        return optionalComment.map(this::commentAsText).orElse("");
    }

    public String commentAsText(Comment comment) {
        if(comment == null) {
            return null;
        }

        String textComment = null;
        if(comment.isJavadocComment()) {
            textComment = ((JavadocComment)comment).parse().getDescription().toText();
        } else {
            textComment = comment.toString();
        }
        return textComment;
    }

    public List<MethodDeclaration> findImplementationMethod(final Method method) {
        List<JavaFile> implementations = findImplementations(method.getParentClass().getQualifiedName());
        if (implementations == null) {
            return null;
        }

        List<MethodDeclaration> implementationMethod = new ArrayList<>();

        for (JavaFile javaFile : implementations) {
            ClassOrInterfaceDeclaration parentImplementation = toDeclaration(javaFile);

            MethodDeclaration methodDeclaration = findMethodDeclaration(method.getName(), method.getParameters(), parentImplementation);
            if (methodDeclaration != null) {
                implementationMethod.add(methodDeclaration);
            }
        }
        return implementationMethod;
    }

    public MethodDeclaration findMethodDeclaration(final MethodCallExpr methodCall) {
        List<QualifiedName> methodParamTypes = resolveMethodParameterTypesFromExpresions(methodCall.getArguments());
        ResolvedReferenceTypeDeclaration callerType = findMethodCallerType(methodCall);

        ClassOrInterfaceDeclaration classOrInterfaceDeclaration = toClassOrInterfaceDeclaration(callerType);

        return findMethodDeclaration(methodCall.getNameAsString(), methodParamTypes, classOrInterfaceDeclaration);
    }


    public ClassOrInterfaceDeclaration toClassOrInterfaceDeclaration(final ResolvedReferenceTypeDeclaration referenceTypeDeclaration) {
        ClassOrInterfaceDeclaration classOrInterfaceDeclaration = null;
        if (referenceTypeDeclaration instanceof JavaParserClassDeclaration) {
            classOrInterfaceDeclaration = ((JavaParserClassDeclaration) referenceTypeDeclaration).getWrappedNode();
        }
        else if (referenceTypeDeclaration instanceof JavaParserEnumDeclaration) {
            classOrInterfaceDeclaration = ((JavaParserClassDeclaration) referenceTypeDeclaration).getWrappedNode();
        }
        else if (referenceTypeDeclaration instanceof JavaParserInterfaceDeclaration) {
            classOrInterfaceDeclaration = ((JavaParserInterfaceDeclaration) referenceTypeDeclaration).getWrappedNode();
        }
        else if (referenceTypeDeclaration instanceof JavassistInterfaceDeclaration) {
            log.warn("toClassOrInterfaceDeclaration: Unsupported type: " + referenceTypeDeclaration);
            //    classOrInterfaceDeclaration = ((JavassistInterfaceDeclaration) referenceTypeDeclaration).asInterface().;
        }
        else {
            log.warn("toClassOrInterfaceDeclaration: Unsupported type: " + referenceTypeDeclaration);
            return null;
        }

        return classOrInterfaceDeclaration;
    }

    public MethodDeclaration findMethodDeclaration(final String methodName, final List<QualifiedName> parameterTypes,
            final ClassOrInterfaceDeclaration parent) {
        if (parent == null) {
            return null;
        }

        List<MethodDeclaration> methodDeclarations = parent.getMethodsBySignature(methodName,
                parameterTypes.stream().map(QualifiedName::getNamePart).collect(Collectors.toList()).toArray(new String[0]));

        if (methodDeclarations.isEmpty()) {
            log.warn("findMethodDeclaration: Not found method " + methodName + "(" + parameterTypes + ")");
            return null;
        }

        return methodDeclarations.get(0);
    }

    public List<QualifiedName> resolveMethodParameterTypes(final NodeList<Parameter> parametersExpresion) {
        if (parametersExpresion == null) {
            return null;
        }
        List<QualifiedName> nameList = parametersExpresion.stream().map(parameter -> {
            return QualifiedName.of(getJavaParserFacade().convertToUsage(parameter.getType()).describe());
        }).collect(Collectors.toList());

        return nameList;
    }

    public List<QualifiedName> resolveMethodParameterTypesFromExpresions(final NodeList<Expression> parameters) {
        try {
            List<QualifiedName> methodParamTypes = new ArrayList<>();

            if (parameters == null) {
                return methodParamTypes;
            }

            for (Expression argument : parameters) {
                try {
                    ResolvedType type = this.javaParserFacade.getType(argument);
                    methodParamTypes.add(QualifiedName.of(type.describe()));
                }
                catch (Exception e) {
                    log.warn("resolveMethodParameterTypesFromExpresions: " + e.getLocalizedMessage());
                }
            }

            return methodParamTypes;
        }
        catch (Exception e) {
            log.warn("resolveMethodParameterTypesFromExpresions: " + e.getLocalizedMessage());
            return new ArrayList<>();
        }
    }

    public ResolvedReferenceTypeDeclaration findMethodCallerType(final MethodCallExpr methodCall) {
        try {
            ResolvedMethodDeclaration methodDeclaration = this.javaParserFacade.solve(methodCall).getCorrespondingDeclaration();
            ResolvedReferenceTypeDeclaration callerType = methodDeclaration.declaringType();

            return callerType;
        }
        catch (Exception e) {
            log.warn("findMethodCallerType: " + e.getLocalizedMessage());
            return null;
        }
    }

    public ClassOrInterfaceDeclaration toDeclaration(JavaFile baseClass) {
        ResolvedReferenceTypeDeclaration referenceTypeDeclaration = combinedTypeSolver.solveType(baseClass.getQualifiedName().toString());

        ClassOrInterfaceDeclaration classOrInterfaceDeclaration = null;
        if (referenceTypeDeclaration instanceof JavaParserInterfaceDeclaration) {
            classOrInterfaceDeclaration = ((JavaParserInterfaceDeclaration) referenceTypeDeclaration).getWrappedNode();
        }
        else {
            classOrInterfaceDeclaration = ((JavaParserClassDeclaration) referenceTypeDeclaration).getWrappedNode();
        }
        return classOrInterfaceDeclaration;
    }

    public List<JavaFile> findClassesInPackage(String packageName) {
        List<JavaFile> classes = new ArrayList<>();

        this.sources.forEach(source -> {
            if (source == null) {
                return;
            }

            if(source.getQualifiedName().getPackagePart().startsWith(packageName)) {
                classes.add(source);
            }
        });

        return classes;
    }

    public List<JavaFile> findImplementations(JavaFile baseClass) {
        return findImplementations(baseClass.getQualifiedName());
    }

    public List<JavaFile> findImplementations(QualifiedName qualifiedName) {
        List<JavaFile> implementationOfBaseClass = new ArrayList<>();
        ResolvedReferenceTypeDeclaration baseClassDeclaration = combinedTypeSolver.solveType(qualifiedName.toString());

        this.sources.forEach(source -> {
            if (source == null) {
                return;
            }
            ResolvedReferenceTypeDeclaration classDeclaration = combinedTypeSolver.solveType(source.getQualifiedName().toString());

            NodeList<ClassOrInterfaceType> classOrInterfaceTypes = null;
            if (baseClassDeclaration.isInterface()) {
                if (classDeclaration instanceof JavaParserInterfaceDeclaration) {
                    classOrInterfaceTypes = ((JavaParserInterfaceDeclaration) classDeclaration).getWrappedNode().getImplementedTypes();
                }
                else if (classDeclaration instanceof JavaParserClassDeclaration) {
                    classOrInterfaceTypes = ((JavaParserClassDeclaration) classDeclaration).getWrappedNode().getImplementedTypes();
                }
                else {
                    log.warn("findImplementations: Unsupported type: " + classDeclaration);
                }
            }
            else {
                if (classDeclaration instanceof JavaParserInterfaceDeclaration) {
                    classOrInterfaceTypes = ((JavaParserInterfaceDeclaration) classDeclaration).getWrappedNode().getExtendedTypes();
                }
                else {
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
                    }
                    catch (Exception e) {
                        log.warn("findImplementations: " + e.getLocalizedMessage());
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

    public List<QualifiedName> findAnnotationTypes(NodeList<AnnotationExpr> annotationExprList) {
        List<QualifiedName> annotations = new ArrayList<>();
        if (annotationExprList == null) {
            return annotations;
        }
        annotationExprList.forEach(annotationExpr -> {
            try {
                ResolvedTypeDeclaration annotation = findAnnotationType(annotationExpr);
                if (annotation != null) {
                    annotations.add(QualifiedName.of(annotation.getQualifiedName()));
                }
            }
            catch (Exception e) {
                log.warn("findAnnotationTypes: " + e.getLocalizedMessage());
                annotations.add(QualifiedName.of(annotationExpr.getNameAsString()));
            }
        });
        return annotations;
    }

    public ResolvedTypeDeclaration findAnnotationType(AnnotationExpr annotationExpr) {
        Context context = JavaParserFactory.getContext(annotationExpr, getJavaParserFacade().getTypeSolver());
        SymbolReference<ResolvedTypeDeclaration> typeDeclarationSymbolReference = context
                .solveType(annotationExpr.getNameAsString(), getJavaParserFacade().getTypeSolver());
        return typeDeclarationSymbolReference.getCorrespondingDeclaration();
        //        ResolvedAnnotationDeclaration annotationDeclaration = (ResolvedAnnotationDeclaration) typeDeclarationSymbolReference.getCorrespondingDeclaration();
        //        if (typeDeclarationSymbolReference.isSolved()) {
        //            return SymbolReference.solved(annotationDeclaration);
        //        } else {
        //            return SymbolReference.unsolved(ResolvedAnnotationDeclaration.class);
        //        }
    }
}
