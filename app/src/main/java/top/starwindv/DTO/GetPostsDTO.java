package top.starwindv.DTO;


@SuppressWarnings({"FieldMayBeFinal", "FieldCanBeLocal", "unused"})
public class GetPostsDTO {
    private String method;
    private int from;
    private int to;
    private int limit=0;
    private String post_id;
    private boolean isArc=true;

    /*
     * We don't need setter
     * DTO should created by method which like "ctx.bodyAsClass"
     */

    public String method() { return this.method; }
    public String post_id() { return this.post_id; }
    public boolean isArc() { return this.isArc; }
    public int limit() { return this.limit; }
    public int from() { return this.from; }
    public int to() { return this.to; }

    public GetPostsDTO() {
        /* Gson Mapper Required Default White Constructor */
    }

}