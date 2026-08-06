<!doctype html>
<html>
<head>
    <meta name="layout" content="admin"/>
    <title>Modifier equipement</title>
</head>
<body>
    <h1 class="text-xl font-bold text-gray-900 mb-6">Modifier equipement</h1>
    <div class="bg-white border border-gray-200 p-6 max-w-lg">
        <g:hasErrors bean="${equipement}">
            <div class="bg-red-100 border border-red-200 px-4 py-3 mb-4 text-sm text-red-800">
                <g:eachError bean="${equipement}"><g:message error="${it}"/><br/></g:eachError>
            </div>
        </g:hasErrors>
            <form action="/admin/equipement/update" method="post">
            <input type="hidden" name="id" value="${equipement.id}"/>
            <div class="mb-4">
                <label class="block text-sm font-semibold text-gray-700 mb-1">Type</label>
                <select name="type.id" id="typeSelect" onchange="toggleNouveauType()" class="w-full px-3 py-2 border border-gray-300 text-sm focus:outline-none focus:border-gray-600">
                    <option value="">-- Selectionner --</option>
                    <g:each var="t" in="${typeEquipementList}">
                        <option value="${t.id}" ${t.id == equipement.type?.id ? 'selected' : ''}>${t.nom}</option>
                    </g:each>
                    <option value="autre">Autre...</option>
                </select>
            </div>
            <div class="mb-4 hidden" id="nouveauTypeDiv">
                <label class="block text-sm font-semibold text-gray-700 mb-1">Nouveau type <span class="text-red-500">*</span></label>
                <input type="text" name="nouveauType" id="nouveauType" class="w-full px-3 py-2 border border-gray-300 text-sm focus:outline-none focus:border-gray-600"/>
            </div>
            <script>
                function toggleNouveauType() {
                    var sel = document.getElementById('typeSelect');
                    var div = document.getElementById('nouveauTypeDiv');
                    if (sel.value === 'autre') {
                        div.classList.remove('hidden');
                    } else {
                        div.classList.add('hidden');
                    }
                }
            </script>
            <div class="mb-4">
                <label class="block text-sm font-semibold text-gray-700 mb-1">N° Serie</label>
                <div class="px-3 py-2 border border-gray-300 text-sm bg-gray-100 text-gray-600">${equipement?.numeroSerie}</div>
            </div>
            <div class="mb-4">
                <label class="block text-sm font-semibold text-gray-700 mb-1">Etat</label>
                <select name="etat" class="w-full px-3 py-2 border border-gray-300 text-sm focus:outline-none focus:border-gray-600">
                    <g:each var="e" in="${proj.equipment.EtatEquipement.values()}">
                        <option value="${e.name()}" ${e == equipement.etat ? 'selected' : ''}>${e.label}</option>
                    </g:each>
                </select>
            </div>
            <div class="mb-4">
                <label class="block text-sm font-semibold text-gray-700 mb-1">Description</label>
                <textarea name="description" rows="3" class="w-full px-3 py-2 border border-gray-300 text-sm focus:outline-none focus:border-gray-600">${equipement?.description?.encodeAsHTML()}</textarea>
            </div>
            <div class="flex gap-3">
                <button type="submit" class="bg-maroon text-white px-4 py-2 text-sm font-semibold hover:bg-red-900 transition-colors">Enregistrer</button>
                <a href="/admin/equipement/list" class="px-4 py-2 text-sm text-gray-600 hover:text-gray-900 border border-gray-300 hover:border-gray-400 transition-colors">Annuler</a>
            </div>
        </form>
    </div>
</body>
</html>