package org.squbich.calltree.model.code;

import lombok.Builder;
import lombok.Getter;

import java.util.Objects;

/**
 * Created by Szymon on 2017-07-28.
 */
@Builder
@Getter
public class ClassDescriptor {
    private QualifiedName qualifiedName;
    private String comment;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ClassDescriptor that = (ClassDescriptor) o;
        return Objects.equals(qualifiedName, that.qualifiedName);
    }

    @Override
    public int hashCode() {

        return Objects.hash(qualifiedName);
    }

    @Override
    public String toString() {
        return qualifiedName.toString();
    }
}