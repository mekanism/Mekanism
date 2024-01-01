package mekanism.util;

import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ElementVisitor;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;

/**
 * Shim type to reuse code which expects an AST parameter where one doesn't actually exist yet.
 */
public class FakeParameter implements VariableElement {

    private final TypeMirror type;
    private final Name name;

    public FakeParameter(TypeMirror type, String nameIn) {
        this.type = type;
        this.name = new Name() {
            @Override
            public boolean contentEquals(CharSequence cs) {
                return cs.equals(nameIn);
            }

            @Override
            public int length() {
                return nameIn.length();
            }

            @Override
            public char charAt(int index) {
                return nameIn.charAt(index);
            }

            @Override
            public CharSequence subSequence(int start, int end) {
                return nameIn.subSequence(start, end);
            }

            @Override
            public String toString() {
                return nameIn;
            }
        };
    }

    @Override
    public TypeMirror asType() {
        return type;
    }

    @Override
    public ElementKind getKind() {
        return ElementKind.PARAMETER;
    }

    @Override
    public Set<Modifier> getModifiers() {
        return Collections.emptySet();
    }

    @Override
    public Object getConstantValue() {
        return null;
    }

    @Override
    public Name getSimpleName() {
        return this.name;
    }

    @Override
    public Element getEnclosingElement() {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public List<? extends Element> getEnclosedElements() {
        return Collections.emptyList();
    }

    @Override
    public List<? extends AnnotationMirror> getAnnotationMirrors() {
        return Collections.emptyList();
    }

    @Override
    public <A extends Annotation> A getAnnotation(Class<A> annotationType) {
        return null;
    }

    @SuppressWarnings({"unchecked", "DataFlowIssue"})
    @Override
    public <A extends Annotation> A[] getAnnotationsByType(Class<A> annotationType) {
        return (A[]) new Object[0];
    }

    @Override
    public <R, P> R accept(ElementVisitor<R, P> v, P p) {
        return v.visitVariable(this, p);
    }
}
