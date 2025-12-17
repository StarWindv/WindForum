package top.starwindv;


import java.util.HashMap;


public enum Permission {
    /**
     * Struct:
     *  Name (Value, Desription)
     */

    IsDeleted(0, "IsDeleted"),
    SuperAdmin(1, "SuperAdmin"),
    Admin (2, "Admin"),
    Normal(3, "Normal");    

    private static final HashMap<Integer, Permission> CMap = new HashMap<>();
    private static final HashMap<String, Permission> DMap = new HashMap<>();

    public final int code;
    public final String description;

    
    Permission(int code, String description){
        this.code = code;
        this.description = description;
    }

    static {
        for (Permission auto : values()) {
            CMap.put(auto.code, auto);
            DMap.put(auto.description, auto);
        }
    }

    public static Permission fromCode(int code) {
        return CMap.get(code);
    }

    public static Permission fromDesc(String description) {
        return DMap.get(description);
    }

    public static String CodeToDesc(int code) {
        return fromCode(code).description;
    }

    public static int DescToCode(String description) {
        return fromDesc(description).code;
    }
}
