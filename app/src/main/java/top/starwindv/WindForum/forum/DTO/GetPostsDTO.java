package top.starwindv.WindForum.forum.DTO;


@SuppressWarnings({"FieldMayBeFinal", "FieldCanBeLocal", "unused"})
public class GetPostsDTO {
    private String method;
    private int from;
    private int to;
    private int limit=0;
    private String post_id;
    private String channel_id;
    private boolean isArc=true;

    /*
     * We don't need setter
     * DTO should created by method which like "ctx.bodyAsClass"
     */
    public String method() { return this.method; }
    public String post_id() { return this.post_id; }
    public String channel_id() { return this.channel_id; }
    public boolean isArc() { return this.isArc; }
    public int limit() { return this.limit; }
    public int from() { return this.from; }
    public int to() { return this.to; }

    public GetPostsDTO() {
        /* Gson Mapper Required Default White Constructor */
    }

    @Override
    public String toString() {
        return String.format("""
            GetPostsDTO:
             method     - %s
             from       - %d
             to         - %d
             limit      - %d
             post_id    - %s
             channel_id - %s
             isArc      - %b
            """,
            this.method,
            this.from,
            this.to,
            this.limit,
            this.post_id,
            this.channel_id,
            this.isArc
        );
    }
}
