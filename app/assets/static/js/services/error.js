class UnAuthorizeError extends Error {
    constructor(message) {
        super(message);
        this.name = "UnAuthorizeError";
    }
}