package org.squbich.calltree.resolver;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.comments.Comment;
import org.squbich.calltree.model.code.JavaFile;
import org.squbich.calltree.model.code.QualifiedName;
import org.squbich.calltree.tools.Assert;
import org.squbich.calltree.tools.FileExtension;
import org.squbich.calltree.tools.FilesUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Created by Szymon on 2017-07-26.
 */
public class SourceResolver {


    public List<SourceAggregate> solve(final List<File> files) {
        if (files == null) {
            return null;
        }

        List<SourceAggregate> sourceAggregates = new ArrayList<>();
        files.forEach(file -> {
            if (file.isDirectory()) {
                DirectorySourceAggregate directorySourceAggregate = solveDirectorySource(file);
                sourceAggregates.add(directorySourceAggregate);
            }
            else if (FileExtension.JAR.equals(FilesUtils.retrieveFileExtension(file))) {
                JarSourceAggregate jarSourceAggregate = solveJarSource(file);
                sourceAggregates.add(jarSourceAggregate);
            }
            else if (FileExtension.JAVA.equals(FilesUtils.retrieveFileExtension(file))) {
                FileSourceAggregate fileSourceAggregate = solveFileSource(file);
                sourceAggregates.add(fileSourceAggregate);
            } else {
                System.out.println("Not supported file [file = " + file + "]");
            }
        });

        return sourceAggregates;
    }

    private JarSourceAggregate solveJarSource(final File jar) {
        Assert.notNull(jar, "File must not be null.");
        Assert.isTrue(FileExtension.JAR.equals(FilesUtils.retrieveFileExtension(jar)), "File must be jar.");

        List<String> files = FilesUtils.readFilesFromJar(jar, FileExtension.JAVA);
        List<JavaFile> javaFiles = new ArrayList<>();
        files.forEach(file -> {
            CompilationUnit cu = JavaParser.parse(file);
            javaFiles.add(compilationUnitToJavaFile(cu));
        });
        JarSourceAggregate jarSourceAggregate = new JarSourceAggregate(jar.getAbsolutePath(), javaFiles);

        return jarSourceAggregate;
    }

    private DirectorySourceAggregate solveDirectorySource(final File directory) {
        Assert.notNull(directory, "File must not be null.");
        Assert.isTrue(directory.isDirectory(), "File must be directory.");

        List<File> files = FilesUtils.findAllFiles(directory, FileExtension.JAVA);
        List<JavaFile> javaFiles = new ArrayList<>();
        files.forEach(file -> {
            try {
                CompilationUnit cu = JavaParser.parse(file);
                javaFiles.add(compilationUnitToJavaFile(cu));
            }
            catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        });
        DirectorySourceAggregate directorySourceAggregate = new DirectorySourceAggregate(directory.getAbsolutePath(), javaFiles);

        return directorySourceAggregate;
    }

    private FileSourceAggregate solveFileSource(final File file) {
        Assert.notNull(file, "File must not be null.");
        Assert.isTrue(FileExtension.JAVA.equals(FilesUtils.retrieveFileExtension(file)), "File must be java file.");

        JavaFile javaFile = null;
        try {
            CompilationUnit cu = JavaParser.parse(file);
            javaFile = compilationUnitToJavaFile(cu);
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        FileSourceAggregate fileSourceAggregate = new FileSourceAggregate(file.getAbsolutePath(), javaFile);

        return fileSourceAggregate;
    }


    private JavaFile compilationUnitToJavaFile(final CompilationUnit compilationUnit) {
        QualifiedName qualifiedName = retrieveQualifiedName(compilationUnit);

        Optional<Comment> commentOptional = compilationUnit.getType(0).getComment();
        String comment = "";
        if(commentOptional.isPresent()) {
            comment = commentOptional.get().getContent();
        }

        JavaFile javaFile = JavaFile.builder().qualifiedName(qualifiedName).comment(comment).build();
        return javaFile;
    }

    private QualifiedName retrieveQualifiedName(final CompilationUnit compilationUnit) {
        String className = compilationUnit.getType(0).getName().asString();
        String packageName = compilationUnit.getPackageDeclaration().get().getNameAsString();
        String qualifiedName = packageName + "." + className;

        return QualifiedName.of(qualifiedName);
    }

}
