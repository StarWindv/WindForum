async function loadChannel() {
    const response = await fetch(
        "/api/channel",
        { method: "GET" }
    );
    if (response.status===200) {
        return await response.json()
    } return [];
}

async function renderChannel() {
    const channelData = await loadChannel();
    const channelList = DOMBuilder.ul({
        className: "postsList"
    });
    if (channelData && channelData.length > 0) {
        channelData.forEach(channel => {
            const channelItem = DOMBuilder.li({
                className: "post channel",
                id: channel.channel_id,
                children: [
                    DOMBuilder.h2({
                        text: channel.channel_name
                    }),
                    (() => {
                        return DOMBuilder.div({
                            className: "channelDesc",
                            text: channel.description
                        });
                    })()]
            });
            channelList.append(channelItem);
        });
    } else {
        const noChannel = DOMBuilder.li({
            text: "暂无频道",
            className: "post"
        });
        channelList.append(noChannel)
    }
    channelContainer.append(channelList);
    whenClick();
}