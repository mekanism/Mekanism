package mekanism;

import com.palantir.javapoet.ClassName;

public class MekAnnotationProcessors {

    public static final String MODULE_OPTION = "mekanismModule";

    //Packages
    public static final String COMPUTER_INTEGRATION_PACKAGE = "mekanism.common.integration.computer";
    public static final String ANNOTATION_PACKAGE = COMPUTER_INTEGRATION_PACKAGE + ".annotation";

    //annotations
    public static final String COMPUTER_METHOD_FACTORY_ANNOTATION_CLASSNAME = ANNOTATION_PACKAGE + ".MethodFactory";
    public static final String ANNOTATION_COMPUTER_METHOD = ANNOTATION_PACKAGE + ".ComputerMethod";
    public static final String ANNOTATION_SYNTHETIC_COMPUTER_METHOD = ANNOTATION_PACKAGE + ".SyntheticComputerMethod";
    public static final String ANNOTATION_WRAPPING_COMPUTER_METHOD = ANNOTATION_PACKAGE + ".WrappingComputerMethod";
    public static final String ANNOTATION_WRAPPING_COMPUTER_METHOD_DOC = ANNOTATION_WRAPPING_COMPUTER_METHOD + ".WrappingComputerMethodHelp";

    //class names
    public static final ClassName COMPUTER_METHOD_FACTORY_ANNOTATION = ClassName.bestGuess(COMPUTER_METHOD_FACTORY_ANNOTATION_CLASSNAME);
}
