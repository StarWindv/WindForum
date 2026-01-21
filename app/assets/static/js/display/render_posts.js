// noinspection JSUnresolvedReference


function formatTimestamp(timestamp) {
    const d = new Date(timestamp);
    return `${d.getFullYear()}-${(d.getMonth()+1).toString().padStart(2,'0')}-${d.getDate().toString().padStart(2,'0')} ${d.getHours().toString().padStart(2,'0')}:${d.getMinutes().toString().padStart(2,'0')}`;
}

function formatCode(content) {
    let normalized = content.replace(/\\n/g, '\n');
    normalized = normalized.replace(
        /```(\w+)?\s*(.+?)```/gs,
        (match, lang, code) => {
            const cleanCode = code.trim().replace(/```/g, '');
            return `\n\`\`\`${lang || ''}\n${cleanCode}\n\`\`\`\n`;
        }
    );
    return normalized;
}

function useStyle(eleTag, method) {
    document.querySelectorAll(eleTag).forEach(el => {
        method(el);
    });
}

marked.setOptions({
    highlight: function(code, lang) {
        if (lang && hljs.getLanguage(lang)) {
            return hljs.highlight(code, { language: lang }).value;
        }
        return hljs.highlightAuto(code).value;
    }
});


async function renderPosts(postContainer, loader) {
    let postsData;
    try {
        postsData = await loader();
    } catch (error) {
        if (error instanceof UnAuthorizeError) {
            window.location.href = "/login";
        }
        throw error;
    }

    const postsList = DOMBuilder.ul({
        className: "postsList"
    });

    if (postsData && postsData.length > 0) {
        postsData.forEach(post => {
            const postItem = DOMBuilder.li({
                className: "post",
                children: [
                    // 标题
                    DOMBuilder.h2({
                        text: post.title
                    }),

                    (() => {
                        const contentElement = DOMBuilder.div({
                            html: marked.parse(formatCode(post.content))
                        });

                        // 高亮代码块
                        setTimeout(() => {
                            useStyle('pre code', hljs.highlightElement);
                        }, 0);

                        return contentElement;
                    })(),

                    // 元信息
                    (() => {
                        return DOMBuilder.div({
                            className: "post-meta",
                            children: [
                                // 作者信息
                                (() => {
                                    return DOMBuilder.span({
                                        style: {whiteSpace: "pre"},
                                        children: [
                                            DOMBuilder.text("Author: "),
                                            DOMBuilder.a({
                                                attrs: {href: `/user/${post.email_str}`},
                                                text: post.email_str
                                            })
                                        ]
                                    });
                                })(),

                                DOMBuilder.text(" | "),

                                // 发布时间
                                DOMBuilder.span({
                                    text: `Publish: ${formatTimestamp(post.create_time)}`
                                }),

                                DOMBuilder.text(" | "),

                                // 更新时间
                                DOMBuilder.span({
                                    text: `Last Update: ${formatTimestamp(post.last_update_time)}`
                                })
                            ]
                        });
                    })()
                ]
            });

            postsList.appendChild(postItem);
        });
    } else {
        // 没有帖子的情况
        const noPostsMessage = DOMBuilder.li({
            text: "暂无帖子",
            className: "no-posts"
        });
        postsList.appendChild(noPostsMessage);
    }

    postContainer.appendChild(postsList);
}