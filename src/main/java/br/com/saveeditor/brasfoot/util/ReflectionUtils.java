package br.com.saveeditor.brasfoot.util;

import java.lang.reflect.Field;

/**
 * Classe de utilitários para operações de reflexão (Reflection), como obter e
 * definir valores de campos privados e converter tipos.
 */
public class ReflectionUtils {

    public static Object getFieldValue(Object obj, String fieldName)
            throws NoSuchFieldException, IllegalAccessException {
        Field field = obj.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.get(obj);
    }

    public static void setFieldValue(Object obj, String fieldName, Object value)
            throws NoSuchFieldException, IllegalAccessException {
        Field field = obj.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(obj, value);
    }

    public static boolean isComplexObject(Object obj) {
        if (obj == null || obj.getClass().isPrimitive()) {
            return false;
        }
        Package pkg = obj.getClass().getPackage();
        return pkg == null || !pkg.getName().startsWith("java");
    }
}
