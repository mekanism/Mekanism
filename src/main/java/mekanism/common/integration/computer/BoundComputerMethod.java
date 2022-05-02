package mekanism.common.integration.computer;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nullable;

public class BoundComputerMethod {

    private final String methodName;
    private final List<ThreadAwareMethodHandle> implementations;

    BoundComputerMethod(String methodName, List<ThreadAwareMethodHandle> implementations) {
        this.methodName = methodName;
        this.implementations = implementations;
    }

    void addMethodImplementation(ThreadAwareMethodHandle implementation) {
        implementations.add(implementation);
    }

    public List<ThreadAwareMethodHandle> getImplementations() {
        return Collections.unmodifiableList(implementations);
    }

    /**
     * @return Index of matching method, an exception is thrown if no method matched
     */
    public <EXCEPTION extends Exception> SelectedMethodInfo findMatchingImplementation(ComputerArgumentHandler<EXCEPTION, ?> argumentHandler) throws EXCEPTION {
        if (implementations.size() == 1) {
            //If we only have one method implementation then validate it with more accurate error messages
            return validateArguments(argumentHandler, implementations.get(0), true);
        }
        //Otherwise, try each method (without throwing errors)
        for (ThreadAwareMethodHandle implementation : implementations) {
            SelectedMethodInfo selected = validateArguments(argumentHandler, implementation, false);
            if (selected != null) {
                //if one matches return it as a match
                return selected;
            }
        }
        //Otherwise, if none match print a generic error message
        throw argumentHandler.error("Parameters do not match any signatures of %s.", methodName);
    }

    @Nullable
    public <EXCEPTION extends Exception> SelectedMethodInfo findMatchingImplementation(ComputerArgumentHandler<EXCEPTION, ?> argumentHandler,
          ThreadAwareMethodHandle overload) throws EXCEPTION {
        int overloadIndex = implementations.indexOf(overload);
        if (overloadIndex == -1) {
            throw argumentHandler.error("Method does not have corresponding overload");
        }
        //Validate it with more accurate error messages
        return validateArguments(argumentHandler, overload, true);
    }

    @Nullable
    private <EXCEPTION extends Exception> SelectedMethodInfo validateArguments(ComputerArgumentHandler<EXCEPTION, ?> argumentHandler, ThreadAwareMethodHandle overload,
          boolean error) throws EXCEPTION {
        int argumentCount = argumentHandler.getCount();
        MethodType methodType = overload.methodHandle.type();
        int expectedCount = methodType.parameterCount();
        if (argumentCount != expectedCount) {
            if (error) {
                throw argumentHandler.error("Mismatched parameter count. %s expected: '%d' arguments, but received: '%d' arguments.", methodName,
                      expectedCount, argumentCount);
            }
            return null;
        }
        //Note: We implement our own type checking here rather than relying on exceptions to occur when trying to invoke
        // the method handle as it is a lot quicker to quickly do some minor type checking, than have the java native stuff
        // run into it and handle it via exceptions. This also allows us to add in some extra sanitation/implicit conversions
        // where necessary as some computer implementations (for example ComputerCraft) only has a concept of doubles for what
        // numbers it passes to us, so we need to wrap them into the correct/compatible type
        Object[] sanitizedArguments = new Object[expectedCount];
        for (int index = 0; index < expectedCount; index++) {
            Class<?> expectedType = methodType.parameterType(index);
            Object argument = argumentHandler.getArgument(index);
            if (argument == null) {
                //We don't allow null arguments for our computer methods
                if (error) {
                    throw argumentHandler.error("Invalid argument %d, %s expected %s but received null.", index, methodName, expectedType.getSimpleName());
                }
                return null;
            }
            Class<?> argumentClass = argument.getClass();
            boolean matches = true;
            if (expectedType != argumentClass) {
                //Types are different, validate if they match and sanitize if needed
                if (expectedType.isPrimitive()) {
                    //See if we can cast the argument to the correct primitive
                    if (argumentClass.isPrimitive()) {
                        if (isInvalidUpcast(argumentClass, expectedType)) {
                            //Validate if we are allowed to upcast or not from one type to another
                            // mark that we don't have a match and should attempt to sanitize it
                            matches = false;
                        }
                    } else {
                        Class<?> primitiveArgumentClass = getPrimitiveType(argumentClass);
                        if (expectedType != primitiveArgumentClass && isInvalidUpcast(primitiveArgumentClass, expectedType)) {
                            //Test if we are able to auto unbox, and if needed after unboxing, upcast
                            // mark that we don't have a match and should attempt to sanitize it
                            matches = false;
                        }
                    }
                } else if (!argumentClass.isPrimitive() || getPrimitiveType(expectedType) != argumentClass) {
                    //if our argument is not a primitive, or the type doesn't match after autoboxing
                    // We "cheat" to check this by just using our existing method to unbox the expected
                    // type to see if it directly matches our primitive type
                    // mark that we don't have a match and should attempt to sanitize it
                    matches = false;
                }
                if (!matches) {
                    //If our arguments don't match, allow the argument handler to attempt to sanitize the argument into the correct type
                    Object sanitizedArgument = argumentHandler.sanitizeArgument(expectedType, argumentClass, argument);
                    if (sanitizedArgument == argument) {
                        //If we could not sanitize it, error
                        if (error) {
                            throw argumentHandler.error("Invalid argument %d, %s expected %s but received type %s with value %s.", index, methodName,
                                  expectedType.getSimpleName(), argumentClass.getSimpleName(), argument);
                        }
                        return null;
                    }
                    //Otherwise, set the argument as the proper sanitized value
                    sanitizedArguments[index] = sanitizedArgument;
                }
            }
            if (matches) {
                //Types are compatible, no sanitation needed
                sanitizedArguments[index] = argument;
            }
        }
        return new SelectedMethodInfo(overload, sanitizedArguments);
    }

    public <EXCEPTION extends Exception, RESULT> RESULT run(ComputerArgumentHandler<EXCEPTION, RESULT> argumentHandler, SelectedMethodInfo methodInfo) throws EXCEPTION {
        MethodHandle methodHandle = methodInfo.threadAwareMethodHandle.methodHandle;
        MethodType methodType = methodHandle.type();
        int argumentCount = methodType.parameterCount();
        Object result;
        try {
            //Note: We manually call invoke for a good number of arguments until we fall back to invokeWithArguments, as there is a pretty
            // sizable performance difference in the two methods. We also call invoke instead of invokeExact for the zero argument count
            // as it requires knowing the return type by casting which causes us issues as we don't know it at compile time and thus cannot
            // specify the cast to the correct type directly
            result = switch (argumentCount) {
                case 0 -> methodHandle.invoke();
                case 1 -> methodHandle.invoke(methodInfo.arguments[0]);
                case 2 -> methodHandle.invoke(methodInfo.arguments[0], methodInfo.arguments[1]);
                case 3 -> methodHandle.invoke(methodInfo.arguments[0], methodInfo.arguments[1], methodInfo.arguments[2]);
                case 4 -> methodHandle.invoke(methodInfo.arguments[0], methodInfo.arguments[1], methodInfo.arguments[2], methodInfo.arguments[3]);
                case 5 -> methodHandle.invoke(methodInfo.arguments[0], methodInfo.arguments[1], methodInfo.arguments[2], methodInfo.arguments[3],
                      methodInfo.arguments[4]);
                case 6 -> methodHandle.invoke(methodInfo.arguments[0], methodInfo.arguments[1], methodInfo.arguments[2], methodInfo.arguments[3], methodInfo.arguments[4],
                      methodInfo.arguments[5]);
                case 7 -> methodHandle.invoke(methodInfo.arguments[0], methodInfo.arguments[1], methodInfo.arguments[2], methodInfo.arguments[3], methodInfo.arguments[4],
                      methodInfo.arguments[5], methodInfo.arguments[6]);
                case 8 -> methodHandle.invoke(methodInfo.arguments[0], methodInfo.arguments[1], methodInfo.arguments[2], methodInfo.arguments[3], methodInfo.arguments[4],
                      methodInfo.arguments[5], methodInfo.arguments[6], methodInfo.arguments[7]);
                case 9 -> methodHandle.invoke(methodInfo.arguments[0], methodInfo.arguments[1], methodInfo.arguments[2], methodInfo.arguments[3], methodInfo.arguments[4],
                      methodInfo.arguments[5], methodInfo.arguments[6], methodInfo.arguments[7], methodInfo.arguments[8]);
                //Note: If we ever really get to the point this needs to be used for the number of parameters, we should heavily consider
                // adding in more argumentCount based special cases to improve the overall performance in the calls to the method
                default -> methodHandle.invokeWithArguments(methodInfo.arguments);
            };
        } catch (Throwable e) {
            //Possible errors for invoke/invokeWithArguments:
            // - WrongMethodTypeException if the target's type is not identical with the caller's symbolic type descriptor
            // - ClassCastException if the target's type can be adjusted to the caller, but a reference cast fails
            // - Throwable anything thrown by the underlying method propagates unchanged through the method handle call
            // In theory none of these should actually happen given we do parameter validation, but just in case one does,
            // we wrap the message into an error message that can be displayed by our handler
            // Note: If the throwable is a computer exception we won't have a stacktrace. We don't use it anywhere here,
            // so it shouldn't matter, but it is something to keep in mind if we ever change this catch block, as then
            // we should extract the computer exception case to an earlier catch block
            throw argumentHandler.error(e.getMessage());
        }
        Class<?> returnType = methodType.returnType();
        //Check both potential void types for methods to see if we have a return type
        if (returnType == Void.class || returnType == Void.TYPE) {
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

    public static class SelectedMethodInfo {

        private final ThreadAwareMethodHandle threadAwareMethodHandle;
        private final Object[] arguments;

        private SelectedMethodInfo(ThreadAwareMethodHandle threadAwareMethodHandle, Object[] arguments) {
            this.threadAwareMethodHandle = threadAwareMethodHandle;
            this.arguments = arguments;
        }

        public ThreadAwareMethodHandle getMethod() {
            return threadAwareMethodHandle;
        }
    }

    public record ThreadAwareMethodHandle(MethodHandle methodHandle, List<String> paramNames, boolean threadSafe) {

        public Class<?> returnType() {
            return methodHandle.type().returnType();
        }

        public List<Class<?>> parameterTypes() {
            return methodHandle.type().parameterList();
        }
    }
}