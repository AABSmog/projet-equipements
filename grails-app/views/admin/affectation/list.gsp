<!doctype html>
<html>
<head>
    <meta name="layout" content="admin"/>
    <title>Affectations</title>
</head>
<body>
    <h1 class="text-xl font-bold text-gray-900 mb-4">Affectations</h1>

    <div class="bg-white border border-gray-200 px-3 py-2 mb-6">
        <h2 class="text-xs font-semibold text-gray-700 mb-2">Nouvelle affectation</h2>
        <form action="/admin/affectation/affecter" method="post" class="flex gap-2 items-end">
            <input type="hidden" name="_csrf" value="${session.csrfToken}"/>
            <div class="flex-1">
                <label class="block text-xs text-gray-500 mb-0.5">Equipement</label>
                <div>
                    <input type="text" id="eqInput" placeholder="Tapez pour rechercher (type, N serie)..." autocomplete="off" onfocus="eqOpen()" oninput="eqSearch()" class="w-full px-2 py-1.5 border border-gray-300 text-xs focus:outline-none focus:border-gray-600"/>
                    <div id="eqMenu" class="hidden border border-gray-300 bg-white shadow text-xs max-h-40 overflow-y-auto">
                        <div class="px-2 py-1.5 text-gray-400">Recherche...</div>
                    </div>
                    <input type="hidden" name="equipementId" id="eqId" required/>
                </div>
            </div>
            <div class="flex-1">
                <label class="block text-xs text-gray-500 mb-0.5">Personnel</label>
                <div>
                    <input type="text" id="persInput" placeholder="Tapez pour rechercher (nom, prenom)..." autocomplete="off" onfocus="persOpen()" oninput="persSearch()" class="w-full px-2 py-1.5 border border-gray-300 text-xs focus:outline-none focus:border-gray-600"/>
                    <div id="persMenu" class="hidden border border-gray-300 bg-white shadow text-xs max-h-40 overflow-y-auto">
                        <div class="px-2 py-1.5 text-gray-400">Recherche...</div>
                    </div>
                    <input type="hidden" name="personnelId" id="persId" required/>
                </div>
            </div>
            <button type="submit" class="px-3 py-1.5 bg-maroon text-white text-xs font-semibold hover:bg-red-900 transition-colors">Affecter</button>
        </form>
    </div>

    <script>
    var eqTimer = null, persTimer = null;

    function showDrop(inpId, menuId) {
        var inp = document.getElementById(inpId);
        var m = document.getElementById(menuId);
        var r = inp.getBoundingClientRect();
        m.style.position = 'fixed';
        m.style.left = r.left + 'px';
        m.style.width = r.width + 'px';
        m.style.top = (r.bottom) + 'px';
        m.style.zIndex = '9999';
        m.classList.remove('hidden');
    }
    function hideDrop(menuId) {
        var m = document.getElementById(menuId);
        m.classList.add('hidden');
        m.style.position = '';
    }
    function renderItems(menuId, items, pickFn) {
        var m = document.getElementById(menuId);
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
    function pickItem(it, inpId, hidId, menuId) {
        document.getElementById(inpId).value = it.label;
        document.getElementById(hidId).value = it.id;
        hideDrop(menuId);
    }
    function eqSearch() {
        showDrop('eqInput', 'eqMenu');
        var q = document.getElementById('eqInput').value.trim();
        clearTimeout(eqTimer);
        eqTimer = setTimeout(function () {
            fetch('/admin/affectation/rechercherEquipements?q=' + encodeURIComponent(q))
                .then(function (r) { return r.json(); })
                .then(function (items) { renderItems('eqMenu', items, function (it) { pickItem(it, 'eqInput', 'eqId', 'eqMenu'); }); })
                .catch(function () {});
        }, 200);
    }
    function persSearch() {
        showDrop('persInput', 'persMenu');
        var q = document.getElementById('persInput').value.trim();
        clearTimeout(persTimer);
        persTimer = setTimeout(function () {
            fetch('/admin/affectation/rechercherPersonnel?q=' + encodeURIComponent(q))
                .then(function (r) { return r.json(); })
                .then(function (items) { renderItems('persMenu', items, function (it) { pickItem(it, 'persInput', 'persId', 'persMenu'); }); })
                .catch(function () {});
        }, 200);
    }
    function eqOpen() { eqSearch(); }
    function persOpen() { persSearch(); }

    document.addEventListener('click', function (e) {
        if (!e.target.closest('#eqInput, #eqMenu')) { hideDrop('eqMenu'); }
        if (!e.target.closest('#persInput, #persMenu')) { hideDrop('persMenu'); }
    });
    </script>

    <p class="text-xs text-gray-500">Consultez l'historique des affectations via le menu « Historique des attributions ».</p>
</body>
</html>