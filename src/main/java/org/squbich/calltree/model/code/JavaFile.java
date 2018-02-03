package org.squbich.calltree.model.code;

import lombok.Builder;
import lombok.Getter;

/**
 * Created by Szymon on 2017-07-28.
 */
@Builder
@Getter
public class JavaFile {
    private QualifiedName qualifiedName;
    private String comment;

    public ClassDescriptor descriptor() {
        ClassDescriptor classDescriptor = ClassDescriptor.builder().comment(comment).qualifiedName(qualifiedName).build();
        return classDescriptor;
    }
}
