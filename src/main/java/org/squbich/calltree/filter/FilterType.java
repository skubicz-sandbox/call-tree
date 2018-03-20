package org.squbich.calltree.filter;

import java.util.function.Predicate;

import org.squbich.calltree.model.calls.MethodCall;
import org.squbich.calltree.model.code.QualifiedName;

public enum FilterType {
    BY_METHOD {
        @Override
        public Predicate<MethodCall> predicate(final String methodName) {
            return (MethodCall call) -> {
                if (call == null || call.getMethod() == null || call.getMethod().getName() == null) {
                    return false;
                }
                return call.getMethod().getName().equals(methodName);
            };
        }
    },
    BY_METHOD_ANNOTATION {
        @Override
        public Predicate<MethodCall> predicate(String annotationType) {
            QualifiedName annotation = QualifiedName.of(annotationType);
            return (MethodCall call) -> {
                if (call == null || call.getMethod() == null || call.getMethod().getAnnotations() == null) {
                    return false;
                }
                return call.getMethod().getAnnotations().stream().filter(type -> type.equals(annotation)).findFirst().isPresent();
            };
        }
    },
    BY_PARENT_CLASS_NAME_REGEXP {
        @Override
        public Predicate<MethodCall> predicate(String parentClassNameRegexp) {
            return (MethodCall call) -> {
                if (call == null || call.getMethod() == null || call.getMethod().getParentClass() == null) {
                    return false;
                }
                return call.getMethod().getParentClass().getQualifiedName().toString().matches(parentClassNameRegexp);
            };
        }
    },
    BY_PARENT_CLASS_NAME {
        @Override
        public Predicate<MethodCall> predicate(String className) {
            QualifiedName classParam = QualifiedName.of(className);
            return (MethodCall call) -> {
                if (call == null || call.getMethod() == null || call.getMethod().getParentClass() == null) {
                    return false;
                }
                return call.getMethod().getParentClass().isEquals(classParam);
            };
        }
    };

    public abstract Predicate<MethodCall> predicate(String searchParam);
}