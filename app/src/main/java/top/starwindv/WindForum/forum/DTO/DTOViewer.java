package top.starwindv.WindForum.forum.DTO;


public interface DTOViewer {
    void initialize();

    Object getattr(
            String name, Object obj
    ) throws Exception;
}
