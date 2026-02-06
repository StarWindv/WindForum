function notice(message, className, duration = 2500) {
    let toast = document.querySelector(className);
    if (!toast) {
        toast = document.createElement('div');
        toast.className = className;
        document.body.appendChild(toast);
    }

    toast.innerHTML = message;
    toast.classList.remove('show', 'hide');

    setTimeout(() => {
        toast.classList.add('show');
    }, 0);

    setTimeout(() => {
        toast.classList.add('hide');
        setTimeout(() => {
            if (toast.parentNode && toast.parentNode.removeChild) {
                toast.parentNode.removeChild(toast);
            }
        }, 1200);
    }, duration);
}
