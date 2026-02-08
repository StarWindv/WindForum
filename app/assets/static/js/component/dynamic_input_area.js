// noinspection JSUnusedGlobalSymbols

function autoResizeHeight(textareaElement) {
    textareaElement.style.height = 'auto';
    textareaElement.style.height = (textareaElement.scrollHeight) + 'px';
}


function insertAfter(newElement, targetElement) {
    const parent = targetElement.parentElement;
    if (parent.lastChild === targetElement) {
        parent.appendChild(newElement);
    } else {
        parent.insertBefore(newElement, targetElement.nextSibling);
    }
}


function addArea(container) {
    const check = document.getElementById("post_content");
    if (check!=null && check.length!==0) { return; }
    container.prepend(DOMBuilder.div({
        className: "content-wrapper",
        id: "content-wrapper",
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
                notice(
                    {
                        title: "NOTICE",
                        message: "Please Enter the Title of the Post."
                    }
                );
                titleElement?.focus();
                return;
            }
            if (!content) {
                notice(
                    {
                        title: "NOTICE",
                        message: "Please Enter the Content of the Post."
                    }
                );
                contentElement?.focus();
                return;
            }
            const email = new Authorizer().getEmail();
            const postInfo = new PostDTO(
                email,
                title,
                content,
                localStorage.getItem("TargetChannelId")
            );
            let resp;
            try {
                resp = await new PostManager().uploadPost(postInfo);
            } catch (e) {
                notice(
                    {
                        title: "ERROR",
                        message: "Post Failed:<br>"+e
                    }
                );
                return;
            }
            if (resp.status) {
                /**
                 * Upload Successful Toast
                 * */
                notice(
                    {
                        title: "INFO",
                        message: "Post Successful:<br>"
                    }
                );

                await renderPosts(container, async () => {
                    return await new PostManager().getUserPosts(email, getUserLatest);
                }, true);
                titleElement.value = '';
                contentElement.value = '';
                autoResizeHeight(titleElement);
                autoResizeHeight(contentElement);
            }
            else {
                /**
                 * Upload Failed Toast
                 * */
                notice(
                    {
                        title: "ERROR",
                        message: "Post Failed:<br>" + (resp.message || "Unknown Error")
                    }
                );
            }
        }
    );
}
