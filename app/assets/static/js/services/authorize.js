/**
 * @param RegisterRoute 需要 `username` 和 `email`
 * 邮件发送失败会返回 400
 * 成功时只返回 200
 * 服务器会给用户发送一个字符串格式的验证码, 原样传递即可
 * 
 * @param Verify 需要 `username` , `email` , `codeHash`, `verifyCode`
 * 验证失败会返回 400
 * 原样传递邮箱验证码给 verifyCode, 然后进行验证即可, 不需要处理, 否则报错
 * 
 * @param Login  需要 `email` 和 `codeHash`
 * 验证失败返回 401 {"Session-ID": Session-ID, "status": false, "message": message}
 * 成功返回 200 {"Session-ID": Session-ID, "status": true, "message": message}
 * 
 * 大小写均敏感
 * 
 * 当发起注册请求后, 服务器会向用户邮箱发送验证码, 之后成功完成了 Verify 后才能算注册完毕
 * 用户名/密码/邮箱验证码 都是用户输入
 * 
 * 本地需要存储 Session-ID, email 和 username
 */


const RegisterRoute="/api/register";
const Verify="/api/verify";


const Login ="/api/login";


class UserDTO {
    constructor(email, username, code) {
        this.email = email;
        this.username = username;
        this.codeHash = this.calculateSHA256(code);
    }

    calculateSHA256(code) {
        return CryptoJS.SHA256(code).toString(CryptoJS.enc.HEX);
    }

    static create(email, username, code) {
        return new UserDTO(email, username, code);
    }
}


class Authorizer {
    constructor() {
        // 本地存储的键名常量
        this.STORAGE_KEYS = {
            SESSION_ID: null,
            EMAIL: null,
            USERNAME: null
        };
    }

    /**
     * 注册请求 - 向服务器发送用户名和邮箱，触发邮箱验证码发送
     * @param {string} username 用户输入的用户名
     * @param {string} email 用户输入的邮箱地址
     * @returns {Promise<boolean>} 仅返回是否成功触发验证码发送（true=成功，false=失败）
     * @throws {Error} 注册请求失败时抛出错误（如400状态）
     */
    async register(username, email) {
        try {
        const response = await fetch(RegisterRoute, {
            method: 'POST',
            headers: {
            'Content-Type': 'application/json',
            },
            body: JSON.stringify({ username, email })
        });

        // 邮件发送失败（400）
        if (response.status === 400) {
            const errorData = await response.json().catch(() => ({}));
            throw new Error(`注册失败: ${errorData.message || '邮件发送失败，请检查邮箱是否有效'}`);
        }

        // 成功触发验证码发送（200）
        if (response.status === 200) {
            return true;
        }

        throw new Error(`注册请求异常，状态码: ${response.status}`);
        } catch (error) {
        console.error('注册请求出错:', error);
        throw error;
        }
    }

    /**
     * 验证验证码 - 完成注册的最后一步
     * @param {string} username 用户输入的用户名
     * @param {string} email 用户输入的邮箱地址
     * @param {string} verifyCode 用户从邮箱获取并手动输入的验证码
     * @returns {Promise<boolean>} 验证是否成功
     * @throws {Error} 验证失败时抛出错误
     */
    async verify(username, email, verifyCode) {
        try {
        // 创建UserDTO对象，自动计算用户输入验证码的SHA256哈希
        const userDTO = UserDTO.create(email, username, verifyCode);

        const response = await fetch(Verify, {
            method: 'POST',
            headers: {
            'Content-Type': 'application/json',
            },
            body: JSON.stringify({
            username: userDTO.username,
            email: userDTO.email,
            codeHash: userDTO.codeHash,
            verifyCode: verifyCode // 原样传递用户输入的原始验证码
            })
        });

        if (response.status === 400) {
            const errorData = await response.json().catch(() => ({}));
            throw new Error(`验证码验证失败: ${errorData.message || '验证码错误、过期或不匹配'}`);
        }

        if (response.status === 200) {
            // 验证成功，存储用户名和邮箱
            this.setUsername(username);
            this.setEmail(email);
            return true;
        }

        throw new Error(`验证请求异常，状态码: ${response.status}`);
        } catch (error) {
        console.error('验证码验证出错:', error);
        throw error;
        }
    }

    /**
     * 登录请求 - 使用邮箱和用户输入的验证码哈希登录
     * @param {string} email 用户输入的邮箱地址
     * @param {string} passwd
     * @returns {Promise<{status: boolean, message: string, sessionId: string|null}>} 登录结果
     * @throws {Error} 请求异常时抛出错误
     */
    async login(email, passwd) {
        try {
            const userDTO = UserDTO.create(email, '', passwd);

            const response = await fetch(Login, {
                method: 'POST',
                headers: {
                'Content-Type': 'application/json',
                },
                body: JSON.stringify({
                email: userDTO.email,
                codeHash: userDTO.codeHash
                })
            });

            const responseData = await response.json();
            const sessionId = (responseData['Session-ID'] && responseData['Session-ID'].trim()) || null;

            if (response.status === 401) {
                return {
                    status: false,
                    message: responseData.message,
                    sessionId: sessionId
                };
            }

            if (response.status === 200) {
                if (sessionId) {
                    this.setSessionId(sessionId);
                    this.setEmail(email);
                } else {
                    throw new Error('No Valid Session-ID');
                }
                return {
                    status: true,
                    message: responseData.message,
                    sessionId: sessionId
                };
            }

        throw new Error(`登录请求异常，状态码: ${response.status}`);
        } catch (error) {
            console.error('登录请求出错:', error);
            throw error;
        }
    }

    setSessionId(sessionId) {
        if (sessionId && sessionId.trim()) {
        localStorage.setItem(this.STORAGE_KEYS.SESSION_ID, sessionId);
        }
    }

    getSessionId() {
        return localStorage.getItem(this.STORAGE_KEYS.SESSION_ID) || null;
    }

    setEmail(email) {
        if (email && email.trim()) {
        localStorage.setItem(this.STORAGE_KEYS.EMAIL, email);
        }
    }

    getEmail() {
        return localStorage.getItem(this.STORAGE_KEYS.EMAIL) || null;
    }

    setUsername(username) {
        if (username && username.trim()) {
        localStorage.setItem(this.STORAGE_KEYS.USERNAME, username);
        }
    }

    getUsername() {
        return localStorage.getItem(this.STORAGE_KEYS.USERNAME) || null;
    }

    /**
     * 清除所有本地存储的用户信息（登出）
     */
    logout() {
        localStorage.removeItem(this.STORAGE_KEYS.SESSION_ID);
        localStorage.removeItem(this.STORAGE_KEYS.EMAIL);
        localStorage.removeItem(this.STORAGE_KEYS.USERNAME);
    }
}
