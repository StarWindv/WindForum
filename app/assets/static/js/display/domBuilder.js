// noinspection JSCheckFunctionSignatures,JSValidateTypes


class DOMBuilder {
    /**
     * 创建一个 DOM 元素
     * @returns {HTMLElement} 创建的 DOM 元素
     */
    static create(tag, options = {}) {
        const element = document.createElement(tag);

        const skipDirectSet = new Set(
            ['html', 'className', 'attrs', 'style', 'on', 'children', 'text']
        );
        const handlers = {
            html: val => element.innerHTML = val,
            className: val => {
                if (Array.isArray(val)) {
                    element.classList.add(...val);
                } else {
                    element.className = val;
                }
            },
            attrs: val => {
                Object.entries(val).forEach(([k, v]) => element.setAttribute(k, v));
            },
            style: val => {
                if (typeof val === 'string') {
                    element.style.cssText = val;
                } else if (typeof val === 'object' && val !== null) {
                    Object.entries(val).forEach(([prop, value]) => {
                        if (value !== null && value !== undefined) {
                            element.style.setProperty(prop, value);
                        }
                    });
                }
            },
            on: val => {
                Object.entries(val).forEach(([evt, fn]) => element.addEventListener(evt, fn));
            },
            children: val => {
                val?.forEach(child => child instanceof Node && element.appendChild(child));
            },
            text: val => {
                if (typeof val === 'object' && val.nodeType === Node.TEXT_NODE) {
                    element.appendChild(val);
                } else {
                    element.textContent = val;
                }
            }
        };
        for (const [key, value] of Object.entries(options)) {
            if (value == null) continue;

            if (skipDirectSet.has(key)) {
                handlers[key]?.(value);
            } else if (key in element) {
                element[key] = value;
            } else {
                element.setAttribute(key, value);
            }
        }
        return element;
    }

    static text(text) {
        return document.createTextNode(text);
    }
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
