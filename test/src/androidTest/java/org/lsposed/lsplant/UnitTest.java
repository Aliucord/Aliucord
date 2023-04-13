package org.lsposed.lsplant;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

@RunWith(AndroidJUnit4.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class UnitTest {

    @Test
    public void t00_init() {
        Assert.assertTrue(LSPTest.initHooker());
    }

    @Test
    public void t01_staticMethod() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        var staticMethod = LSPTest.class.getDeclaredMethod("staticMethod");
        var staticMethodReplacement = Replacement.class.getDeclaredMethod("staticMethodReplacement", Hooker.MethodCallback.class);
        Assert.assertFalse(LSPTest.staticMethod());

        Hooker hooker = Hooker.hook(staticMethod, staticMethodReplacement, null);
        Assert.assertNotNull(hooker);
        Assert.assertTrue(LSPTest.staticMethod());
        Assert.assertFalse((boolean) hooker.backup.invoke(null));

        Assert.assertTrue(hooker.unhook());
        Assert.assertFalse(LSPTest.staticMethod());
    }

    @Test
    public void t02_normalMethod() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        var normalMethod = LSPTest.class.getDeclaredMethod("normalMethod", String.class, int.class, long.class);
        var normalMethodReplacement = Replacement.class.getDeclaredMethod("normalMethodReplacement", Hooker.MethodCallback.class);
        var a = "test";
        var b = 114514;
        var c = 1919810L;
        var o = a + b + c;
        var r = a + b + c + "replace";
        LSPTest test = new LSPTest();
        Assert.assertEquals(o, test.normalMethod(a, b, c));

        Hooker hooker = Hooker.hook(normalMethod, normalMethodReplacement, new Replacement());
        Assert.assertNotNull(hooker);
        Assert.assertEquals(r, test.normalMethod(a, b, c));
        Assert.assertEquals(o, hooker.backup.invoke(test, a, b, c));

        Assert.assertTrue(hooker.unhook());
        Assert.assertEquals(o, test.normalMethod(a, b, c));
    }

    @Test
    public void t03_constructor() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, InstantiationException {
        var constructor = LSPTest.class.getDeclaredConstructor();
        var constructorReplacement = Replacement.class.getDeclaredMethod("constructorReplacement", Hooker.MethodCallback.class);
        Assert.assertFalse(new LSPTest().field);
        Assert.assertFalse(constructor.newInstance().field);

        Hooker hooker = Hooker.hook(constructor, constructorReplacement, new Replacement());
        Assert.assertNotNull(hooker);
        Assert.assertTrue(new LSPTest().field);
        Assert.assertTrue(constructor.newInstance().field);

        Assert.assertTrue(hooker.unhook());
        Assert.assertFalse(new LSPTest().field);
        Assert.assertFalse(constructor.newInstance().field);
    }

    @Test
    public void t04_manyParametersMethod() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        var manyParametersMethod = LSPTest.class.getDeclaredMethod("manyParametersMethod", String.class, boolean.class, byte.class, short.class, int.class, long.class, float.class, double.class, Integer.class, Long.class);
        var manyParametersReplacement = Replacement.class.getDeclaredMethod("manyParametersReplacement", Hooker.MethodCallback.class);
        var a = "test";
        var b = true;
        var c = (byte) 114;
        var d = (short) 514;
        var e = 19;
        var f = 19L;
        var g = 810f;
        var h = 12345f;
        var o = a + b + c + d + e + f + g + h + e + f;
        var r = a + b + c + d + e + f + g + h + e + f + "replace";
        LSPTest test = new LSPTest();
        Assert.assertEquals(o, test.manyParametersMethod(a, b, c, d, e, f, g, h, e, f));

        Hooker hooker = Hooker.hook(manyParametersMethod, manyParametersReplacement, new Replacement());
        Assert.assertNotNull(hooker);
        Assert.assertEquals(r, test.manyParametersMethod(a, b, c, d, e, f, g, h, e, f));
        Assert.assertEquals(o, hooker.backup.invoke(test, a, b, c, d, e, f, g, h, e, f));

        Assert.assertTrue(hooker.unhook());
        Assert.assertEquals(o, test.manyParametersMethod(a, b, c, d, e, f, g, h, e, f));
    }

    @Test
    public void t05_uninitializedStaticMethod() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, ClassNotFoundException {
        var uninitializedClass = Class.forName("org.lsposed.lsplant.LSPTest$NeedInitialize", false, LSPTest.class.getClassLoader());
        var staticMethod = uninitializedClass.getDeclaredMethod("staticMethod");
        var callStaticMethod = uninitializedClass.getDeclaredMethod("callStaticMethod");
        var staticMethodReplacement = Replacement.class.getDeclaredMethod("staticMethodReplacement", Hooker.MethodCallback.class);

        Hooker hooker = Hooker.hook(staticMethod, staticMethodReplacement, null);
        Assert.assertNotNull(hooker);
        for (int i = 0; i < 5000; ++i) {
            Assert.assertTrue("Iter " + i, (Boolean) callStaticMethod.invoke(null));
            Assert.assertFalse("Iter " + i, (boolean) hooker.backup.invoke(null));
        }

        Assert.assertTrue(hooker.unhook());
        Assert.assertFalse((Boolean) callStaticMethod.invoke(null));
    }

    @Test
    public void t06_proxyMethod() throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        var proxyInterface = Class.forName("org.lsposed.lsplant.LSPTest$ForProxy");
        var proxy = Proxy.newProxyInstance(proxyInterface.getClassLoader(), new Class[]{proxyInterface}, (proxy1, method, args) -> {
            if (method.getName().equals("abstractMethod")) {
                return (String) args[0] + (boolean) args[1] + (byte) args[2] + (short) args[3] + (int) args[4] + (long) args[5] + (float) args[6] + (double) args[7] + (Integer) args[8] + (Long) args[9];
            }
            return method.invoke(proxy1, args);
        });
        var abstractMethod = proxy.getClass().getDeclaredMethod("abstractMethod", String.class, boolean.class, byte.class, short.class, int.class, long.class, float.class, double.class, Integer.class, Long.class);
        var abstractMethodReplacement = Replacement.class.getDeclaredMethod("manyParametersReplacement", Hooker.MethodCallback.class);
        var a = "test";
        var b = true;
        var c = (byte) 114;
        var d = (short) 514;
        var e = 19;
        var f = 19L;
        var g = 810f;
        var h = 12345f;
        var o = a + b + c + d + e + f + g + h + e + f;
        var r = a + b + c + d + e + f + g + h + e + f + "replace";

        Assert.assertEquals(abstractMethod.invoke(proxy, a, b, c, d, e, f, g, h, e, f), o);
        Hooker hooker = Hooker.hook(abstractMethod, abstractMethodReplacement, new Replacement());
        Assert.assertNotNull(hooker);
        Assert.assertEquals(abstractMethod.invoke(proxy, a, b, c, d, e, f, g, h, e, f), r);
        Assert.assertEquals(hooker.backup.invoke(proxy, a, b, c, d, e, f, g, h, e, f), o);
    }
}
