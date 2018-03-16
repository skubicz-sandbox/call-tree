package org.squbich.calltree.model.code;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.List;
import java.util.Objects;

/**
 * Created by Szymon on 2017-07-28.
 */
@Builder
@Getter
@EqualsAndHashCode
public class ClassDescriptor {
    private QualifiedName qualifiedName;
    private List<QualifiedName> annotations;
    private String comment;

    public boolean isEquals(QualifiedName qualifiedName) {
        return this.qualifiedName.equals(qualifiedName);
    }

    @Override
    public String toString() {
        return qualifiedName.toString();
    }
}