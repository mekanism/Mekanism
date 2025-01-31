package mekanism.builder;

import com.palantir.javapoet.AnnotationSpec;
import com.palantir.javapoet.ClassName;
import com.palantir.javapoet.CodeBlock;
import com.palantir.javapoet.FieldSpec;
import com.palantir.javapoet.JavaFile;
import com.palantir.javapoet.MethodSpec;
import com.palantir.javapoet.ParameterSpec;
import com.palantir.javapoet.ParameterizedTypeName;
import com.palantir.javapoet.TypeName;
import com.palantir.javapoet.TypeSpec;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.WrongMethodTypeException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import javax.tools.Diagnostic.Kind;
import mekanism.MekAnnotationProcessors;
import mekanism.util.FakeParameter;
import mekanism.visitors.AnnotationHelper;
import mekanism.visitors.ParamToHelperMapper;

/**
 * Heavy lifting class to generate a Factory for a single target class
 */
public class ComputerHandlerBuilder {

    private static final ClassName computerMethodFactoryRaw = ClassName.get(MekAnnotationProcessors.COMPUTER_INTEGRATION_PACKAGE, "ComputerMethodFactory");
    private static final ClassName baseComputerHelper = ClassName.get(MekAnnotationProcessors.COMPUTER_INTEGRATION_PACKAGE, "BaseComputerHelper");
    private static final ClassName computerException = ClassName.get(MekAnnotationProcessors.COMPUTER_INTEGRATION_PACKAGE, "ComputerException");
    private static final ClassName methodData = ClassName.get(MekAnnotationProcessors.COMPUTER_INTEGRATION_PACKAGE, "MethodData");
    private static TypeMirror computerMethodAnnotationType;
    private static TypeMirror syntheticComputerMethodAnnotationType;
    private static TypeMirror wrappingComputerMethodAnnotationType;
    private static TypeMirror wrappingComputerMethodDocAnnotationType;
    private static TypeMirror collectionType;
    private static TypeMirror mapType;
    private static TypeMirror convertableType;
    private static TypeMirror eitherType;
    private static final ParameterSpec helperParam = ParameterSpec.builder(baseComputerHelper, "helper").build();
    private static ParamToHelperMapper paramToHelperMapper;

    /**
     * Called from annotation processor init, sets up our common helper members.
     *
     * @param elementUtils Elements from processor
     * @param typeUtils    Types from processor
     */
    public static void init(Elements elementUtils, Types typeUtils) {
        computerMethodAnnotationType = Objects.requireNonNull(elementUtils.getTypeElement(MekAnnotationProcessors.ANNOTATION_COMPUTER_METHOD)).asType();
        syntheticComputerMethodAnnotationType = Objects.requireNonNull(elementUtils.getTypeElement(MekAnnotationProcessors.ANNOTATION_SYNTHETIC_COMPUTER_METHOD)).asType();
        wrappingComputerMethodAnnotationType = Objects.requireNonNull(elementUtils.getTypeElement(MekAnnotationProcessors.ANNOTATION_WRAPPING_COMPUTER_METHOD)).asType();
        wrappingComputerMethodDocAnnotationType = Objects.requireNonNull(elementUtils.getTypeElement(MekAnnotationProcessors.ANNOTATION_WRAPPING_COMPUTER_METHOD_DOC)).asType();
        collectionType = Objects.requireNonNull(elementUtils.getTypeElement("java.util.Collection")).asType();
        mapType = Objects.requireNonNull(elementUtils.getTypeElement("java.util.Map")).asType();
        eitherType = Objects.requireNonNull(elementUtils.getTypeElement("com.mojang.datafixers.util.Either")).asType();
        convertableType = Objects.requireNonNull(elementUtils.getTypeElement(MekAnnotationProcessors.COMPUTER_INTEGRATION_PACKAGE + ".Convertable")).asType();
        TypeMirror filterInterface = typeUtils.erasure(elementUtils.getTypeElement("mekanism.common.content.filter.IFilter").asType());
        paramToHelperMapper = new ParamToHelperMapper(helperParam, filterInterface, typeUtils);
    }

    private final Types typeUtils;
    private final Messager messager;
    private final Elements elementUtils;
    /** the eventual output factory type builder */
    private final TypeSpec.Builder handlerTypeSpec;
    /** param spec for the subject provided to each method */
    private final ParameterSpec subjectParam;
    /** the generate Factory simple classname */
    private final String handlerClassName;
    /** class name of the subject type */
    private final ClassName containingClassName;
    /** the constructor of the factory */
    private final MethodSpec.Builder constructorBuilder = MethodSpec.constructorBuilder().addModifiers(Modifier.PUBLIC);

    /** stores field getter methods which internally call the method handle. Used to call by name */
    private final Map<Element, MethodSpec> fieldGetters = new HashMap<>();
    /** stores proxy methods for MethodHandle provided methods. Used to call by name */
    private final Map<Element, MethodSpec> methodProxies = new HashMap<>();

    private final Map<List<String>, FieldSpec> paramNameConstants = new HashMap<>();
    private final Map<List<String>, FieldSpec> paramTypeConstants = new HashMap<>();

    public ComputerHandlerBuilder(TypeElement containingType, ProcessingEnvironment env) {
        this.typeUtils = env.getTypeUtils();
        this.messager = env.getMessager();
        this.elementUtils = env.getElementUtils();
        this.handlerClassName = containingType.getSimpleName() + "$ComputerHandler";
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

    /**
     * Builds the Factory class
     *
     * @param annotatedElements the elements annotated with one of the supported annotations
     *
     * @return a build JavaFile ready for output
     */
    public JavaFile build(List<Element> annotatedElements) {

        for (Element annotatedElement : annotatedElements) {
            String annotatedName = annotatedElement.getSimpleName().toString();

            Set<Modifier> modifiers = annotatedElement.getModifiers();
            boolean isPrivate = modifiers.contains(Modifier.PRIVATE);
            boolean isStatic = modifiers.contains(Modifier.STATIC);

            for (AnnotationMirror annotationMirror : annotatedElement.getAnnotationMirrors()) {
                AnnotationHelper annotationValues = new AnnotationHelper(elementUtils, annotationMirror);
                if (typeUtils.isSameType(annotationMirror.getAnnotationType(), computerMethodAnnotationType)) {
                    handlerTypeSpec.addOriginatingElement(annotatedElement);
                    if (annotatedElement instanceof ExecutableElement executableElement) {
                        handleAtComputerMethod(annotatedName, isPrivate, isStatic, annotationValues, executableElement);
                    } else {
                        messager.printMessage(Kind.ERROR, "Unable to handle, expected ExecutableElement but found " + annotatedElement.getClass().getSimpleName());
                    }
                } else if (typeUtils.isSameType(annotationMirror.getAnnotationType(), syntheticComputerMethodAnnotationType)) {
                    handlerTypeSpec.addOriginatingElement(annotatedElement);
                    if (annotatedElement instanceof VariableElement fieldElement) {
                        handleAtSyntheticMethod(annotatedName, isPrivate, isStatic, annotationValues, fieldElement);
                    } else {
                        messager.printMessage(Kind.ERROR, "Unable to handle, expected VariableElement but found " + annotatedElement.getClass().getSimpleName());
                    }
                } else if (typeUtils.isSameType(annotationMirror.getAnnotationType(), wrappingComputerMethodAnnotationType)) {
                    handlerTypeSpec.addOriginatingElement(annotatedElement);
                    handleAtWrappingComputerMethod(annotatedElement, annotatedName, isPrivate, isStatic, annotationValues);
                }
            }
        }

        handlerTypeSpec.addMethod(constructorBuilder.build());
        handlerTypeSpec.addFields(paramNameConstants.values());
        handlerTypeSpec.addFields(paramTypeConstants.values());
        TypeSpec factorySpec = handlerTypeSpec.build();

        return JavaFile.builder(containingClassName.packageName(), factorySpec).build();
    }

    /**
     * Build method for a @WrappingComputerMethod
     *
     * @param annotatedElement the field or method annotated
     * @param annotatedName    the field or method simple name
     * @param isPrivate        modifiers checked previously
     * @param isStatic         modifier checked previously
     * @param annotationValues an annotation helper for this annotation's values
     */
    private void handleAtWrappingComputerMethod(Element annotatedElement, String annotatedName, boolean isPrivate, boolean isStatic, AnnotationHelper annotationValues) {
        //get the wrapper type and find its static methods
        TypeElement wrapperTypeEl;
        TypeMirror wrapperTypeMirror = annotationValues.getClassValue("wrapper");
        if (typeUtils.asElement(wrapperTypeMirror) instanceof TypeElement typeElement) {
            wrapperTypeEl = typeElement;
        } else {
            messager.printMessage(Diagnostic.Kind.ERROR, "Could not find wrapper Type Element", annotatedElement);
            return;
        }
        handlerTypeSpec.addOriginatingElement(wrapperTypeEl);
        List<ExecutableElement> wrapperMethods = gatherWrapperMethods(wrapperTypeEl);

        //gather the target method names and check we got the same amount
        List<String> targetMethodNames = annotationValues.getStringArray("methodNames");
        if (targetMethodNames.size() != wrapperMethods.size()) {
            messager.printMessage(Diagnostic.Kind.ERROR, "Mismatched methodName count. Expected " + wrapperMethods.size() + ", found " + targetMethodNames.size(), annotatedElement);
            return;
        }
        String docPlaceholder = annotationValues.getStringValue("docPlaceholder", null);

        //get the value part of the getter (i.e. what we use as the param to the wrapper)
        CodeBlock targetReference;
        if (annotatedElement instanceof VariableElement fieldElement) {
            targetReference = getReadTargetReferenceForField(annotatedName, isPrivate, isStatic, fieldElement);
        } else if (annotatedElement instanceof ExecutableElement executableElement) {
            //get either the proxy method or call the actual method
            if (isPrivate) {
                MethodSpec proxyMethod = methodProxies.computeIfAbsent(executableElement, el -> getMethodProxy(annotatedName, executableElement));
                if (isStatic) {
                    targetReference = CodeBlock.of("$N()", proxyMethod);
                } else {
                    targetReference = CodeBlock.of("$N($N)", proxyMethod, subjectParam);
                }
            } else {
                targetReference = callTargetMethod(annotatedName, isStatic, Collections.emptyList());
            }
        } else {
            throw new IllegalStateException("Unknown element type: " + annotatedElement.getClass());
        }

        //create a method for each wrapper method in format 'annotatedName$wrapperMethodName'
        for (int i = 0; i < targetMethodNames.size(); i++) {
            String targetMethodName = targetMethodNames.get(i);
            ExecutableElement wrapperMethod = wrapperMethods.get(i);
            handlerTypeSpec.addOriginatingElement(wrapperMethod);
            //invoke the wrapper
            CodeBlock targetInvoker = CodeBlock.of("$T.$L($L)", wrapperTypeMirror, wrapperMethod.getSimpleName(), targetReference);
            //convert the wrapper's return value
            CodeBlock valueReturner = convertValueToReturn(wrapperMethod, wrapperMethod.getReturnType(), targetInvoker);

            //build the method
            CodeBlock methodBody = CodeBlock.builder()
                  .add(valueReturner)
                  .build();
            MethodSpec handlerMethod = buildHandlerMethod(annotatedName + "$" + targetMethodName, methodBody);
            handlerTypeSpec.addMethod(handlerMethod);

            //generate the doc string, if we have all the data
            String methodDescription = null;
            if (docPlaceholder != null) {
                String unformattedDescription = wrapperMethod.getAnnotationMirrors()
                      .stream()
                      //find the doc annotation on the target method
                      .filter(it -> typeUtils.isSameType(it.getAnnotationType(), wrappingComputerMethodDocAnnotationType))
                      .findFirst()
                      //find the 'value' member
                      .flatMap(am -> am.getElementValues()
                            .entrySet().stream()
                            .filter(it -> it.getKey().getSimpleName().contentEquals("value"))
                            //we just need its value
                            .map(Entry::getValue)
                            .findFirst()
                      )
                      //get the value as a string
                      .map(v -> (String) v.getValue())
                      //something in the chain failed, default to null
                      .orElse(null);
                if (unformattedDescription != null) {
                    //format it
                    methodDescription = String.format(Locale.ROOT, unformattedDescription, docPlaceholder);
                }
            }

            //add a call to register() in the handler class's constructor
            CodeBlock registerMethodBuilder = buildRegisterMethodCall(annotationValues, Collections.emptyList(), wrapperMethod.getReturnType(), handlerMethod, targetMethodName, annotationValues.getBooleanValue("threadSafe", false), methodDescription);
            constructorBuilder.addStatement(registerMethodBuilder);

        }
    }

    /**
     * Get a list of public static methods of the wrapper type. They should have only one param, but this is not currently checked.
     *
     * @param wrapperTypeEl the wrapper type to check
     *
     * @return a list of method elements
     */
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

    /**
     * Generate a getter and/or setter for a field. Private fields do not support setter (currently unused anyway)
     *
     * @param annotatedName    the field or method simple name
     * @param isPrivate        modifiers checked previously
     * @param isStatic         modifier checked previously
     * @param annotationValues an annotation helper for this annotation's values
     * @param fieldElement     the field annotated
     */
    private void handleAtSyntheticMethod(String annotatedName, boolean isPrivate, boolean isStatic, AnnotationHelper annotationValues, VariableElement fieldElement) {
        //get a code reference to the field, or a call of the generated getter
        CodeBlock targetReference = getReadTargetReferenceForField(annotatedName, isPrivate, isStatic, fieldElement);

        //the type of the annotated field
        TypeMirror fieldType = fieldElement.asType();

        //generate a getter if needed
        String getterName = annotationValues.getStringValue("getter", null);
        if (getterName != null) {
            //convert the field result
            CodeBlock valueReturner = convertValueToReturn(fieldElement, fieldType, targetReference);
            //add a method for it
            MethodSpec handlerMethod = buildHandlerMethod(getterName + "_0", valueReturner);
            handlerTypeSpec.addMethod(handlerMethod);

            //ensure the getter is registered
            CodeBlock getterRegistration = buildRegisterMethodCall(annotationValues, Collections.emptyList(), fieldType, handlerMethod, getterName, annotationValues.getBooleanValue("threadSafeGetter", false), annotationValues.getStringValue("getterDescription", null));
            constructorBuilder.addStatement(getterRegistration);
        }

        //generate a setter if needed
        String setterName = annotationValues.getStringValue("setter", null);
        if (setterName != null) {
            //bail if the setter is not directly accessible
            if (isPrivate) {
                messager.printMessage(Diagnostic.Kind.ERROR, "Setter not implemented for private fields");
                return;
            }
            //generate a method which sets the value and then returns a void result
            CodeBlock setterBody = CodeBlock.builder()
                  .addStatement("$L = $L", targetReference, fieldType.accept(paramToHelperMapper, 0))
                  .addStatement("return $N.voidResult()", helperParam)
                  .build();
            MethodSpec handlerMethod = buildHandlerMethod(setterName + "_1", setterBody);
            handlerTypeSpec.addMethod(handlerMethod);

            //register the setter
            CodeBlock setterRegistration = buildRegisterMethodCall(annotationValues, Collections.singletonList(new FakeParameter(fieldType, "value")), typeUtils.getNoType(TypeKind.VOID), handlerMethod, setterName, annotationValues.getBooleanValue("threadSafeSetter", false), annotationValues.getStringValue("setterDescription", null));
            constructorBuilder.addStatement(setterRegistration);
        }

        if (getterName == null && setterName == null) {
            messager.printMessage(Diagnostic.Kind.ERROR, "SyntheticMethod with no getter nor setter", fieldElement);
        }
    }

    /**
     * Handle an @ComputerMethod annotated method
     *
     * @param annotatedName     the field or method simple name
     * @param isPrivate         modifiers checked previously
     * @param isStatic          modifier checked previously
     * @param annotationValues  an annotation helper for this annotation's values
     * @param executableElement the method with the annotation
     */
    private void handleAtComputerMethod(String annotatedName, boolean isPrivate, boolean isStatic, AnnotationHelper annotationValues, ExecutableElement executableElement) {
        //bail if method isn't directly accessible for performance reasons
        if (isPrivate) {
            messager.printMessage(Diagnostic.Kind.ERROR, "Target for @ComputerMethod should be not be private", executableElement);
            return;
        }
        TypeMirror returnType = executableElement.getReturnType();
        @SuppressWarnings("unchecked")
        List<VariableElement> parameters = (List<VariableElement>) executableElement.getParameters();

        //generate a call to the target method
        CodeBlock targetInvoker = callTargetMethod(annotatedName, isStatic, parameters);

        //determine the return method, value or no value
        //wrap the target method code, or call it and return void
        CodeBlock valueReturner = convertValueToReturn(executableElement, returnType, targetInvoker);

        String nameOverride = annotationValues.getStringValue("nameOverride", annotatedName);

        //add the method to the class
        MethodSpec handlerMethod = buildHandlerMethod(nameOverride + "_" + parameters.size(), valueReturner);
        handlerTypeSpec.addMethod(handlerMethod);

        //add a call to register() in the handler class's constructor
        CodeBlock registerMethodBuilder = buildRegisterMethodCall(annotationValues, parameters, returnType, handlerMethod, nameOverride, annotationValues.getBooleanValue("threadSafe", false), annotationValues.getStringValue("methodDescription", null));
        constructorBuilder.addStatement(registerMethodBuilder);
    }

    /**
     * Generate a CodeBlock which grabs the value of the field.
     *
     * @param annotatedName the simple name of the field to get
     * @param isPrivate     if we need to use a proxy getter / method handle
     * @param isStatic      is it a static method
     * @param fieldElement  the element we're getting
     *
     * @return CodeBlock which references the field
     */
    private CodeBlock getReadTargetReferenceForField(String annotatedName, boolean isPrivate, boolean isStatic, VariableElement fieldElement) {
        CodeBlock.Builder targetFieldBuilder = CodeBlock.builder();
        if (isPrivate) {
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

    /**
     * Generates and adds a Proxy Method and MethodHandle field for a private/protected method Should always be called as a part of computeIfAbsent
     *
     * @param annotatedName     the simple name of the method
     * @param executableElement the Element of the method
     *
     * @return a generated method to use as an $N parameter
     */
    private MethodSpec getMethodProxy(String annotatedName, ExecutableElement executableElement) {
        /* the types of the params for use as $L for the Method(Handle) lookup */
        CodeBlock.Builder paramTypes = CodeBlock.builder();
        //the params of the target method, used as proxy method params
        List<ParameterSpec> proxyParams = new ArrayList<>();

        //non-static methods need a subject as the first param (signature and calling method handle)
        if (!executableElement.getModifiers().contains(Modifier.STATIC)) {
            proxyParams.add(subjectParam);
        }
        //gather the classes and the parameters
        for (VariableElement parameter : executableElement.getParameters()) {
            TypeMirror paramType = parameter.asType();
            paramTypes.add(", $T.class", typeUtils.erasure(paramType));
            proxyParams.add(ParameterSpec.get(parameter));
        }
        CodeBlock builtParamTypes = paramTypes.build();

        //build and add a field to hold the MethodHandle
        FieldSpec methodHandleField = FieldSpec.builder(MethodHandle.class, "method$" + annotatedName, Modifier.STATIC, Modifier.PRIVATE)
              .initializer(CodeBlock.of("getMethodHandle($T.class, $S$L)", containingClassName, annotatedName, builtParamTypes))
              .build();
        handlerTypeSpec.addField(methodHandleField);

        //Build and add the proxy method
        TypeName returnType = TypeName.get(executableElement.getReturnType());
        MethodSpec proxyMethod = MethodSpec.methodBuilder("proxy$" + annotatedName)
              .addParameters(proxyParams)
              .addModifiers(Modifier.STATIC, Modifier.PRIVATE)
              .addException(computerException)
              .returns(returnType)
              .beginControlFlow("try")
              //invoke the method handle
              .addStatement("return ($T)$N.invokeExact($L)", returnType, methodHandleField, proxyParams.stream().map(ParameterSpec::name).collect(Collectors.joining(", ")))
              //catch a failing method handle (throw as RuntimeException)
              .nextControlFlow("catch ($T wmte)", WrongMethodTypeException.class)
              .addStatement("throw new $T($S, wmte)", RuntimeException.class, "Method not bound correctly")
              //catch and rethrow a ComputerException
              .nextControlFlow("catch ($T cex)", computerException)
              .addStatement("throw cex")
              //catch other exceptions and rethrow as a RuntimeException
              .nextControlFlow("catch ($T t)", Throwable.class)
              .addStatement("throw new $T(t.getMessage(), t)", RuntimeException.class)
              .endControlFlow()
              .build();
        handlerTypeSpec.addMethod(proxyMethod);
        return proxyMethod;
    }

    /**
     * Create and add a field and proxy method to access a private/protected field
     *
     * @param annotatedName the simple name of the field to get
     * @param fieldElement  the Element of the referenced field
     *
     * @return a build method to be used as an $N value
     */
    private MethodSpec getFieldGetter(String annotatedName, VariableElement fieldElement) {
        //create and add the MethodHandle field
        FieldSpec fieldGetterHandle = FieldSpec.builder(MethodHandle.class, "fieldGetter$" + annotatedName, Modifier.STATIC, Modifier.PRIVATE)
              .initializer(CodeBlock.of("getGetterHandle($T.class, $S)", containingClassName, annotatedName))
              .build();
        handlerTypeSpec.addField(fieldGetterHandle);

        //build a proxy getter which uses the MethodHandle
        TypeName fieldType = TypeName.get(fieldElement.asType());
        MethodSpec.Builder getterMethodBuilder = MethodSpec.methodBuilder("getter$" + annotatedName)
              .addModifiers(Modifier.STATIC, Modifier.PRIVATE)
              .returns(fieldType)
              //.addException(computerException)
              .beginControlFlow("try");
        //call the MethodHandle
        if (fieldElement.getModifiers().contains(Modifier.STATIC)) {
            getterMethodBuilder
                  .addStatement("return ($T)$N.invokeExact()", fieldType, fieldGetterHandle);
        } else {
            getterMethodBuilder
                  .addParameter(subjectParam)
                  .addStatement("return ($T)$N.invokeExact($N)", fieldType, fieldGetterHandle, subjectParam);
        }
        //catch a failing method handle (throw as RuntimeException)
        getterMethodBuilder.nextControlFlow("catch ($T wmte)", WrongMethodTypeException.class)
              .addStatement("throw new $T($S, wmte)", RuntimeException.class, "Getter not bound correctly")
              //catch other unknown exception and rethrow as RuntimeException
              .nextControlFlow("catch ($T t)", Throwable.class)
              .addStatement("throw new $T(t.getMessage(), t)", RuntimeException.class)
              .endControlFlow();

        //build the proxy and add to the class
        MethodSpec getterMethod = getterMethodBuilder.build();
        handlerTypeSpec.addMethod(getterMethod);
        return getterMethod;
    }

    /**
     * Generates a CodeBlock which runs the return value through BaseComputerHelper.convert()
     *
     * @param annotatedElement the element to point an error to when the return type is ambiguous
     * @param returnType       the type that is returned from the target
     * @param targetInvoker    CodeBlock which produces the value
     *
     * @return a CodeBlock which contains a return statement with conversions applied
     */
    private CodeBlock convertValueToReturn(Element annotatedElement, TypeMirror returnType, CodeBlock targetInvoker) {
        CodeBlock.Builder valueReturner = CodeBlock.builder();
        //void methods just insert the target and return a void result
        if (returnType.getKind() == TypeKind.VOID) {
            valueReturner.addStatement(targetInvoker)
                  .addStatement("return $N.voidResult()", helperParam);
        } else {
            TypeKind returnTypeKind = returnType.getKind();
            //complain on Object return as it can't be run through convert methods
            if (returnTypeKind == TypeKind.DECLARED && returnType.toString().equals("java.lang.Object")) {
                messager.printMessage(Diagnostic.Kind.ERROR, "Raw Object returned for computer method, use a concrete type, Either, or Convertable (with annotation) instead", annotatedElement);
            }
            //uncomment to allow primitives to pass through unconverted
            /*if (returnTypeKind.isPrimitive() || (returnTypeKind == TypeKind.DECLARED && (returnType).toString().equals("java.lang.String"))) {
                valueReturner.addStatement("return $L", targetInvoker);
            } else */
            //check for Lists and Maps, which require extra conversion arguments
            TypeMirror erasedType = typeUtils.erasure(returnType);
            if (typeUtils.isAssignable(erasedType, collectionType)) {
                valueReturner.addStatement("return $N.convert($L, $N::convert)", helperParam, targetInvoker, helperParam);
            } else if (typeUtils.isAssignable(erasedType, mapType)) {
                valueReturner.addStatement("return $N.convert($L, $N::convert, $N::convert)", helperParam, targetInvoker, helperParam, helperParam);
            } else if (typeUtils.isAssignable(erasedType, eitherType)) {
                valueReturner.addStatement("return $L.map($N::convert, $N::convert)", targetInvoker, helperParam, helperParam);
            } else {
                //let the compiler figure out which method to call
                valueReturner.addStatement("return $N.convert($L)", helperParam, targetInvoker);
            }
        }
        return valueReturner.build();
    }

    /**
     * Directly call an accessible method, pulling parameters from the BaseComputerHelper
     *
     * @param annotatedName the simple name of the method to call
     * @param isStatic      is the method static
     * @param parameters    param elements of the referenced ExecutableElement
     *
     * @return a CodeBlock which calls the method
     */
    private CodeBlock callTargetMethod(String annotatedName, boolean isStatic, List<VariableElement> parameters) {
        CodeBlock.Builder methodCallArguments = CodeBlock.builder();
        //if the target method has params, add them to a CodeBlock we can insert
        //params are sourced from the Helper, i.e. helper.getX(idx)
        if (!parameters.isEmpty()) {
            List<CodeBlock> paramGetters = new ArrayList<>();
            for (int i = 0; i < parameters.size(); i++) {
                TypeMirror paramType = parameters.get(i).asType();
                paramGetters.add(paramType.accept(paramToHelperMapper, i));
            }
            //join them with ,
            methodCallArguments.add(CodeBlock.join(paramGetters, ", "));
        }

        //build the actual call
        CodeBlock.Builder targetMethodCodeBuilder = CodeBlock.builder();
        if (isStatic) {
            targetMethodCodeBuilder.add("$T.$L(", containingClassName, annotatedName);
        } else {
            targetMethodCodeBuilder.add("$N.$L(", subjectParam, annotatedName);
        }

        //add any params
        targetMethodCodeBuilder.add(methodCallArguments.build());

        //complete!
        targetMethodCodeBuilder.add(")");
        return targetMethodCodeBuilder.build();
    }

    /**
     * Build a CodeBlock which calls register() for a computer exposed method
     *
     * @param annotationValues    the values for the annotation used. Common params are pulled from here
     * @param parameters          the Java parameters of the method to be called. Param names/types pulled from here
     * @param returnType          the unconverted Java return type of the method/field
     * @param handlerMethod       the method that was generated to handle this computer method
     * @param computerExposedName either a name override or the annotated name, exposed to a computer
     * @param threadSafeLiteral   the value of the threadsafe annotation member (name varies in the case of synthetics)
     *
     * @return a CodeBlock to be added to the constructor
     */
    private CodeBlock buildRegisterMethodCall(AnnotationHelper annotationValues, List<VariableElement> parameters, TypeMirror returnType, MethodSpec handlerMethod, String computerExposedName, boolean threadSafeLiteral, String methodDescription) {
        CodeBlock.Builder registerMethodBuilder = CodeBlock.builder();
        //Computer exposed method name & handler reference
        registerMethodBuilder.add("register($T.builder($S, $N::$N)", methodData, computerExposedName, handlerClassName, handlerMethod);
        //restriction
        if (!annotationValues.getEnumConstantName("restriction", "NONE").equals("NONE")) {
            registerMethodBuilder.add(".restriction($L)", annotationValues.getLiteral("restriction", null));
        }
        //mods required
        List<String> modsRequired = annotationValues.getStringArray("requiredMods");
        if (!modsRequired.isEmpty()) {
            registerMethodBuilder.add(".requiredMods($L)", annotationValues.getLiteral("requiredMods", "NO_STRINGS"));
        }
        //threadsafe
        if (threadSafeLiteral) {
            registerMethodBuilder.add(".threadSafe()");
        }
        //return type
        TypeMirror erasedReturnType = typeUtils.erasure(returnType);
        if (erasedReturnType.getKind() != TypeKind.VOID) {
            registerMethodBuilder.add(".returnType($T.class)", TypeName.get(erasedReturnType));
        }
        //return extra
        if (typeUtils.isAssignable(erasedReturnType, collectionType) || typeUtils.isAssignable(erasedReturnType, mapType) || typeUtils.isAssignable(erasedReturnType, eitherType)) {
            if (returnType instanceof DeclaredType declaredType && !declaredType.getTypeArguments().isEmpty()) {
                registerMethodBuilder.add(".returnExtra($L)", typeMirrorListToClassCodeblock(declaredType.getTypeArguments()));
            } else {
                throw new RuntimeException("Unknown type: " + returnType.getClass());
            }
        } else if (typeUtils.isAssignable(erasedReturnType, convertableType)) {
            List<TypeMirror> possibleReturns = annotationValues.getClassArray("possibleReturns");
            if (!possibleReturns.isEmpty()) {
                registerMethodBuilder.add(".returnExtra($L)", typeMirrorListToClassCodeblock(possibleReturns));
            }
        }

        //method description
        if (methodDescription != null && !methodDescription.isBlank()) {
            registerMethodBuilder.add(".methodDescription($S)", methodDescription);
        }
        //requires public security
        if (annotationValues.getBooleanValue("requiresPublicSecurity", false)) {
            registerMethodBuilder.add(".requiresPublicSecurity()");
        }
        //param names
        if (!parameters.isEmpty()) {
            List<String> paramNames = parameters.stream().map(variableElement -> variableElement.getSimpleName().toString()).toList();
            FieldSpec paramNameField = this.paramNameConstants.computeIfAbsent(paramNames, params ->
                  FieldSpec.builder(String[].class, "NAMES_" + String.join("_", params), Modifier.PRIVATE, Modifier.FINAL)
                        .initializer("new String[]{$L}", params.stream().map(p -> CodeBlock.of("$S", p)).collect(CodeBlock.joining(",")))
                        .build()
            );
            List<String> paramTypes = parameters.stream().map(param -> typeUtils.erasure(param.asType()).toString()).toList();
            FieldSpec paramTypesField = this.paramTypeConstants.computeIfAbsent(paramTypes, typesKey ->
                  FieldSpec.builder(Class[].class, "TYPES_" + Integer.toHexString(typesKey.hashCode()), Modifier.PRIVATE, Modifier.FINAL)
                        .initializer("new Class[]{$L}", parameters.stream().map(param -> CodeBlock.of("$T.class", typeUtils.erasure(param.asType()))).collect(CodeBlock.joining(",")))
                        .build()
            );
            registerMethodBuilder.add(".arguments($N, $N)", paramNameField, paramTypesField);
        }
        registerMethodBuilder.add(")");
        return registerMethodBuilder.build();
    }

    private CodeBlock typeMirrorListToClassCodeblock(List<? extends TypeMirror> mirrors) {
        return mirrors.stream().map(typeMirror -> CodeBlock.of("$T.class", typeUtils.erasure(typeMirror))).collect(CodeBlock.joining(", "));
    }

    /**
     * Builds a handler method with a body. Accepts 2 params: subject & helper.
     *
     * @param methodName the raw name to use in java
     * @param methodBody the body of the method
     *
     * @return a MethodSpec to add to the TypeSpec
     */
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