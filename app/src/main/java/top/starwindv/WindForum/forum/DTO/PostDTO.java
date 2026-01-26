package top.starwindv.WindForum.forum.DTO;


@SuppressWarnings("unused")
public class PostDTO {
    private String userEmail;
    private String title;
    private String content;
    private String belongTo;

    /*
     * We don't need setter
     * DTO should created by method which like "ctx.bodyAsClass"
     */

    public String userEmail() { return this.userEmail; }
    public String title() { return this.title; }
    public String content() { return this.content; }
    public String belongTo() { return this.belongTo; }

    public boolean isEmpty() {
        return userEmail == null && title == null && content == null;
    }

    public PostDTO() {
        /* Gson Mapper Required Default White Constructor */
    }

    public PostDTO(String userEmail, String title, String content, String belongTo) {
        this.userEmail = userEmail;
        this.title = title;
        this.content= content;
        this.belongTo= belongTo;
    }
}