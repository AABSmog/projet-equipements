<!doctype html>
<html>
<head>
    <meta name="layout" content="admin"/>
    <title>Nouvel equipement</title>
</head>
<body>
    <h1 class="text-xl font-bold text-gray-900 mb-6">Nouvel equipement</h1>
    <div class="bg-white border border-gray-200 p-6 max-w-lg">
        <g:hasErrors bean="${equipement}">
            <div class="bg-red-100 border border-red-200 px-4 py-3 mb-4 text-sm text-red-800">
                <span class="font-semibold">Erreurs :</span><br/>
                <g:eachError bean="${equipement}"><g:message error="${it}"/><br/></g:eachError>
            </div>
        </g:hasErrors>
            <form action="/admin/equipement/save" method="post">
            <div class="mb-4">
                <label class="block text-sm font-semibold text-gray-700 mb-1">Type <span class="text-red-500">*</span></label>
                <select name="type.id" id="typeSelect" onchange="toggleNouveauType()" class="w-full px-3 py-2 border ${equipement?.errors?.getFieldErrors('type') ? 'border-red-500' : 'border-gray-300'} text-sm focus:outline-none focus:border-gray-600">
                    <option value="">-- Selectionner --</option>
                    <g:each var="t" in="${typeEquipementList}">
                        <option value="${t.id}">${t.nom}</option>
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
            <input type="hidden" name="etat" value="DISPONIBLE"/>
            <div class="mb-4">
                <label class="block text-sm font-semibold text-gray-700 mb-1">Description <span class="text-red-500">*</span></label>
                <textarea name="description" rows="3" class="w-full px-3 py-2 border ${equipement?.errors?.getFieldErrors('description') ? 'border-red-500' : 'border-gray-300'} text-sm focus:outline-none focus:border-gray-600">${equipement?.description}</textarea>
                <g:eachError bean="${equipement}" field="description"><p class="text-xs text-red-600 mt-1"><g:message error="${it}"/></p></g:eachError>
            </div>
            <div class="flex gap-3">
                <button type="submit" class="bg-maroon text-white px-4 py-2 text-sm font-semibold hover:bg-red-900 transition-colors">Creer</button>
                <a href="/admin/equipement/list" class="px-4 py-2 text-sm text-gray-600 hover:text-gray-900 border border-gray-300 hover:border-gray-400 transition-colors">Annuler</a>
            </div>
        </form>
    </div>
</body>
</html>