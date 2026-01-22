package top.starwindv.forum.DTO;


public interface DTOViewer {
    void initialize();

    Object getattr(
            String name, Object obj
    ) throws Exception;
}
