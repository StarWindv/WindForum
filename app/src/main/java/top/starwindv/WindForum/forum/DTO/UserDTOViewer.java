package top.starwindv.WindForum.forum.DTO;


import org.apache.commons.lang3.StringUtils;


public class UserDTOViewer extends DTOViewer{
    private static final String[] property = {
        "username", "email", "codeHash"
    };

    @Override
    public void initialize() {
        this.targetCls = UserDTO.class;
        this.init = true;
    }

    public String Stringify(UserDTO obj) {
        StringBuilder sb = new StringBuilder();
        sb.append("UserInfo:");
        for (var ele: property) {
            if (ele.equals("codeHash")) {
                sb.append("\n - ")
                    .append(StringUtils.capitalize(ele))
                    .append(": ")
                    .append("<Hidden Length: ")
                    .append(getattr(ele, obj).toString().length())
                    .append(">");
            }
            else {
                sb.append("\n - ")
                    .append(StringUtils.capitalize(ele))
                    .append(": ")
                    .append(getattr(ele, obj));
            }
        }
        return sb.toString();
    }
}
