<!doctype html>
<html>
<head>
    <meta name="layout" content="admin"/>
    <title>Types d'equipement</title>
</head>
<body>
    <div class="flex items-center justify-between mb-4">
        <h1 class="text-xl font-bold text-gray-900">Types d'equipement</h1>
    </div>
    <div class="bg-white border border-gray-200 px-2 py-1.5 mb-4">
        <form method="get" class="flex items-center gap-1.5">
            <input type="text" name="q" value="${params.q}" placeholder="Nom..." class="flex-1 px-2 py-1.5 border border-gray-300 text-xs focus:outline-none focus:border-gray-600"/>
            <button type="submit" class="px-3 py-1.5 bg-gray-800 text-white text-xs font-semibold hover:bg-gray-700 transition-colors">Chercher</button>
            <g:if test="${params.q}"><a href="/admin/typeEquipement/list" class="px-3 py-1.5 text-xs text-gray-600 border border-gray-300 hover:border-gray-400 transition-colors">Effacer</a></g:if>
        </form>
    </div>
    <div class="bg-white border border-gray-200 overflow-hidden">
        <table class="w-full text-sm">
            <thead>
                <tr class="bg-gray-100 text-left">
                    
                    <th class="px-4 py-3 font-semibold text-gray-700">Nom</th>
                    <th class="px-4 py-3 font-semibold text-gray-700">Actions</th>
                </tr>
            </thead>
                <tbody>
                <g:each var="t" in="${typeEquipementList}">
                    <tr class="border-t border-gray-200 hover:bg-gray-50">
                        
                        <td class="px-4 py-3 font-medium">${t.nom}</td>
                        <td class="px-4 py-3 text-gray-400 text-xs">(cree via equipement)</td>
                    </tr>
                </g:each>
                <g:if test="${!typeEquipementList}">
                    <tr><td colspan="2" class="px-4 py-8 text-center text-gray-400">Aucun type</td></tr>
                </g:if>
                </tbody>
        </table>
    </div>
</body>
</html>