(function () {
    function ready(fn) {
        if (document.readyState !== 'loading') { fn(); }
        else { document.addEventListener('DOMContentLoaded', fn); }
    }

    ready(function () {
        var eqInput = document.getElementById('eqInput');
        var eqMenu = document.getElementById('eqMenu');
        var eqId = document.getElementById('eqId');
        var eqType = document.getElementById('eqType');
        var persInput = document.getElementById('persInput');
        var persMenu = document.getElementById('persMenu');
        var persId = document.getElementById('persId');
        var eqTimer = null;
        var persTimer = null;

        function showDrop(inp, m) {
            var r = inp.getBoundingClientRect();
            m.style.position = 'fixed';
            m.style.left = r.left + 'px';
            m.style.width = r.width + 'px';
            m.style.top = r.bottom + 'px';
            m.style.zIndex = '9999';
            m.classList.remove('hidden');
        }

        function hideDrop(m) {
            m.classList.add('hidden');
            m.style.position = '';
        }

        function renderItems(m, items, pickFn) {
            m.innerHTML = '';
            if (!items || items.length === 0) {
                var empty = document.createElement('div');
                empty.className = 'px-2 py-1.5 text-gray-400';
                empty.textContent = 'Aucun resultat';
                m.appendChild(empty);
                return;
            }
            items.forEach(function (it) {
                var d = document.createElement('div');
                d.className = 'px-2 py-1.5 cursor-pointer hover:bg-gray-100 border-b border-gray-100 last:border-b-0';
                d.setAttribute('data-id', it.id);
                d.textContent = it.label;
                d.addEventListener('click', function () { pickFn(it); });
                m.appendChild(d);
            });
        }

        function pickItem(it, inp, hid, m) {
            inp.value = it.label;
            hid.value = it.id;
            hideDrop(m);
        }

        function eqSearch() {
            showDrop(eqInput, eqMenu);
            var q = eqInput.value.trim();
            var typeId = eqType ? eqType.value : '';
            clearTimeout(eqTimer);
            eqTimer = setTimeout(function () {
                var url = '/admin/affectation/rechercherEquipements?q=' + encodeURIComponent(q);
                if (typeId) { url += '&typeId=' + encodeURIComponent(typeId); }
                fetch(url)
                    .then(function (r) { return r.json(); })
                    .then(function (items) { renderItems(eqMenu, items, function (it) { pickItem(it, eqInput, eqId, eqMenu); }); })
                    .catch(function () {});
            }, 200);
        }

        function persSearch() {
            showDrop(persInput, persMenu);
            var q = persInput.value.trim();
            clearTimeout(persTimer);
            persTimer = setTimeout(function () {
                fetch('/admin/affectation/rechercherPersonnel?q=' + encodeURIComponent(q))
                    .then(function (r) { return r.json(); })
                    .then(function (items) { renderItems(persMenu, items, function (it) { pickItem(it, persInput, persId, persMenu); }); })
                    .catch(function () {});
            }, 200);
        }

        eqInput.addEventListener('focus', eqSearch);
        eqInput.addEventListener('input', eqSearch);
        if (eqType) { eqType.addEventListener('change', eqSearch); }
        persInput.addEventListener('focus', persSearch);
        persInput.addEventListener('input', persSearch);

        document.addEventListener('click', function (e) {
            if (!e.target.closest('#eqInput, #eqMenu')) { hideDrop(eqMenu); }
            if (!e.target.closest('#persInput, #persMenu')) { hideDrop(persMenu); }
        });
    });
})();
