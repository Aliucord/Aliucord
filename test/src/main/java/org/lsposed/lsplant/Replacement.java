package org.lsposed.lsplant;

import java.lang.reflect.InvocationTargetException;

public class Replacement {

    static boolean staticMethodReplacement(Hooker.MethodCallback callback) {
        return true;
    }

    String normalMethodReplacement(Hooker.MethodCallback callback) {
        var a = (String) callback.args[1];
        var b = (int) callback.args[2];
        var c = (long) callback.args[3];
        return a + b + c + "replace";
    }

    void constructorReplacement(Hooker.MethodCallback callback) throws InvocationTargetException, IllegalAccessException {
        var test = (LSPTest) callback.args[0];
        callback.backup.invoke(test);
        test.field = true;
    }

    String manyParametersReplacement(Hooker.MethodCallback callback) {
        var a = (String) callback.args[1];
        var b = (boolean) callback.args[2];
        var c = (byte) callback.args[3];
        var d = (short) callback.args[4];
        var e = (int) callback.args[5];
        var f = (long) callback.args[6];
        var g = (float) callback.args[7];
        var h = (double) callback.args[8];
        var i = (Integer) callback.args[9];
        var j = (Long) callback.args[10];
        return a + b + c + d + e + f + g + h + i + j + "replace";
    }
}
