package top.starwindv.DTO;


import java.util.ArrayList;


public interface DTOViewer {
    void initialize();

    Object getattr(
            String name, Object obj
    ) throws Exception;
}
