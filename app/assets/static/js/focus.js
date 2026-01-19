const originalTitle = document.title;
const blurTitle = "快回来哇 Σ(っ °Д °;)っ";
const welcomeTitle = "哇, 你回来啦!ヾ(≧▽≦*)o";

window.addEventListener(
    "blur", () => {
        document.title = blurTitle;
    }
);

window.addEventListener("focus", () => {
    document.title = welcomeTitle;
    setTimeout(() => {
        document.title = originalTitle;
    }, 1500);
});
