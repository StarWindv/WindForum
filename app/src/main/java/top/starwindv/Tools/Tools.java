package top.starwindv.Tools;


public class Tools {
    public static boolean hasattr(Object obj, String fieldName) {
        for (Class<?> c = obj.getClass(); c != Object.class; c = c.getSuperclass())
            try { c.getDeclaredField(fieldName); return true; }
            catch (NoSuchFieldException e) {}
        return false;
    }
}
