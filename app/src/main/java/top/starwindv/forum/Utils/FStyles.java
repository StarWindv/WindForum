package top.starwindv.forum.Utils;


@SuppressWarnings("unused")
public class FStyles {
    // Frontend Styles
    private final String template = "\033[%sm";
    public String Red    = String.format(template, "31");
    public String Green  = String.format(template, "32");
    public String Yellow = String.format(template, "38;5;220");
    public String Blue   = String.format(template, "34");
    public String Purple = String.format(template, "35");
    public String Cyan   = String.format(template, "36");
    public String Reset  = String.format(template, "0" );
    
    public String Bold = String.format(template, "1" );
    public String BYellow = Yellow+Bold;
    public String BCyan   = Cyan+Bold;

    public String frontFrom(int r, int g, int b) {
        return String.format(this.template, "38;2;"+r+";"+g+";"+b);
    }

    public String backgroundFrom(int r, int g, int b) {
        return String.format(this.template, "48;2;"+r+";"+g+";"+b);
    }
}
