package mekanism;

import com.squareup.javapoet.JavaFile;
import mekanism.builder.ComputerHandlerBuilder;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Annotation processor to generate ComputerMethodFactory subclasses for computer methods.
 * Must only produce files directly related to the annotated elements, and not based on other elements.
 * This is due to being marked as an Isolating processor in Gradle.
 * <p>
 * Technically we violate this with the Wrapping methods, but if new methods are added to a wrapper,
 * a manually triggered full rebuild should catch it.
 */
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

        annotatedElementsByParent.forEach(this::processTypeWithAnnotations);

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
