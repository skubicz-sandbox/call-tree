package org.squbich.calltree.model.code;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.squbich.calltree.serialize.JsonFilters;

import com.fasterxml.jackson.annotation.JsonFilter;

/**
 * Created by Szymon on 2017-07-29.
 */
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@JsonFilter(JsonFilters.METHOD)
public class Method {
    private ClassDescriptor parentClass;
    private List<QualifiedName> parameters;
    private List<QualifiedName> annotations;
    private QualifiedName returnType;
    private String comment;
    private String name;

//    private Method(ClassDescriptor parentClass, List<QualifiedName> parameters, QualifiedName returnType, String comment, String name) {
//        this.parentClass = parentClass;
//        this.parameters = parameters;
//        this.returnType = returnType == null ? QualifiedName.builder().namePart("void").build() : returnType;
//        this.comment = comment;
//        this.name = name;
//    }

    public static class MethodBuilder {

        // TODO do poprawy
        public MethodBuilder parameters2(List<String> parameterTypes) {
            List<QualifiedName> parameters = new ArrayList<>();
            if (parameterTypes != null) {
                parameterTypes.forEach(parameterType -> parameters.add(QualifiedName.of(parameterType)));
            }

            return parameters(parameters);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Method method = (Method) o;
        return Objects.equals(parentClass, method.parentClass) &&
                Objects.equals(parameters, method.parameters) &&
                Objects.equals(returnType, method.returnType) &&
                Objects.equals(name, method.name);
    }

    @Override
    public int hashCode() {

        return Objects.hash(parentClass, parameters, returnType, name);
    }

    @Override
    public String toString() {
        // != null ? returnType : "void")
        return String.valueOf(returnType) + " " + String.valueOf(parentClass) + "." + name + "(" + (parameters != null ? parameters
                .toString() : "") + ")";
    }

}
