package mekanism;

import com.squareup.javapoet.*;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.type.*;
import javax.lang.model.util.*;
import java.io.IOException;
import java.lang.invoke.MethodHandle;
import java.lang.reflect.Method;
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
public class ComputerMethodProcessor extends AbstractProcessor {
    private static final String MODULE_OPTION = "mekanismModule";

    TypeMirror filterInterface;
    private ClassName fancyComputerHelper = ClassName.get("mekanism.common.integration.computer", "FancyComputerHelper");
    private ClassName computerException = ClassName.get("mekanism.common.integration.computer", "ComputerException");
    private ClassName lazyInterfaceRaw = ClassName.get("net.minecraftforge.common.util", "Lazy");
    private ParameterizedTypeName lazyMethodHandleType = ParameterizedTypeName.get(lazyInterfaceRaw, ClassName.get(MethodHandle.class));
    private ParameterSpec helperParam = ParameterSpec.builder(fancyComputerHelper, "helper").build();
    private ParamToHelperMapper paramToHelperMapper = new ParamToHelperMapper();
    private String mekModule;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        mekModule = processingEnv.getOptions().getOrDefault(MODULE_OPTION, "value_not_supplied");
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotatedTypes, RoundEnvironment roundEnvironment) {
        Types typeUtils = processingEnv.getTypeUtils();
        Elements elementUtils = processingEnv.getElementUtils();
        TypeElement computerMethodAnnotationType = elementUtils.getTypeElement("mekanism.common.integration.computer.annotation.ComputerMethod");

        //map annotated elements to multimap by enclosing class
        Map<TypeElement, List<Element>> annotatedElementsByParent = new HashMap<>();
        for (Element element : roundEnvironment.getElementsAnnotatedWithAny(annotatedTypes.toArray(new TypeElement[0]))) {
            annotatedElementsByParent.computeIfAbsent((TypeElement) element.getEnclosingElement(), i -> new ArrayList<>()).add(element);
        }

        TypeSpec.Builder registryType = TypeSpec.classBuilder("ComputerMethodRegistry_"+mekModule).addModifiers(Modifier.PUBLIC);
        MethodSpec.Builder registryInit = MethodSpec.methodBuilder("init").addModifiers(Modifier.STATIC, Modifier.PUBLIC);
        ClassName factoryRegistry = ClassName.get("mekanism.common.integration.computer", "FactoryRegistry");

        ClassName computerMethodFactoryRaw = ClassName.get("mekanism.common.integration.computer", "ComputerMethodFactory");
        filterInterface = typeUtils.erasure(elementUtils.getTypeElement("mekanism.common.content.filter.IFilter").asType());

        for (Map.Entry<TypeElement, List<Element>> entry : annotatedElementsByParent.entrySet()) {
            TypeElement containingType = entry.getKey();
            String handlerClassName = "Handler" + containingType.getSimpleName();
            ClassName containingClassName = ClassName.get(containingType);

            TypeSpec.Builder typeSpec = TypeSpec.classBuilder(handlerClassName)
                    .superclass(ParameterizedTypeName.get(computerMethodFactoryRaw, containingClassName))
                    .addOriginatingElement(containingType)
                    .addModifiers(Modifier.PUBLIC);
            ParameterSpec subjectParam = ParameterSpec.builder(containingClassName, "subject").build();
            registryType.addOriginatingElement(containingType);

            MethodSpec.Builder constructorBuilder = MethodSpec.constructorBuilder().addModifiers(Modifier.PUBLIC);

            for (Element annotatedElement : entry.getValue()) {
                String annotatedName = annotatedElement.getSimpleName().toString();
                typeSpec.addOriginatingElement(annotatedElement);
                registryType.addOriginatingElement(annotatedElement);
                annotatedElement.getAnnotationMirrors().stream().filter(it -> it.getAnnotationType().equals(computerMethodAnnotationType.asType())).findFirst().ifPresent(regularComputerMethodAnnotation -> {
                    AnnotationHelper values = new AnnotationHelper(elementUtils, regularComputerMethodAnnotation);
                    MethodSpec.Builder myMethodBuilder = MethodSpec.methodBuilder(annotatedName)
                            .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                            .returns(Object.class)
                            .addException(computerException)
                            .addParameter(subjectParam)
                            .addParameter(helperParam);
                    ExecutableElement executableElement = (ExecutableElement) annotatedElement;
                    @SuppressWarnings("unchecked")
                    List<VariableElement> parameters = (List<VariableElement>) executableElement.getParameters();

                    {
                        CodeBlock.Builder paramTypes = CodeBlock.builder();
                        CodeBlock.Builder methodCallArguments = CodeBlock.builder();
                        if (!parameters.isEmpty()) {
                            List<CodeBlock> paramGetters = new ArrayList<>();
                            for (int i = 0; i < parameters.size(); i++) {
                                TypeMirror paramType = parameters.get(i).asType();
                                paramGetters.add(paramType.accept(paramToHelperMapper, i));
                                paramTypes.add(", $T.class", typeUtils.erasure(paramType));
                            }
                            methodCallArguments.add(CodeBlock.join(paramGetters, ", "));
                        }
                        boolean isPublic = annotatedElement.getModifiers().contains(Modifier.PUBLIC);
                        boolean isStatic = annotatedElement.getModifiers().contains(Modifier.STATIC);
                        CodeBlock.Builder codeBuilder = CodeBlock.builder();
                        TypeMirror returnType = executableElement.getReturnType();
                        if (isPublic) {
                            if (isStatic) {
                                codeBuilder.add("$T.$L(", containingClassName, annotatedName);
                            } else {
                                codeBuilder.add("$N.$L(", subjectParam, annotatedName);
                            }
                        } else {
                            //MethodSpec handleSupplierMethod = MethodSpec.methodBuilder("methodHandleSupplier_"+annotatedName)
                            //        .addModifiers(Modifier.STATIC, Modifier.PRIVATE)
                            //        .returns(MethodHandle.class)
                            //        .addCode(
                            //        )
                            //        .build();
                            //typeSpec.addMethod(handleSupplierMethod);
                            FieldSpec myMethodField = FieldSpec.builder(lazyMethodHandleType, "handle_" + annotatedName, Modifier.PRIVATE, Modifier.STATIC)
                                    .initializer("$T.of(()->{\n$>$L\n$<})", lazyInterfaceRaw, CodeBlock.builder()
                                            .beginControlFlow("try")
                                            .addStatement("$T method = $T.class.getDeclaredMethod($S$L)", Method.class, containingClassName, annotatedName, paramTypes.build())
                                            .addStatement("method.setAccessible(true)")
                                            .addStatement("return lookup.unreflect(method)")
                                            .nextControlFlow("catch ($T e)", ReflectiveOperationException.class)
                                            .addStatement("throw new $T(e)", RuntimeException.class)
                                            .endControlFlow()
                                            .build())
                                    .build();
                            typeSpec.addField(myMethodField);
                            if (returnType.getKind() != TypeKind.VOID) {
                                codeBuilder.add("($T)", TypeName.get(returnType));
                            }
                            codeBuilder.add("$N.get().invokeExact(", myMethodField);
                            if (!isStatic) {
                                codeBuilder.add("$N", subjectParam);
                                if (!parameters.isEmpty()) {
                                    codeBuilder.add(", ");//no need to add comma on static methods
                                }
                            }
                        }

                        codeBuilder.add(methodCallArguments.build());

                        codeBuilder.add(")");
                        CodeBlock methodCaller = codeBuilder.build();

                        //determine the return method, value or no value
                        CodeBlock valueReturner;
                        if (returnType.getKind() == TypeKind.VOID) {
                            valueReturner = CodeBlock.builder()
                                    .addStatement(methodCaller)
                                    .addStatement("return $N.voidResult()", helperParam).build();
                        } else {
                            valueReturner = CodeBlock.of("return $N.result($L);", helperParam, methodCaller);
                        }

                        if (!isPublic) {
                            myMethodBuilder.addCode("return catchingMethodHandle(()->{\n$>$L\n$<});", valueReturner);
                        } else {
                            myMethodBuilder.addCode(valueReturner);
                        }
                    }

                    {
                        MethodSpec myMethod = myMethodBuilder.build();
                        typeSpec.addMethod(myMethod);
                        //register(String name, MethodRestriction restriction, String[] requiredMods, boolean threadSafe, String[] arguments, BiConsumer<T, FancyComputerHelper> handler)
                        CodeBlock.Builder codeBuilder = CodeBlock.builder();
                        codeBuilder.add("register($L, ", values.getLiteral("nameOverride", annotatedName));
                        codeBuilder.add("$L, ", values.getLiteral("restriction", null));
                        codeBuilder.add("$L, ", values.getLiteral("requiredMods", "emptyArray()"));
                        codeBuilder.add("$L, ", values.getLiteral("threadSafe", false));
                        if (parameters.isEmpty()) {
                            codeBuilder.add("emptyArray(), ");
                        } else {
                            codeBuilder.add("new String[]{$L}, ", parameters.stream().map(param -> CodeBlock.of("$S", param.getSimpleName())).collect(CodeBlock.joining(",")));
                        }
                        codeBuilder.add("$N::$N", handlerClassName, myMethod);
                        codeBuilder.add(")");
                        constructorBuilder.addStatement(codeBuilder.build());
                    }
                });
            }

            typeSpec.addMethod(constructorBuilder.build());
            TypeSpec factorySpec = typeSpec.build();

            JavaFile factoryFile = JavaFile.builder("mekanism.computer", factorySpec).build();

            try {
                factoryFile.writeTo(processingEnv.getFiler());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            registryInit.addStatement("$T.register($T.class, $T::new)", factoryRegistry, containingClassName, ClassName.get(factoryFile.packageName, factoryFile.typeSpec.name));
        }

        if (annotatedElementsByParent.size() > 0) {
            registryType.addMethod(registryInit.build());
            TypeSpec registrySpec = registryType.build();
            try {
                JavaFile.builder("mekanism.computer", registrySpec).build().writeTo(processingEnv.getFiler());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        return true;
    }

    private static Map<String, AnnotationValue> getAnnotationValueMapWithDefaults(Elements elementUtils, AnnotationMirror annotationMirror) {
        Map<String, AnnotationValue> values = new HashMap<>();
        for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> value : elementUtils.getElementValuesWithDefaults(annotationMirror).entrySet()) {
            values.put(value.getKey().getSimpleName().toString(), value.getValue());
        }
        return values;
    }

    private static class AnnotationHelper {
        private final Map<? extends ExecutableElement, ? extends AnnotationValue> annotationValueMap;
        private final Map<String, ExecutableElement> nameToElement = new HashMap<>();

        AnnotationHelper(Elements elementUtils, AnnotationMirror annotationMirror) {
            this.annotationValueMap = elementUtils.getElementValuesWithDefaults(annotationMirror);
            for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> value : elementUtils.getElementValuesWithDefaults(annotationMirror).entrySet()) {
                //annotationValueMap.put(value.getKey().getSimpleName().toString(), value.getValue());
                nameToElement.put(value.getKey().getSimpleName().toString(), value.getKey());
            }
        }

        Object getLiteral(String key, Object defaultValue) {
            AnnotationValue value = annotationValueMap.get(nameToElement.get(key));
            return value.accept(new AnnotationValueToLiteral(
                    defaultValue instanceof String ? CodeBlock.of("$S", defaultValue) : defaultValue
            ), nameToElement.get(key).getReturnType());
        }
    }

    private static class AnnotationValueToLiteral extends SimpleAnnotationValueVisitor14<Object, TypeMirror> {
        AnnotationValueToLiteral() {
        }

        AnnotationValueToLiteral(Object defaultValue) {
            super(defaultValue);
        }

        @Override
        public Object visitString(String s, TypeMirror valueType) {
            return s != null && !"".equals(s) ? CodeBlock.of("$S", s) : super.defaultAction(s, valueType);
        }

        @Override
        public Object visitEnumConstant(VariableElement c, TypeMirror valueType) {
            return CodeBlock.of("$T.$L", ClassName.get(c.asType()), c.getSimpleName());
        }

        @Override
        public Object visitArray(List<? extends AnnotationValue> vals, TypeMirror valueType) {
            if (vals == null || vals.isEmpty()) {
                return "emptyArray()";
            } else {
                TypeMirror componentType = ((ArrayType) valueType).getComponentType();
                AnnotationValueToLiteral elementVisitor = new AnnotationValueToLiteral();
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

    private class ParamToHelperMapper extends SimpleTypeVisitor14<CodeBlock, Integer> {

        @Override
        protected CodeBlock defaultAction(TypeMirror e, Integer paramNum) {
            throw new IllegalStateException("Unhandled type: " + e);
        }

        @Override
        public CodeBlock visitPrimitive(PrimitiveType t, Integer paramNum) {
            return CodeBlock.of("$N.$L($L)", helperParam, "get" + switch (t.getKind()) {
                case BOOLEAN -> "Bool";
                case BYTE -> "Byte";
                case SHORT -> "Short";
                case INT -> "Int";
                case LONG -> "Long";
                case CHAR -> "Char";
                case FLOAT -> "Float";
                case DOUBLE -> "Double";
                default -> throw new IllegalStateException("Unknown primitive: " + t.getKind());
            }, paramNum);
        }

        @Override
        public CodeBlock visitDeclared(DeclaredType t, Integer paramNum) {
            ClassName className = ClassName.get((TypeElement) t.asElement());
            TypeElement typeElement = (TypeElement) t.asElement();
            if (typeElement.getKind() == ElementKind.ENUM) {
                return CodeBlock.of("FancyComputerHelper.getEnum($N, $L, $T.class)", helperParam, paramNum, className);
            } else if (processingEnv.getTypeUtils().isAssignable(t, filterInterface)) {
                return CodeBlock.of("FancyComputerHelper.getFilter($N, $L, $T.class)", helperParam, paramNum, className);
            }
            switch (className.canonicalName()) {
                case "java.util.List" -> {
                    return CodeBlock.of("$N.getList($L /* $L */)", helperParam, paramNum, t.getTypeArguments().get(0).toString());
                }
                case "java.util.Map" -> {
                    return CodeBlock.of("$N.getMap($L)", helperParam, paramNum);
                }
                default -> {
                    return CodeBlock.of("$N.get$L($L)", helperParam, className.simpleName(), paramNum);
                }
            }
        }
    }
}
