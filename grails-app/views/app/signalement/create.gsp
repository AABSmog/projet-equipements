<!doctype html>
<html>
<head>
    <meta name="layout" content="app"/>
    <title>Signaler un probleme</title>
</head>
<body>
    <div class="max-w-lg">
        <a href="/app/equipement/show/${equipement.id}" class="text-sm text-gray-500 hover:text-gray-700">&larr; Retour</a>
        <h1 class="text-xl font-bold text-gray-900 mt-2 mb-6">Signaler un probleme</h1>
        <div class="bg-white border border-gray-200 p-5 mb-4">
            <div class="text-sm text-gray-600"><span class="font-semibold text-gray-900">${equipement.type?.nom}</span> - ${equipement.description?.encodeAsHTML()}</div>
        </div>
        <div class="bg-white border border-gray-200 p-6">
            <form action="/app/signalement/save" method="post">
                <input type="hidden" name="_csrf" value="${session.csrfToken}"/>
                <input type="hidden" name="equipementId" value="${equipement.id}"/>
                <div class="mb-4">
                    <label class="block text-sm font-semibold text-gray-700 mb-1">Type de probleme</label>
                    <select name="type" class="w-full px-3 py-2 border border-gray-300 text-sm focus:outline-none focus:border-gray-600">
                        <g:each var="t" in="${proj.equipment.TypeSignalement.values()}">
                            <option value="${t.name()}">${t.label}</option>
                        </g:each>
                    </select>
                </div>
                <div class="mb-4">
                    <label class="block text-sm font-semibold text-gray-700 mb-1">Description</label>
                    <textarea name="description" rows="4" required class="w-full px-3 py-2 border border-gray-300 text-sm focus:outline-none focus:border-gray-600"></textarea>
                </div>
                <div class="flex gap-3">
                    <button type="submit" class="bg-maroon text-white px-4 py-2 text-sm font-semibold hover:bg-red-900 transition-colors">Envoyer</button>
                    <a href="/app/equipement/show/${equipement.id}" class="px-4 py-2 text-sm text-gray-600 hover:text-gray-900 border border-gray-300 hover:border-gray-400 transition-colors">Annuler</a>
                </div>
            </form>
        </div>
    </div>
</body>
</html>