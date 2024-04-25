package mekanism;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import org.openzen.zencode.java.ZenCodeType;

public class ParamNameMapper extends AbstractProcessor {

    public static final Type PARAM_TREE_TYPE_TOKEN = new TypeToken<Map</*class name*/String, Map</*method name*/String, Map</*descriptor*/String,/*params*/List<String>>>>>() {
    }.getType();

    private Set<AnnotationParamScanner> scanners = Collections.emptySet();

    @Override
    public Set<String> getSupportedOptions() {
        return Collections.singleton(MekAnnotationProcessors.MODULE_OPTION);
    }

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        String mekModule = processingEnv.getOptions().getOrDefault(MekAnnotationProcessors.MODULE_OPTION, "mekanism");
        if (mekModule.equals("mekanism")) {
            scanners = Set.of(
                  new AnnotationParamScanner("crafttweaker_parameter_names", ZenCodeType.Method.class)
            );
        }
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        Types typeUtils = processingEnv.getTypeUtils();
        Elements elementUtils = processingEnv.getElementUtils();
        Set<Class<? extends Annotation>> supportedAnnotations = getSupportedAnnotations();
        Map<AnnotationParamScanner, Map</*class name*/String, Map</*method name*/String, Map</*descriptor*/String,/*params*/List<String>>>>> annotatedData = new IdentityHashMap<>();
        for (Element annotatedElement : roundEnv.getElementsAnnotatedWithAny(supportedAnnotations)) {
            if (annotatedElement.getKind() == ElementKind.METHOD) {
                ExecutableElement executableElement = (ExecutableElement) annotatedElement;
                List<? extends VariableElement> parameters = executableElement.getParameters();
                if (!parameters.isEmpty()) {
                    String methodName = executableElement.getSimpleName().toString();
                    StringBuilder signatureBuilder = new StringBuilder("(");
                    List<String> paramNames = new ArrayList<>();
                    for (VariableElement parameter : parameters) {
                        signatureBuilder.append(getParamDescriptor(typeUtils, elementUtils, parameter.asType()));
                        paramNames.add(parameter.getSimpleName().toString());
                    }
                    String methodSignature = signatureBuilder + ")" + getParamDescriptor(typeUtils, elementUtils, executableElement.getReturnType());
                    String className = executableElement.getEnclosingElement().toString();
                    for (AnnotationParamScanner scanner : scanners) {
                        //For each scanner see if it should have data about this method added based on the
                        // annotations that this element possesses
                        for (Class<? extends Annotation> annotation : scanner.supportedAnnotations()) {
                            if (annotatedElement.getAnnotation(annotation) != null) {
                                scanner.originatingElements().add(annotatedElement);
                                Map<String, List<String>> signatures = annotatedData
                                      .computeIfAbsent(scanner, unused -> new TreeMap<>())
                                      .computeIfAbsent(className, unused -> new TreeMap<>())
                                      .computeIfAbsent(methodName, unused -> new TreeMap<>());
                                signatures.put(methodSignature, paramNames);
                                //We can skip checking other annotations this scanner may support as we have
                                // already added the signature to this scanner
                                break;
                            }
                        }
                    }
                }
            }
        }
        if (!annotatedData.isEmpty()) {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            Filer filer = processingEnv.getFiler();
            //Note: We know allMethods is not empty, as we only add annotated data if we have a method to add
            // We also sort the methods to ensure the order is consistent when saved
            for (Entry<AnnotationParamScanner, Map<String, Map<String, Map<String, List<String>>>>> entry : annotatedData.entrySet()) {
                AnnotationParamScanner scanner = entry.getKey();
                Map<String, Map<String, Map<String, List<String>>>> allMethods = entry.getValue();
                try {
                    FileObject resource = filer.createResource(StandardLocation.CLASS_OUTPUT, "", scanner.targetFile() + ".json", scanner.originatingElements().toArray(new Element[0]));
                    try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(resource.openOutputStream(), StandardCharsets.UTF_8))) {
                        writer.write(gson.toJson(allMethods, PARAM_TREE_TYPE_TOKEN));
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        //Don't mark the annotation as used to allow other processors to process them if we ever end up having any
        return false;
    }

    private String getParamDescriptor(Types typeUtils, Elements elementUtils, TypeMirror type) {
        //Erase the type as we don't have it during reflection
        type = typeUtils.erasure(type);
        return switch (type.getKind()) {
            case ARRAY -> "[" + getParamDescriptor(typeUtils, elementUtils, ((ArrayType) type).getComponentType());
            case BOOLEAN -> "Z";
            case BYTE -> "B";
            case CHAR -> "C";
            case DOUBLE -> "D";
            case FLOAT -> "F";
            case INT -> "I";
            case LONG -> "J";
            case SHORT -> "S";
            case VOID -> "V";
            case DECLARED -> "L" + getClassDescriptor(typeUtils, elementUtils, type) + ";";
            default -> throw new IllegalStateException("Unexpected value: " + type.getKind());
        };
    }

    private String getClassDescriptor(Types typeUtils, Elements elementUtils, TypeMirror type) {
        Name binaryName = elementUtils.getBinaryName((TypeElement) typeUtils.asElement(type));
        return binaryName.toString().replace('.', '/');
    }

    private Set<Class<? extends Annotation>> getSupportedAnnotations() {
        return scanners.stream()
              .flatMap(scanner -> scanner.supportedAnnotations().stream())
              .collect(Collectors.toUnmodifiableSet());
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return scanners.stream()
              .flatMap(scanner -> scanner.supportedAnnotations().stream())
              .map(Class::getCanonicalName)
              .collect(Collectors.toUnmodifiableSet());
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.RELEASE_21;
    }
}