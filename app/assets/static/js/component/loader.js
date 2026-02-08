function loader(
    scripts_list,
    defer=false,
    after=null
) {
    let ele;
    scripts_list.forEach(src => {
        if (src.endsWith("js")) {
            ele = document.createElement('script');
            ele.defer = defer;
            ele.src = src;
        } else if (src.endsWith("css")) {
            ele = document.createElement('link');
            ele.href = src;
            ele.rel = "stylesheet";
            ele.type = "text/css";
        }
        document.body.prepend(ele);
    });
    if (typeof after == 'function') {
        window.addEventListener('load', function() {
            after();
        });
    }
}
