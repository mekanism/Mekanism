package mekanism.builder;

import com.squareup.javapoet.*;
import mekanism.MekAnnotationProcessors;
import mekanism.util.FakeParameter;
import mekanism.visitors.AnnotationHelper;
import mekanism.visitors.ParamToHelperMapper;

import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.*;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.WrongMethodTypeException;
import java.util.*;
import java.util.stream.Collectors;

public class ComputerHandlerBuilder {
    private static final ClassName computerMethodFactoryRaw = ClassName.get("mekanism.common.integration.computer", "ComputerMethodFactory");
    private static final ClassName baseComputerHelper = ClassName.get("mekanism.common.integration.computer", "BaseComputerHelper");
    private static final ClassName computerException = ClassName.get("mekanism.common.integration.computer", "ComputerException");
    private static TypeMirror computerMethodAnnotationType;
    private static TypeMirror syntheticComputerMethodAnnotationType;
    private static TypeMirror wrappingComputerMethodAnnotationType;
    private static TypeMirror collectionType;
    private static TypeMirror mapType;
    private static TypeMirror filterInterface;
    private static final ParameterSpec helperParam = ParameterSpec.builder(baseComputerHelper, "helper").build();
    private static ParamToHelperMapper paramToHelperMapper;

    public static void init(Elements elementUtils, Types typeUtils) {
        computerMethodAnnotationType = Objects.requireNonNull(elementUtils.getTypeElement("mekanism.common.integration.computer.annotation.ComputerMethod")).asType();
        syntheticComputerMethodAnnotationType = Objects.requireNonNull(elementUtils.getTypeElement("mekanism.common.integration.computer.annotation.SyntheticComputerMethod")).asType();
        wrappingComputerMethodAnnotationType = Objects.requireNonNull(elementUtils.getTypeElement("mekanism.common.integration.computer.annotation.WrappingComputerMethod")).asType();
        collectionType = Objects.requireNonNull(elementUtils.getTypeElement("java.util.Collection")).asType();
        mapType = Objects.requireNonNull(elementUtils.getTypeElement("java.util.Map")).asType();
        filterInterface = typeUtils.erasure(elementUtils.getTypeElement("mekanism.common.content.filter.IFilter").asType());
        paramToHelperMapper = new ParamToHelperMapper(helperParam, filterInterface, typeUtils);
    }

    private final Types typeUtils;
    private final Messager messager;
    private final Elements elementUtils;
    private final TypeSpec.Builder handlerTypeSpec;
    private final ParameterSpec subjectParam;
    private final String handlerClassName;
    private final ClassName containingClassName;
    private final MethodSpec.Builder constructorBuilder = MethodSpec.constructorBuilder().addModifiers(Modifier.PUBLIC);

    private final Map<Element, MethodSpec> fieldGetters = new HashMap<>();
    private final Map<Element, MethodSpec> methodProxies = new HashMap<>();

    public ComputerHandlerBuilder(TypeElement containingType, ProcessingEnvironment env) {
        this.typeUtils = env.getTypeUtils();
        this.messager = env.getMessager();
        this.elementUtils = env.getElementUtils();
        this.handlerClassName = containingType.getSimpleName()+"$ComputerHandler";
        this.containingClassName = ClassName.get(containingType);
        this.handlerTypeSpec = TypeSpec.classBuilder(handlerClassName)
                .superclass(ParameterizedTypeName.get(computerMethodFactoryRaw, containingClassName))
                .addOriginatingElement(containingType)
                .addAnnotation(
                        AnnotationSpec.builder(MekAnnotationProcessors.COMPUTER_METHOD_FACTORY_ANNOTATION)
                                .addMember("target", CodeBlock.of("$T.class", containingClassName))
                                .build()
                )
                .addModifiers(Modifier.PUBLIC);
        this.subjectParam = ParameterSpec.builder(containingClassName, "subject").build();
    }
    
    public JavaFile build(List<Element> annotatedElementList) {

        for (Element annotatedElement : annotatedElementList) {
            String annotatedName = annotatedElement.getSimpleName().toString();
            handlerTypeSpec.addOriginatingElement(annotatedElement);

            Set<Modifier> modifiers = annotatedElement.getModifiers();
            //boolean isPublic = modifiers.contains(Modifier.PUBLIC);
            boolean isPrivateOrProtected = modifiers.contains(Modifier.PRIVATE) || modifiers.contains(Modifier.PROTECTED);
            boolean isStatic = modifiers.contains(Modifier.STATIC);

            for (AnnotationMirror annotationMirror : annotatedElement.getAnnotationMirrors()) {
                AnnotationHelper annotationValues = new AnnotationHelper(elementUtils, annotationMirror);
                if (typeUtils.isSameType(annotationMirror.getAnnotationType(), computerMethodAnnotationType) && annotatedElement instanceof ExecutableElement executableElement) {
                    handleAtComputerMethod(annotatedName, isPrivateOrProtected, isStatic, annotationValues, executableElement);
                } else if (typeUtils.isSameType(annotationMirror.getAnnotationType(), syntheticComputerMethodAnnotationType) && annotatedElement instanceof VariableElement fieldElement) {
                    handleAtSyntheticMethod(annotatedName, isPrivateOrProtected, isStatic, annotationValues, fieldElement);
                } else if (typeUtils.isSameType(annotationMirror.getAnnotationType(), wrappingComputerMethodAnnotationType)) {
                    handleAtWrappingComputerMethod(annotatedElement, annotatedName, isPrivateOrProtected, isStatic, annotationValues);
                }
            }
        }

        handlerTypeSpec.addMethod(constructorBuilder.build());
        TypeSpec factorySpec = handlerTypeSpec.build();

        return JavaFile.builder(containingClassName.packageName(), factorySpec).build();
    }

    private void handleAtWrappingComputerMethod(Element annotatedElement, String annotatedName, boolean isPrivateOrProtected, boolean isStatic, AnnotationHelper annotationValues) {
        //get the wrapper type and find its static methods
        TypeElement wrapperTypeEl;
        TypeMirror wrapperTypeMirror = annotationValues.getClassValue("wrapper");
        if (typeUtils.asElement(wrapperTypeMirror) instanceof TypeElement typeElement){
            wrapperTypeEl = typeElement;
        } else {
            messager.printMessage(Diagnostic.Kind.ERROR, "Could not find wrapper Type Element", annotatedElement);
            return;
        }
        List<ExecutableElement> wrapperMethods = gatherWrapperMethods(wrapperTypeEl);

        List<String> targetMethodNames = annotationValues.getStringArray("methodNames");
        if (targetMethodNames.size() != wrapperMethods.size()) {
            messager.printMessage(Diagnostic.Kind.ERROR, "Mismatched methodName count. Expected "+wrapperMethods.size()+", found "+targetMethodNames.size(), annotatedElement);
            return;
        }

        //get the value part of the getter
        CodeBlock targetReference;
        //TypeMirror wrappedType;
        if (annotatedElement instanceof VariableElement fieldElement) {
            targetReference = getReadTargetReferenceForField(annotatedName, isPrivateOrProtected, isStatic, fieldElement);
            //wrappedType = fieldElement.asType();
        } else if (annotatedElement instanceof ExecutableElement executableElement) {
            if (isPrivateOrProtected) {
                MethodSpec proxyMethod = methodProxies.computeIfAbsent(executableElement, el->getMethodProxy(annotatedName, executableElement));
                if (isStatic) {
                    targetReference = CodeBlock.of("$N()", proxyMethod);
                } else {
                    targetReference = CodeBlock.of("$N($N)", proxyMethod, subjectParam);
                }
            } else {
                targetReference = callTargetMethod(annotatedName, isStatic, Collections.emptyList());
            }
            //wrappedType = executableElement.getReturnType();
        } else {
            throw new IllegalStateException("Unknown element type: "+ annotatedElement.getClass());
        }

        //TypeName wrappedTypeName = TypeName.get(wrappedType);
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

            MethodSpec handlerMethod = buildHandlerMethod(annotatedName +"$"+targetMethodName, methodBody);
            handlerTypeSpec.addMethod(handlerMethod);

            //add a call to register() in the handler class's constructor
            CodeBlock registerMethodBuilder = buildRegisterMethodCall(annotationValues, Collections.emptyList(), wrapperMethod.getReturnType(), handlerMethod, targetMethodName, annotationValues.getLiteral("threadSafe", false));
            constructorBuilder.addStatement(registerMethodBuilder);

        }
    }

    private static List<ExecutableElement> gatherWrapperMethods(TypeElement wrapperTypeEl) {
        List<ExecutableElement> wrapperMethods = new ArrayList<>();
        for (Element element : wrapperTypeEl.getEnclosedElements()) {
            if (element instanceof ExecutableElement exec) {
                Set<Modifier> modifiers = exec.getModifiers();
                if (modifiers.contains(Modifier.PUBLIC) && modifiers.contains(Modifier.STATIC)) {
                    wrapperMethods.add(exec);
                }
            }
        }
        return wrapperMethods;
    }

    private void handleAtSyntheticMethod(String annotatedName, boolean isPrivateOrProtected, boolean isStatic, AnnotationHelper annotationValues, VariableElement fieldElement) {
        CodeBlock targetReference = getReadTargetReferenceForField(annotatedName, isPrivateOrProtected, isStatic, fieldElement);

        TypeMirror fieldType = fieldElement.asType();

        String getterName = annotationValues.getStringValue("getter", null);
        if (getterName != null) {
            CodeBlock valueReturner = convertValueToReturn(fieldElement, fieldType, targetReference);
            MethodSpec handlerMethod = buildHandlerMethod(getterName+"_0", valueReturner);
            handlerTypeSpec.addMethod(handlerMethod);

            CodeBlock getterRegistration = buildRegisterMethodCall(annotationValues, Collections.emptyList(), fieldType, handlerMethod, getterName, annotationValues.getLiteral("threadSafeGetter", false));
            constructorBuilder.addStatement(getterRegistration);
        }
        String setterName = annotationValues.getStringValue("setter", null);
        if (setterName != null) {
            if (isPrivateOrProtected) {
                messager.printMessage(Diagnostic.Kind.ERROR, "Setter not implemented for private/protected fields");
                return;
            }
            CodeBlock setterBody = CodeBlock.builder()
                    .addStatement("$L = $L", targetReference, fieldType.accept(paramToHelperMapper, 0))
                    .addStatement("return $N.voidResult()", helperParam)
                    .build();
            MethodSpec handlerMethod = buildHandlerMethod(setterName+"_1", setterBody);
            handlerTypeSpec.addMethod(handlerMethod);

            CodeBlock setterRegistration = buildRegisterMethodCall(annotationValues, Collections.singletonList(new FakeParameter(fieldType,"value")), typeUtils.getNoType(TypeKind.VOID), handlerMethod, setterName, annotationValues.getLiteral("threadSafeSetter", false));
            constructorBuilder.addStatement(setterRegistration);
        }
    }

    private void handleAtComputerMethod(String annotatedName, boolean isPrivateOrProtected, boolean isStatic, AnnotationHelper annotationValues, ExecutableElement executableElement) {
        if (isPrivateOrProtected) {
            messager.printMessage(Diagnostic.Kind.ERROR, "Target for @ComputerMethod should be public or package private", executableElement);
            return;
        }
        TypeMirror returnType = executableElement.getReturnType();
        @SuppressWarnings("unchecked")
        List<VariableElement> parameters = (List<VariableElement>) executableElement.getParameters();

        CodeBlock targetInvoker = callTargetMethod(annotatedName, isStatic, parameters);

        //determine the return method, value or no value
        //wrap the target method code, or call it and return void
        CodeBlock valueReturner = convertValueToReturn(executableElement, returnType, targetInvoker);

        MethodSpec handlerMethod = buildHandlerMethod(annotationValues.getStringValue("nameOverride", annotatedName)+"_"+parameters.size(), valueReturner);
        handlerTypeSpec.addMethod(handlerMethod);

        //add a call to register() in the handler class's constructor
        CodeBlock registerMethodBuilder = buildRegisterMethodCall(annotationValues, parameters, returnType, handlerMethod, annotationValues.getLiteral("nameOverride", annotatedName), annotationValues.getLiteral("threadSafe", false));
        constructorBuilder.addStatement(registerMethodBuilder);
    }

    private CodeBlock getReadTargetReferenceForField(String annotatedName, boolean isPrivateOrProtected, boolean isStatic, VariableElement fieldElement) {
        CodeBlock.Builder targetFieldBuilder = CodeBlock.builder();
        if (isPrivateOrProtected) {
            MethodSpec getterMethod = fieldGetters.computeIfAbsent(fieldElement, el -> getFieldGetter(annotatedName, fieldElement));
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

    private MethodSpec getMethodProxy(String annotatedName, ExecutableElement executableElement) {
        CodeBlock.Builder paramTypes = CodeBlock.builder();
        List<ParameterSpec> proxyParams = new ArrayList<>();
        if (!executableElement.getModifiers().contains(Modifier.STATIC)) {
            proxyParams.add(subjectParam);
        }
        for (VariableElement parameter : executableElement.getParameters()) {
            TypeMirror paramType = parameter.asType();
            paramTypes.add(", $T.class", typeUtils.erasure(paramType));
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

    private MethodSpec getFieldGetter(String annotatedName, VariableElement fieldElement) {
        FieldSpec fieldGetterHandle = FieldSpec.builder(MethodHandle.class, "fieldGetter$" + annotatedName, Modifier.STATIC, Modifier.PRIVATE)
                .initializer(CodeBlock.of("getGetterHandle($T.class, $S)", containingClassName, annotatedName))
                .build();
        handlerTypeSpec.addField(fieldGetterHandle);

        TypeName fieldType = TypeName.get(fieldElement.asType());

        MethodSpec.Builder getterMethodBuilder = MethodSpec.methodBuilder("getter$"+ annotatedName)
                .addModifiers(Modifier.STATIC, Modifier.PRIVATE)
                .returns(fieldType)
                //.addException(computerException)
                .beginControlFlow("try");
        if (fieldElement.getModifiers().contains(Modifier.STATIC)) {
            getterMethodBuilder
                    .addStatement("return ($T)$N.invokeExact()", fieldType, fieldGetterHandle);
        } else {
            getterMethodBuilder
                    .addParameter(subjectParam)
                    .addStatement("return ($T)$N.invokeExact($N)", fieldType, fieldGetterHandle, subjectParam);
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

    private CodeBlock convertValueToReturn(Element annotatedElement, TypeMirror returnType, CodeBlock targetInvoker) {
        CodeBlock.Builder valueReturner = CodeBlock.builder();
        if (returnType.getKind() == TypeKind.VOID) {
            valueReturner.addStatement(targetInvoker)
                    .addStatement("return $N.voidResult()", helperParam)
                    .build();
        } else {
            TypeKind returnTypeKind = returnType.getKind();
            if (returnTypeKind == TypeKind.DECLARED && returnType.toString().equals("java.lang.Object")) {
                messager.printMessage(Diagnostic.Kind.ERROR, "Raw Object returned for computer method, use a concrete type or Convertable instead", annotatedElement);
            }
            /*if (returnTypeKind.isPrimitive() || (returnTypeKind == TypeKind.DECLARED && (returnType).toString().equals("java.lang.String"))) {
                valueReturner.addStatement("return $L", targetInvoker);
            } else */
            TypeMirror erasedType = typeUtils.erasure(returnType);
            if (typeUtils.isAssignable(erasedType, collectionType)) {
                valueReturner.addStatement("return $N.convert($L, $N::convert)", helperParam, targetInvoker, helperParam);
            } else if (typeUtils.isAssignable(erasedType, mapType)) {
                valueReturner.addStatement("return $N.convert($L, $N::convert, $N::convert)", helperParam, targetInvoker, helperParam, helperParam);
            } else {
                valueReturner.addStatement("return $N.convert($L)", helperParam, targetInvoker);
            }
        }
        return valueReturner.build();
    }

    private CodeBlock callTargetMethod(String annotatedName, boolean isStatic, List<VariableElement> parameters) {
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

        targetMethodCodeBuilder.add(methodCallArguments.build());

        targetMethodCodeBuilder.add(")");
        return targetMethodCodeBuilder.build();
    }

    private CodeBlock buildRegisterMethodCall(AnnotationHelper annotationValues, List<VariableElement> parameters, TypeMirror returnType, MethodSpec handlerMethod, String computerExposedName, Object threadSafeLiteral) {
        return buildRegisterMethodCall(annotationValues, parameters, returnType, handlerMethod, CodeBlock.of("$S", computerExposedName),threadSafeLiteral);
    }

    private CodeBlock buildRegisterMethodCall(AnnotationHelper annotationValues, List<VariableElement> parameters, TypeMirror returnType, MethodSpec handlerMethod, Object computerExposedName, Object threadSafeLiteral) {
        CodeBlock.Builder registerMethodBuilder = CodeBlock.builder();
        //Computer exposed method name
        registerMethodBuilder.add("register($L, ", computerExposedName);
        //restriction
        registerMethodBuilder.add("$L, ", annotationValues.getLiteral("restriction", null));
        //mods required
        List<String> modsRequired = annotationValues.getStringArray("requiredMods");
        if (modsRequired.isEmpty()) {
            registerMethodBuilder.add("NO_STRINGS, ");
        } else {
            registerMethodBuilder.add("$L, ", annotationValues.getLiteral("requiredMods", "NO_STRINGS"));
        }
        //threadsafe
        registerMethodBuilder.add("$L, ", threadSafeLiteral);
        //param names
        if (parameters.isEmpty()) {
            registerMethodBuilder.add("NO_STRINGS, ");//names
            registerMethodBuilder.add("NO_CLASSES, ");//classes
        } else {
            registerMethodBuilder.add("new String[]{$L}, ", parameters.stream().map(param -> CodeBlock.of("$S", param.getSimpleName())).collect(CodeBlock.joining(",")));
            registerMethodBuilder.add("new Class[]{$L}, ", parameters.stream().map(param -> CodeBlock.of("$T.class", typeUtils.erasure(param.asType()))).collect(CodeBlock.joining(",")));
        }
        //return type
        registerMethodBuilder.add("$T.class, ", TypeName.get(typeUtils.erasure(returnType)));
        //method reference to handler method
        registerMethodBuilder.add("$N::$N", handlerClassName, handlerMethod);
        registerMethodBuilder.add(")");
        return registerMethodBuilder.build();
    }

    private MethodSpec buildHandlerMethod(String methodName, CodeBlock methodBody) {
        return MethodSpec.methodBuilder(methodName)
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(Object.class)
                .addException(computerException)
                .addParameter(subjectParam)
                .addParameter(helperParam)
                .addCode(methodBody).build();
    }
}
