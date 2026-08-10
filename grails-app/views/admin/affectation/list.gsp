<!doctype html>
<html>
<head>
    <meta name="layout" content="admin"/>
    <title>Affectations</title>
</head>
<body>
    <h1 class="text-xl font-bold text-gray-900 mb-4">Affectations</h1>

    <div class="bg-white border border-gray-200 px-2 py-1.5 mb-4">
        <label for="globalSearch" class="block text-xs text-gray-500 mb-1">Recherche rapide (filtre les listes d'equipements et de personnel ci-dessous)</label>
        <div class="flex items-center gap-1.5">
            <input type="text" id="globalSearch" placeholder="Type, N serie, prenom ou nom..." autocomplete="off" onkeyup="filterDropdowns()" class="flex-1 px-2 py-1.5 border border-gray-300 text-xs focus:outline-none focus:border-gray-600"/>
            <button type="button" onclick="clearGlobalSearch()" class="px-3 py-1.5 text-xs text-gray-600 border border-gray-300 hover:border-gray-400 transition-colors">Effacer</button>
        </div>
    </div>

    <div class="bg-white border border-gray-200 px-3 py-2 mb-6">
        <h2 class="text-xs font-semibold text-gray-700 mb-2">Nouvelle affectation</h2>
        <form action="/admin/affectation/affecter" method="post" class="flex gap-2 items-end">
            <input type="hidden" name="_csrf" value="${session.csrfToken}"/>
            <div class="flex-1">
                <label class="block text-xs text-gray-500 mb-0.5">Equipement</label>
                <div>
                    <input type="text" id="eqInput" placeholder="Tapez pour rechercher..." autocomplete="off" onkeyup="eqFilter()" onfocus="eqOpen()" class="w-full px-2 py-1.5 border border-gray-300 text-xs focus:outline-none focus:border-gray-600"/>
                    <div id="eqMenu" class="hidden border border-gray-300 bg-white shadow text-xs max-h-40 overflow-y-auto">
                        <g:each in="${proj.equipment.Equipement.findAllByEtat(proj.equipment.EtatEquipement.DISPONIBLE, [sort: 'numeroSerie'])}" var="eq">
                            <div class="px-2 py-1.5 cursor-pointer hover:bg-gray-100 border-b border-gray-100 last:border-b-0" data-id="${eq.id}" data-search="${(eq.type?.nom + ' ' + eq.numeroSerie).toLowerCase()}" onclick="eqPick(this)">${eq.type?.nom} - ${eq.numeroSerie}</div>
                        </g:each>
                    </div>
                    <input type="hidden" name="equipementId" id="eqId" required/>
                </div>
            </div>
            <div class="flex-1">
                <label class="block text-xs text-gray-500 mb-0.5">Personnel</label>
                <div>
                    <input type="text" id="persInput" placeholder="Tapez pour rechercher..." autocomplete="off" onkeyup="persFilter()" onfocus="persOpen()" class="w-full px-2 py-1.5 border border-gray-300 text-xs focus:outline-none focus:border-gray-600"/>
                    <div id="persMenu" class="hidden border border-gray-300 bg-white shadow text-xs max-h-40 overflow-y-auto">
                        <g:each in="${proj.equipment.Personnel.list(sort: 'nom')}" var="p">
                            <div class="px-2 py-1.5 cursor-pointer hover:bg-gray-100 border-b border-gray-100 last:border-b-0" data-id="${p.id}" data-search="${(p.prenom + ' ' + p.nom).toLowerCase()}" onclick="persPick(this)">${p.prenom} ${p.nom}</div>
                        </g:each>
                    </div>
                    <input type="hidden" name="personnelId" id="persId" required/>
                </div>
            </div>
            <button type="submit" class="px-3 py-1.5 bg-maroon text-white text-xs font-semibold hover:bg-red-900 transition-colors">Affecter</button>
        </form>
    </div>
    <script>
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
    function menuTokens(menuId) {
        var local = document.getElementById(menuId === 'eqMenu' ? 'eqInput' : 'persInput').value.toLowerCase();
        var global = document.getElementById('globalSearch').value.toLowerCase();
        return (local + ' ' + global).split(/\s+/).filter(Boolean);
    }
    function menuFilter(menuId) {
        var tokens = menuTokens(menuId);
        var m = document.getElementById(menuId);
        var items = m.children;
        for (var i = 0; i < items.length; i++) {
            var s = items[i].getAttribute('data-search');
            var ok = true;
            for (var t = 0; t < tokens.length; t++) {
                if (s.indexOf(tokens[t]) === -1) { ok = false; break; }
            }
            items[i].style.display = ok ? '' : 'none';
        }
    }
    function filterDropdowns() { menuFilter('eqMenu'); menuFilter('persMenu'); }
    function clearGlobalSearch() {
        document.getElementById('globalSearch').value = '';
        document.getElementById('eqInput').value = '';
        document.getElementById('eqId').value = '';
        document.getElementById('persInput').value = '';
        document.getElementById('persId').value = '';
        filterDropdowns();
    }
    function eqFilter() { showDrop('eqInput','eqMenu'); menuFilter('eqMenu'); }
    function persFilter() { showDrop('persInput','persMenu'); menuFilter('persMenu'); }
    function eqOpen() { showDrop('eqInput','eqMenu'); }
    function persOpen() { showDrop('persInput','persMenu'); }
    function eqPick(el) { pick(el,'eqInput','eqId','eqMenu'); }
    function persPick(el) { pick(el,'persInput','persId','persMenu'); }
    function pick(el, inpId, hidId, menuId) {
        document.getElementById(inpId).value = el.textContent.trim();
        document.getElementById(hidId).value = el.getAttribute('data-id');
        document.getElementById(menuId).classList.add('hidden');
    }
    document.addEventListener('click', function(e) {
        if (!e.target.closest('#eqInput, #eqMenu')) { var m = document.getElementById('eqMenu'); m.classList.add('hidden'); m.style.position = ''; }
        if (!e.target.closest('#persInput, #persMenu')) { var m = document.getElementById('persMenu'); m.classList.add('hidden'); m.style.position = ''; }
    });
    </script>

    <p class="text-xs text-gray-500">Consultez l'historique des affectations via le menu « Historique des attributions ».</p>
</body>
</html>