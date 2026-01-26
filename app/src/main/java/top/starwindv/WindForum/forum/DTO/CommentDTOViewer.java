package top.starwindv.WindForum.forum.DTO;

import org.apache.commons.lang3.StringUtils;


@SuppressWarnings("unused")
public class CommentDTOViewer extends DTOViewer{
    protected static final String[] property = {
        "email", "post_id", "content"
    };

    @Override
    public void initialize() {
        this.targetCls = CommentDTO.class;
        this.init = true;
    }

    public String Stringify(UserDTO obj) {
        StringBuilder sb = new StringBuilder();
        sb.append("CommentInfo:");
        for (var ele: property) {
            sb.append("\n - ")
                .append(StringUtils.capitalize(ele))
                .append(": ")
                .append(getattr(ele, obj));
        }
        return sb.toString();
    }
}
