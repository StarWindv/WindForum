const originalTitle = document.title;
const blurTitle = "快回来哇 Σ(っ °Д °;)っ";

window.addEventListener(
    "blur", () => {
        document.title = blurTitle;
    }
);

window.addEventListener(
    "focus", () => {
        document.title = originalTitle;
    }
);

body {
    cursor: url('/static/image/cursor/cursor_arrow.png'), auto;
}
