package top.starwindv.forum.Tools;


import java.util.Random;


public class VerifyCode {
    @SuppressWarnings("SpellCheckingInspection")
    private static final String CHAR_SET = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final Random random = new Random(System.currentTimeMillis());
    public final int genLength;

    public VerifyCode(int genLength) {
        this.genLength = genLength;
    }

    public String generate() {
        StringBuilder sb = new StringBuilder(genLength);
        for (int i = 0; i < genLength; i++) {
            int index = random.nextInt(CHAR_SET.length());
            sb.append(CHAR_SET.charAt(index));
        }
        return sb.toString();
    }
}