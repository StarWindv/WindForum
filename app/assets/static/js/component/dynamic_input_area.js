function autoResizeHeight(textareaElement) {
    textareaElement.style.height = 'auto';
    textareaElement.style.height = (textareaElement.scrollHeight) + 'px';
}


function addArea(container) {
    const check = document.getElementById("post_content");
    if (check!=null && check.length!==0) { return; }
    container.prepend(DOMBuilder.div({
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
    container.prepend(DOMBuilder.label({
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
    installAnimation();
    bindPostMethod();
}


function installAnimation() {
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


function bindPostMethod() {
    const upload_post_button = document.getElementById("upload_post");
    upload_post_button.addEventListener(
        "click",
        async() => {
            /**
             * obtain post data:
             *  - title   ( from: textarea )
             *  - content ( from: textarea )
             *  - email   ( from: new Authorizer().getEmail() )
             *  - channel_id ( from: localStorage, key: TargetChannelId)
             * */

            const titleElement = document.getElementById('post_title');
            const contentElement = document.getElementById('post_content');

            const title = titleElement ? titleElement.value.trim() : '';
            const content = contentElement ? contentElement.value.trim() : '';
            if (!title) {
                alert('请填写帖子标题');
                titleElement?.focus();
                return;
            }
            if (!content) {
                alert('请填写帖子正文');
                contentElement?.focus();
                return;
            }
            const postInfo = new PostDTO(
                new Authorizer().getEmail(),
                title,
                content,
                localStorage.getItem("TargetChannelId")
            );

            const resp = await new PostManager().uploadPost(postInfo);
            if (resp.status) {
                /**
                 * Upload Successful Toast
                 * */
                alert('True');
                titleElement.value = '';
                contentElement.value = '';
                autoResizeHeight(titleElement);
                autoResizeHeight(contentElement);
            }
            else {
                /**
                 * Upload Failed Toast
                 * */
                alert('Failed: ' + (resp.message || '未知错误'));
            }
        }
    );
}
