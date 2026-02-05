package top.starwindv.WindForum.forum.DTO;


@SuppressWarnings("unused")
public class ChannelDTO {
    private String channel_name;
    private String channel_desc;

    /*
     * We don't need setter
     * DTO should created by method which like "ctx.bodyAsClass"
     */

    public String channel_name() { return this.channel_name; }
    public String channel_desc() { return this.channel_desc; }

    public boolean isEmpty() {
        return channel_name == null && channel_desc == null;
    }

    public ChannelDTO() {
        /* Gson Mapper Required Default White Constructor */
    }

    public ChannelDTO(String title, String content) {
        this.channel_name = title;
        this.channel_desc = content;
    }
}