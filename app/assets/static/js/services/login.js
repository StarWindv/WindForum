const form = document.getElementById('auth');
const Auth = new Authorizer();
const els = {
    username: document.getElementById('username'),
    email: document.getElementById('email'),
    password: document.getElementById('password'),
    verifyCode: document.getElementById('verifyCode')
};

function resetRequired() {
    els.username.required = false;
    els.email.required = false;
    els.password.required = false;
    els.verifyCode.required = false;
}

async function login() {
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
        window.location.href = "/index"; // 登录成功跳转
    }
    return result;
}

async function sendVerifyCode(username, email) {
    const result = await Auth.register(username, email);
    return result;
}

async function verifyCodeAndRegister() {
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

    const verifyResult = await Auth.verify(username, email, password, verifyCode);
    console.log('验证验证码结果:', verifyResult);

    if (verifyResult) {
        await login(); // 验证成功自动登录
    } else {
        throw new Error("邮箱验证码验证失败");
    }
    return verifyResult;
}

document.getElementById('login').addEventListener('click', async (e) => {
    e.preventDefault();
    await login();
});

document.getElementById('send_code').addEventListener('click', async (e) => {
    e.preventDefault();
    resetRequired();
    els.username.required = true;
    els.email.required = true;

    if (!form.checkValidity()) {
        form.reportValidity();
        return;
    }

    const username = els.username.value;
    const email = els.email.value;
    await sendVerifyCode(username, email);
    alert(`验证码已发送至邮箱：${email}`);
});

document.getElementById('register').addEventListener('click', async (e) => {
    e.preventDefault();
    try {
        await verifyCodeAndRegister();
    } catch (err) {
        alert(err.message);
    }
});