function formatTimestamp(timestamp) {
    const d = new Date(timestamp);
    return `${d.getFullYear()}-${(d.getMonth()+1).toString().padStart(2,'0')}-${d.getDate().toString().padStart(2,'0')} ${d.getHours().toString().padStart(2,'0')}:${d.getMinutes().toString().padStart(2,'0')}`;
}


async function renderPosts(postContainer, loader) {
        let postsData;
        try {
            postsData = await loader();
        } catch (error) {
            if (error  instanceof UnAuthorizeError) {
                window.location.href = "/login";
            } throw error;
        }
        const postsList = document.createElement("ul");
        postsList.className = "postsList";

        if (postsData && postsData.length > 0) {
            postsData.forEach(post => {

                const postItem = document.createElement("li");
                postItem.className = "post";

                const titleElement = document.createElement("h2");
                titleElement.textContent = post.title;

                const contentElement = document.createElement("div");
                contentElement.textContent = post.content;

                const metaInfo = document.createElement("div");
                metaInfo.className = "post-meta";

                const authorElement = document.createElement("span");
                authorElement.textContent = `Author: `;
                authorElement.style.whiteSpace = "pre";
                const authorLink = document.createElement("a");
                authorLink.href = `/user/${post.email_str}`;
                authorLink.textContent = post.email_str;
                authorElement.appendChild(authorLink);

                const timeElement = document.createElement("span");
                timeElement.textContent = `Publish: ${formatTimestamp(post.create_time)}`;

                metaInfo.appendChild(authorElement);
                metaInfo.appendChild(document.createTextNode(" | "));
                metaInfo.appendChild(timeElement);

                const updateTimeElement = document.createElement("span");
                updateTimeElement.textContent = `Last Update: ${formatTimestamp(post.last_update_time)}`;
                metaInfo.appendChild(document.createTextNode(" | "));
                metaInfo.appendChild(updateTimeElement);

                postItem.appendChild(titleElement);
                postItem.appendChild(contentElement);
                postItem.appendChild(metaInfo);

                postsList.appendChild(postItem);
            });
        } else {
            const noPostsMessage = document.createElement("li");
            noPostsMessage.textContent = "暂无帖子";
            noPostsMessage.className = "no-posts";
            postsList.appendChild(noPostsMessage);
        }

        postContainer.appendChild(postsList);
    }