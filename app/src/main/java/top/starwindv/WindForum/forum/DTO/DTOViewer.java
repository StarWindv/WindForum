package top.starwindv.WindForum.forum.DTO;


import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;


@SuppressWarnings("unused")
public abstract class DTOViewer {
    abstract void initialize();
    protected Boolean init = false;
    protected final HashMap<String, Method> getterCache = new HashMap<>();
    protected Class<?> targetCls;
    protected static String[] property;

    public Object getattr(
        String name, Object obj
    ) {
        if (!init) {
            initialize();
        }
        try {
            if (this.getterCache.containsKey(name)) {
                return this.getterCache.get(name).invoke(obj);
            }
            else {
                Method method = targetCls.getMethod(name);
                this.getterCache.put(name, method);
                Object result = method.invoke(obj);
                if (result==null) { result=""; }
                return result;
            }
        } catch (
            NoSuchMethodException |
            IllegalAccessException |
            InvocationTargetException ignored
        ) { return ""; }
    }

    public String toString(UserDTO obj) {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        for (int idx=0;idx<property.length;idx++) {
            String ele = property[idx];
            sb.append("\"")
                .append(ele)
                .append("\": \"")
                .append(getattr(ele, obj))
                .append("\"");
            if (idx!=property.length-1) {
                sb.append(",");
            }
        }
        sb.append("}");
        return sb.toString();
    }
}
