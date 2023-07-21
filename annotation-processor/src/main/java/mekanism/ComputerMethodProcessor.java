package mekanism;

import com.squareup.javapoet.*;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.type.*;
import javax.lang.model.util.*;
import javax.tools.Diagnostic;
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
    private ClassName factoryRegistry = ClassName.get("mekanism.common.integration.computer", "FactoryRegistry");
    private ClassName computerMethodFactoryRaw = ClassName.get("mekanism.common.integration.computer", "ComputerMethodFactory");
    private TypeMirror computerMethodAnnotationType;
    private TypeMirror collectionType;
    private TypeMirror mapType;
    private String mekModule;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        mekModule = processingEnv.getOptions().getOrDefault(MODULE_OPTION, "value_not_supplied");
        computerMethodAnnotationType = Objects.requireNonNull(elementUtils().getTypeElement("mekanism.common.integration.computer.annotation.ComputerMethod")).asType();
        collectionType = Objects.requireNonNull(elementUtils().getTypeElement("java.util.Collection")).asType();
        mapType = Objects.requireNonNull(elementUtils().getTypeElement("java.util.Map")).asType();
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

        filterInterface = typeUtils().erasure(elementUtils().getTypeElement("mekanism.common.content.filter.IFilter").asType());

        for (Map.Entry<TypeElement, List<Element>> entry : annotatedElementsByParent.entrySet()) {
            processTypeWithAnnotations(registryType, registryInit, entry);
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

    private void processTypeWithAnnotations(TypeSpec.Builder registryType, MethodSpec.Builder registryInit, Map.Entry<TypeElement, List<Element>> entry) {
        TypeElement containingType = entry.getKey();
        String handlerClassName = containingType.getSimpleName()+"$ComputerHandler";
        ClassName containingClassName = ClassName.get(containingType);

        TypeSpec.Builder handlerTypeSpec = TypeSpec.classBuilder(handlerClassName)
                .superclass(ParameterizedTypeName.get(computerMethodFactoryRaw, containingClassName))
                .addOriginatingElement(containingType)
                .addModifiers(Modifier.PUBLIC);
        ParameterSpec subjectParam = ParameterSpec.builder(containingClassName, "subject").build();
        registryType.addOriginatingElement(containingType);

        MethodSpec.Builder constructorBuilder = MethodSpec.constructorBuilder().addModifiers(Modifier.PUBLIC);

        for (Element annotatedElement : entry.getValue()) {
            String annotatedName = annotatedElement.getSimpleName().toString();
            handlerTypeSpec.addOriginatingElement(annotatedElement);
            registryType.addOriginatingElement(annotatedElement);
            for (AnnotationMirror annotationMirror : annotatedElement.getAnnotationMirrors()) {
                //process @ComputerMethod
                if (typeUtils().isSameType(annotationMirror.getAnnotationType(), computerMethodAnnotationType)) {
                    AnnotationHelper annotationValues = new AnnotationHelper(elementUtils(), annotationMirror);
                    ExecutableElement executableElement = (ExecutableElement) annotatedElement;
                    @SuppressWarnings("unchecked")
                    List<VariableElement> parameters = (List<VariableElement>) executableElement.getParameters();

                    CodeBlock.Builder methodCallArguments = CodeBlock.builder();
                    if (!parameters.isEmpty()) {
                        List<CodeBlock> paramGetters = new ArrayList<>();
                        for (int i = 0; i < parameters.size(); i++) {
                            TypeMirror paramType = parameters.get(i).asType();
                            paramGetters.add(paramType.accept(paramToHelperMapper, i));
                        }
                        methodCallArguments.add(CodeBlock.join(paramGetters, ", "));
                    }
                    Set<Modifier> modifiers = annotatedElement.getModifiers();
                    boolean isPublic = modifiers.contains(Modifier.PUBLIC);
                    boolean isPrivateOrProtected = modifiers.contains(Modifier.PRIVATE) || modifiers.contains(Modifier.PROTECTED);
                    boolean isStatic = modifiers.contains(Modifier.STATIC);
                    TypeMirror returnType = executableElement.getReturnType();

                    CodeBlock.Builder targetMethodCodeBuilder = CodeBlock.builder();
                    if (!isPrivateOrProtected) {
                        if (isStatic) {
                            targetMethodCodeBuilder.add("$T.$L(", containingClassName, annotatedName);
                        } else {
                            targetMethodCodeBuilder.add("$N.$L(", subjectParam, annotatedName);
                        }
                    } else {
                        processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "ComputerMethod should be public or package private", annotatedElement);
                        //getMethodHandleCall(containingClassName, handlerTypeSpec, subjectParam, annotatedName, parameters, isStatic, returnType, targetMethodCodeBuilder);
                    }

                    targetMethodCodeBuilder.add(methodCallArguments.build());

                    targetMethodCodeBuilder.add(")");
                    CodeBlock targetMethodCode = targetMethodCodeBuilder.build();

                    //determine the return method, value or no value
                    //wrap the target method code, or call it and return void
                    CodeBlock.Builder valueReturner = CodeBlock.builder();
                    if (returnType.getKind() == TypeKind.VOID) {
                        valueReturner.addStatement(targetMethodCode)
                                .addStatement("return $N.voidResult()", helperParam)
                                .build();
                    } else {
                        TypeKind returnTypeKind = returnType.getKind();
                        if (returnTypeKind == TypeKind.DECLARED && (returnType).toString().equals("java.lang.Object")) {
                            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "Raw Object returned for computer method, use a concrete type or Convertable instead", annotatedElement);
                        }
                        /*if (returnTypeKind.isPrimitive() || (returnTypeKind == TypeKind.DECLARED && (returnType).toString().equals("java.lang.String"))) {
                            valueReturner.addStatement("return $L", targetMethodCode);
                        } else */if (typeUtils().isAssignable(typeUtils().erasure(returnType), collectionType)) {
                            valueReturner.addStatement("return $N.convert($L, $N::convert)", helperParam, targetMethodCode, helperParam);
                        } else if (typeUtils().isAssignable(typeUtils().erasure(returnType), mapType)) {
                            valueReturner.addStatement("return $N.convert($L, $N::convert, $N::convert)", helperParam, targetMethodCode, helperParam, helperParam);
                        } else {
                            valueReturner.addStatement("return $N.convert($L)", helperParam, targetMethodCode);
                        }
                    }

                    MethodSpec handlerMethod = buildHandlerMethod(subjectParam, annotationValues.getStringValue("nameOverride", annotatedName), !isPrivateOrProtected, valueReturner.build());
                    handlerTypeSpec.addMethod(handlerMethod);
                    //add a call to register() in the handler class's constructor
                    CodeBlock registerMethodBuilder = buildRegisterMethodCall(handlerClassName, annotatedName, annotationValues, parameters, handlerMethod);
                    constructorBuilder.addStatement(registerMethodBuilder);
                }
            }
        }

        handlerTypeSpec.addMethod(constructorBuilder.build());
        TypeSpec factorySpec = handlerTypeSpec.build();

        JavaFile factoryFile = JavaFile.builder(containingClassName.packageName(), factorySpec).build();

        try {
            factoryFile.writeTo(processingEnv.getFiler());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        addHandlerToRegistry(registryInit, factoryRegistry, containingType, containingClassName, factoryFile);
    }

    private void getMethodHandleCall(ClassName containingClassName, TypeSpec.Builder handlerTypeSpec, ParameterSpec subjectParam, String annotatedName, List<VariableElement> parameters, boolean isStatic, TypeMirror returnType, CodeBlock.Builder targetMethodCodeBuilder) {
        CodeBlock.Builder paramTypes = CodeBlock.builder();
        if (!parameters.isEmpty()) {
            for (VariableElement parameter : parameters) {
                TypeMirror paramType = parameter.asType();
                paramTypes.add(", $T.class", typeUtils().erasure(paramType));
            }
        }
        FieldSpec methodHandleField = FieldSpec.builder(lazyMethodHandleType, "handle_" + annotatedName, Modifier.PRIVATE, Modifier.STATIC)
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
        handlerTypeSpec.addField(methodHandleField);
        if (returnType.getKind() != TypeKind.VOID) {
            targetMethodCodeBuilder.add("($T)", TypeName.get(returnType));
        }
        targetMethodCodeBuilder.add("$N.get().invokeExact(", methodHandleField);
        if (!isStatic) {
            targetMethodCodeBuilder.add("$N", subjectParam);
            if (!parameters.isEmpty()) {
                targetMethodCodeBuilder.add(", ");//no need to add comma on static methods
            }
        }
    }

    private void addHandlerToRegistry(MethodSpec.Builder registryInit, ClassName factoryRegistry, TypeElement containingType, ClassName containingClassName, JavaFile factoryFile) {
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
                .add("$T.register($T.class, $T::new", factoryRegistry, containingClassName, ClassName.get(factoryFile.packageName, factoryFile.typeSpec.name));
        //add all super classes, so we don't have to calculate at runtime
        for (ClassName cls : superClasses) {
            registerStatement.add(", $T.class", cls);
        }
        registerStatement.add(")");
        registryInit.addStatement(registerStatement.build());
    }

    //for @ComputerMethod
    private static CodeBlock buildRegisterMethodCall(String handlerClassName, String annotatedName, AnnotationHelper annotationValues, List<VariableElement> parameters, MethodSpec handlerMethod) {
        CodeBlock.Builder registerMethodBuilder = CodeBlock.builder();
        //Computer exposed method name
        registerMethodBuilder.add("register($L, ", annotationValues.getLiteral("nameOverride", annotatedName));
        //restriction
        registerMethodBuilder.add("$L, ", annotationValues.getLiteral("restriction", null));
        //mods required
        registerMethodBuilder.add("$L, ", annotationValues.getLiteral("requiredMods", "emptyArray()"));
        //threadsafe
        registerMethodBuilder.add("$L, ", annotationValues.getLiteral("threadSafe", false));
        //param names
        if (parameters.isEmpty()) {
            registerMethodBuilder.add("emptyArray(), ");
        } else {
            registerMethodBuilder.add("new String[]{$L}, ", parameters.stream().map(param -> CodeBlock.of("$S", param.getSimpleName())).collect(CodeBlock.joining(",")));
        }
        //method reference to handler method
        registerMethodBuilder.add("$N::$N", handlerClassName, handlerMethod);
        registerMethodBuilder.add(")");
        return registerMethodBuilder.build();
    }

    private MethodSpec buildHandlerMethod(ParameterSpec subjectParam, String annotatedName, boolean isPublic, CodeBlock valueReturner) {
        MethodSpec.Builder handlerMethodBuilder = MethodSpec.methodBuilder(annotatedName)
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(Object.class)
                .addException(computerException)
                .addParameter(subjectParam)
                .addParameter(helperParam);
        if (!isPublic) {
            //wrap in MethodHandle try/catch
            handlerMethodBuilder.addCode(wrapMethodHandle(valueReturner));
        } else {
            handlerMethodBuilder.addCode(valueReturner);
        }

        return handlerMethodBuilder.build();
    }

    private Elements elementUtils() {
        return processingEnv.getElementUtils();
    }

    private Types typeUtils() {
        return processingEnv.getTypeUtils();
    }

    private CodeBlock wrapMethodHandle(CodeBlock inner) {
        return CodeBlock.builder()
                .beginControlFlow("try")
                .add(inner)
                .nextControlFlow("catch ($T t)", Throwable.class)
                .addStatement("unwrapException(t)")
                .addStatement("return null;//unreachable")
                .endControlFlow()
                .build();
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

        String getStringValue(String key, String defaultValue) {
            AnnotationValue value = annotationValueMap.get(nameToElement.get(key));
            if (value != null && value.getValue() instanceof String s && !s.isBlank())
                return s;
            return defaultValue;
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
                return CodeBlock.of("$N.getEnum($L, $T.class)", helperParam, paramNum, className);
            } else if (typeUtils().isAssignable(t, filterInterface)) {
                return CodeBlock.of("$N.getFilter($L, $T.class)", helperParam, paramNum, className);
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
