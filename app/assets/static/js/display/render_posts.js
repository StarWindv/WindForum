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


let cachedPostsList = null;

async function renderPosts(postContainer, loader, fromHead = false) {
    let postsData;
    let error = false;
    try {
        postsData = await loader();
    } catch (error) {
        if (error instanceof UnAuthorizeError) {
            window.location.href = "/login";
        }
    }
    try {
        if (postsData.length > 1) {
            postsData.sort((a, b) => b.last_update_time - a.last_update_time);
        }
    } catch (e) {
        error = true;
    }
    const postsList = cachedPostsList || DOMBuilder.ul({
        className: "postsList",
        id: "postsList"
    });
    cachedPostsList ??= postsList;
    try {
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
                            return DOMBuilder.div({
                                html: marked.parse(formatCode(post.content))
                            });
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
                                                    text: post.user_name
                                                })
                                            ]
                                        });
                                    })(),

                                    DOMBuilder.text(" | "),

                                    DOMBuilder.span({
                                        text: `Publish: ${formatTimestamp(post.create_time)}`
                                    }),

                                    DOMBuilder.text(" | "),

                                    DOMBuilder.span({
                                        text: `Last Update: ${formatTimestamp(post.last_update_time)}`
                                    })
                                ]
                            });
                        })()
                    ]
                });
                if (fromHead) {
                    postsList.insertBefore(postItem, postsList.firstChild);
                } else {
                    postsList.append(postItem);
                }
            });

            setTimeout(() => {
                useStyle('pre code', hljs.highlightElement);
            }, 0);
        } else {
            error = true;
        }
    } catch (e) {
        error = true;
    }
    if (error) {
        const existingEmptyMessage = cachedPostsList.querySelector('.post:not([data-has-content])');
        if (!existingEmptyMessage) {
            const noPostsMessage = DOMBuilder.li({
                text: "暂无帖子",
                className: "post",
                id: "no-post",
                attrs: { "data-has-content": "false" }
            });
            postsList.appendChild(noPostsMessage);
        }
    } else {
        const _ = document.getElementById("no-post");
        if (_) {
            _.remove();
        }
    }
    if (!cachedPostsList.parentNode) {
        postsList.appendChild(footer());
        postContainer.appendChild(postsList);
    }
}
