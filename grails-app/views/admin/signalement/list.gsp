<!doctype html>
<html>
<head>
    <meta name="layout" content="admin"/>
    <title>Signalements</title>
</head>
<body>
    <h1 class="text-xl font-bold text-gray-900 mb-4">Signalements</h1>

    <div class="bg-white border border-gray-200 px-2 py-1.5 mb-4">
        <form method="get" class="flex items-center gap-1.5">
            <input type="text" name="q" value="${params.q}" placeholder="Description ou N° serie..." class="flex-1 px-2 py-1.5 border border-gray-300 text-xs focus:outline-none focus:border-gray-600"/>
            <button type="submit" class="px-3 py-1.5 bg-gray-800 text-white text-xs font-semibold hover:bg-gray-700 transition-colors">Chercher</button>
            <g:if test="${params.q}"><a href="/admin/signalement/list" class="px-3 py-1.5 text-xs text-gray-600 border border-gray-300 hover:border-gray-400 transition-colors">Effacer</a></g:if>
        </form>
    </div>
    <div class="bg-white border border-gray-200 overflow-hidden">
        <table class="w-full text-sm">
            <thead>
                <tr class="bg-gray-100 text-left">
                    
                    <th class="px-4 py-3 font-semibold text-gray-700">Equipement</th>
                    <th class="px-4 py-3 font-semibold text-gray-700">Signale par</th>
                    <th class="px-4 py-3 font-semibold text-gray-700">Type</th>
                    <th class="px-4 py-3 font-semibold text-gray-700">Description</th>
                    <th class="px-4 py-3 font-semibold text-gray-700">Date</th>
                    <th class="px-4 py-3 font-semibold text-gray-700">Actions</th>
                </tr>
            </thead>
            <tbody>
                <g:each var="s" in="${signalementList}">
                    <tr class="border-t border-gray-200 hover:bg-gray-50">
                        
                        <td class="px-4 py-3">${s.equipement?.type?.nom ?: s.infoEquipement} </td>
                        <td class="px-4 py-3">${s.personnel?.prenom} ${s.personnel?.nom}</td>
                        <td class="px-4 py-3"><span class="text-xs font-semibold px-2 py-1 bg-red-100 text-red-800">${s.type?.label}</span></td>
                        <td class="px-4 py-3 text-gray-600 max-w-xs truncate">${s.description?.encodeAsHTML()}</td>
                        <td class="px-4 py-3 text-gray-500"><g:formatDate format="dd/MM/yyyy" date="${s.dateCreated}" /></td>
                        <td class="px-4 py-3">
                            <form action="/admin/signalement/delete/${s.id}" method="post" class="inline">
                                <input type="hidden" name="_csrf" value="${session.csrfToken}"/>
                                <button type="submit" class="text-red-600 hover:text-red-800 text-sm bg-transparent border-0 p-0 cursor-pointer" onclick="return confirm('Supprimer ce signalement ?')">Supprimer</button>
                            </form>
                        </td>
                    </tr>
                </g:each>
                <g:if test="${!signalementList}">
                    <tr><td colspan="7" class="px-4 py-8 text-center text-gray-400">Aucun signalement</td></tr>
                </g:if>
            </tbody>
        </table>
    </div>
    <g:render template="/shared/pagination" model="[total: total, max: max, offset: offset]"/>
</body>
</html>