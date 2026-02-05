function autoResizeHeight(textareaElement) {
    textareaElement.style.height = 'auto';
    textareaElement.style.height = (textareaElement.scrollHeight) + 'px';
}

function addArea(container) {
    container.append(DOMBuilder.label({
        children: [
            DOMBuilder.create(
                "textarea",
                {
                    id: "post_title",
                    name: "post_title",
                    autocomplete: "on",
                    placeholder: "帖子标题"
                }
            )
        ]
    }));
    container.append(DOMBuilder.div({
        className: "content-wrapper",
        children: [
            DOMBuilder.label({
                children: [
                    DOMBuilder.create("textarea", {
                        id: "post_content",
                        name: "post_content",
                        autocomplete: "on",
                        placeholder: "帖子正文"
                    }),
                    DOMBuilder.create("button", {
                        text: "Upload",
                        type: "button",
                        id: "upload_post"
                    })
                ]
            })
        ]
    }));
}

function install() {
    const postTitle = document.getElementById('post_title');
    const postContent = document.getElementById('post_content');

    (()=>{
        autoResizeHeight(postTitle);
        autoResizeHeight(postContent);
    })();
    postTitle.addEventListener('input', function () {
        autoResizeHeight(this);
    });
    postContent.addEventListener('input', function () {
        autoResizeHeight(this);
    });
}
