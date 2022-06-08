package mekanism;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.lang.annotation.Annotation;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import mekanism.common.integration.computer.annotation.ComputerMethod;
import mekanism.common.integration.computer.annotation.WrappingComputerMethod;
import org.openzen.zencode.java.ZenCodeType;

public class ParamNameMapper extends AbstractProcessor {

    private static final String MODULE_OPTION = "mekanismModule";

    private static String forDatagen(String path) {
        return "resources/" + path;
    }

    private static AnnotationParamScanner computer(String module) {
        return new AnnotationParamScanner("annotation_generated/data/" + module + "/parameter_names/computer", ComputerMethod.class, WrappingComputerMethod.class);
    }

    private Set<AnnotationParamScanner> scanners = Collections.emptySet();

    @Override
    public Set<String> getSupportedOptions() {
        return Collections.singleton(MODULE_OPTION);
    }

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        String mekModule = processingEnv.getOptions().getOrDefault(MODULE_OPTION, "mekanism");
        if (mekModule.equals("mekanism")) {
            scanners = Set.of(
                  new AnnotationParamScanner(forDatagen("crafttweaker_parameter_names"), ZenCodeType.Method.class),
                  computer(mekModule)
            );
        } else {
            scanners = Collections.singleton(computer(mekModule));
        }
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        Types typeUtils = processingEnv.getTypeUtils();
        Elements elementUtils = processingEnv.getElementUtils();
        Set<Class<? extends Annotation>> supportedAnnotations = getSupportedAnnotations();
        Map<AnnotationParamScanner, JsonObject> annotatedData = new HashMap<>();
        for (Element annotatedElement : roundEnv.getElementsAnnotatedWithAny(supportedAnnotations)) {
            if (annotatedElement.getKind() == ElementKind.METHOD) {
                ExecutableElement executableElement = (ExecutableElement) annotatedElement;
                List<? extends VariableElement> parameters = executableElement.getParameters();
                if (!parameters.isEmpty()) {
                    String methodName = executableElement.getSimpleName().toString();
                    StringBuilder signatureBuilder = new StringBuilder("(");
                    JsonArray paramNames = new JsonArray(parameters.size());
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
                                JsonObject classMethods = annotatedData.computeIfAbsent(scanner, a -> new JsonObject());
                                JsonObject methods = getOrAddObject(classMethods, className);
                                JsonObject signatures = getOrAddObject(methods, methodName);
                                if (paramNames.size() == 1) {//Flatten the array to a single element
                                    signatures.addProperty(methodSignature, paramNames.get(0).getAsString());
                                } else {
                                    signatures.add(methodSignature, paramNames);
                                }
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
            for (Map.Entry<AnnotationParamScanner, JsonObject> entry : annotatedData.entrySet()) {
                AnnotationParamScanner scanner = entry.getKey();
                //Note: We know allMethods is not empty, as we only add annotated data if we have a method to add
                // We also sort the methods to ensure the order is consistent when saved
                JsonObject allMethods = sortJson(entry.getValue());
                try {
                    FileObject resource = filer.createResource(StandardLocation.SOURCE_OUTPUT, "", scanner.targetFile() + ".json");
                    try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(resource.openOutputStream(), StandardCharsets.UTF_8))) {
                        writer.write(gson.toJson(allMethods));
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        //Don't mark the annotation as used to allow other processors to process them if we ever end up having any
        return false;
    }

    private static JsonObject sortJson(JsonObject obj) {
        if (obj.size() < 2) {
            //Don't bother creating new objects to sort it if there isn't multiple elements
            return obj;
        }
        JsonObject sorted = new JsonObject();
        for (String key : new TreeSet<>(obj.keySet())) {
            JsonElement element = obj.get(key);
            if (element.isJsonObject()) {
                element = sortJson(element.getAsJsonObject());
            }
            sorted.add(key, element);
        }
        return sorted;
    }

    private JsonObject getOrAddObject(JsonObject parent, String key) {
        JsonObject element = parent.getAsJsonObject(key);
        if (element == null) {
            parent.add(key, element = new JsonObject());
        }
        return element;
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
        PackageElement pkg = elementUtils.getPackageOf(typeUtils.asElement(type));
        String packageName = pkg.getQualifiedName().toString();
        String className = type.toString();
        if (packageName.isEmpty()) {
            return className.replace('.', '$');
        } else if (className.startsWith(packageName)) {
            //Should always start with but validate to make sure, also increment by one to get past the dot between the package and the class
            String path = className.substring(packageName.length() + 1);
            return packageName.replace('.', '/') + "/" + path.replace('.', '$');
        }
        throw new IllegalStateException("Fully qualified name of Class '" + className + "' does not start with expected package: " + packageName);
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
        return SourceVersion.RELEASE_17;
    }
}