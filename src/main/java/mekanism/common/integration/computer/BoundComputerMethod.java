package mekanism.common.integration.computer;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import javax.annotation.Nullable;

//TODO - 10.1: Validate this all works in Java9+ as it is possible something changed with primitives given the previous
// issues we had with changes LambdaMetaFactory received in relation to primitives
public class BoundComputerMethod {

    private static final String NULL_ARGUMENT_FORMAT = "Illegal argument #%d, %s expected %s but received %null";
    private static final String ILLEGAL_ARGUMENT_FORMAT = "Illegal argument #%d, %s expected %s but received %s of type %s";

    private final MethodHandle methodHandle;
    private final String methodName;

    BoundComputerMethod(MethodHandle methodHandle, String methodName) {
        this.methodHandle = methodHandle;
        this.methodName = methodName;
    }

    public <EXCEPTION extends Exception> void validateArguments(IComputerArgumentHandler<EXCEPTION, ?> argumentHandler) throws EXCEPTION {
        int argumentCount = argumentHandler.getCount();
        MethodType methodType = methodHandle.type();
        int expectedCount = methodType.parameterCount();
        if (argumentCount != expectedCount) {
            throw argumentHandler.error("Mismatched parameter count. %s expected: '%d' arguments, but received: '%d' arguments.", methodName, expectedCount,
                  argumentCount);
        }
        //Note: We implement our own type checking here rather than relying on exceptions to occur when trying to invoke
        // the method handle as it is a lot quicker to quickly do some minor type checking, than have the java native stuff
        // run into it and handle it via exceptions
        for (int index = 0; index < expectedCount; index++) {
            Class<?> expectedType = methodType.parameterType(index);
            Object argument = argumentHandler.getArgument(index);
            if (expectedType.isPrimitive()) {
                if (argument == null) {
                    //Argument is null, so there is no chance it is valid as a primitive
                    throw argumentHandler.error(NULL_ARGUMENT_FORMAT, index, methodName, expectedType.getSimpleName());
                }//Else see if we can cast it
                Class<?> argumentClass = argument.getClass();
                if (expectedType != argumentClass) {
                    //Types are different
                    if (argumentClass.isPrimitive()) {
                        if (isInvalidUpcast(argumentClass, expectedType)) {
                            //Validate if we are allowed to upcast or not from one type to another
                            throw argumentHandler.error(ILLEGAL_ARGUMENT_FORMAT, index, methodName, expectedType.getSimpleName(), argument, argumentClass.getSimpleName());
                        }
                    } else {
                        Class<?> primitiveArgumentClass = getPrimitiveType(argumentClass);
                        if (expectedType != primitiveArgumentClass || isInvalidUpcast(primitiveArgumentClass, expectedType)) {
                            //Test if we are able to auto unbox, and if needed after unboxing, upcast; if we can't error
                            throw argumentHandler.error(ILLEGAL_ARGUMENT_FORMAT, index, methodName, expectedType.getSimpleName(), argument, argumentClass.getSimpleName());
                        }
                    }
                }
            } else if (argument == null) {
                //Note: For now we treat things as nonnull, though we may want to eventually allow for nulls in which case
                // we would need to come up with some way to decide if it can be null or not in the given position
                throw argumentHandler.error(NULL_ARGUMENT_FORMAT, index, methodName, expectedType.getSimpleName());
            } else {
                Class<?> argumentClass = argument.getClass();
                if (expectedType != argumentClass) {
                    //Types are different
                    if (!argumentClass.isPrimitive() || expectedType != getPrimitiveType(argumentClass)) {
                        //if our argument is not a primitive, or the type doesn't match after autoboxing, error
                        throw argumentHandler.error(ILLEGAL_ARGUMENT_FORMAT, index, methodName, expectedType.getSimpleName(), argument, argumentClass.getSimpleName());
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
            //Note: We manually call invoke for a good number of arguments until we fallback to invokeWithArguments, as there is a pretty
            // sizable performance difference in the two methods. We also call invoke instead of invokeExact for the zero argument count
            // as it requires knowing the return type by casting which causes us issues as we don't know it at compile time and thus cannot
            // specify the cast to the correct type directly
            if (argumentCount == 0) {
                result = methodHandle.invoke();
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
                //Note: If we ever really get to the point this needs to be used for the number of parameters, we should heavily consider
                // adding in more argumentCount based special cases so as to improve the overall performance in the calls to the method
                result = methodHandle.invokeWithArguments(argumentHandler.getArguments());
            }
        } catch (Throwable e) {
            //Possible errors for invoke/invokeWithArguments:
            // - WrongMethodTypeException if the target's type is not identical with the caller's symbolic type descriptor
            // - ClassCastException if the target's type can be adjusted to the caller, but a reference cast fails
            // - Throwable anything thrown by the underlying method propagates unchanged through the method handle call
            // In theory none of these should actually happen given we do parameter validation, but just in case one does
            // we wrap the message into an error message that can be displayed by our handler
            throw argumentHandler.error(e.getMessage());
        }
        Class<?> returnType = methodType.returnType();
        //Check both potential void types for methods to see if we have a return type
        if (returnType == Void.class || returnType == void.class) {
            return argumentHandler.noResult();
        }
        return argumentHandler.wrapResult(result);
    }

    private static boolean isInvalidUpcast(Class<?> argumentClass, Class<?> targetClass) {
        if (argumentClass == Byte.TYPE) {
            return targetClass != Short.TYPE && targetClass != Integer.TYPE && targetClass != Long.TYPE && targetClass != Float.TYPE && targetClass != Double.TYPE;
        } else if (argumentClass == Character.TYPE || argumentClass == Short.TYPE) {
            return targetClass != Integer.TYPE && targetClass != Long.TYPE && targetClass != Float.TYPE && targetClass != Double.TYPE;
        } else if (argumentClass == Integer.TYPE) {
            return targetClass != Long.TYPE && targetClass != Float.TYPE && targetClass != Double.TYPE;
        } else if (argumentClass == Long.TYPE) {
            return targetClass != Float.TYPE && targetClass != Double.TYPE;
        } else if (argumentClass == Float.TYPE) {
            return targetClass != Double.TYPE;
        }
        return true;
    }

    @Nullable
    private static Class<?> getPrimitiveType(Class<?> objectClass) {
        if (objectClass == Boolean.class) {
            return Boolean.TYPE;
        } else if (objectClass == Character.class) {
            return Character.TYPE;
        } else if (objectClass == Byte.class) {
            return Byte.TYPE;
        } else if (objectClass == Short.class) {
            return Short.TYPE;
        } else if (objectClass == Integer.class) {
            return Integer.TYPE;
        } else if (objectClass == Long.class) {
            return Long.TYPE;
        } else if (objectClass == Float.class) {
            return Float.TYPE;
        } else if (objectClass == Double.class) {
            return Double.TYPE;
        } else if (objectClass == Void.class) {
            return Void.TYPE;
        }
        return null;
    }
}