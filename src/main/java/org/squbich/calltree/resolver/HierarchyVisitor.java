package org.squbich.calltree.resolver;

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
//import com.github.javaparser.symbolsolver.model.declarations.ReferenceTypeDeclaration;
//import com.github.javaparser.symbolsolver.model.typesystem.Type;

/**
 * Created by Szymon on 2017-07-23.
 */
public class HierarchyVisitor extends GenericVisitorAdapter<List<Execution>, Object> {
    private TypeResolver typeResolver;

    public HierarchyVisitor(final TypeResolver typeResolver) {
        this.typeResolver = typeResolver;
    }

    @Override
    public List<Execution> visit(MethodCallExpr methodCall, Object arg) {
        String callExpression = methodCall.toString();
        System.out.println("Method call: " + callExpression);


        Method currentMethod = typeResolver.findMethod(methodCall);
        if (currentMethod == null) {
            return Lists.newArrayList(MethodExecution.builder().callExpression(callExpression).build());
        }

        ResolvedReferenceTypeDeclaration callerClass = typeResolver.findMethodCallerType(methodCall);

        if (callerClass == null || !allowedType(callerClass)) {
            Execution methodExecution = MethodExecution.builder().method(currentMethod).callExpression(callExpression).build();
            return Lists.newArrayList(methodExecution);
        }

        if (callerClass.isInterface()) {
            List<Execution> methodExecutions = findMethodImplementationsExecutions(callExpression, currentMethod);
            return methodExecutions;
        } else {
            List<Execution> methodExecutions = findMethodExecutions(methodCall, currentMethod);
            return methodExecutions;
        }


    }

    private List<Execution> findMethodExecutions(MethodCallExpr methodCall, Method currentMethod) {
        MethodDeclaration methodDeclaration = typeResolver.findMethodDeclaration(methodCall);
        if (methodDeclaration == null) {
            return Lists.newArrayList();
        }
        System.out.println("currentMethod: " + methodDeclaration.getName());
        List<Execution> obj = methodDeclaration.accept(this, null);

        Execution methodExecution = MethodExecution.builder()
                .method(currentMethod)
                .callExpression(methodCall.toString())
                .executions(obj)
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
                Execution implementationMethodExecution = ImplementationOfAbstractMethodExecution.builder()
                        .implementedMethod(currentMethod)
                        .method(implementationMethod)
                        .callExpression(callExpression)
                        .executions(executions)
                        .build();
                implementationMethodExecutions.add(implementationMethodExecution);

            });
        }
        Execution methodExecution = AbstractMethodExecution.builder()
                .method(currentMethod)
                .callExpression(callExpression)
                .implementationExecutions(implementationMethodExecutions)
                .build();

        System.out.println(methodDeclarations);

        return Lists.newArrayList(methodExecution);
    }

    private boolean allowedType(ResolvedReferenceTypeDeclaration callerClass) {
        return callerClass.getPackageName().startsWith("org.sk");
    }

}