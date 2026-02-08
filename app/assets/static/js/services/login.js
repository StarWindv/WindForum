// noinspection ExceptionCaughtLocallyJS,HtmlUnknownTarget

const Auth = new Authorizer();
const form = document.getElementById('auth');
let currentMode = 'login';


function renderLoginForm() {
    currentMode = 'login';
    form.innerHTML = '';
    form.innerHTML += `
        <label class="collector">
            Email
            <input type="text" id="email" name="email" autocomplete="on">
        </label>
    `;
    form.innerHTML += `
        <label class="collector">
            Password
            <input type="password" id="password" name="password" autocomplete="on">
        </label>
    `;
    form.innerHTML += `
        <div class="switch-link">
            还没有账户? <a id="toRegister">点击注册</a>
        </div>
    `;
    form.innerHTML += `
        <div class="container">
            <div class="part">
                <button type="button" id="loginBtn" class="part">Login</button>
            </div>
        </div>
    `;
    document.getElementById('loginBtn').addEventListener('click', async (e) => {
        e.preventDefault();
        await handleLogin();
    });
    document.getElementById('toRegister').addEventListener('click', () => {
        renderRegisterForm();
    });
    updateElementReferences();
}

function renderRegisterForm() {
    currentMode = 'register';
    form.innerHTML = '';

    form.innerHTML += `
        <label class="collector">
            UserName
            <input type="text" id="username" name="username" autocomplete="on">
        </label>
    `;
    form.innerHTML += `
        <label class="collector">
            Email
            <input type="text" id="email" name="email" autocomplete="on">
        </label>
    `;
    form.innerHTML += `
        <label class="collector">
            Password
            <input type="password" id="password" name="password" autocomplete="on">
        </label>
    `;
    form.innerHTML += `
        <label class="collector">
            Email Verify Code
            <input type="text" id="verifyCode" name="verifyCode" autocomplete="on">
        </label>
    `;
    form.innerHTML += `
        <div class="license-text">
            注册即代表您已详细理解并同意<a href="/show/LICENSE.html">用户协议</a>
        </div>
    `;
    form.innerHTML += `
        <div class="container">
            <div class="part">
                <button type="button" id="sendCodeBtn" class="part">Send Email Code</button>
            </div>
            <div class="part">
                <button type="button" id="registerBtn" class="part">Register</button>
            </div>
        </div>
    `;
    form.innerHTML += `
        <div class="switch-link">
            已有账户？<a id="toLogin">返回登录</a>
        </div>
    `;
    document.getElementById('sendCodeBtn').addEventListener('click', async (e) => {
        e.preventDefault();
        await handleSendCode();
    });
    document.getElementById('registerBtn').addEventListener('click', async (e) => {
        e.preventDefault();
        await handleRegister();
    });
    document.getElementById('toLogin').addEventListener('click', () => {
        renderLoginForm();
    });
    updateElementReferences();
}

let els;

function updateElementReferences() {
    els = {
        username: document.getElementById('username'),
        email: document.getElementById('email'),
        password: document.getElementById('password'),
        verifyCode: document.getElementById('verifyCode')
    };
}

function resetRequired() {
    if (els.username) els.username.required = false;
    if (els.email) els.email.required = false;
    if (els.password) els.password.required = false;
    if (els.verifyCode) els.verifyCode.required = false;
}

async function handleLogin() {
    resetRequired();
    els.email.required = true;
    els.password.required = true;

    if (!form.checkValidity()) {
        form.reportValidity();
        return false;
    }

    const email = els.email.value;
    const password = els.password.value;
    const result = await Auth.login(email, password);
    if (result.status) {
        form.reset();
        window.location.href = "/index";
    }
    return result;
}

async function handleSendCode() {
    resetRequired();
    els.username.required = true;
    els.email.required = true;

    if (!form.checkValidity()) {
        form.reportValidity();
        return;
    }

    const username = els.username.value;
    const email = els.email.value;
    const sendStatus = await Auth.register(username, email);
    if (sendStatus) {
        notice({
            title: "INFO",
            message: "验证码发送成功<br>请检查您的收件箱或垃圾邮件"
        });
    } else {
        notice({
            title: "ERROR",
            message: "验证码发送失败"
        });
    }
}

async function handleRegister() {
    resetRequired();
    els.username.required = true;
    els.email.required = true;
    els.password.required = true;
    els.verifyCode.required = true;

    if (!form.checkValidity()) {
        form.reportValidity();
        return false;
    }

    const username = els.username.value;
    const email = els.email.value;
    const password = els.password.value;
    const verifyCode = els.verifyCode.value;

    try {
        const verifyResult = await Auth.verify(username, email, password, verifyCode);
        console.log('验证验证码结果:', verifyResult);

        if (verifyResult) {
            await handleLogin();
        } else {
            throw new Error("邮箱验证码验证失败");
        }
        return verifyResult;
    } catch (err) {
        notice({
            title: "ERROR",
            message: err.message
        });
    }
}

document.addEventListener('DOMContentLoaded', () => {
    renderLoginForm();
});
