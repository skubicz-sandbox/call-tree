package org.squbich.calltree.resolver;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.comments.Comment;
import com.github.javaparser.ast.expr.BinaryExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.visitor.GenericVisitorAdapter;
import com.github.javaparser.resolution.declarations.ResolvedReferenceTypeDeclaration;
import com.github.javaparser.resolution.types.ResolvedType;
import org.squbich.calltree.model.code.ClassDescriptor;
import org.squbich.calltree.model.code.JavaFile;
import org.squbich.calltree.model.code.Method;
import org.squbich.calltree.model.code.QualifiedName;
import org.squbich.calltree.model.executions.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
//import com.github.javaparser.symbolsolver.model.declarations.ReferenceTypeDeclaration;
//import com.github.javaparser.symbolsolver.model.typesystem.Type;

/**
 * Created by Szymon on 2017-07-23.
 */
public class HierarchyVisitorOld extends GenericVisitorAdapter<Executable, Object> {
    private TypeResolver typeResolver;

    public HierarchyVisitorOld(final TypeResolver typeResolver) {
        this.typeResolver = typeResolver;
    }

    @Override
    public Executable visit(ClassOrInterfaceDeclaration declaration, Object arg) {
        List<MethodRoot> methods = new ArrayList<>();
        declaration.getMembers().forEach(bodyDeclaration -> {
            if (bodyDeclaration != null) {
                //                List<?> s = (List)bodyDeclaration.accept(this, arg);
                //                System.out.println(s);
                try {
                    MethodRoot method = (MethodRoot) bodyDeclaration.accept(this, declaration);
                    methods.add(method);
                } catch (RuntimeException e) {
                    e.printStackTrace();
                    System.out.println(e.getMessage());
                }
            }
        });
        //List<?> calls = (List) declaration.getMembers().accept(this, null);

        QualifiedName className = QualifiedName.of(declaration.getName().asString());
        ClassRoot classRoot = new ClassRoot(className, methods);
        return classRoot;
    }

//    @Override
//    public Object visit(NodeList n, Object arg) {
//        List<Object> results = new ArrayList<>();
//        for (final Object v : n) {
//            Object result = ((Node) v).accept(this, arg);
//            if (result != null) {
//                if (result instanceof List) {
//                    if (((List) result).size() == 1) {
//                        results.add(((List) result).get(0));
//                    }
//                    else {
//                        results.add(result);
//                    }
//                }
//                else {
//                    results.add(result);
//                }
//            }
//        }
//        if (results.isEmpty()) {
//            return null;
//        }
//        return results;
//    }

    private void vod() {

    }

    @Override
    public Executable visit(MethodCallExpr methodCall, Object arg) {
        System.out.print("Method call: " + methodCall.getName() + "\n");
//        List<Expression> args = methodCall.getArguments();
//
//        if (args != null) {
//            handleExpressions(args);
//        }

        String callExpression = methodCall.toString();
        String methodName = methodCall.getNameAsString();
        List<String> parameterTypes = typeResolver.resolveMethodParameterTypes2(methodCall.getArguments());

        MethodDeclaration methodDeclaration2 = typeResolver.findMethodDeclaration(methodCall);

        Method method = typeResolver.findMethod(methodCall);
        if (method == null) {
            return null;
        }
        Execution methodExecution = null;
        try {
            ResolvedReferenceTypeDeclaration callerType = typeResolver.findMethodCallerType(methodCall);

            if (callerType != null) {
                if (callerType.getPackageName().startsWith("org.sk")) {
                    if (callerType.isInterface()) {
                        List<Execution> implementationMethodExecutions = new ArrayList<>();
                        List<MethodDeclaration> methodDeclarations = typeResolver.findImplementationMethod(method);
                        if (methodDeclarations != null) {
                            methodDeclarations.forEach(methodDeclaration -> {
                                Executable obj = methodDeclaration.accept(this, arg);
                                if (obj instanceof MethodRoot) {
                                    List<Execution> executions = ((MethodRoot) obj).getExecutions();
                                    Method implementationMethod = typeResolver.toMethod(methodDeclaration);
                                    Execution implementationMethodExecution = ImplementationOfAbstractMethodExecution.builder()
                                            .implementedMethod(method)
                                            .method(implementationMethod)
                                            .callExpression(callExpression)
                                            .executions(executions)
                                            .build();
                                    implementationMethodExecutions.add(implementationMethodExecution);
                                }
                                else {
                                    System.out.println(obj);
                                }
                            });
                        }
                        methodExecution = AbstractMethodExecution.builder()
                                .method(method)
                                .callExpression(callExpression)
                                .implementationExecutions(implementationMethodExecutions)
                                .build();

                        System.out.println(methodDeclarations);
                    } else {
                        if (methodDeclaration2 != null) {
                            System.out.println("method: " + methodDeclaration2.getName());
                            Object obj = methodDeclaration2.accept(this, arg);
                            if (obj instanceof MethodRoot) {
                                methodExecution = MethodExecution.builder()
                                        .method(((MethodRoot) obj).getMethod())
                                        .callExpression(callExpression)
                                        .executions(((MethodRoot) obj).getExecutions())
                                        .build();
                            }
                        }
                    }
                }
            }
        } catch (RuntimeException e) {
            e.printStackTrace();
        }

        if (methodExecution == null) {
            methodExecution = MethodExecution.builder()
                    .method(null)
                    .callExpression(callExpression)
                    .executions(null)
                    .build();
        }
        return methodExecution;
    }

    @Override
    public Executable visit(MethodDeclaration declaration, Object arg) {
        List<Execution> calls = new ArrayList<>();

        Object result = super.visit(declaration, arg);
        try {
            if (result instanceof List) {
                List results = (List) result;
                calls.addAll(results);
            } else {
                Execution call = (Execution) result;
                calls.add(call);
            }
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
//        declaration.getBody().ifPresent(blockStmt -> {
//            blockStmt.getStatements().forEach(statement -> {
//                try {
//                    Object result = statement.accept(this, arg);
//                    if (result instanceof List) {
//                        List results = (List) result;
//                        calls.addAll(results);
//                    }
//                    else {
//                        Execution call = (Execution) result;
//                        calls.add(call);
//                    }
//                }
//                catch (RuntimeException e) {
//                    e.printStackTrace();
//                }
//            });
//        });

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

        Method method = Method.builder().name(declaration.getName().asString()).parentClass(parent).build();
        MethodRoot methodRoot = new MethodRoot(method, calls);
        return methodRoot;
    }

    private void handleExpressions(List<Expression> expressions) {
        for (Expression expr : expressions) {
            if (expr instanceof MethodCallExpr) {
                visit((MethodCallExpr) expr, null);
            } else if (expr instanceof BinaryExpr) {
                BinaryExpr binExpr = (BinaryExpr) expr;
                handleExpressions(Arrays.asList(binExpr.getLeft(), binExpr.getRight()));
            }
        }
    }
}