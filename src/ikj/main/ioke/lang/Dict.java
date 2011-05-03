/*
 * See LICENSE file in distribution for copyright and licensing information.
 */
package ioke.lang;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;

import ioke.lang.exceptions.ControlFlow;

/**
 *
 * @author <a href="mailto:ola.bini@gmail.com">Ola Bini</a>
 */
public class Dict extends IokeData {
    private Map<Object, Object> dict;
    private IokeObject defaultValue;

    public Dict() {
        this(new HashMap<Object, Object>());
    }

    public Dict(Map<Object, Object> d) {
        this.dict = d;
    }

    public static IokeObject getDefaultValue(Object on, IokeObject context, IokeObject message) throws ControlFlow {
        Dict dict = (Dict)IokeObject.data(on);
        if(dict.defaultValue == null) {
            return context.runtime.nil;
        } else {
            return dict.defaultValue;
        }
    }

    public static void setDefaultValue(Object on, IokeObject defaultValue) throws ControlFlow {
        Dict dict = (Dict)IokeObject.data(on);
        dict.defaultValue = defaultValue;
    }

    @Override
    public void init(IokeObject obj) throws ControlFlow {
        final Runtime runtime = obj.runtime;

        obj.setKind("Dict");
        obj.mimics(IokeObject.as(runtime.mixins.getCell(null, null, "Sequenced"), null), runtime.nul, runtime.nul);

        obj.registerMethod(runtime.newNativeMethod("returns a hash for the dictionary", new NativeMethod.WithNoArguments("hash") {
                @Override
                public Object activate(IokeObject method, IokeObject context, IokeObject message, Object on) throws ControlFlow {
                    getArguments().getEvaluatedArguments(context, message, on, new ArrayList<Object>(), new HashMap<String, Object>());
                    return context.runtime.newNumber(((Dict)IokeObject.data(on)).dict.hashCode());
                }
            }));

        obj.registerMethod(runtime.newNativeMethod("returns true if the left hand side dictionary is equal to the right hand side dictionary.", new TypeCheckingNativeMethod("==") {
                private final TypeCheckingArgumentsDefinition ARGUMENTS = TypeCheckingArgumentsDefinition
                    .builder()
                    .receiverMustMimic(runtime.dict)
                    .withRequiredPositional("other")
                    .getArguments();

                @Override
                public TypeCheckingArgumentsDefinition getArguments() {
                    return ARGUMENTS;
                }

                @Override
                public Object activate(IokeObject self, Object on, List<Object> args, Map<String, Object> keywords, IokeObject context, IokeObject message) throws ControlFlow {
                    getArguments().getEvaluatedArguments(context, message, on, args, new HashMap<String, Object>());
                    Object other = args.get(0);
                    return ((other instanceof IokeObject) &&
                            (IokeObject.data(other) instanceof Dict) &&
                            ((Dict)IokeObject.data(on)).dict.equals(((Dict)IokeObject.data(other)).dict)) ? context.runtime._true : context.runtime._false;
                }
            }));


        obj.registerMethod(runtime.newNativeMethod("takes one argument, that should be a default value, and returns a new mimic of the receiver, with the default value for that new dict set to the argument", new TypeCheckingNativeMethod("withDefault") {
                private final TypeCheckingArgumentsDefinition ARGUMENTS = TypeCheckingArgumentsDefinition
                    .builder()
                    .receiverMustMimic(runtime.dict)
                    .withRequiredPositional("defaultValue")
                    .getArguments();

                @Override
                public TypeCheckingArgumentsDefinition getArguments() {
                    return ARGUMENTS;
                }

                @Override
                public Object activate(IokeObject method, Object on, List<Object> args, Map<String, Object> keywords, IokeObject context, IokeObject message) throws ControlFlow {
                    Object newDict = IokeObject.mimic(on, message, context);
                    setDefaultValue(newDict, IokeObject.as(args.get(0), context));
                    return newDict;
                }}));

        obj.registerMethod(runtime.newNativeMethod("creates a new Dict from the arguments provided, combined with the values in the receiver. the arguments provided will override those in the receiver. the rules for arguments are the same as for dict, except that dicts can also be provided. all positional arguments will be added before the keyword arguments.", new TypeCheckingNativeMethod("merge") {
                private final TypeCheckingArgumentsDefinition ARGUMENTS = TypeCheckingArgumentsDefinition
                    .builder()
                    .receiverMustMimic(runtime.dict)
                    .withRest("pairsAndDicts")
                    .withKeywordRest("keywordPairs")
                    .getArguments();

                @Override
                public TypeCheckingArgumentsDefinition getArguments() {
                    return ARGUMENTS;
                }

                @Override
                public Object activate(IokeObject method, Object on, List<Object> args, Map<String, Object> keywords, IokeObject context, IokeObject message) throws ControlFlow {
                    Map<Object, Object> newMap = new HashMap<Object, Object>();
                    newMap.putAll(getMap(on));

                    for(Object o : args) {
                        if(IokeObject.data(o) instanceof Dict) {
                            newMap.putAll(getMap(o));
                        } else if(IokeObject.data(o) instanceof Pair) {
                            newMap.put(Pair.getFirst(o), Pair.getSecond(o));
                        } else {
                            newMap.put(o, context.runtime.nil);
                        }
                    }
                    for(Map.Entry<String, Object> entry : keywords.entrySet()) {
                        String s = entry.getKey();
                        Object key = context.runtime.getSymbol(s.substring(0, s.length()-1));
                        Object value = entry.getValue();
                        if(value == null) {
                            value = context.runtime.nil;
                        }
                        newMap.put(key, value);
                    }

                    return context.runtime.newDict(newMap);
                }
            }));

        obj.aliasMethod("merge", "+", null, null);

        obj.registerMethod(runtime.newNativeMethod("takes one argument, the key of the element to return. if the key doesn't map to anything in the dict, returns the default value", new TypeCheckingNativeMethod("at") {
                private final TypeCheckingArgumentsDefinition ARGUMENTS = TypeCheckingArgumentsDefinition
                    .builder()
                    .receiverMustMimic(runtime.dict)
                    .withRequiredPositional("key")
                    .getArguments();

                @Override
                public TypeCheckingArgumentsDefinition getArguments() {
                    return ARGUMENTS;
                }

                @Override
                public Object activate(IokeObject method, Object on, List<Object> args, Map<String, Object> keywords, IokeObject context, IokeObject message) throws ControlFlow {
                    Object result = Dict.getMap(on).get(args.get(0));
                    if(result == null) {
                        return getDefaultValue(on, context, message);
                    } else {
                        return result;
                    }
                }}));

        obj.registerMethod(runtime.newNativeMethod("returns true if this dict is empty, false otherwise", new TypeCheckingNativeMethod.WithNoArguments("empty?", runtime.dict) {
                @Override
                public Object activate(IokeObject method, Object on, List<Object> args, Map<String, Object> keywords, IokeObject context, IokeObject message) throws ControlFlow {
                    return Dict.getMap(on).isEmpty() ? context.runtime._true : context.runtime._false;
                }
            }));

        obj.registerMethod(runtime.newNativeMethod("takes one argument, the key to check if it is in the dict.", new TypeCheckingNativeMethod("key?") {
                private final TypeCheckingArgumentsDefinition ARGUMENTS = TypeCheckingArgumentsDefinition
                    .builder()
                    .receiverMustMimic(runtime.dict)
                    .withRequiredPositional("key")
                    .getArguments();

                @Override
                public TypeCheckingArgumentsDefinition getArguments() {
                    return ARGUMENTS;
                }

                @Override
                public Object activate(IokeObject method, Object on, List<Object> args, Map<String, Object> keywords, IokeObject context, IokeObject message) throws ControlFlow {
                    return (Dict.getMap(on).containsKey(args.get(0))) ? context.runtime._true : context.runtime._false;
                }}));

        obj.registerMethod(runtime.newNativeMethod("takes two arguments, the key of the element to set and the value to set it too. returns the value set", new TypeCheckingNativeMethod("[]=") {
                private final TypeCheckingArgumentsDefinition ARGUMENTS = TypeCheckingArgumentsDefinition
                    .builder()
                    .receiverMustMimic(runtime.dict)
                    .withRequiredPositional("key")
                    .withRequiredPositional("value")
                    .getArguments();

                @Override
                public TypeCheckingArgumentsDefinition getArguments() {
                    return ARGUMENTS;
                }

                @Override
                public Object activate(IokeObject method, Object on, List<Object> args, Map<String, Object> keywords, IokeObject context, IokeObject message) throws ControlFlow {
                    Dict.getMap(on).put(args.get(0), args.get(1));
                    return args.get(1);
                }}));

        obj.registerMethod(runtime.newNativeMethod("Returns the number of pairs contained in this dict.", new TypeCheckingNativeMethod.WithNoArguments("size", runtime.dict) {
                @Override
                public Object activate(IokeObject method, Object on, List<Object> args, Map<String, Object> keywords, IokeObject context, IokeObject message) throws ControlFlow {
                    return runtime.newNumber(Dict.getMap(on).size());
                }
            }));


        obj.registerMethod(runtime.newNativeMethod("Returns a text inspection of the object", new TypeCheckingNativeMethod.WithNoArguments("inspect", runtime.dict) {
                @Override
                public Object activate(IokeObject method, Object on, List<Object> args, Map<String, Object> keywords, IokeObject context, IokeObject message) throws ControlFlow {
                    return method.runtime.newText(Dict.getInspect(on));
                }
            }));

        obj.registerMethod(runtime.newNativeMethod("Returns a brief text inspection of the object", new TypeCheckingNativeMethod.WithNoArguments("notice", runtime.dict) {
                @Override
                public Object activate(IokeObject method, Object on, List<Object> args, Map<String, Object> keywords, IokeObject context, IokeObject message) throws ControlFlow {
                    return method.runtime.newText(Dict.getNotice(on));
                }
            }));

        obj.registerMethod(runtime.newNativeMethod("Returns all the keys of this dict", new TypeCheckingNativeMethod.WithNoArguments("keys", runtime.dict) {
                @Override
                public Object activate(IokeObject method, Object on, List<Object> args, Map<String, Object> keywords, IokeObject context, IokeObject message) throws ControlFlow {
                    return method.runtime.newSet(Dict.getKeys(on));
                }
            }));


        obj.registerMethod(obj.runtime.newNativeMethod("returns a new sequence to iterate over this dictionary", new TypeCheckingNativeMethod.WithNoArguments("seq", runtime.dict) {
                @Override
                public Object activate(IokeObject method, Object on, List<Object> args, Map<String, Object> keywords, IokeObject context, IokeObject message) throws ControlFlow {
                    IokeObject obj = method.runtime.keyValueIteratorSequence.allocateCopy(null, null);
                    obj.mimicsWithoutCheck(method.runtime.keyValueIteratorSequence);
                    obj.setData(new Sequence.KeyValueIteratorSequence(Dict.getMap(on).entrySet().iterator()));
                    return obj;
                }
            }));


        obj.registerMethod(runtime.newNativeMethod("takes either one or two or three arguments. if one argument is given, it should be a message chain that will be sent to each object in the dict. the result will be thrown away. if two arguments are given, the first is an unevaluated name that will be set to each of the entries in the dict in succession, and then the second argument will be evaluated in a scope with that argument in it. if three arguments is given, the first one is an unevaluated name that will be set to the index of each element, and the other two arguments are the name of the argument for the value, and the actual code. the code will evaluate in a lexical context, and if the argument name is available outside the context, it will be shadowed. the method will return the dict. the entries yielded will be mimics of Pair.", new NativeMethod("each") {
                private final DefaultArgumentsDefinition ARGUMENTS = DefaultArgumentsDefinition
                    .builder()
                    .withOptionalPositionalUnevaluated("indexOrArgOrCode")
                    .withOptionalPositionalUnevaluated("argOrCode")
                    .withOptionalPositionalUnevaluated("code")
                    .getArguments();

                @Override
                public DefaultArgumentsDefinition getArguments() {
                    return ARGUMENTS;
                }

                @Override
                public Object activate(IokeObject method, IokeObject context, IokeObject message, Object on) throws ControlFlow {
                    getArguments().checkArgumentCount(context, message, on);

                    on = runtime.dict.convertToThis(on, message, context);

                    Runtime runtime = context.runtime;
                    Map<Object, Object> ls = Dict.getMap(on);
                    switch(message.getArgumentCount()) {
                    case 0: {
                        return Interpreter.send(runtime.seqMessage, context, on);
                    }
                    case 1: {
                        IokeObject code = IokeObject.as(message.getArguments().get(0), context);

                        for(Map.Entry<Object, Object> o : ls.entrySet()) {
                            runtime.interpreter.evaluate(code, context, context.getRealContext(), runtime.newPair(o.getKey(), o.getValue()));
                        }
                        break;
                    }
                    case 2: {
                        IokeObject c = context.runtime.newLexicalContext(context, "Lexical activation context for Set#each", context);
                        String name = IokeObject.as(message.getArguments().get(0), context).getName();
                        IokeObject code = IokeObject.as(message.getArguments().get(1), context);

                        for(Map.Entry<Object, Object> o : ls.entrySet()) {
                            c.setCell(name, runtime.newPair(o.getKey(), o.getValue()));
                            runtime.interpreter.evaluate(code, c, c.getRealContext(), c);
                        }
                        break;
                    }
                    case 3: {
                        IokeObject c = context.runtime.newLexicalContext(context, "Lexical activation context for Set#each", context);
                        String iname = IokeObject.as(message.getArguments().get(0), context).getName();
                        String name = IokeObject.as(message.getArguments().get(1), context).getName();
                        IokeObject code = IokeObject.as(message.getArguments().get(2), context);

                        int index = 0;
                        for(Map.Entry<Object, Object> o : ls.entrySet()) {
                            c.setCell(name, runtime.newPair(o.getKey(), o.getValue()));
                            c.setCell(iname, runtime.newNumber(index++));
                            runtime.interpreter.evaluate(code, c, c.getRealContext(), c);
                        }
                        break;
                    }
                    }
                    return on;
                }
            }));
    }

    public static Map<Object, Object> getMap(Object dict) {
        return ((Dict)IokeObject.data(dict)).getMap();
    }

    public static Set<Object> getKeys(Object dict) {
        return ((Dict)IokeObject.data(dict)).getMap().keySet();
    }

    public Map<Object, Object> getMap() {
        return dict;
    }

    public IokeData cloneData(IokeObject obj, IokeObject m, IokeObject context) {
        return new Dict(new HashMap<Object, Object>(dict));
    }

    @Override
    public String toString() {
        return dict.toString();
    }

    @Override
    public String toString(IokeObject obj) {
        return dict.toString();
    }

    public static String getInspect(Object on) throws ControlFlow {
        return ((Dict)(IokeObject.data(on))).inspect(on);
    }

    public static String getNotice(Object on) throws ControlFlow {
        return ((Dict)(IokeObject.data(on))).notice(on);
    }

    public String inspect(Object obj) throws ControlFlow {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        String sep = "";

        for(Map.Entry<Object, Object> o : dict.entrySet()) {
            sb.append(sep);
            Object key = o.getKey();

            if((IokeObject.data(key) instanceof Symbol) && Symbol.onlyGoodChars(key)) {
                sb.append(Symbol.getText(key)).append(": ");
            } else {
                sb.append(IokeObject.inspect(key)).append(" => ");
            }

            sb.append(IokeObject.inspect(o.getValue()));
            sep = ", ";
        }

        sb.append("}");
        return sb.toString();
    }

    public String notice(Object obj) throws ControlFlow {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        String sep = "";

        for(Map.Entry<Object, Object> o : dict.entrySet()) {
            sb.append(sep);
            Object key = o.getKey();

            if((IokeObject.data(key) instanceof Symbol) && Symbol.onlyGoodChars(key)) {
                sb.append(Symbol.getText(key)).append(": ");
            } else {
                sb.append(IokeObject.notice(key)).append(" => ");
            }

            sb.append(IokeObject.notice(o.getValue()));
            sep = ", ";
        }

        sb.append("}");
        return sb.toString();
    }
}// Dict
