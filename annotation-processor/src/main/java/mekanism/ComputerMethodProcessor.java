package mekanism;

import com.squareup.javapoet.*;
import mekanism.builder.ComputerHandlerBuilder;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import java.io.IOException;
import java.util.*;

@SupportedSourceVersion(SourceVersion.RELEASE_17)
@SupportedAnnotationTypes({
        "mekanism.common.integration.computer.annotation.ComputerMethod",
        "mekanism.common.integration.computer.annotation.SyntheticComputerMethod",
        "mekanism.common.integration.computer.annotation.WrappingComputerMethod"
})
public class ComputerMethodProcessor extends AbstractProcessor {
    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        ComputerHandlerBuilder.init(processingEnv.getElementUtils(), processingEnv.getTypeUtils());
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotatedTypes, RoundEnvironment roundEnvironment) {
        //map annotated elements to multimap by enclosing class
        Map<TypeElement, List<Element>> annotatedElementsByParent = new HashMap<>();
        for (Element element : roundEnvironment.getElementsAnnotatedWithAny(annotatedTypes.toArray(new TypeElement[0]))) {
            annotatedElementsByParent.computeIfAbsent((TypeElement) element.getEnclosingElement(), i -> new ArrayList<>()).add(element);
        }

        for (Map.Entry<TypeElement, List<Element>> entry : annotatedElementsByParent.entrySet()) {
            processTypeWithAnnotations(entry.getKey(), entry.getValue());
        }

        return true;
    }

    private void processTypeWithAnnotations(TypeElement containingType, List<Element> annotatedElementList) {
        JavaFile factoryFile = new ComputerHandlerBuilder(containingType, processingEnv).build(annotatedElementList);
        try {
            factoryFile.writeTo(processingEnv.getFiler());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
