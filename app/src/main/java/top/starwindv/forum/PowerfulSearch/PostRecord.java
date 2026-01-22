package top.starwindv.forum.PowerfulSearch;


import com.google.gson.annotations.SerializedName;


public record PostRecord(
    @SerializedName("post_id") Long postId,
    @SerializedName("email_str") String emailStr,
    @SerializedName("title") String title,
    @SerializedName("content") String content,
    @SerializedName("status") Integer status,
    @SerializedName("create_time") Long createTime,
    @SerializedName("last_update_time") Long lastUpdateTime
) {}