package mekanism;

import com.squareup.javapoet.ClassName;

public class MekAnnotationProcessors {
    public static final String MODULE_OPTION = "mekanismModule";
    public static final String COMPUTER_METHOD_FACTORY_ANNOTATION_CLASSNAME = "mekanism.common.integration.computer.annotation.MethodFactory";
    public static final ClassName COMPUTER_METHOD_FACTORY_ANNOTATION = ClassName.bestGuess(COMPUTER_METHOD_FACTORY_ANNOTATION_CLASSNAME);
}
