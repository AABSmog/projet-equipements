(function () {
    function ready(fn) {
        if (document.readyState !== 'loading') { fn(); }
        else { document.addEventListener('DOMContentLoaded', fn); }
    }

    ready(function () {
        function hide(el) {
            el.style.transition = 'opacity .4s ease, transform .4s ease';
            el.style.opacity = '0';
            el.style.transform = 'translateY(-6px)';
            setTimeout(function () { el.remove(); }, 400);
        }
        document.querySelectorAll('.flash-alert').forEach(function (el) {
            var close = el.querySelector('.flash-close');
            if (close) { close.addEventListener('click', function () { hide(el); }); }
            if (el.classList.contains('flash-success')) { setTimeout(function () { hide(el); }, 8000); }
        });
    });
})();
