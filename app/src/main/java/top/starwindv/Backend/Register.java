package top.starwindv.Backend;


import org.apache.commons.lang3.tuple.Triple;
import top.starwindv.Email;
import top.starwindv.Models.Users;
import top.starwindv.Tools.VerifyCode;
import top.starwindv.Utils.Values;

import java.util.Map;


public class Register {
    public final Users UsersTool;
    public final VerifyCode CodeGen;
    public final Map<
        String, Triple<String, String, Long>
    > codeCache = new java.util.HashMap<>();
    public final Email Poster;
    public final long validityPeriod = 5 * 60; // 5 minutes

    public void register(
        String username,
        String password,
        String email,
        String registerIP
    ) {
        Values regResult = this.UsersTool.register(
            username, password, email, registerIP
        );
        if ((boolean) regResult.get(0)) {
            String code = this.CodeGen.generate();
            this.codeCache.put(
                email, Triple.of(
                    email,
                    code,
                    System.currentTimeMillis()
                )
            );
            String responseID = this.Poster.verifyCode(code, email);
            if (!responseID.isEmpty()) {
                System.out.println(responseID);
            }
        }
        System.out.println(regResult.get(1));
    }

    public Register(Users UsersTool, int codeLength, Email Poster) {
        this.UsersTool = UsersTool;
        this.CodeGen = new VerifyCode(codeLength);
        this.Poster = Poster;
    }

    public Values verifyCodeAfterRegister(String email, String code) {
        // <Email, Email, code, Timestamp>
        Values result;
        if (
            this.codeCache.containsKey(email) &&
            this.codeCache.get(email).getMiddle().equals(code)
        ) {
            if (System.currentTimeMillis() - this.codeCache.get(email).getRight() < this.validityPeriod) {
                result = Values.from(true, "Register Success");
            } else { result = Values.from(false, "Verify Code Has Expired"); }
            this.codeCache.remove(email);
        } else { result = Values.from(false, "Verify Code is invalid"); }
        return result;
    }
}
