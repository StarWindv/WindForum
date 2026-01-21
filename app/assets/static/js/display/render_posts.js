function formatTimestamp(timestamp) {
    const d = new Date(timestamp);
    return `${d.getFullYear()}-${(d.getMonth()+1).toString().padStart(2,'0')}-${d.getDate().toString().padStart(2,'0')} ${d.getHours().toString().padStart(2,'0')}:${d.getMinutes().toString().padStart(2,'0')}`;
}

function formatCode(Content) {
  let normalized = Content.replace(/\\n/g, '\n');
  normalized = normalized.replace(
    /```(\w+)?\s*(.+?)```/gs,
    (match, lang, code) => {
      const cleanCode = code.trim().replace(/```/g, '');
      return `\n\`\`\`${lang || ''}\n${cleanCode}\n\`\`\`\n`;
    }
  );

  return normalized;
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
            if (error  instanceof UnAuthorizeError) {
                window.location.href = "/login";
            } throw error;
        }
        const postsList = document.createElement("ul");
        postsList.className = "postsList";

        if (postsData && postsData.length > 0) {
            // console.log(postsData, typeof postsData);
            postsData.forEach(post => {

                const postItem = document.createElement("li");
                postItem.className = "post";

                const titleElement = document.createElement("h2");
                titleElement.textContent = post.title;

                const contentElement = document.createElement("div");
                contentElement.innerHTML = marked.parse(formatCode(post.content));

                /** 高亮 */
                contentElement.querySelectorAll('pre code').forEach(block => {
                    hljs.highlightElement(block);
                });
                // console.log(contentElement.innerHTML);

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
