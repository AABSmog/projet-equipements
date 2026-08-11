<!doctype html>
<html>
<head>
    <meta name="layout" content="admin"/>
    <title>Affectations</title>
    <asset:javascript src="affectationAutocomplete.js"/>
</head>
<body>
    <h1 class="text-xl font-bold text-gray-900 mb-4">Affectations</h1>

    <div class="bg-white border border-gray-200 px-3 py-2 mb-6">
        <h2 class="text-xs font-semibold text-gray-700 mb-2">Nouvelle affectation</h2>
        <form action="/admin/affectation/affecter" method="post" class="flex gap-2 items-end">
            <input type="hidden" name="_csrf" value="${session.csrfToken}"/>
            <div class="flex-1">
                <label class="block text-xs text-gray-500 mb-0.5">Type (filtre)</label>
                <select id="eqType" class="w-full px-2 py-1.5 border border-gray-300 text-xs focus:outline-none focus:border-gray-600">
                    <option value="">Tous les types</option>
                    <g:each var="t" in="${typeEquipementList}">
                        <option value="${t.id}">${t.nom}</option>
                    </g:each>
                </select>
            </div>
            <div class="flex-1">
                <label class="block text-xs text-gray-500 mb-0.5">Equipement</label>
                <div>
                    <input type="text" id="eqInput" placeholder="Tapez pour rechercher (type, N serie)..." autocomplete="off" class="w-full px-2 py-1.5 border border-gray-300 text-xs focus:outline-none focus:border-gray-600"/>
                    <div id="eqMenu" class="hidden border border-gray-300 bg-white shadow text-xs max-h-40 overflow-y-auto">
                        <div class="px-2 py-1.5 text-gray-400">Recherche...</div>
                    </div>
                    <input type="hidden" name="equipementId" id="eqId" required/>
                </div>
            </div>
            <div class="flex-1">
                <label class="block text-xs text-gray-500 mb-0.5">Personnel</label>
                <div>
                    <input type="text" id="persInput" placeholder="Tapez pour rechercher (nom, prenom)..." autocomplete="off" class="w-full px-2 py-1.5 border border-gray-300 text-xs focus:outline-none focus:border-gray-600"/>
                    <div id="persMenu" class="hidden border border-gray-300 bg-white shadow text-xs max-h-40 overflow-y-auto">
                        <div class="px-2 py-1.5 text-gray-400">Recherche...</div>
                    </div>
                    <input type="hidden" name="personnelId" id="persId" required/>
                </div>
            </div>
            <button type="submit" class="px-3 py-1.5 bg-maroon text-white text-xs font-semibold hover:bg-red-900 transition-colors">Affecter</button>
        </form>
    </div>

    <p class="text-xs text-gray-500">Consultez l'historique des affectations via le menu « Historique des attributions ».</p>
</body>
</html>