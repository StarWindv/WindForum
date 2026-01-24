package top.starwindv.WindForum.logger;


import top.starwindv.WindForum.logger.Errors.HexColorFormatError;

import java.util.ArrayList;
import java.util.List;


@SuppressWarnings("unused")
public class Colors {
    // Frontend and Background Styles in Terminal
    private final static String base = "\033[%sm";
    private final static String rgbTemplate = "\033[%s;%s;%s;%sm";

    // Commonly Used Colors
    public static String Red    = String.format(base, "31");
    public static String Green  = String.format(base, "32");
    public static String Yellow = String.format(base, "38;5;220");
    public static String Blue   = String.format(base, "34");
    public static String Purple = String.format(base, "35");
    public static String Cyan   = String.format(base, "36");
    public static String Reset  = String.format(base, "0" );
    
    public static String Bold = String.format(base, "1" );
    public static String BYellow = Yellow+Bold;
    public static String BCyan   = Cyan+Bold;

    public static String frontFrom(int r, int g, int b) {
        return String.format(rgbTemplate, "38;2", r, g, b);
    }

    public static List<Integer> hexProcessor(String hexColor) {
        hexColor = hexColor.replace("#", "");
        List<Integer> result = new ArrayList<>();
        return switch (hexColor.length()) {
            case 3 -> {
                for (int idx=0;idx<hexColor.length();idx++) {
                    String c = String.valueOf(hexColor.charAt(idx));
                    result.add(Integer.parseInt(c+c, 16));
                }
                yield result;
            }
            case 6 -> {
                result.add(Integer.parseInt(hexColor.substring(0, 2), 16));
                result.add(Integer.parseInt(hexColor.substring(2, 4), 16));
                result.add(Integer.parseInt(hexColor.substring(4, 6), 16));
                yield result;
            }
            default -> throw new HexColorFormatError(hexColor);
        };
    }

    public static String frontFrom(String hexColor) {
        List<Integer> rgb = hexProcessor(hexColor);
        return frontFrom(rgb.getFirst(), rgb.get(1), rgb.getLast());
    }

    public static String backgroundFrom(int r, int g, int b) {
        return String.format(rgbTemplate, "48;2", r, g, b);
    }

    public static String backgroundFrom(String hexColor) {
        List<Integer> rgb = hexProcessor(hexColor);
        return backgroundFrom(rgb.getFirst(), rgb.get(1), rgb.getLast());
    }
}
