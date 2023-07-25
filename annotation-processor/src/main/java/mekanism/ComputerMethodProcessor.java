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
import java.lang.invoke.VarHandle;
import java.lang.invoke.WrongMethodTypeException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

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
    private final ClassName baseComputerHelper = ClassName.get("mekanism.common.integration.computer", "BaseComputerHelper");
    private final ClassName computerException = ClassName.get("mekanism.common.integration.computer", "ComputerException");
    private final ClassName lazyInterfaceRaw = ClassName.get("net.minecraftforge.common.util", "Lazy");
    private final ParameterizedTypeName lazyMethodHandleType = ParameterizedTypeName.get(lazyInterfaceRaw, ClassName.get(MethodHandle.class));
    private final ParameterSpec helperParam = ParameterSpec.builder(baseComputerHelper, "helper").build();
    private final ParamToHelperMapper paramToHelperMapper = new ParamToHelperMapper();
    private final ClassName factoryRegistry = ClassName.get("mekanism.common.integration.computer", "FactoryRegistry");
    private final ClassName computerMethodFactoryRaw = ClassName.get("mekanism.common.integration.computer", "ComputerMethodFactory");
    private TypeMirror computerMethodAnnotationType;
    private TypeMirror syntheticComputerMethodAnnotationType;
    private TypeMirror wrappingComputerMethodAnnotationType;
    private TypeMirror collectionType;
    private TypeMirror mapType;
    private String mekModule;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        mekModule = processingEnv.getOptions().getOrDefault(MODULE_OPTION, "value_not_supplied");
        computerMethodAnnotationType = Objects.requireNonNull(elementUtils().getTypeElement("mekanism.common.integration.computer.annotation.ComputerMethod")).asType();
        syntheticComputerMethodAnnotationType = Objects.requireNonNull(elementUtils().getTypeElement("mekanism.common.integration.computer.annotation.SyntheticComputerMethod")).asType();
        wrappingComputerMethodAnnotationType = Objects.requireNonNull(elementUtils().getTypeElement("mekanism.common.integration.computer.annotation.WrappingComputerMethod")).asType();
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
                JavaFile.builder("mekanism.generated."+mekModule, registrySpec).build().writeTo(processingEnv.getFiler());
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

        Map<Element, FieldSpec> varHandles = new HashMap<>();
        Map<Element, MethodSpec> fieldGetters = new HashMap<>();
        Map<Element, MethodSpec> methodProxies = new HashMap<>();

        for (Element annotatedElement : entry.getValue()) {
            String annotatedName = annotatedElement.getSimpleName().toString();
            handlerTypeSpec.addOriginatingElement(annotatedElement);
            registryType.addOriginatingElement(annotatedElement);
            Set<Modifier> modifiers = annotatedElement.getModifiers();
            boolean isPublic = modifiers.contains(Modifier.PUBLIC);
            boolean isPrivateOrProtected = modifiers.contains(Modifier.PRIVATE) || modifiers.contains(Modifier.PROTECTED);
            boolean isStatic = modifiers.contains(Modifier.STATIC);

            for (AnnotationMirror annotationMirror : annotatedElement.getAnnotationMirrors()) {
                AnnotationHelper annotationValues = new AnnotationHelper(elementUtils(), annotationMirror);
                //process @ComputerMethod
                if (typeUtils().isSameType(annotationMirror.getAnnotationType(), computerMethodAnnotationType) && annotatedElement instanceof ExecutableElement executableElement) {
                    if (isPrivateOrProtected) {
                        processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "Target for @ComputerMethod should be public or package private", annotatedElement);
                        continue;
                    }
                    TypeMirror returnType = executableElement.getReturnType();
                    @SuppressWarnings("unchecked")
                    List<VariableElement> parameters = (List<VariableElement>) executableElement.getParameters();

                    CodeBlock targetInvoker = callTargetMethod(containingClassName, subjectParam, annotatedName, isStatic, parameters);

                    //determine the return method, value or no value
                    //wrap the target method code, or call it and return void
                    CodeBlock valueReturner = convertValueToReturn(annotatedElement, returnType, targetInvoker);

                    MethodSpec handlerMethod = buildHandlerMethod(subjectParam, annotationValues.getStringValue("nameOverride", annotatedName)+"_"+parameters.size(), true, valueReturner);
                    handlerTypeSpec.addMethod(handlerMethod);

                    //add a call to register() in the handler class's constructor
                    CodeBlock registerMethodBuilder = buildRegisterMethodCall(handlerClassName, annotationValues, parameters, returnType, handlerMethod, annotationValues.getLiteral("nameOverride", annotatedName), annotationValues.getLiteral("threadSafe", false));
                    constructorBuilder.addStatement(registerMethodBuilder);
                } else if (typeUtils().isSameType(annotationMirror.getAnnotationType(), syntheticComputerMethodAnnotationType) && annotatedElement instanceof VariableElement fieldElement) {
                    CodeBlock targetReference = getReadTargetReferenceForField(containingClassName, handlerTypeSpec, subjectParam, varHandles, fieldGetters, annotatedName, isPrivateOrProtected, isStatic, fieldElement);

                    TypeMirror fieldType = fieldElement.asType();

                    String getterName = annotationValues.getStringValue("getter", null);
                    if (getterName != null) {
                        CodeBlock valueReturner = convertValueToReturn(annotatedElement, fieldType, targetReference);
                        MethodSpec handlerMethod = buildHandlerMethod(subjectParam, getterName+"_0", true, valueReturner);
                        handlerTypeSpec.addMethod(handlerMethod);

                        CodeBlock getterRegistration = buildRegisterMethodCall(handlerClassName, annotationValues, Collections.emptyList(), fieldType, handlerMethod, getterName, annotationValues.getLiteral("threadSafeGetter", false));
                        constructorBuilder.addStatement(getterRegistration);
                    }
                    String setterName = annotationValues.getStringValue("setter", null);
                    if (setterName != null) {
                        if (isPrivateOrProtected) {
                            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "Setter not implemented for private/protected fields");
                            continue;
                        }
                        CodeBlock setterBody = CodeBlock.builder()
                                .addStatement("$L = $L", targetReference, fieldType.accept(paramToHelperMapper, 0))
                                .addStatement("return $N.voidResult()", helperParam)
                                .build();
                        MethodSpec handlerMethod = buildHandlerMethod(subjectParam, setterName+"_1", true, setterBody);
                        handlerTypeSpec.addMethod(handlerMethod);

                        CodeBlock setterRegistration = buildRegisterMethodCall(handlerClassName, annotationValues, Collections.singletonList(new FakeParameter(fieldType,"value")), typeUtils().getNoType(TypeKind.VOID), handlerMethod, setterName, annotationValues.getLiteral("threadSafeSetter", false));
                        constructorBuilder.addStatement(setterRegistration);
                    }
                } else if (typeUtils().isSameType(annotationMirror.getAnnotationType(), wrappingComputerMethodAnnotationType)) {
                    //get the wrapper type and find its static methods
                    TypeElement wrapperTypeEl = null;
                    TypeMirror wrapperTypeMirror = annotationValues.getClassValue("wrapper");
                    if (typeUtils().asElement(wrapperTypeMirror) instanceof TypeElement typeElement){
                        wrapperTypeEl = typeElement;
                    }
                    if (wrapperTypeEl == null) {
                        processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "Could not find wrapper Type Element", annotatedElement);
                        continue;
                    }
                    List<ExecutableElement> wrapperMethods = wrapperTypeEl.getEnclosedElements().stream().filter(element -> element instanceof ExecutableElement exec && exec.getModifiers().containsAll(Arrays.asList(Modifier.PUBLIC, Modifier.STATIC))).map(element -> (ExecutableElement)element).toList();

                    List<String> targetMethodNames = annotationValues.getStringArray("methodNames");
                    if (targetMethodNames.size() != wrapperMethods.size()) {
                        processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "Mismatched methodName count. Expected "+wrapperMethods.size()+", found "+targetMethodNames.size(), annotatedElement);
                        continue;
                    }

                    //get the value part of the getter
                    CodeBlock targetReference;
                    TypeMirror wrappedType;
                    if (annotatedElement instanceof VariableElement fieldElement) {
                        targetReference = getReadTargetReferenceForField(containingClassName, handlerTypeSpec, subjectParam, varHandles, fieldGetters, annotatedName, isPrivateOrProtected, isStatic, fieldElement);
                        wrappedType = fieldElement.asType();
                    } else if (annotatedElement instanceof ExecutableElement executableElement) {
                        if (isPrivateOrProtected) {
                            MethodSpec proxyMethod = methodProxies.computeIfAbsent(executableElement, el->getMethodProxy(containingClassName, handlerTypeSpec, subjectParam, annotatedName, executableElement));
                            if (isStatic) {
                                targetReference = CodeBlock.of("$N()", proxyMethod);
                            } else {
                                targetReference = CodeBlock.of("$N($N)", proxyMethod, subjectParam);
                            }
                        } else {
                            targetReference = callTargetMethod(containingClassName, subjectParam, annotatedName, isStatic, Collections.emptyList());
                        }
                        wrappedType = executableElement.getReturnType();
                    } else {
                        throw new IllegalStateException("Unknown element type: "+annotatedElement.getClass());
                    }

                    TypeName wrappedTypeName = TypeName.get(wrappedType);
                    //ParameterSpec wrappedSubject = ParameterSpec.builder(wrappedTypeName, "wrappedSubject").build();
                    //CodeBlock getNewSubject = CodeBlock.builder().addStatement("$T $N = $L", wrappedTypeName, wrappedSubject, targetReference).build();

                    for (int i = 0; i < targetMethodNames.size(); i++) {
                        String targetMethodName = targetMethodNames.get(i);
                        ExecutableElement wrapperMethod = wrapperMethods.get(i);

                        CodeBlock targetInvoker = CodeBlock.of("$T.$L($L)", wrapperTypeMirror, wrapperMethod.getSimpleName(), targetReference);

                        CodeBlock valueReturner = convertValueToReturn(wrapperMethod, wrapperMethod.getReturnType(), targetInvoker);

                        CodeBlock methodBody = CodeBlock.builder()
                                //.add(getNewSubject)
                                .add(valueReturner)
                                .build();

                        MethodSpec handlerMethod = buildHandlerMethod(subjectParam, annotatedName+"$"+targetMethodName, true, methodBody);
                        handlerTypeSpec.addMethod(handlerMethod);

                        //add a call to register() in the handler class's constructor
                        CodeBlock registerMethodBuilder = buildRegisterMethodCall(handlerClassName, annotationValues, Collections.emptyList(), wrapperMethod.getReturnType(), handlerMethod, targetMethodName, annotationValues.getLiteral("threadSafe", false));
                        constructorBuilder.addStatement(registerMethodBuilder);

                    }
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

    private CodeBlock getReadTargetReferenceForField(ClassName containingClassName, TypeSpec.Builder handlerTypeSpec, ParameterSpec subjectParam, Map<Element, FieldSpec> varHandles, Map<Element, MethodSpec> fieldGetters, String annotatedName, boolean isPrivateOrProtected, boolean isStatic, VariableElement fieldElement) {
        CodeBlock.Builder targetFieldBuilder = CodeBlock.builder();
        if (isPrivateOrProtected) {
            MethodSpec getterMethod = fieldGetters.computeIfAbsent(fieldElement, el -> getFieldGetter(containingClassName, handlerTypeSpec, subjectParam, varHandles, annotatedName, fieldElement));
            if (isStatic) {
                targetFieldBuilder.add("$N()", getterMethod);
            } else {
                targetFieldBuilder.add("$N($N)", getterMethod, subjectParam);
            }
        } else {
            if (isStatic) {
                targetFieldBuilder.add("$T.$L", containingClassName, annotatedName);
            } else {
                targetFieldBuilder.add("$N.$L", subjectParam, annotatedName);
            }
        }
        return targetFieldBuilder.build();
    }

    private MethodSpec getMethodProxy(ClassName containingClassName, TypeSpec.Builder handlerTypeSpec, ParameterSpec subjectParam, String annotatedName, ExecutableElement executableElement) {
        CodeBlock.Builder paramTypes = CodeBlock.builder();
        //List<CharSequence> paramInvocations = new ArrayList<>();
        List<ParameterSpec> proxyParams = new ArrayList<>();
        if (!executableElement.getModifiers().contains(Modifier.STATIC)) {
            proxyParams.add(subjectParam);
        }
        for (VariableElement parameter : executableElement.getParameters()) {
            TypeMirror paramType = parameter.asType();
            paramTypes.add(", $T.class", typeUtils().erasure(paramType));
            //paramInvocations.add(parameter.getSimpleName());
            proxyParams.add(ParameterSpec.get(parameter));
        }
        CodeBlock builtParamTypes = paramTypes.build();
        FieldSpec methodHandleField = FieldSpec.builder(MethodHandle.class, "method$" + annotatedName, Modifier.STATIC, Modifier.PRIVATE)
                .initializer(CodeBlock.of("getMethodHandle($T.class, $S$L)", containingClassName, annotatedName, builtParamTypes))
                .build();
        handlerTypeSpec.addField(methodHandleField);
        TypeName returnType = TypeName.get(executableElement.getReturnType());
        MethodSpec proxyMethod = MethodSpec.methodBuilder("proxy$"+ annotatedName)
                .addParameters(proxyParams)
                .addModifiers(Modifier.STATIC, Modifier.PRIVATE)
                .addException(computerException)
                .returns(returnType)
                .beginControlFlow("try")
                .addStatement("return ($T)$N.invokeExact($L)", returnType, methodHandleField, proxyParams.stream().map(param->param.name).collect(Collectors.joining(", ")))
                .nextControlFlow("catch ($T wmte)", WrongMethodTypeException.class)
                .addStatement("throw new $T($S, wmte)", RuntimeException.class, "Method not bound correctly")
                .nextControlFlow("catch ($T cex)", computerException)
                .addStatement("throw cex")
                .nextControlFlow("catch ($T t)", Throwable.class)
                .addStatement("throw new $T(t.getMessage(), t)", RuntimeException.class)
                .endControlFlow()
                .build();
        handlerTypeSpec.addMethod(proxyMethod);
        return proxyMethod;
    }

    private MethodSpec getFieldGetter(ClassName containingClassName, TypeSpec.Builder handlerTypeSpec, ParameterSpec subjectParam, Map<Element, FieldSpec> varHandles, String annotatedName, VariableElement fieldElement) {
        FieldSpec varHandleField = varHandles.computeIfAbsent(fieldElement, el->getVarHandleField(containingClassName, handlerTypeSpec, annotatedName));

        TypeName fieldType = TypeName.get(fieldElement.asType());

        MethodSpec.Builder getterMethodBuilder = MethodSpec.methodBuilder("getter$"+ annotatedName)
                .addModifiers(Modifier.STATIC, Modifier.PRIVATE)
                .returns(fieldType)
                //.addException(computerException)
                .beginControlFlow("try");
        if (fieldElement.getModifiers().contains(Modifier.STATIC)) {
            getterMethodBuilder
                    .addStatement("return ($T)$N.invokeExact()", fieldType, varHandleField);
        } else {
            getterMethodBuilder
                    .addParameter(subjectParam)
                    .addStatement("return ($T)$N.invokeExact($N)", fieldType, varHandleField, subjectParam);
        }
        getterMethodBuilder.nextControlFlow("catch ($T wmte)", WrongMethodTypeException.class)
                .addStatement("throw new $T($S, wmte)", RuntimeException.class, "Getter not bound correctly")
                .nextControlFlow("catch ($T t)", Throwable.class)
                .addStatement("throw new $T(t.getMessage(), t)", RuntimeException.class)
                .endControlFlow();

        MethodSpec getterMethod = getterMethodBuilder.build();
        handlerTypeSpec.addMethod(getterMethod);
        return getterMethod;
    }

    private static FieldSpec getVarHandleField(ClassName containingClassName, TypeSpec.Builder handlerTypeSpec, String annotatedName) {
        FieldSpec varHandleField = FieldSpec.builder(MethodHandle.class, "fieldGetter$" + annotatedName, Modifier.STATIC, Modifier.PRIVATE)
                .initializer(CodeBlock.of("getGetterHandle($T.class, $S)", containingClassName, annotatedName))
                .build();
        handlerTypeSpec.addField(varHandleField);
        return varHandleField;
    }

    private CodeBlock convertValueToReturn(Element annotatedElement, TypeMirror returnType, CodeBlock targetInvoker) {
        CodeBlock.Builder valueReturner = CodeBlock.builder();
        if (returnType.getKind() == TypeKind.VOID) {
            valueReturner.addStatement(targetInvoker)
                    .addStatement("return $N.voidResult()", helperParam)
                    .build();
        } else {
            TypeKind returnTypeKind = returnType.getKind();
            if (returnTypeKind == TypeKind.DECLARED && returnType.toString().equals("java.lang.Object")) {
                processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "Raw Object returned for computer method, use a concrete type or Convertable instead", annotatedElement);
            }
            /*if (returnTypeKind.isPrimitive() || (returnTypeKind == TypeKind.DECLARED && (returnType).toString().equals("java.lang.String"))) {
                valueReturner.addStatement("return $L", targetInvoker);
            } else */
            TypeMirror erasedType = typeUtils().erasure(returnType);
            if (typeUtils().isAssignable(erasedType, collectionType)) {
                valueReturner.addStatement("return $N.convert($L, $N::convert)", helperParam, targetInvoker, helperParam);
            } else if (typeUtils().isAssignable(erasedType, mapType)) {
                valueReturner.addStatement("return $N.convert($L, $N::convert, $N::convert)", helperParam, targetInvoker, helperParam, helperParam);
            } else {
                valueReturner.addStatement("return $N.convert($L)", helperParam, targetInvoker);
            }
        }
        return valueReturner.build();
    }

    private CodeBlock callTargetMethod(ClassName containingClassName, ParameterSpec subjectParam, String annotatedName, boolean isStatic, List<VariableElement> parameters) {
        CodeBlock.Builder methodCallArguments = CodeBlock.builder();
        if (!parameters.isEmpty()) {
            List<CodeBlock> paramGetters = new ArrayList<>();
            for (int i = 0; i < parameters.size(); i++) {
                TypeMirror paramType = parameters.get(i).asType();
                paramGetters.add(paramType.accept(paramToHelperMapper, i));
            }
            methodCallArguments.add(CodeBlock.join(paramGetters, ", "));
        }

        CodeBlock.Builder targetMethodCodeBuilder = CodeBlock.builder();
        if (isStatic) {
            targetMethodCodeBuilder.add("$T.$L(", containingClassName, annotatedName);
        } else {
            targetMethodCodeBuilder.add("$N.$L(", subjectParam, annotatedName);
        }
        //getMethodHandleCall was here

        targetMethodCodeBuilder.add(methodCallArguments.build());

        targetMethodCodeBuilder.add(")");
        return targetMethodCodeBuilder.build();
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

    private CodeBlock buildRegisterMethodCall(String handlerClassName, AnnotationHelper annotationValues, List<VariableElement> parameters, TypeMirror returnType, MethodSpec handlerMethod, String computerExposedName, Object threadSafeLiteral) {
        return buildRegisterMethodCall(handlerClassName, annotationValues, parameters, returnType, handlerMethod, CodeBlock.of("$S", computerExposedName),threadSafeLiteral);
    }

    private CodeBlock buildRegisterMethodCall(String handlerClassName, AnnotationHelper annotationValues, List<VariableElement> parameters, TypeMirror returnType, MethodSpec handlerMethod, Object computerExposedName, Object threadSafeLiteral) {
        CodeBlock.Builder registerMethodBuilder = CodeBlock.builder();
        //Computer exposed method name
        registerMethodBuilder.add("register($L, ", computerExposedName);
        //restriction
        registerMethodBuilder.add("$L, ", annotationValues.getLiteral("restriction", null));
        //mods required
        registerMethodBuilder.add("$L, ", annotationValues.getLiteral("requiredMods", "NO_STRINGS"));
        //threadsafe
        registerMethodBuilder.add("$L, ", threadSafeLiteral);
        //param names
        if (parameters.isEmpty()) {
            registerMethodBuilder.add("NO_STRINGS, ");//names
            registerMethodBuilder.add("NO_CLASSES, ");//classes
        } else {
            registerMethodBuilder.add("new String[]{$L}, ", parameters.stream().map(param -> CodeBlock.of("$S", param.getSimpleName())).collect(CodeBlock.joining(",")));
            registerMethodBuilder.add("new Class[]{$L}, ", parameters.stream().map(param -> CodeBlock.of("$T.class", typeUtils().erasure(param.asType()))).collect(CodeBlock.joining(",")));
        }
        //return type
        registerMethodBuilder.add("$T.class, ", TypeName.get(typeUtils().erasure(returnType)));
        //method reference to handler method
        registerMethodBuilder.add("$N::$N", handlerClassName, handlerMethod);
        registerMethodBuilder.add(")");
        return registerMethodBuilder.build();
    }

    private MethodSpec buildHandlerMethod(ParameterSpec subjectParam, String methodName, boolean isPublic, CodeBlock methodBody) {
        MethodSpec.Builder handlerMethodBuilder = MethodSpec.methodBuilder(methodName)
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(Object.class)
                .addException(computerException)
                .addParameter(subjectParam)
                .addParameter(helperParam);
        if (!isPublic) {
            //wrap in MethodHandle try/catch
            handlerMethodBuilder.addCode(wrapMethodHandle(methodBody));
        } else {
            handlerMethodBuilder.addCode(methodBody);
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

        TypeMirror getClassValue(String key) {
            AnnotationValue value = annotationValueMap.get(nameToElement.get(key));
            if (value.getValue() instanceof TypeMirror tm) {
                return tm;
            }
            return null;
        }

        List<String> getStringArray(String key) {
            AnnotationValue value = annotationValueMap.get(nameToElement.get(key));
            List<String> returnVal = new ArrayList<>();
            value.accept(new SimpleAnnotationValueVisitor14<Void, Void>(){
                @Override
                public Void visitArray(List<? extends AnnotationValue> vals, Void unused) {
                    for (AnnotationValue annotationValue : vals) {
                        annotationValue.accept(this, null);
                    }
                    return null;
                }

                @Override
                public Void visitString(String s, Void unused) {
                    if (s != null && !s.isBlank()) {
                        returnVal.add(s);
                    }
                    return null;
                }
            }, null);
            return returnVal;
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
            TypeMirror componentType = ((ArrayType) valueType).getComponentType();
            if (vals == null || vals.isEmpty()) {
                return CodeBlock.of("new $T[0]", ClassName.get(componentType));
            } else {
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
