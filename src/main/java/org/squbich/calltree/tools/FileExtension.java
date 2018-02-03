package org.squbich.calltree.tools;

/**
 * Created by Szymon on 2017-07-26.
 */
public interface FileExtension {
    FileExtension JAVA = of(".java");
    FileExtension CLASS = of(".class");
    FileExtension JAR = of(".jar");

    String asText();

    static FileExtension fromFileName(final String fileName) {
        String[] splitFile = fileName.split("\\.");
        if(splitFile.length > 1) {
            String extension = splitFile[splitFile.length - 1];
            return of("." + extension);
        }
        return null;
    }

    static FileExtension of(final String extension) {
        FileExtension fileExtension = new FileExtensionImpl(extension);

        return fileExtension;
    }

    class FileExtensionImpl implements FileExtension {
        private String textRepresentation;

        private FileExtensionImpl(final String textRepresentation) {
            this.textRepresentation = textRepresentation;
        }

        @Override
        public String asText() {
            return this.textRepresentation;
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            FileExtensionImpl that = (FileExtensionImpl) o;

            return textRepresentation != null ? textRepresentation.equals(that.textRepresentation) : that.textRepresentation == null;
        }

        @Override
        public int hashCode() {
            return textRepresentation != null ? textRepresentation.hashCode() : 0;
        }
    }
}