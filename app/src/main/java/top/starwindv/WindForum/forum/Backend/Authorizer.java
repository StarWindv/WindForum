package top.starwindv.WindForum.forum.Backend;


import org.apache.commons.lang3.tuple.Triple;
import top.starwindv.WindForum.forum.Email;
import top.starwindv.WindForum.forum.Forum;
import top.starwindv.WindForum.forum.Models.Users;
import top.starwindv.WindForum.forum.Tools.VerifyCode;
import top.starwindv.WindForum.logger.Colorful.Colors;
import top.starwindv.WindForum.forum.Utils.Values;

import java.util.Map;


public class Authorizer {
    public final Users UsersTool;
    public final VerifyCode CodeGen;
    public final static Map<
        String, Triple<String, String, Long>
    > codeCache = new java.util.HashMap<>();
    public final Email Poster;
    public final long validityPeriod = 5 * 60 * 1000L; // 5 minutes

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
        Forum.Logger().debug(email + ", " + code);
        String responseID = this.Poster.verifyCode(code, email);
        if (!responseID.isEmpty()) {
            Forum.Logger().debug(responseID);
            return Values.from(true, "Email Sent");
        } else {
            Forum.Logger().println(
                Colors.Bold + Colors.Red + "[X]" + Colors.Reset + "Failed to Send Email, Removed"
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
            Values resp = this.SessionOperator.addSession(email);
//            System.out.println(resp);
            if ((boolean) resp.get(0)) {
                String session_id = (String) resp.get(2);
                return Values.from(true, "", session_id);
            } return Values.from(false, "Add Session-ID Failed");
        } else {return Values.from(false, "Login Failed"); }
    }
}
