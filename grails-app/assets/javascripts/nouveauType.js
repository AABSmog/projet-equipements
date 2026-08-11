(function () {
    function ready(fn) {
        if (document.readyState !== 'loading') { fn(); }
        else { document.addEventListener('DOMContentLoaded', fn); }
    }

    ready(function () {
        var sel = document.getElementById('typeSelect');
        var div = document.getElementById('nouveauTypeDiv');
        if (!sel || !div) { return; }
        function toggle() {
            if (sel.value === 'autre') { div.classList.remove('hidden'); }
            else { div.classList.add('hidden'); }
        }
        sel.addEventListener('change', toggle);
        toggle();
    });
})();
