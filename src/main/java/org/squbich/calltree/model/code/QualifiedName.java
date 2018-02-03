package org.squbich.calltree.model.code;

import lombok.Builder;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.squbich.calltree.tools.Assert;

import java.util.Objects;

/**
 * Created by Szymon on 2017-07-28.
 */
@Getter
@Builder
public class QualifiedName {
    private String namePart;
    private String packagePart;

    public static QualifiedName of(final String qualifiedName) {
        Assert.notNull(qualifiedName, "Parameter qualifiedName must not be null.");

        int lastDotIndex = qualifiedName.lastIndexOf('.');

        if(lastDotIndex >= 0) {
            String packagePart = qualifiedName.substring(0, lastDotIndex);
            String namePart = qualifiedName.substring(lastDotIndex + 1);
            return QualifiedName.builder().namePart(namePart).packagePart(packagePart).build();
        } else {
            return QualifiedName.builder().namePart(qualifiedName).packagePart("").build();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        QualifiedName that = (QualifiedName) o;
        return Objects.equals(namePart, that.namePart) &&
                Objects.equals(packagePart, that.packagePart);
    }

    @Override
    public int hashCode() {
        return Objects.hash(namePart, packagePart);
    }

    @Override
    public String toString() {
        if(StringUtils.isNotBlank(packagePart)) {
            return packagePart + "." + namePart;
        } else {
            return namePart;
        }
    }
}