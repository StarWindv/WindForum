package top.starwindv.WindForum.forum;


import com.resend.Resend;
import com.resend.core.exception.ResendException;
import com.resend.services.emails.model.CreateEmailOptions;
import com.resend.services.emails.model.CreateEmailResponse;

import java.time.LocalDate;


class SimpleConfig {
    public final static String admin = "admin";
    public final static String developer = "starwindv";
    public final static String domain = "@mail.starwindv.top";
    public final static String logo = """
            <table border="0" cellpadding="0" cellspacing="0" role="presentation" style="border-collapse: collapse;">
                <tr>
                    <td valign="middle">
                        <span style="font-size: 24px; color: #333333; font-weight: bold;">
                            WindForum
                        </span>
                    </td>
                </tr>
            </table>
        """;
}


@SuppressWarnings("unused")
public class Email {
    private final static String apiKey = System.getenv("RESEND_API_KEY");
    private final static Resend Postman = new Resend(apiKey);

    private String template;
    private final String AppName;

    public Email(String template, String AppName) {
        this.template = template.replace(
                            "<|content|>", 
                            "以下是您的验证码, 请不要透露给任何人"
                            + "<br>"
                            + "如不是您主动操作, 请忽略此邮件"
                            + "<br>"
                            + "验证码 <strong>5</strong> 分钟内有效"
                        );
        this.AppName = AppName;
    }

    @SuppressWarnings("CallToPrintStackTrace")
    public String verifyCode(String code, String to) {
        String from = SimpleConfig.admin+SimpleConfig.domain;

        String html = this.template
                .replace("<|verify_code|>" , code)
                .replace("<|send_date|>"   , LocalDate.now().toString())
                .replace("<|sender_title|>", from)
                .replace("<|cta_link|>"    , "")
                .replace("<|receiver|>"    , to)
                .replace("<|sender|>"      , this.AppName)
                .replace("<|sender_logo_text|>", SimpleConfig.logo);

        CreateEmailOptions Options = CreateEmailOptions.builder()
                .from(from)
                .to(to)
                .html(html)
                .subject("Verify Your Email Address")
                .build();
       try {
            CreateEmailResponse response = Postman.emails().send(Options);
            return response.getId();
        } catch (ResendException e) {
            e.printStackTrace();
            return "Error";
        }
    }

    public void setTemplate(String template) {
        this.template = template;
    }
}
