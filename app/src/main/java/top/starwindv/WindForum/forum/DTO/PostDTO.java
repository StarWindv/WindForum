package top.starwindv.WindForum.forum.DTO;


@SuppressWarnings("unused")
public class PostDTO {
    private String userEmail;
    private String title;
    private String content;
    private String channel_id;

    /*
     * We don't need setter
     * DTO should created by method which like "ctx.bodyAsClass"
     */

    public String userEmail() { return this.userEmail; }
    public String title() { return this.title; }
    public String content() { return this.content; }
    public String channel_id() { return this.channel_id; }

    public boolean isEmpty() {
        return userEmail == null && title == null && content == null;
    }

    public PostDTO() {
        /* Gson Mapper Required Default White Constructor */
    }

    public PostDTO(String userEmail, String title, String content, String channel_id) {
        this.userEmail = userEmail;
        this.title = title;
        this.content= content;
        this.channel_id = channel_id;
    }
}