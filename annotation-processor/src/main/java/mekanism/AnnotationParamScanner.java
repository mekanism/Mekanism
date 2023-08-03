package mekanism;

import javax.lang.model.element.Element;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public record AnnotationParamScanner(String targetFile, Set<Class<? extends Annotation>> supportedAnnotations, List<Element> originatingElements) {

    @SafeVarargs
    public AnnotationParamScanner(String targetFile, Class<? extends Annotation>... supportedAnnotations) {
        this(targetFile, Set.of(supportedAnnotations), new ArrayList<>());
    }
}