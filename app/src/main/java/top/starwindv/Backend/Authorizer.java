package top.starwindv.Backend;


import org.apache.commons.lang3.tuple.Triple;
import top.starwindv.Email;
import top.starwindv.Models.Users;
import top.starwindv.Tools.VerifyCode;
import top.starwindv.Utils.FStyles;
import top.starwindv.Utils.Values;

import java.util.Map;


public class Authorizer {
    public final Users UsersTool;
    public final VerifyCode CodeGen;
    public final static Map<
        String, Triple<String, String, Long>
    > codeCache = new java.util.HashMap<>();
    public final Email Poster;
    public final long validityPeriod = 5 * 60 * 1000L; // 5 minutes
    public final static FStyles styles = new FStyles();

    public final SessionController SessionOperator;

    public Values sendCode(
        String username,
        String email
    ) {
        Values checkResult = this.UsersTool.unionNameEmailCheck(username, email);
        if (!(boolean) checkResult.get(0)) { return checkResult; }
        String code = this.CodeGen.generate();
        codeCache.put(
            email, Triple.of(
                email,
                code,
                System.currentTimeMillis()
            )
        );
        System.out.println(email + ", " + code);
        String responseID = this.Poster.verifyCode(code, email);
        if (!responseID.isEmpty()) {
            System.out.println(responseID);
            return Values.from(true, "Email Sent");
        } else {

            System.out.println(
                styles.Bold + styles.Red + "[X]" + styles.Reset + "Failed to Send Email, Removed"
            );
            codeCache.remove(email);
            return Values.from(false, "Failed when Send Email");
        }

    }

    public Authorizer(Users UsersTool, int codeLength, Email Poster, SessionController sc) {
        this.UsersTool = UsersTool;
        this.CodeGen = new VerifyCode(codeLength);
        this.Poster = Poster;
        this.SessionOperator = sc;
    }

    public Values verifyCodeAfterRegister(String email, String userName, String code, String codeHash, String IP) {
        // <Email, Email, code, Timestamp>
        Values result;
        if (
            codeCache.containsKey(email) &&
            codeCache.get(email).getMiddle().equals(code)
        ) {
            if (System.currentTimeMillis() - codeCache.get(email).getRight() < this.validityPeriod) {
                result = Values.from(true, "Register Success");
                Values regResult = this.UsersTool.register(userName, email, codeHash, IP);
                if (!(boolean) regResult.get(0)) { result = regResult; }
            } else {
                result = Values.from(false, "Verify Code Has Expired");
                codeCache.remove(email);
            }
        } else {
            result = Values.from(false, "Verify Code is invalid");
        }
        System.out.println(codeCache);
        return result;
    }

    public Values login(String email, String codeHash, String IP) {
        Values result = this.UsersTool.login(email, codeHash, IP);
        if ((boolean) result.get(0)) {
            String session_id = (String) this.SessionOperator.addSession(email).get(2);
            return Values.from(true, session_id);
        } else {return Values.from(false, "Login Failed"); }
    }
}
