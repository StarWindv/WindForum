function notice(message, className, duration = 2000) {
    let toast = document.querySelector(className);
    if (!toast) {
        toast = document.createElement('div');
        toast.className = className;
        document.body.appendChild(toast);
    }

    toast.innerHTML = message;

    setTimeout(() => {
        toast.classList.add('show');
    }, 0);

    setTimeout(() => {
        setTimeout(() => {
            if (toast.parentNode) {
                toast.parentNode.removeChild(toast);
            }
        }, 200);
    }, duration);
}