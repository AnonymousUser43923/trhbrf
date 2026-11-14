/*
 * Decompiled with CFR 0.152.
 */
package name.modid.client;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.Locale;

public final class AutoRespawn {
    private AutoRespawn() {
    }

    public static void tryRespawn(Object object) {
        try {
            Object object2;
            Object object3;
            Object object4 = AutoRespawn.fieldValue(object, "gui");
            if (object4 == null) {
                return;
            }
            Object object5 = AutoRespawn.callNoArgs(object4, "screen");
            if (object5 == null || !object5.getClass().getName().endsWith("DeathScreen")) {
                return;
            }
            for (object3 = object5.getClass(); object3 != null; object3 = ((Class)object3).getSuperclass()) {
                for (Field object6 : ((Class)object3).getDeclaredFields()) {
                    if (!object6.getName().toLowerCase(Locale.ROOT).contains("respawn")) continue;
                    object6.setAccessible(true);
                    object2 = object6.get(object5);
                    if (object2 == null || !AutoRespawn.callPress(object2)) continue;
                    return;
                }
            }
            object3 = AutoRespawn.callNoArgs(object5, "children");
            if (object3 instanceof Iterable) {
                Iterable iterable = (Iterable)object3;
                Iterator iterator = iterable.iterator();
                while (iterator.hasNext()) {
                    Object t = iterator.next();
                    try {
                        Object object6 = AutoRespawn.callNoArgs(t, "getMessage");
                        object2 = object6 == null ? null : AutoRespawn.callNoArgs(object6, "getString");
                        if (object2 == null || !object2.toString().toLowerCase(Locale.ROOT).contains("respawn") || !AutoRespawn.callPress(t)) continue;
                        return;
                    }
                    catch (Throwable throwable) {
                    }
                }
            }
        }
        catch (Throwable throwable) {
            // empty catch block
        }
    }

    private static Object fieldValue(Object object, String string) throws Exception {
        for (Class<?> clazz = object.getClass(); clazz != null; clazz = clazz.getSuperclass()) {
            try {
                Field field = clazz.getDeclaredField(string);
                field.setAccessible(true);
                return field.get(object);
            }
            catch (NoSuchFieldException noSuchFieldException) {
                continue;
            }
        }
        return null;
    }

    private static Object callNoArgs(Object object, String string) throws Exception {
        for (Class<?> clazz = object.getClass(); clazz != null; clazz = clazz.getSuperclass()) {
            try {
                Method method = clazz.getDeclaredMethod(string, new Class[0]);
                method.setAccessible(true);
                return method.invoke(object, new Object[0]);
            }
            catch (NoSuchMethodException noSuchMethodException) {
                continue;
            }
        }
        return null;
    }

    private static boolean callPress(Object object) {
        try {
            AutoRespawn.callNoArgs(object, "onPress");
            return true;
        }
        catch (Throwable throwable) {
            return false;
        }
    }
}

