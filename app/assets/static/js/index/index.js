const channelContainer = document.getElementById("channelContainer");
const container = document.getElementById("postContainer");

let TargetChannelId = null;

function whenClick() {
    const channels = document.querySelectorAll(".channel");
    channels.forEach(
        channel => {
            channel.addEventListener(
                "click",
                () => {
                    TargetChannelId = channel.id;
                    localStorage.setItem("TargetChannelId", TargetChannelId);
                    channelContainer.classList.add("hide");
                    addArea(container);
                    renderPosts(container, () => {
                        return loadPosts(TargetChannelId);
                    }).then(() => {
                    });
                }
            );
        }
    );
}

function create_menu() {
    const returnMethod = DOMBuilder.div({
        text: "返回频道",
        id: "menu_return"
    });
    const left_banner = document.getElementById("left_banner");
    left_banner.append(returnMethod);
    returnMethod.addEventListener(
        "click",
        () => {
            while (container.firstChild) {
                container.removeChild(container.firstChild);
            }
            cachedPostsList = null;
            channelContainer.classList.remove("hide");
            localStorage.removeItem("TargetChannelId");
        }
    );
}

function main() {
    create_menu();
    renderChannel().then(() => {});
    document.addEventListener('click', function (event) {
        const codeElement = event.target.closest('code');
        if (codeElement) {
            event.preventDefault();
            handleCodeClick(codeElement);
        }
    });
}

function handleCodeClick(codeElement) {
    const codeContent = codeElement.textContent;
    navigator.clipboard.writeText(codeContent)
        .then(() => {
            notice({ title: "INFO", message: 'Copy Successful' });
        })
        .catch(e => {
            notice({ title: "ERROR", message: 'Copy Failed' });
            console.log(e);
        });
}
async function loadPosts(channel_id = 0) {
    return await new PostManager().getPosts(
        {
            from: 1,
            to: 20,
            isArc: true,
            channel_id: channel_id
        }
    );
}
