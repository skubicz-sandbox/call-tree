package org.squbich.calltree.tools;

import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Created by Szymon on 2017-07-26.
 */
public class FilesUtils {

    public static List<File> findAllFiles(final File destination, final FileExtension fileType) {
        List<File> found = new ArrayList<File>();
        if (destination.isFile()) {
            if (fileType == null || fileType.equals(retrieveFileExtension(destination.getName()))) {
                found.add(destination);
            }
        }
        else if (destination.isDirectory()) {
            for (File file : destination.listFiles()) {
                found.addAll(findAllFiles(file, fileType));
            }
        }
        return found;
    }

    public static List<String> readFilesFromJar(final File jar, final FileExtension fileType) {
        List<String> found = new ArrayList<>();

        try {
                        JarFile jarFile = new JarFile(jar);
                       // JarEntry entry = null;

            Enumeration<JarEntry> e = jarFile.entries();
            while(e.hasMoreElements()) {
                JarEntry entry = e.nextElement();
                System.out.println("jar file: " + (entry != null ? entry.getName() : ""));
                if(entry != null && (fileType == null || fileType.equals(retrieveFileExtension(entry.getName())))) {
                    String file = IOUtils.toString(jarFile.getInputStream(entry), Charset.forName("UTF-8"));
                    found.add(file);
                }
            }
//                        for (Enumeration<JarEntry> e = jarFile.entries(); e.hasMoreElements(); entry = e.nextElement()) {
//                            System.out.println("jar file: " + (entry != null ? entry.getName() : ""));
//                            if(entry != null && (fileType == null || fileType.equals(retrieveFileExtension(entry.getName())))) {
//                                String file = IOUtils.toString(jarFile.getInputStream(entry), Charset.forName("UTF-8"));
//                                found.add(file);
//                            }
//                        }

//            ZipFile jarFile = new ZipFile(jar);
//            ZipEntry entry = null;
//
//            for (Enumeration<? extends ZipEntry> e = jarFile.entries(); e.hasMoreElements(); entry = e.nextElement()) {
//                System.out.println("jar file: " + (entry != null ? entry.getName() : ""));
//                if (entry != null && (fileType == null || fileType.equals(retrieveFileExtension(entry.getName())))) {
//                    String file = IOUtils.toString(jarFile.getInputStream(entry), Charset.forName("UTF-8"));
//                    found.add(file);
//                }
//            }
        //    getClasses(jar);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return found;
    }

    public static Class[] getClasses(File jar) throws ClassNotFoundException {
        ArrayList<Class> classes = new ArrayList<Class>();

        if (jar.exists()) {
            try {
                JarInputStream jarFile = new JarInputStream(new FileInputStream(jar.getAbsolutePath()));
                JarEntry jarEntry;

                while (true) {
                    jarEntry = jarFile.getNextJarEntry();
                    System.out.println("jar file: " + (jarEntry != null ? jarEntry.getName() : ""));
                    if (jarEntry == null) {
                        break;
                    }

                }
            }
            catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }
        else {
            return null;
        }
    }

    public static FileExtension retrieveFileExtension(final File file) {
        if (file.isFile()) {
            return retrieveFileExtension(file.getName());
        }
        return null;
    }

    public static FileExtension retrieveFileExtension(final String file) {
        return FileExtension.fromFileName(file);
    }
}
