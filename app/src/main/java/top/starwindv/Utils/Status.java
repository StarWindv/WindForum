import java.util.HashMap;
import java.util.List;


public class Status {
    public static final int Active  = 1;
    public static final int Deleted =-1;
    public static final int Ban     = 0;

    private int current;

    public int current() { return this.current; } /* As concise as possible */

    public Boolean isActivate() { return this.current == Active ; }
    public Boolean isDeleted () { return this.current == Deleted; }
    public Boolean isBan     () { return this.current == Ban    ; }

    public static Boolean isActivate(int code) { return code == Active ; }
    public static Boolean isDeleted (int code) { return code == Deleted; }
    public static Boolean isBan     (int code) { return code == Ban    ; }

    public Values Ban  () { 
        if (this.current == Active) {
            this.current = Ban;
            return Values.from(true, "Successful"); 
        } else if (this.current == Ban ) {
            return Values.from(false, "This User Already Baned");
        }
        return Values.from(false, "Failed, Cannot Ban a Deleted User");
    }

    public Values UnBan  () { 
        if (this.current == Ban) {
            this.current = Active;
            return Values.from(true, "Successful"); 
        } else if (this.current == Active) {
            return Values.from(false, "This User is Active, No Need to UnBan");
        }
        return Values.from(false, "Failed, Cannot UnBan a Deleted User");
    }

    public void Delete    () { this.current = Deleted; }
    public void ReActivate() { this.current = Active ; }
}
