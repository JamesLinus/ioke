/*
 * See LICENSE file in distribution for copyright and licensing information.
 */
package ioke.lang;

import java.util.ArrayList;
import java.util.List;

import ioke.lang.exceptions.ControlFlow;

/**
 *
 * @author <a href="mailto:ola.bini@gmail.com">Ola Bini</a>
 */
public class Restart {
    public static void init(IokeObject restart) throws ControlFlow {
        Runtime runtime = restart.runtime;
        restart.setKind("Restart");

        restart.registerCell("name", runtime.nil);
        restart.registerCell("report", runtime.evaluateString("fn(r, \"restart: \" + r name)"));
        restart.registerCell("test", runtime.evaluateString("fn(c, true)"));
        restart.registerCell("code", runtime.evaluateString("fn()"));
    }


    public abstract static class JavaRestart {
        protected String name;
        public String getName() {
            return this.name;
        }

        public abstract IokeObject invoke(IokeObject context, List<Object> arguments) throws ControlFlow;
    }

    public static class ArgumentGivingRestart extends JavaRestart {
        public ArgumentGivingRestart(String name) {
            this.name = name;
        }

        public IokeObject invoke(IokeObject context, List<Object> arguments) throws ControlFlow {
            return context.runtime.newList(arguments);
        }
    }

    public static class DefaultValuesGivingRestart extends JavaRestart {
        private IokeObject value;
        private int repeat;
        public DefaultValuesGivingRestart(String name, IokeObject value, int repeat) {
            this.name = name;
            this.value = value;
            this.repeat = repeat;
        }

        public IokeObject invoke(IokeObject context, List<Object> arguments) throws ControlFlow {
            List<Object> result = new ArrayList<Object>();
            for(int i=0; i<repeat; i++) {
                result.add(value);
            }
            return context.runtime.newList(result);
        }
    }
}// Restart
