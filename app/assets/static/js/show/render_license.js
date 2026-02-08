async function loadLicense() {
    return await (
        await fetch(
            "/static/text/LICENSE.txt",
            {
                method: "GET"
            }
        )
    ).text();
}

function formatLicenseText(text) {
    const lines = text.split('\n');
    let html = '';
    let currentClause = null;

    html += '<style>body {counter-reset: clause;}</style>';
    let count = 0;
    lines.forEach((line, _) => {
        const trimmedLine = line.trim();

        if (!trimmedLine) return;

        const clauseMatch = trimmedLine.match(/^第([零一二三四五六七八九十]+)条\s+(.+)/);
        if (clauseMatch) {
            if (currentClause) {
                html += '</div>';
            }
            currentClause = {
                number: clauseMatch[1],
                title: clauseMatch[2]
            };
            html += `
                                <div class="license-clause">
                                    <h2 class="clause-title">第${currentClause.number}条 ${currentClause.title}</h2>
                                    <div class="clause-content">
                            `;
            return;
        }

        const subClauseMatch = trimmedLine.match(/^(\d+\.\d+)\s+(.+)/);
        if (subClauseMatch && currentClause) {
            html += `
                                <div class="sub-clause">
                                    <strong>${subClauseMatch[1]}</strong> ${subClauseMatch[2]}
                                </div>
                            `;
            return;
        }

        const letterSubClauseMatch = trimmedLine.match(/^\(([a-z])\)\s+(.+)/);
        if (letterSubClauseMatch && currentClause) {
            html += `
                                <div class="sub-clause" style="margin-left: 40px;">
                                    <strong>(${letterSubClauseMatch[1]})</strong> ${letterSubClauseMatch[2]}
                                </div>
                            `;
            return;
        }

        if (trimmedLine.includes('申诉') || trimmedLine.includes('删除') || trimmedLine.includes('免责')) {
            html += `<div class="highlight">${trimmedLine}</div>`;
            return;
        }

        if (currentClause) {
            html += `<p>${trimmedLine}</p>`;
        } else {
            if (count===0) {
                const header = document.getElementById("header");
                const a = document.createElement("a");
                a.href = "/static/text/LICENSE.txt";
                a.tips = "点击这里查看协议原文件";
                const title = document.createElement("h1");
                title.className = "title";
                title.textContent = trimmedLine;
                a.appendChild(title);
                header.prepend(a);
            }
            count++;
        }
    });

    if (currentClause) {
        html += '</div></div>';
    }

    return html;
}

function bindTooltipEvents() {
    const tooltip = document.querySelector('.tooltip');
    if (!tooltip) return;

    const headerLinks = document.querySelectorAll('.header a');
    headerLinks.forEach(link => {
        link.removeEventListener('mouseenter', handleMouseEnter);
        link.removeEventListener('mousemove', updateTooltipPosition);
        link.removeEventListener('mouseleave', handleMouseLeave);

        link.addEventListener('mouseenter', handleMouseEnter);
        link.addEventListener('mousemove', updateTooltipPosition);
        link.addEventListener('mouseleave', handleMouseLeave);
    });

    function handleMouseEnter(e) {
        const tooltipText = this.tips;
        if (!tooltipText) return;

        tooltip.textContent = tooltipText;
        tooltip.style.display = 'block';
        updateTooltipPosition.call(this, e);
    }

    function handleMouseLeave() {
        tooltip.style.display = 'none';
    }

    function updateTooltipPosition(e) {
        const x = e.clientX + 15;
        const y = e.clientY + 15;

        tooltip.style.left = `${x}px`;
        tooltip.style.top = `${y}px`;
    }
}

document.addEventListener('DOMContentLoaded', async () => {
    const tooltip = document.createElement('div');
    tooltip.className = 'tooltip';
    document.body.appendChild(tooltip);

    const licenseText = await loadLicense();
    const licenseContainer = document.getElementById('LICENSE');

    if (licenseText.startsWith('用户生成内容')) {
        licenseContainer.innerHTML = formatLicenseText(licenseText);
    } else {
        licenseContainer.innerHTML = `<div class="clause-content">${licenseText}</div>`;
    }

    bindTooltipEvents();
});
