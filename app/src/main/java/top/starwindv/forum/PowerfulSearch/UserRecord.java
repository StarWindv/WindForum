package top.starwindv.forum.PowerfulSearch;


import com.google.gson.annotations.SerializedName;


public record UserRecord(
    @SerializedName("user_id") Long userId,
    @SerializedName("user_name") String userName,
    @SerializedName("email_str") String emailStr,
    @SerializedName("passwd_hash") String passwdHash,
    @SerializedName("last_log_ip") String lastLogIp,
    @SerializedName("register_ip") String registerIp,
    @SerializedName("permission") Integer permission,
    @SerializedName("status") Integer status,
    @SerializedName("reg_time") Long regTime,
    @SerializedName("last_log_time") Long lastLogTime
) {}