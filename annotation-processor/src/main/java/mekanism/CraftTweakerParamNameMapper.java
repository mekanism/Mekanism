package mekanism;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.Types;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import org.openzen.zencode.java.ZenCodeType;

public class CraftTweakerParamNameMapper extends AbstractProcessor {

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        Map<String, List<String>> methods = new HashMap<>();
        Types typeUtils = processingEnv.getTypeUtils();
        for (Element annotatedElement : roundEnv.getElementsAnnotatedWith(ZenCodeType.Method.class)) {
            if (annotatedElement.getKind() == ElementKind.METHOD) {
                ExecutableElement executableElement = (ExecutableElement) annotatedElement;
                List<? extends VariableElement> parameters = executableElement.getParameters();
                if (!parameters.isEmpty()) {
                    StringBuilder keyBuilder = new StringBuilder(executableElement.getSimpleName());
                    keyBuilder.append('(');
                    StringBuilder resultBuilder = new StringBuilder();
                    for (VariableElement parameter : parameters) {
                        //Erase the type as we don't have it during reflection
                        keyBuilder.append(typeUtils.erasure(parameter.asType())).append(';');
                        resultBuilder.append(parameter.getSimpleName()).append(',');
                    }
                    methods.computeIfAbsent(executableElement.getEnclosingElement().toString(), clazz -> new ArrayList<>()).add(keyBuilder + ")=" + resultBuilder);
                }
            }
        }
        if (!methods.isEmpty()) {
            Filer filer = processingEnv.getFiler();
            try {
                FileObject resource = filer.createResource(StandardLocation.SOURCE_OUTPUT, "", "crafttweaker_param_names.txt");
                try (PrintWriter writer = new PrintWriter(new OutputStreamWriter(resource.openOutputStream(), StandardCharsets.UTF_8))) {
                    for (Map.Entry<String, List<String>> entry : methods.entrySet()) {
                        writer.println("Class: " + entry.getKey());
                        for (String method : entry.getValue()) {
                            writer.println(method);
                        }
                    }
                    if (writer.checkError()) {
                        throw new IOException("Error writing to the file");
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        //Don't mark the annotation as used to allow other processors to process them if we ever end up having any
        return false;
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return Collections.singleton(ZenCodeType.Method.class.getCanonicalName());
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        //TODO - 1.18: Java 17
        return SourceVersion.RELEASE_8;
    }
}