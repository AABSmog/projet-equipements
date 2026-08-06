<!doctype html>
<html>
<head>
    <meta name="layout" content="admin"/>
    <title>Modifier type</title>
</head>
<body>
    <h1 class="text-xl font-bold text-gray-900 mb-6">Modifier le type</h1>
    <div class="bg-white border border-gray-200 p-6 max-w-md">
        <g:hasErrors bean="${typeEquipement}">
            <div class="bg-red-100 border border-red-200 px-4 py-3 mb-4 text-sm text-red-800">
                <g:eachError bean="${typeEquipement}"><g:message error="${it}"/><br/></g:eachError>
            </div>
        </g:hasErrors>
        <form action="/admin/typeEquipement/update" method="post">
            <input type="hidden" name="id" value="${typeEquipement.id}"/>
            <div class="mb-4">
                <label class="block text-sm font-semibold text-gray-700 mb-1">Nom</label>
                <input type="text" name="nom" value="${typeEquipement.nom}" required
                    class="w-full px-3 py-2 border border-gray-300 text-sm focus:outline-none focus:border-gray-600"/>
            </div>
            <div class="flex gap-3">
                <button type="submit" class="bg-maroon text-white px-4 py-2 text-sm font-semibold hover:bg-red-900 transition-colors">Enregistrer</button>
                <a href="/admin/typeEquipement/list" class="px-4 py-2 text-sm text-gray-600 hover:text-gray-900 border border-gray-300 hover:border-gray-400 transition-colors">Annuler</a>
            </div>
        </form>
    </div>
</body>
</html>