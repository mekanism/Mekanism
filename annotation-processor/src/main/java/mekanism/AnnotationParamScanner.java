package mekanism;

import java.lang.annotation.Annotation;
import java.util.Set;

public record AnnotationParamScanner(String targetFile, Set<Class<? extends Annotation>> supportedAnnotations) {

    @SafeVarargs
    public AnnotationParamScanner(String targetFile, Class<? extends Annotation>... supportedAnnotations) {
        this(targetFile, Set.of(supportedAnnotations));
    }
}