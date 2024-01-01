package mekanism;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.lang.model.element.Element;

public record AnnotationParamScanner(String targetFile, Set<Class<? extends Annotation>> supportedAnnotations, List<Element> originatingElements) {

    @SafeVarargs
    public AnnotationParamScanner(String targetFile, Class<? extends Annotation>... supportedAnnotations) {
        this(targetFile, Set.of(supportedAnnotations), new ArrayList<>());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        return (o instanceof AnnotationParamScanner that) &&
               targetFile.equals(that.targetFile) &&
               supportedAnnotations.equals(that.supportedAnnotations);
    }

    @Override
    public int hashCode() {
        int result = targetFile.hashCode();
        result = 31 * result + supportedAnnotations.hashCode();
        return result;
    }
}
