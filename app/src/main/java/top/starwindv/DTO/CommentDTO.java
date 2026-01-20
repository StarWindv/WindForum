package top.starwindv.DTO;


@SuppressWarnings("unused")
public class CommentDTO {
    private String email;
    private String post_id;
    private String content;

    public final String email() { return this.email; }
    public final String Post_id() { return this.post_id; }
    public final String content() { return this.content; }

    public CommentDTO() {
        /* Gson Mapper Required Default White Constructor */
    }

    public CommentDTO(String email, String post_id, String content) {
        this.email = email;
        this.post_id = post_id;
        this.content = content;
    }

    public final boolean isEmpty() {
        return this.email == null
            && this.post_id == null
            && this.content == null;
    }
}