/*
 * Copyright 2016 Federico Tomassetti
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package org.squbich.calltree.resolver;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.resolution.UnsolvedSymbolException;
import com.github.javaparser.resolution.declarations.ResolvedReferenceTypeDeclaration;
import com.github.javaparser.symbolsolver.javaparser.Navigator;
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade;
import com.github.javaparser.symbolsolver.model.resolution.SymbolReference;
import com.github.javaparser.symbolsolver.model.resolution.TypeSolver;
import org.squbich.calltree.model.code.QualifiedName;

import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * @author Federico Tomassetti
 */
public class SourceJarTypeSolver implements TypeSolver {

    private static SourceJarTypeSolver instance;

    private TypeSolver parent;
    private Map<String, ClasspathElement> classpathElements = new HashMap<>();

    public SourceJarTypeSolver(String pathToJar) {
        try {
            addPathToJar(pathToJar);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static SourceJarTypeSolver getJarTypeSolver(String pathToJar) throws IOException {
        if (instance == null) {
            instance = new SourceJarTypeSolver(pathToJar);
        } else {
            instance.addPathToJar(pathToJar);
        }
        return instance;
    }

    private void addPathToJar(String pathToJar) throws IOException {
//        try {
//            classPool.appendClassPath(pathToJar);
//            classPool.appendSystemPath();
//        } catch (NotFoundException e) {
//            throw new RuntimeException(e);
//        }
        JarFile jarFile = new JarFile(pathToJar);
        Enumeration<JarEntry> e = jarFile.entries();
        while(e.hasMoreElements()) {
            JarEntry entry = e.nextElement();
            System.out.println(entry);
            if (entry != null && !entry.isDirectory() && entry.getName().endsWith(".java")) {
                String name = entryPathToClassName(entry.getName());
                classpathElements.put(name, new ClasspathElement(jarFile, entry, name));
            } else {
            }
        }
    }

    @Override
    public TypeSolver getParent() {
        return parent;
    }

    @Override
    public void setParent(TypeSolver parent) {
        this.parent = parent;
    }

    private String entryPathToClassName(String entryPath) {
        if (!entryPath.endsWith(".java")) {
            throw new IllegalStateException();
        }
        String className = entryPath.substring(0, entryPath.length() - ".java".length());
        className = className.replace("src/main/java/", "");
        className = className.replace('/', '.');
        className = className.replace('$', '.');
        return className;
    }

    @Override
    public SymbolReference<ResolvedReferenceTypeDeclaration> tryToSolveType(String name) {
        try {
            if (classpathElements.containsKey(name)) {
                String typeName = QualifiedName.of(name).getNamePart();
                Optional<TypeDeclaration<?>> astTypeDeclaration = Navigator.findType(classpathElements.get(name).toCompilationUnit(), typeName);
                if (astTypeDeclaration.isPresent()) {
                    return SymbolReference.solved(JavaParserFacade.get(this).getTypeDeclaration(astTypeDeclaration.get()));
                } else {
                    return SymbolReference.unsolved(ResolvedReferenceTypeDeclaration.class);
                }

//                return SymbolReference.solved(
//                        JavassistFactory.toTypeDeclaration(classpathElements.get(name).toCtClass(), getRoot()));
            } else {
                return SymbolReference.unsolved(ResolvedReferenceTypeDeclaration.class);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public ResolvedReferenceTypeDeclaration solveType(String name) throws UnsolvedSymbolException {
        SymbolReference<ResolvedReferenceTypeDeclaration> ref = tryToSolveType(name);
        if (ref.isSolved()) {
            return ref.getCorrespondingDeclaration();
        } else {
            throw new UnsolvedSymbolException(name);
        }
    }

    private class ClasspathElement {
        private JarFile jarFile;
        private JarEntry entry;
        private String path;

        public ClasspathElement(JarFile jarFile, JarEntry entry, String path) {
            this.jarFile = jarFile;
            this.entry = entry;
            this.path = path;
        }

//        CtClass toCtClass() throws IOException {
//            try (InputStream is = jarFile.getInputStream(entry)) {
//                CtClass ctClass = classPool.makeClass(is);
//                return ctClass;
//            }
//        }
        CompilationUnit toCompilationUnit() throws IOException {
            try (InputStream is = jarFile.getInputStream(entry)) {
                CompilationUnit compilationUnit = JavaParser.parse(is);
                return compilationUnit;
            }
        }
    }
}
