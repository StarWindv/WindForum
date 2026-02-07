const activeNotices = new Map();
const NOTICE_GAP = 10;

/**
 * @param {Object} options 配置项
 * @param {string} options.message 通知内容（必填）
 * @param {string} [options.title=''] 通知标题（可选）
 * @param {number} [options.duration=2500] 显示时长（毫秒）
 */
function notice(options) {
    const {
        message = '',
        title = '',
        duration = 2500
    } = typeof options === 'object' ? options : {
        message: options,
        duration: arguments[1] || 2500
    };

    const noticeId = `notice_${Date.now()}_${Math.random().toString(36).slice(2)}`;
    const toast = document.createElement('div');
    toast.className = 'notice_card';
    toast.dataset.noticeId = noticeId;

    toast.innerHTML = `
        ${title ? `<div class="notice_title">${title}</div>` : ''}
        <div class="notice_content">${message}</div>
    `;

    document.body.appendChild(toast);

    activeNotices.set(noticeId, toast);

    setTimeout(() => {
        toast.classList.add('show');
        updateNoticeOffsets();
    }, 0);

    setTimeout(() => {
        toast.classList.add('hide');

        setTimeout(() => {
            if (toast.parentNode) {
                toast.parentNode.removeChild(toast);
            }
            activeNotices.delete(noticeId);
            updateNoticeOffsets();
        }, 2000);
    }, duration);
}

function updateNoticeOffsets() {
    const noticeList = Array.from(activeNotices.values());

    let totalOffset = 0;
    for (let i = noticeList.length - 1; i >= 0; i--) {
        const notice = noticeList[i];
        const noticeHeight = notice.offsetHeight;

        notice.style.bottom = `${20 + totalOffset}px`;

        totalOffset += noticeHeight + NOTICE_GAP;
    }
}
