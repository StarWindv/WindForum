function loader(scripts_list, defer=false, after=null) {
    scripts_list.forEach(src => {
        const script = document.createElement('script');
        script.src = src;
        script.defer = defer;
        document.body.prepend(script);
    });
    if (typeof after == 'function') {
        window.addEventListener('load', function() {
            after();
        });
    }
}
