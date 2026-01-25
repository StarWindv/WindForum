package top.starwindv.WindForum.logger.Colorful;


import org.apache.commons.lang3.StringUtils;


import java.util.*;

/**
 * [#FF00FF] <br>
 * [#F0F] <br>
 * [255, 0, 255] <br>
 * [255, , 255] <br>
 * [,,255] <br>
 * 省略代表为0<br>
 * 我不想做判断了, 那就十六进制必须带井号吧
 * <p>
 * <p>
 * <p>
 * <h3>
 * 这个类用来处理带颜色的文本，主要就是从字符串里找出颜色标记然后转成终端能显示的颜色代码。
 *
 * <p>支持的格式有这些:
 * <ul>
 *   <li>[#FF00FF] - 六位十六进制（必须带#号）</li>
 *   <li>[#F0F] - 三位十六进制简写</li>
 *   <li>[255, 0, 255] - RGB数值</li>
 *   <li>[255, , 255] - 中间省略表示0</li>
 *   <li>[,,255] - 只写最后一个，前面都是0</li>
 * </ul>
 *
 * <p><b>注意:</b>
 * <ul>
 *   <li>十六进制必须带井号，不然我分不清是RGB还是十六进制</li>
 *   <li>RGB里省略的数字会自动当作0处理</li>
 *   <li>方括号里可以有空格，反正我会去掉的</li>
 * </ul>
 *
 * <p>例子:
 * <pre>
 * new Rich().parse("Hello [#FF0000]World[/]")
 * → 会输出红色的"World"
 * </pre>
 */
@SuppressWarnings("unused")
public class Rich {
    /**
     * 从字符串里提取括号里的内容，比如从 "[hello]" 里拿到 "hello"。
     *
     * <p>这个方法是通用的，可以用来提取各种括号内容，不只是颜色。</p>
     *
     * @param input 要处理的字符串
     * @param left 左括号字符，比如 '['
     * @param right 右括号字符，比如 ']'
     * @param minLength 括号内容的最小长度（不含括号本身）
     * @param maxLength 括号内容的最大长度
     * @param cleanBlank 要不要去掉括号内容里的所有空白字符（空格、制表符啥的）
     * @return 一个Map，key是带括号的完整标记（如"[red]"），value是括号里的内容（如"red"）
     *
     * <p><b>举个例子：</b>
     * <pre>
     * extractBracketContent("Hello [red]World[blue]", '[', ']', 2, 10, true)
     * → 返回 {"[red]": "red", "[blue]": "blue"}
     * </pre>
     */
    public static Map<String, String> extractBracketContent(
        String input,
        char left,
        char right,
        int minLength,
        int maxLength,
        boolean cleanBlank
    ) {
        Map<String, String> result = new HashMap<>();
        Deque<Integer> leftBracketIndices = new ArrayDeque<>();
        if (input == null || input.isBlank()) {
            return result;
        }
        for (int i = 0; i < input.length(); i++) {
            char currentChar = input.charAt(i);
            if (i > 0 && input.charAt(i-1) == '\\') {
                continue;
            }
            if (currentChar == left) {
                leftBracketIndices.push(i);
            } else if (currentChar == right && !leftBracketIndices.isEmpty()) {
                int leftIndex = leftBracketIndices.pop();
                String key = input.substring(leftIndex + 1, i);
                String value = key;
                if (cleanBlank) {
                    value = key.replaceAll("\\s+", "");
                }
                if (!key.isEmpty() && key.indexOf('[') == -1) {
                    if (
                        key.length()<minLength
                            || key.length()>maxLength
                    ) { continue; }
                    result.put("["+key+"]", value);
                }
            }
        }
        return result;
    }

    /**
     * 因为把空格都去掉了<br>
     * 所以内部最长的就是 [255,255,255]: 11<br>
     * 最短的就是 [#FFF]: 4
     * <br>
     * <br>
     * <br>
     * 专门用来匹配颜色标记的，比如 [#FF0000] 或 [255,0,0]。
     *
     * <p>其实就是调用了上面的通用方法，但参数已经设好了：
     * <ul>
     *   <li>括号是方括号 [ 和 ]</li>
     *   <li>最小长度4（比如#FFF）</li>
     *   <li>最大长度11（比如255,255,255）</li>
     *   <li>会自动去掉空格</li>
     * </ul>
     *
     * @param input 要处理的字符串
     * @return 找到的所有颜色标记
     * */
    public static Map<String, String> colorMatch(String input) {
        return extractBracketContent(
            input,
            '[',
            ']',
            4,
            11,
            true
        );
    }

    public static boolean counting(String source, String target, int expectedCount) {
        return StringUtils.countMatches(source, target) == expectedCount;
    }

    public String parse(String us) {
        Map<String, String> group = colorMatch(us);
        for (var kv: group.entrySet()) {
            if (kv.getValue().startsWith("#")) {
                us = us.replace(kv.getKey(), Colors.frontFrom(kv.getValue()));
            }
            if (counting(kv.getValue(), ",", 2)) {
                List<String> rgbGroup = List.of(kv.getValue().trim().split(",", -1));
                String r = rgbGroup.getFirst();
                String g = rgbGroup.get(1);
                String b = rgbGroup.getLast();
                if (r.isEmpty()) { r = "0";}
                if (g.isEmpty()) { g = "0";}
                if (b.isEmpty()) { b = "0";}
                us = us.replace(
                    kv.getKey(),
                    Colors.frontFrom(r, g, b)
                );
            }
        }
        return us+ Colors.Reset;
    }
}