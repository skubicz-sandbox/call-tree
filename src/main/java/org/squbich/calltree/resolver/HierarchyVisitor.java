package org.squbich.calltree.resolver;

import java.util.ArrayList;
import java.util.List;

import org.squbich.calltree.model.calls.MethodCall;
import org.squbich.calltree.model.code.Method;
import org.squbich.calltree.model.calls.AbstractMethodCall;
import org.squbich.calltree.model.calls.ImplementationOfAbstractMethodCall;
import org.squbich.calltree.model.calls.DirectMethodCall;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.comments.Comment;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ForStmt;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.stmt.TryStmt;
import com.github.javaparser.ast.visitor.GenericVisitorAdapter;
import com.github.javaparser.resolution.declarations.ResolvedReferenceTypeDeclaration;
import com.google.common.collect.Lists;

import lombok.extern.slf4j.Slf4j;

/**
 * Created by Szymon on 2017-07-23.
 */
@Slf4j
public class HierarchyVisitor extends GenericVisitorAdapter<List<MethodCall>, Object> {
    private TypeResolver typeResolver;
    /**
     * Base package to search method execution. Methods from different packages are not analyze recursive - they are end of the search.
     */
    private String packageScan;

    public HierarchyVisitor(final TypeResolver typeResolver, String packageScan) {
        this.typeResolver = typeResolver;
        this.packageScan = packageScan;
    }

    @Override
    public List<MethodCall> visit(MethodCallExpr methodCall, Object arg) {
        String callExpression = methodCall.toString();
        //   System.out.println("Method call: " + callExpression);

        // wywołanie metody może zawierać inne wywołanie, np. root.foo(root2.foo())
        List<MethodCall> childrenCalls = new ArrayList<>();
       // getChildrenWithoutArguments(methodCall).forEach(node -> {
        methodCall.getChildNodes().forEach(node -> {
            List<MethodCall> calls = node.accept(this, arg);
            if (calls != null) {
                childrenCalls.addAll(calls);
            }
        });

        List<MethodCall> argumentsCalls = new ArrayList<>();
//        methodCall.getArguments().forEach(node -> {
//            List<MethodCall> calls = node.accept(this, arg);
//            if (calls != null) {
//                argumentsCalls.addAll(calls);
//            }
//        });

        List<MethodCall> methodCalls = new ArrayList<>();
        methodCalls.addAll(childrenCalls);

        Method currentMethod = typeResolver.findMethod(methodCall);
        if (currentMethod == null) {
            methodCalls.add(DirectMethodCall.builder().expression(callExpression).children(argumentsCalls).build());
            return methodCalls;
        }

        ResolvedReferenceTypeDeclaration callerClass = typeResolver.findMethodCallerType(methodCall);

        if (callerClass == null || !allowedType(callerClass)) {
            MethodCall methodExecution = DirectMethodCall.builder().children(new ArrayList<>(argumentsCalls)).method(currentMethod)
                    .expression(callExpression).build();
            methodCalls.add(methodExecution);
            return methodCalls;
        }

        if (callerClass.isInterface()) {
            methodCalls.addAll(findMethodImplementationsExecutions(callExpression, currentMethod));
        }
        else {
            methodCalls.addAll(findMethodCalls(methodCall, currentMethod));
        }

        return methodCalls;

    }

    private List<Node> getChildrenWithoutArguments(MethodCallExpr methodCall) {
        if(methodCall.getChildNodes() == null || methodCall.getChildNodes().isEmpty()) {
            return new ArrayList<>();
        }
        return methodCall.getChildNodes().subList(0, methodCall.getChildNodes().size() - methodCall.getArguments().size());
    }

    @Override
    public List<MethodCall> visit(NodeList n, Object arg) {
        List<MethodCall> results = visitNodes(n, arg);
        return results;
    }

    @Override
    public List<MethodCall> visit(ForStmt n, Object arg) {
        List<MethodCall> results = visitNodes(n.getChildNodes(), arg);
        return results;
    }

    @Override
    public List<MethodCall> visit(IfStmt n, Object arg) {
        List<MethodCall> results = visitNodes(n.getChildNodes(), arg);
        return results;
    }

    @Override
    public List<MethodCall> visit(TryStmt n, Object arg) {
        List<MethodCall> results = visitNodes(n.getChildNodes(), arg);
        return results;
    }

    public List<MethodCall> visitNodes(List<Node> nodes, Object arg) {
        List<MethodCall> results = null;
        for (final Object v : nodes) {
            List<MethodCall> calls = ((Node) v).accept(this, arg);
            if (calls != null) {
                if (results == null) {
                    results = new ArrayList<>();
                }
                results.addAll(calls);
            }
        }
        return results;
    }

    private List<MethodCall> findMethodCalls(MethodCallExpr methodCall, Method currentMethod) {
        MethodDeclaration methodDeclaration = typeResolver.findMethodDeclaration(methodCall);
        if (methodDeclaration == null) {
            return Lists.newArrayList();
        }
        log.info("findMethodCalls: processing method: " + methodDeclaration.getName());
        List<MethodCall> obj = methodDeclaration.accept(this, currentMethod);

        MethodCall methodExecution = DirectMethodCall.builder().method(currentMethod).expression(methodCall.toString()).children(obj)
                .build();
        return Lists.newArrayList(methodExecution);
    }

    private List<MethodCall> findMethodImplementationsExecutions(String callExpression, Method currentMethod) {
        List<MethodCall> implementationMethodCalls = new ArrayList<>();
        List<MethodDeclaration> methodDeclarations = typeResolver.findImplementationMethod(currentMethod);
        if (methodDeclarations != null) {
            methodDeclarations.forEach(methodDeclaration -> {
                List<MethodCall> calls = methodDeclaration.accept(this, null);

                Method implementationMethod = typeResolver.toMethod(methodDeclaration);
                MethodCall implementationMethodCall = ImplementationOfAbstractMethodCall.builder().implementedMethod(currentMethod)
                        .method(implementationMethod).expression(callExpression).children(calls).build();
                implementationMethodCalls.add(implementationMethodCall);

            });
        }
        MethodCall methodCall = AbstractMethodCall.builder().method(currentMethod).expression(callExpression)
                .implementationCalls(implementationMethodCalls).build();

        //System.out.println(methodDeclarations);

        return Lists.newArrayList(methodCall);
    }

    private boolean allowedType(ResolvedReferenceTypeDeclaration callerClass) {
        return callerClass.getPackageName().startsWith(packageScan);
        //    return callerClass.getPackageName().startsWith("org.sk");
    }

}