package top.starwindv.DTO;


import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;

import org.apache.commons.lang3.StringUtils;


public class UserDTOViewer implements DTOViewer{
    private Class<?> targetCls;
    private Boolean init = false;

    private static final String[] property = {
        "username", "email", "codeHash"
    };

    private final HashMap<String, Method> getterCache = new HashMap<>();

    @Override
    public void initialize() {
        this.targetCls = UserDTO.class;
        this.init = true;
    }

    @Override
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

    public String Stringify(UserDTO obj) {
        StringBuilder sb = new StringBuilder();
        sb.append("UserInfo:");
        for (var ele: property) {
            if (ele.equals("codeHash")) {
                sb.append("\n - ")
                    .append(StringUtils.capitalize(ele))
                    .append(": ")
                    .append("<Hidden Length: ")
                    .append(getattr(ele, obj).toString().length())
                    .append(">");
            }
            else {
                sb.append("\n - ")
                    .append(StringUtils.capitalize(ele))
                    .append(": ")
                    .append(getattr(ele, obj));
            }
        }
        return sb.toString();
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
