package org.squbich.calltree.model.code;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

/**
 * Created by Szymon on 2017-07-28.
 */
@Builder
@Getter
@ToString
public class JavaFile {
    private QualifiedName qualifiedName;
    private String comment;

    public ClassDescriptor descriptor() {
        ClassDescriptor classDescriptor = ClassDescriptor.builder().comment(comment).qualifiedName(qualifiedName).build();
        return classDescriptor;
    }
}
