package top.starwindv.WindForum.forum.DTO;


public class UserDTO {
    private String username;
    private String email;
    private String codeHash;
    private String verifyCode;
    public final static UserDTOViewer viewer = new UserDTOViewer();

    /*
    * We don't need setter
    * DTO should created by method which like "ctx.bodyAsClass"
    */

    public String username() { return this.username; }
    public String email()    { return this.email; }
    public int    hiddenCode() { return this.codeHash.length(); }
    public String codeHash() { return this.codeHash; }
    public String verifyCode() { return this.verifyCode; }

    public UserDTO() {
        /* Gson Mapper Required Default White Constructor */
    }

    public UserDTO(String username, String email, String codeHash, String verifyCode) {
        this.username = username;
        this.email = email;
        this.codeHash = codeHash;
        this.verifyCode = verifyCode;
    }
}