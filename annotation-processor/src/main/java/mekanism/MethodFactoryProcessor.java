package mekanism;

import com.squareup.javapoet.*;
import mekanism.visitors.AnnotationHelper;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import java.io.IOException;
import java.util.*;

/**
 * Gathering (Gradle) annotation processor which generates a ComputerMethodRegistry for the Factories generated
 * by the {@link ComputerMethodProcessor} processor.
 */
@SupportedSourceVersion(SourceVersion.RELEASE_17)
@SupportedAnnotationTypes(MekAnnotationProcessors.COMPUTER_METHOD_FACTORY_ANNOTATION_CLASSNAME)
@SupportedOptions(MekAnnotationProcessors.MODULE_OPTION)
public class MethodFactoryProcessor extends AbstractProcessor {
    private String mekModule;
    private final ClassName factoryRegistry = ClassName.get("mekanism.common.integration.computer", "FactoryRegistry");
    private final MethodSpec.Builder registryInit = MethodSpec.methodBuilder("init").addModifiers(Modifier.STATIC, Modifier.PUBLIC);

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        mekModule = processingEnv.getOptions().getOrDefault(MekAnnotationProcessors.MODULE_OPTION, "value_not_supplied");
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotatedTypes, RoundEnvironment roundEnvironment) {
        TypeMirror methodFactoryType = processingEnv.getElementUtils().getTypeElement(MekAnnotationProcessors.COMPUTER_METHOD_FACTORY_ANNOTATION_CLASSNAME).asType();
        TypeSpec.Builder registryType = TypeSpec.classBuilder("ComputerMethodRegistry_" + mekModule).addModifiers(Modifier.PUBLIC);

        //this should only ever be 1 annotation
        for (Element element : roundEnvironment.getElementsAnnotatedWithAny(annotatedTypes.toArray(new TypeElement[0]))) {
            if (element instanceof TypeElement factoryTypeEl) {
                //get the annotation mirror for @MethodFactory
                AnnotationMirror annotationMirror = null;
                for (AnnotationMirror am : factoryTypeEl.getAnnotationMirrors()) {
                    if (typeUtils().isSameType(am.getAnnotationType(), methodFactoryType)) {
                        annotationMirror = am;
                        break;
                    }
                }
                if (annotationMirror == null) {
                    processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "Couldn't find annotation mirror", factoryTypeEl);
                    continue;
                }
                registryType.addOriginatingElement(factoryTypeEl);
                AnnotationHelper helper = new AnnotationHelper(processingEnv.getElementUtils(), annotationMirror);
                addHandlerToRegistry((TypeElement) processingEnv.getTypeUtils().asElement(helper.getClassValue("target")), ClassName.get(factoryTypeEl));
            }
        }

        if (registryType.originatingElements.size() > 0) {
            registryType.addMethod(registryInit.build());
            TypeSpec registrySpec = registryType.build();
            try {
                JavaFile.builder("mekanism.generated."+mekModule, registrySpec).build().writeTo(processingEnv.getFiler());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return false;
    }

    /**
     * Gather superclasses for handledType and add a register call
     * @param handledType the subject type of the handler
     * @param factoryClassName the factory's class name
     */
    private void addHandlerToRegistry(TypeElement handledType, ClassName factoryClassName) {
        //gather all superclasses (in mekanism package)
        List<ClassName> superClasses = new ArrayList<>();
        TypeMirror superClass = handledType.getSuperclass();
        TypeElement superTypeElement;
        do {
            superTypeElement = (TypeElement) typeUtils().asElement(superClass);
            if (superTypeElement == null) {
                break;
            }
            ClassName clazz = ClassName.get(superTypeElement);
            if (clazz.canonicalName().startsWith("mekanism")) {
                superClasses.add(0, clazz);
            }
        } while ((superClass = superTypeElement.getSuperclass()).getKind() != TypeKind.NONE);

        //add register call to the factory
        CodeBlock.Builder registerStatement = CodeBlock.builder()
                .add("$T.register($T.class, $T::new", factoryRegistry, processingEnv.getTypeUtils().erasure(handledType.asType()), factoryClassName);
        //add all super classes, so we don't have to calculate at runtime
        for (ClassName cls : superClasses) {
            registerStatement.add(", $T.class", cls);
        }
        registerStatement.add(")");
        registryInit.addStatement(registerStatement.build());
    }

    private Types typeUtils() {
        return processingEnv.getTypeUtils();
    }
}
