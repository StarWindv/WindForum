package top.starwindv.forum.DTO;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.javalin.json.JsonMapper;
import org.jetbrains.annotations.NotNull;
import top.starwindv.forum.Utils.Values;

import java.lang.reflect.Type;


public record GsonMapper(Gson gson) implements JsonMapper {

    public GsonMapper() {
        this(
            new GsonBuilder()
                .setPrettyPrinting()
                .serializeNulls()
                .setPrettyPrinting()
                .create()
        );
    }

    @NotNull
    @Override
    public String toJsonString(@NotNull Object obj, @NotNull Type type) {
        if (type.equals(Values.class)) {
//            System.err.println(type);
            return ((Values)obj).serialize();
        }
        return gson.toJson(obj, type);
    }

    @NotNull
    @Override
    public <T> T fromJsonString(@NotNull String json, @NotNull Type type) {
        return gson.fromJson(json, type);
    }
}