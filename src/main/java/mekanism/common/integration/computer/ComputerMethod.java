package mekanism.common.integration.computer;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import javax.annotation.Nonnull;

public class ComputerMethod {

    private static final String ILLEGAL_ARGUMENT_FORMAT = "Illegal argument #%d, expected %s but received %s";

    private final MethodHandle methodHandle;
    private final String methodName;

    //TODO: figure out how we want to handle the constructor for this and if we really want to just be storing a MethodHandle
    public ComputerMethod(MethodHandle methodHandle, String methodName) {
        this.methodHandle = methodHandle;
        this.methodName = methodName;
    }

    @Nonnull
    public String getMethodName() {
        return methodName;
    }

    public <EXCEPTION extends Exception> void validateArguments(IComputerArgumentHandler<EXCEPTION, ?> argumentHandler) throws EXCEPTION {
        int argumentCount = argumentHandler.getCount();
        MethodType methodType = methodHandle.type();
        int expectedCount = methodType.parameterCount();
        if (argumentCount != expectedCount) {
            throw argumentHandler.error("Mismatched parameter count. %s expected: '%d' arguments, but received: '%d' arguments.", getMethodName(),
                  expectedCount, argumentCount);
        }
        //TODO: Validate this all works in Java9+ as it is possible maybe the primitive based stuff is all weird now
        //TODO: Check the performance implications of all this, and see if we can maybe somehow improve/cache stuff to simplify the checks?
        // Would it potentially be more performant to loop over the expected ones, and then compare it to the actual
        //TODO: Figure out the performance of doing our own type checking vs:
        // methodHandle.invoke(argumentHandler.getArguments()) or methodHandle.invokeWithArguments(argumentHandler.getArguments())
        // It probably is worthwhile to do some profiling/testing to see if the various invoke methods even properly cast things
        // that we want to have cast in addition to seeing what the performance is like
        // Note: I think the most performant thing will be to do our own type checking, and then when calling have presets for a few
        // low number of param calls to MethodHandle#invoke, and then a fallback for some absurd number that uses MethodHandle#invokeWithArguments
        // as invoke allows for passing objects but not passing an array of objects, whereas invokeWithArguments does allow for this, but has a
        // pretty decent performance penalty
        for (int index = 0; index < expectedCount; index++) {
            Class<?> expectedType = methodType.parameterType(index);
            Object argument = argumentHandler.getArgument(index);
            if (expectedType.isPrimitive()) {
                if (argument == null) {
                    //Argument is null, so there is no chance it is valid as a primitive
                    //TODO: Validate if this properly prints the name of the primitive
                    throw argumentHandler.error(ILLEGAL_ARGUMENT_FORMAT, index, expectedType.getSimpleName(), null);
                }//Else see if we can cast it
                Class<?> argumentClass = argument.getClass();
                if (expectedType != argumentClass) {
                    //Types are different
                    if (argumentClass.isPrimitive()) {
                        //TODO: Does LUA allow for implicit casts/should we allow for them?
                    } else {
                        //TODO: See if we can auto unbox (we might need to as I think CCTweaked may only keep track of the numbers as Number
                        // instead of the corresponding numeric type)
                    }
                }
            } else if (argument == null) {
                //TODO: Decide if we want to allow for nulls or make things nonnull?
                // If we decide to allow for nulls, validate that it can be null in this spot
                throw argumentHandler.error(ILLEGAL_ARGUMENT_FORMAT, index, expectedType.getSimpleName(), null);
            } else {
                Class<?> argumentClass = argument.getClass();
                if (expectedType != argumentClass) {
                    //Types are different
                    if (argumentClass.isPrimitive()) {
                        //TODO: See if we can auto box it to the type we are expecting

                    } else {
                        //TODO: Decide if we should check about implicit casts from say Integer to Double? (Probably not)
                    }
                }
            }
        }
    }

    public <EXCEPTION extends Exception, RESULT> RESULT run(IComputerArgumentHandler<EXCEPTION, RESULT> argumentHandler) throws EXCEPTION {
        MethodType methodType = methodHandle.type();
        int argumentCount = methodType.parameterCount();
        Object result;
        try {
            //TODO: Do some profiling to see if actually manually calling invoke instead of invokeWithArguments has better performance
            //TODO: Do we need to be passing the class in for the method handle, given it is not static, or is that just part of grabbing the MethodHandle
            // if so is that what MethodHandle#bindTo is used for, and if so we should potentially do that when creating the ComputerMethod
            // Though maybe we want to abstract a layer of it so that we only have to bind it when running so we can keep the same method handle for all
            // sub classes as well?
            if (argumentCount == 0) {
                result = methodHandle.invokeExact();
            } else if (argumentCount == 1) {
                result = methodHandle.invoke(argumentHandler.getArgument(0));
            } else if (argumentCount == 2) {
                result = methodHandle.invoke(argumentHandler.getArgument(0), argumentHandler.getArgument(1));
            } else if (argumentCount == 3) {
                result = methodHandle.invoke(argumentHandler.getArgument(0), argumentHandler.getArgument(1), argumentHandler.getArgument(2));
            } else if (argumentCount == 4) {
                result = methodHandle.invoke(argumentHandler.getArgument(0), argumentHandler.getArgument(1), argumentHandler.getArgument(2),
                      argumentHandler.getArgument(3));
            } else if (argumentCount == 5) {
                result = methodHandle.invoke(argumentHandler.getArgument(0), argumentHandler.getArgument(1), argumentHandler.getArgument(2),
                      argumentHandler.getArgument(3), argumentHandler.getArgument(4));
            } else if (argumentCount == 6) {
                result = methodHandle.invoke(argumentHandler.getArgument(0), argumentHandler.getArgument(1), argumentHandler.getArgument(2),
                      argumentHandler.getArgument(3), argumentHandler.getArgument(4), argumentHandler.getArgument(5));
            } else if (argumentCount == 7) {
                result = methodHandle.invoke(argumentHandler.getArgument(0), argumentHandler.getArgument(1), argumentHandler.getArgument(2),
                      argumentHandler.getArgument(3), argumentHandler.getArgument(4), argumentHandler.getArgument(5),
                      argumentHandler.getArgument(6));
            } else if (argumentCount == 8) {
                result = methodHandle.invoke(argumentHandler.getArgument(0), argumentHandler.getArgument(1), argumentHandler.getArgument(2),
                      argumentHandler.getArgument(3), argumentHandler.getArgument(4), argumentHandler.getArgument(5),
                      argumentHandler.getArgument(6), argumentHandler.getArgument(7));
            } else if (argumentCount == 9) {
                result = methodHandle.invoke(argumentHandler.getArgument(0), argumentHandler.getArgument(1), argumentHandler.getArgument(2),
                      argumentHandler.getArgument(3), argumentHandler.getArgument(4), argumentHandler.getArgument(5),
                      argumentHandler.getArgument(6), argumentHandler.getArgument(7), argumentHandler.getArgument(8));
            } else {
                result = methodHandle.invokeWithArguments(argumentHandler.getArguments());
            }
        } catch (Throwable e) {
            //TODO: Handle errors:
            // invokeExact:
            //  - WrongMethodTypeException if the target's type is not identical with the caller's symbolic type descriptor
            //  - Throwable anything thrown by the underlying method propagates unchanged through the method handle call
            // invoke/invokeWithArguments:
            //  - WrongMethodTypeException if the target's type is not identical with the caller's symbolic type descriptor
            //  - ClassCastException if the target's type can be adjusted to the caller, but a reference cast fails
            //  - Throwable anything thrown by the underlying method propagates unchanged through the method handle call
            throw argumentHandler.error(e.getMessage());
        }
        Class<?> returnType = methodType.returnType();
        //Check both potential void types for methods to see if we have a return type
        if (returnType == Void.class || returnType == void.class) {
            return argumentHandler.noResult();
        }
        return argumentHandler.wrapResult(result);
    }
}