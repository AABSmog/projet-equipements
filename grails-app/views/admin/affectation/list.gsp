<!doctype html>
<html>
<head>
    <meta name="layout" content="admin"/>
    <title>Affectations</title>
</head>
<body>
    <h1 class="text-xl font-bold text-gray-900 mb-4">Affectations</h1>

    <div class="bg-white border border-gray-200 px-2 py-1.5 mb-4">
        <form method="get" class="flex items-center gap-1.5">
            <input type="text" name="q" value="${params.q}" placeholder="N° serie ou nom du personnel..." class="flex-1 px-2 py-1.5 border border-gray-300 text-xs focus:outline-none focus:border-gray-600"/>
            <button type="submit" class="px-3 py-1.5 bg-gray-800 text-white text-xs font-semibold hover:bg-gray-700 transition-colors">Chercher</button>
            <g:if test="${params.q}"><a href="/admin/affectation/list" class="px-3 py-1.5 text-xs text-gray-600 border border-gray-300 hover:border-gray-400 transition-colors">Effacer</a></g:if>
        </form>
    </div>

    <div class="bg-white border border-gray-200 px-3 py-2 mb-6">
        <h2 class="text-xs font-semibold text-gray-700 mb-2">Nouvelle affectation</h2>
        <form action="/admin/affectation/affecter" method="post" class="flex gap-2 items-end">
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
    function eqFilter() { showDrop('eqInput','eqMenu'); filterDrop('eqMenu'); }
    function persFilter() { showDrop('persInput','persMenu'); filterDrop('persMenu'); }
    function eqOpen() { showDrop('eqInput','eqMenu'); }
    function persOpen() { showDrop('persInput','persMenu'); }
    function eqPick(el) { pick(el,'eqInput','eqId','eqMenu'); }
    function persPick(el) { pick(el,'persInput','persId','persMenu'); }
    function filterDrop(menuId) {
        var q = document.getElementById(menuId === 'eqMenu' ? 'eqInput' : 'persInput').value.toLowerCase();
        var m = document.getElementById(menuId);
        var items = m.children;
        for (var i = 0; i < items.length; i++) {
            items[i].style.display = items[i].getAttribute('data-search').indexOf(q) > -1 ? '' : 'none';
        }
    }
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

    <div class="bg-white border border-gray-200 overflow-hidden">
        <table class="w-full text-sm">
            <thead>
                <tr class="bg-gray-100 text-left">
                    
                    <th class="px-4 py-3 font-semibold text-gray-700">Equipement</th>
                    <th class="px-4 py-3 font-semibold text-gray-700">N° Serie</th>
                    <th class="px-4 py-3 font-semibold text-gray-700">Affecte a</th>
                    <th class="px-4 py-3 font-semibold text-gray-700">Date affectation</th>
                    <th class="px-4 py-3 font-semibold text-gray-700">Etat</th>
                    <th class="px-4 py-3 font-semibold text-gray-700">Date retour</th>
                    <th class="px-4 py-3 font-semibold text-gray-700">Raison</th>
                    <th class="px-4 py-3 font-semibold text-gray-700">Actions</th>
                </tr>
            </thead>
            <tbody>
                <g:each var="a" in="${affectationList}">
                    <tr class="border-t border-gray-200 hover:bg-gray-50">
                        
                        <td class="px-4 py-3">${a.equipement?.type?.nom ?: a.infoEquipement} </td>
                        <td class="px-4 py-3 text-gray-500">${a.equipement?.numeroSerie ?: '-'}</td>
                        <td class="px-4 py-3">${a.personnel?.prenom} ${a.personnel?.nom}</td>
                        <td class="px-4 py-3 text-gray-600"><g:formatDate format="dd/MM/yyyy" date="${a.dateAffectation}" /></td>
                        <td class="px-4 py-3">
                            <g:if test="${a.equipement}">
                                <span class="text-xs font-semibold px-2 py-1
                                    ${a.equipement.etat == proj.equipment.EtatEquipement.DISPONIBLE ? 'bg-green-100 text-green-800' :
                                      a.equipement.etat == proj.equipment.EtatEquipement.AFFECTE ? 'bg-blue-100 text-blue-800' :
                                      a.equipement.etat == proj.equipment.EtatEquipement.EN_PANNE ? 'bg-red-100 text-red-800' :
                                      a.equipement.etat == proj.equipment.EtatEquipement.REPARE ? 'bg-yellow-100 text-yellow-800' :
                                      'bg-gray-100 text-gray-800'}">${a.equipement.etat?.label}</span>
                            </g:if>
                            <g:else><span class="text-xs font-semibold px-2 py-1 bg-gray-100 text-gray-800">-</span></g:else>
                        </td>
                        <td class="px-4 py-3">
                            <g:if test="${a.dateRetour}">
                                <span class="text-xs font-semibold px-2 py-1 bg-green-100 text-green-800"><g:formatDate format="dd/MM/yyyy" date="${a.dateRetour}" /></span>
                            </g:if>
                            <g:else><span class="text-xs font-semibold px-2 py-1 bg-red-100 text-red-800">En cours</span></g:else>
                        </td>
                        <td class="px-4 py-3 text-gray-500 max-w-xs truncate">${a.raisonRetour?.encodeAsHTML() ?: '-'}</td>
                        <td class="px-4 py-3">
                            <g:if test="${!a.dateRetour}">
                                <form action="/admin/affectation/retour/${a.id}" method="post" class="flex gap-2 items-center" onsubmit="return confirm('Enregistrer le retour ?')">
                                    <input type="text" name="raisonRetour" placeholder="Raison du retour" required class="px-2 py-1 border border-gray-300 text-xs w-32 rounded-none focus:outline-none focus:border-gray-600"/>
                                    <button type="submit" class="px-3 py-1 text-xs bg-green-600 text-white font-semibold hover:bg-green-700 rounded-none">Retour</button>
                                </form>
                            </g:if>
                        </td>
                    </tr>
                </g:each>
                <g:if test="${!affectationList}">
                    <tr><td colspan="8" class="px-4 py-8 text-center text-gray-400">Aucune affectation</td></tr>
                </g:if>
            </tbody>
        </table>
    </div>
</body>
</html>