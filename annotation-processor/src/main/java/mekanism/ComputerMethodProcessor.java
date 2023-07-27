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

/**
 * Created by Thiakil on 15/07/2023.
 */
@SupportedSourceVersion(SourceVersion.RELEASE_17)
@SupportedAnnotationTypes({
        "mekanism.common.integration.computer.annotation.ComputerMethod",
        "mekanism.common.integration.computer.annotation.SyntheticComputerMethod",
        "mekanism.common.integration.computer.annotation.WrappingComputerMethod"
})
@SupportedOptions(MekAnnotationProcessors.MODULE_OPTION)
public class ComputerMethodProcessor extends AbstractProcessor {

    //private final ClassName lazyInterfaceRaw = ClassName.get("net.minecraftforge.common.util", "Lazy");
    //private final ParameterizedTypeName lazyMethodHandleType = ParameterizedTypeName.get(lazyInterfaceRaw, ClassName.get(MethodHandle.class));

    private final ClassName factoryRegistry = ClassName.get("mekanism.common.integration.computer", "FactoryRegistry");

    private String mekModule;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        mekModule = processingEnv.getOptions().getOrDefault(MekAnnotationProcessors.MODULE_OPTION, "value_not_supplied");
        ComputerHandlerBuilder.init(processingEnv.getElementUtils(), processingEnv.getTypeUtils());
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotatedTypes, RoundEnvironment roundEnvironment) {
        //map annotated elements to multimap by enclosing class
        Map<TypeElement, List<Element>> annotatedElementsByParent = new HashMap<>();
        for (Element element : roundEnvironment.getElementsAnnotatedWithAny(annotatedTypes.toArray(new TypeElement[0]))) {
            annotatedElementsByParent.computeIfAbsent((TypeElement) element.getEnclosingElement(), i -> new ArrayList<>()).add(element);
        }

        TypeSpec.Builder registryType = TypeSpec.classBuilder("ComputerMethodRegistry_" + mekModule).addModifiers(Modifier.PUBLIC);
        MethodSpec.Builder registryInit = MethodSpec.methodBuilder("init").addModifiers(Modifier.STATIC, Modifier.PUBLIC);

        for (Map.Entry<TypeElement, List<Element>> entry : annotatedElementsByParent.entrySet()) {
            processTypeWithAnnotations(registryType, registryInit, entry.getKey(), entry.getValue());
        }

        if (annotatedElementsByParent.size() > 0) {
            registryType.addMethod(registryInit.build());
            TypeSpec registrySpec = registryType.build();
            try {
                JavaFile.builder("mekanism.generated."+mekModule, registrySpec).build().writeTo(processingEnv.getFiler());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        return true;
    }

    private void processTypeWithAnnotations(TypeSpec.Builder registryType, MethodSpec.Builder registryInit, TypeElement containingType, List<Element> annotatedElementList) {
        //todo move registry to separate processor
        registryType.addOriginatingElement(containingType);
        for (Element element : annotatedElementList) {
            registryType.addOriginatingElement(element);
        }

        ClassName containingClassName = ClassName.get(containingType);

        JavaFile factoryFile = new ComputerHandlerBuilder(containingType, processingEnv).build(annotatedElementList);
        try {
            factoryFile.writeTo(processingEnv.getFiler());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        addHandlerToRegistry(registryInit, factoryRegistry, containingType, containingClassName, ClassName.get(factoryFile.packageName, factoryFile.typeSpec.name));
    }

    private void addHandlerToRegistry(MethodSpec.Builder registryInit, ClassName factoryRegistry, TypeElement containingType, ClassName containingClassName, ClassName factoryClassName) {
        //gather all superclasses (in mekanism package)
        List<ClassName> superClasses = new ArrayList<>();
        TypeMirror superClass = containingType.getSuperclass();
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
                .add("$T.register($T.class, $T::new", factoryRegistry, containingClassName, factoryClassName);
        //add all super classes, so we don't have to calculate at runtime
        for (ClassName cls : superClasses) {
            registerStatement.add(", $T.class", cls);
        }
        registerStatement.add(")");
        registryInit.addStatement(registerStatement.build());
    }



    private Elements elementUtils() {
        return processingEnv.getElementUtils();
    }

    private Types typeUtils() {
        return processingEnv.getTypeUtils();
    }
}
