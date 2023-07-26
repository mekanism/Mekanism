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

public class AnnotationHelper {
    private final Map<? extends ExecutableElement, ? extends AnnotationValue> annotationValueMap;
    private final Map<String, ExecutableElement> nameToElement = new HashMap<>();

    public AnnotationHelper(Elements elementUtils, AnnotationMirror annotationMirror) {
        this.annotationValueMap = elementUtils.getElementValuesWithDefaults(annotationMirror);
        for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> value : elementUtils.getElementValuesWithDefaults(annotationMirror).entrySet()) {
            //annotationValueMap.put(value.getKey().getSimpleName().toString(), value.getValue());
            nameToElement.put(value.getKey().getSimpleName().toString(), value.getKey());
        }
    }

    public Object getLiteral(String key, Object defaultValue) {
        AnnotationValue value = annotationValueMap.get(nameToElement.get(key));
        return value.accept(new AnnotationValueToLiteralVisitor(
                defaultValue instanceof String ? CodeBlock.of("$S", defaultValue) : defaultValue
        ), nameToElement.get(key).getReturnType());
    }

    public String getStringValue(String key, String defaultValue) {
        AnnotationValue value = annotationValueMap.get(nameToElement.get(key));
        if (value != null && value.getValue() instanceof String s && !s.isBlank())
            return s;
        return defaultValue;
    }

    public TypeMirror getClassValue(String key) {
        AnnotationValue value = annotationValueMap.get(nameToElement.get(key));
        if (value.getValue() instanceof TypeMirror tm) {
            return tm;
        }
        return null;
    }

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
