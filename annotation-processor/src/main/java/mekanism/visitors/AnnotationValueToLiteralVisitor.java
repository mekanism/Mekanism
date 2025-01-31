package mekanism.visitors;

import com.palantir.javapoet.ClassName;
import com.palantir.javapoet.CodeBlock;
import java.util.List;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.SimpleAnnotationValueVisitor14;

/**
 * Converts an annotation value to a format suitable for a CodeBlock's $L formatter
 */
public class AnnotationValueToLiteralVisitor extends SimpleAnnotationValueVisitor14<Object, TypeMirror> {

    AnnotationValueToLiteralVisitor() {
    }

    public AnnotationValueToLiteralVisitor(Object defaultValue) {
        super(defaultValue);
    }

    @Override
    public Object visitString(String s, TypeMirror valueType) {
        return s != null && !s.isBlank() ? CodeBlock.of("$S", s) : super.defaultAction(s, valueType);
    }

    @Override
    public Object visitEnumConstant(VariableElement c, TypeMirror valueType) {
        return CodeBlock.of("$T.$L", ClassName.get(c.asType()), c.getSimpleName());
    }

    @Override
    public Object visitArray(List<? extends AnnotationValue> vals, TypeMirror valueType) {
        TypeMirror componentType = ((ArrayType) valueType).getComponentType();
        if (vals == null || vals.isEmpty()) {
            return CodeBlock.of("new $T[0]", ClassName.get(componentType));
        } else {
            AnnotationValueToLiteralVisitor elementVisitor = new AnnotationValueToLiteralVisitor();
            CodeBlock elements = vals.stream().map(value -> {
                Object mappedValue = value.accept(elementVisitor, componentType);
                return CodeBlock.of((mappedValue instanceof String ? "$S" : "$L"), mappedValue);
            }).collect(CodeBlock.joining(", "));
            return CodeBlock.of("new $T[]{$L}", ClassName.get(componentType), elements);
        }
    }

    @Override
    public Object visitBoolean(boolean b, TypeMirror typeMirror) {
        return b;
    }

    @Override
    public Object visitByte(byte b, TypeMirror typeMirror) {
        return b;
    }

    @Override
    public Object visitChar(char c, TypeMirror typeMirror) {
        return c;
    }

    @Override
    public Object visitDouble(double d, TypeMirror typeMirror) {
        return d;
    }

    @Override
    public Object visitFloat(float f, TypeMirror typeMirror) {
        return f;
    }

    @Override
    public Object visitInt(int i, TypeMirror typeMirror) {
        return i;
    }

    @Override
    public Object visitLong(long i, TypeMirror typeMirror) {
        return i;
    }

    @Override
    public Object visitShort(short s, TypeMirror typeMirror) {
        return s;
    }

    @Override
    public Object visitType(TypeMirror t, TypeMirror typeMirror) {
        return t;
    }

    @Override
    public Object visitAnnotation(AnnotationMirror a, TypeMirror typeMirror) {
        throw new IllegalStateException("Don't know how to convert annotation to literal");
    }
}
