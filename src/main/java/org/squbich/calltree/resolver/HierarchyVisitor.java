package org.squbich.calltree.resolver;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.BinaryExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.visitor.GenericVisitorAdapter;
import com.github.javaparser.resolution.declarations.ResolvedReferenceTypeDeclaration;
import com.github.javaparser.resolution.types.ResolvedType;
import com.google.common.collect.Lists;

import org.squbich.calltree.model.code.ClassDescriptor;
import org.squbich.calltree.model.code.Method;
import org.squbich.calltree.model.code.QualifiedName;
import org.squbich.calltree.model.executions.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import lombok.extern.slf4j.Slf4j;
//import com.github.javaparser.symbolsolver.model.declarations.ReferenceTypeDeclaration;
//import com.github.javaparser.symbolsolver.model.typesystem.Type;

/**
 * Created by Szymon on 2017-07-23.
 */
@Slf4j
public class HierarchyVisitor extends GenericVisitorAdapter<List<Execution>, Object> {
    private TypeResolver typeResolver;
    private String allowedPackagePattern;

    public HierarchyVisitor(final TypeResolver typeResolver, String allowedPackagePattern) {
        this.typeResolver = typeResolver;
        this.allowedPackagePattern = allowedPackagePattern;
    }

    @Override
    public List<Execution> visit(MethodCallExpr methodCall, Object arg) {
        String callExpression = methodCall.toString();
        //   System.out.println("Method call: " + callExpression);


        // wywołanie metody może zawierać inne wywołanie, np. root.foo(root2.foo())
        List<Execution> childrenExecutions = new ArrayList<>();
        methodCall.getChildNodes().forEach(node -> {
            List<Execution> executions = node.accept(this, arg);
            if (executions != null) {
                childrenExecutions.addAll(executions);
            }
        });

        List<Execution> methodExecutions = new ArrayList<>();
        methodExecutions.addAll(childrenExecutions);

        Method currentMethod = typeResolver.findMethod(methodCall);
        if (currentMethod == null) {
            methodExecutions.add(MethodExecution.builder().callExpression(callExpression).build());
            return methodExecutions;
        }

        ResolvedReferenceTypeDeclaration callerClass = typeResolver.findMethodCallerType(methodCall);

        if (callerClass == null || !allowedType(callerClass)) {
            Execution methodExecution = MethodExecution.builder().executions(new ArrayList<>(methodExecutions)).method(currentMethod)
                    .callExpression(callExpression).build();
            methodExecutions.add(methodExecution);
            return methodExecutions;
        }

        if (callerClass.isInterface()) {
            methodExecutions.addAll(findMethodImplementationsExecutions(callExpression, currentMethod));
        }
        else {
            methodExecutions.addAll(findMethodExecutions(methodCall, currentMethod));
        }

        return methodExecutions;

    }

    @Override
    public List<Execution> visit(NodeList n, Object arg) {
        List<Execution> results = null;
        for (final Object v : n) {
            List<Execution> executions = ((Node) v).accept(this, arg);
            if (executions != null) {
                if (results == null) {
                    results = new ArrayList<>();
                }
                results.addAll(executions);
            }
        }
        return results;
    }

    private List<Execution> findMethodExecutions(MethodCallExpr methodCall, Method currentMethod) {
        MethodDeclaration methodDeclaration = typeResolver.findMethodDeclaration(methodCall);
        if (methodDeclaration == null) {
            return Lists.newArrayList();
        }
        log.info("findMethodExecutions: processing method: " + methodDeclaration.getName());
        List<Execution> obj = methodDeclaration.accept(this, currentMethod);

        Execution methodExecution = MethodExecution.builder().method(currentMethod).callExpression(methodCall.toString()).executions(obj)
                .build();
        return Lists.newArrayList(methodExecution);
    }

    private List<Execution> findMethodImplementationsExecutions(String callExpression, Method currentMethod) {
        List<Execution> implementationMethodExecutions = new ArrayList<>();
        List<MethodDeclaration> methodDeclarations = typeResolver.findImplementationMethod(currentMethod);
        if (methodDeclarations != null) {
            methodDeclarations.forEach(methodDeclaration -> {
                List<Execution> executions = methodDeclaration.accept(this, null);

                Method implementationMethod = typeResolver.toMethod(methodDeclaration);
                Execution implementationMethodExecution = ImplementationOfAbstractMethodExecution.builder().implementedMethod(currentMethod)
                        .method(implementationMethod).callExpression(callExpression).executions(executions).build();
                implementationMethodExecutions.add(implementationMethodExecution);

            });
        }
        Execution methodExecution = AbstractMethodExecution.builder().method(currentMethod).callExpression(callExpression)
                .implementationExecutions(implementationMethodExecutions).build();

        //System.out.println(methodDeclarations);

        return Lists.newArrayList(methodExecution);
    }

    private boolean allowedType(ResolvedReferenceTypeDeclaration callerClass) {
        return callerClass.getPackageName().startsWith(allowedPackagePattern);
        //    return callerClass.getPackageName().startsWith("org.sk");
    }

}