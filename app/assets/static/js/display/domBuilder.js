// noinspection JSCheckFunctionSignatures,JSValidateTypes


class DOMBuilder {
    /**
     * 创建一个 DOM 元素
     * @param {string} tag - 元素标签名
     * @param {Object} options - 配置选项
     * @param {string|Object} options.text - 文本内容或文本节点
     * @param {string} options.html - HTML 内容
     * @param {Object} options.attrs - 属性对象
     * @param {Object} options.style - 样式对象
     * @param {Object} options.id    - 对象ID
     * @param {Object} options.name  - 对象名称
     * @param {Object} options.autocomplete - 自动补全
     * @param {Object} options.placeholder  - 占位符
     * @param {Object} options.type  - 对象类型
     * @param {string|Array} options.className - CSS 类名
     * @param {Array} options.children - 子元素数组
     * @param {Function} options.on - 事件监听器对象
     * @returns {HTMLElement} 创建的 DOM 元素
     */
    static create(tag, options = {}) {
        const element = document.createElement(tag);

        // 设置文本内容
        if (options.text !== undefined) {
            if (typeof options.text === 'object' && options.text.nodeType === Node.TEXT_NODE) {
                element.appendChild(options.text);
            } else {
                element.textContent = options.text;
            }
        }

        // 设置 HTML 内容
        if (options.html !== undefined) {
            element.innerHTML = options.html;
        }

        // 设置属性
        if (options.attrs) {
            Object.entries(options.attrs).forEach(([key, value]) => {
                element.setAttribute(key, value);
            });
        }

        // 设置样式
        if (options.style) {
            Object.assign(element.style, options.style);
        }

        if (options.id) {
            element.id = options.id;
        }

        if (options.name) {
            element.name = options.name;
        }

        if (options.autocomplete) {
            element.autocomplete = options.autocomplete;
        }

        if (options.placeholder) {
            element.placeholder = options.placeholder;
        }

        if (options.type) {
            element.type = options.type;
        }

        // 设置类名
        if (options.className) {
            if (Array.isArray(options.className)) {
                element.classList.add(...options.className);
            } else {
                element.className = options.className;
            }
        }

        // 添加事件监听器
        if (options.on) {
            Object.entries(options.on).forEach(([event, handler]) => {
                element.addEventListener(event, handler);
            });
        }

        // 添加子元素
        if (options.children && Array.isArray(options.children)) {
            options.children.forEach(child => {
                if (child instanceof Node) {
                    element.appendChild(child);
                }
            });
        }

        return element;
    }

    /**
     * 创建文本节点
     * @param {string} text - 文本内容
     * @returns {Text} 文本节点
     */
    static text(text) {
        return document.createTextNode(text);
    }

    /**
     * 快速创建常用元素的方法
     */
    static div(options) { return this.create('div', options); }
    static span(options) { return this.create('span', options); }
    static p(options) { return this.create('p', options); }
    static h1(options) { return this.create('h1', options); }
    static h2(options) { return this.create('h2', options); }
    static h3(options) { return this.create('h3', options); }
    static ul(options) { return this.create('ul', options); }
    static li(options) { return this.create('li', options); }
    static a(options) { return this.create('a', options); }
    static pre(options) { return this.create('pre', options); }
    static code(options) { return this.create('code', options); }
    static label(options) { return this.create('label', options); }
}
