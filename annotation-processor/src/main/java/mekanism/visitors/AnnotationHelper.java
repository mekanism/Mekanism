package mekanism.visitors;

import com.squareup.javapoet.CodeBlock;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.SimpleAnnotationValueVisitor14;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Helper methods for using an AnnotationMirror and getting elements by name.
 */
public class AnnotationHelper {
    private final Map<? extends ExecutableElement, ? extends AnnotationValue> annotationValueMap;
    private final Map<String, ExecutableElement> nameToElement = new HashMap<>();

    public AnnotationHelper(Elements elementUtils, AnnotationMirror annotationMirror) {
        this.annotationValueMap = elementUtils.getElementValuesWithDefaults(annotationMirror);
        for (ExecutableElement key : this.annotationValueMap.keySet()) {
            nameToElement.put(key.getSimpleName().toString(), key);
        }
    }

    /**
     * Get a value suitable for use in an $L substitution. May be raw primitive or CodeBlock
     *
     * @param key the annotation member name
     * @param defaultValue a default value to use if no value found or string is empty
     * @return a raw primitive or CodeBlock representing the value
     */
    public Object getLiteral(String key, Object defaultValue) {
        ExecutableElement element = nameToElement.get(key);
        if (element == null) {
            return defaultValue;
        }
        AnnotationValue value = annotationValueMap.get(element);
        return value.accept(new AnnotationValueToLiteralVisitor(
                defaultValue instanceof String ? CodeBlock.of("$S", defaultValue) : defaultValue
        ), element.getReturnType());
    }

    /**
     * Get a string value from the annotation.
     * @param key the annotation member name
     * @param defaultValue a value to return if the value found is empty or not a string
     * @return the string value or the default
     */
    public String getStringValue(String key, String defaultValue) {
        AnnotationValue value = annotationValueMap.get(nameToElement.get(key));
        if (value != null && value.getValue() instanceof String s && !s.isBlank())
            return s;
        return defaultValue;
    }

    /**
     * Get a Class value from the annotation
     * @param key the annotation member name
     * @return a TypeMirror or null if not a class value
     */
    public TypeMirror getClassValue(String key) {
        AnnotationValue value = annotationValueMap.get(nameToElement.get(key));
        if (value.getValue() instanceof TypeMirror tm) {
            return tm;
        }
        return null;
    }

    /**
     * Get a list of String values from the annotation. Non string values will be ignored
     * @param key the annotation member name
     * @return a list with any values found
     */
    public List<String> getStringArray(String key) {
        AnnotationValue value = annotationValueMap.get(nameToElement.get(key));
        List<String> returnVal = new ArrayList<>();
        value.accept(new SimpleAnnotationValueVisitor14<Void, Void>() {
            @Override
            public Void visitArray(List<? extends AnnotationValue> vals, Void unused) {
                for (AnnotationValue annotationValue : vals) {
                    annotationValue.accept(this, null);
                }
                return null;
            }

            @Override
            public Void visitString(String s, Void unused) {
                if (s != null && !s.isBlank()) {
                    returnVal.add(s);
                }
                return null;
            }
        }, null);
        return returnVal;
    }
}
