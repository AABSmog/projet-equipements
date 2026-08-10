<!doctype html>
<html>
<head>
    <meta name="layout" content="app"/>
    <title>Mes signalements</title>
</head>
<body>
    <h1 class="text-xl font-bold text-gray-900 mb-6">Mes signalements</h1>
    <div class="bg-white border border-gray-200 overflow-hidden">
        <table class="w-full text-sm">
            <thead>
                <tr class="bg-gray-100 text-left">
                    <th class="px-4 py-3 font-semibold text-gray-700">Equipement</th>
                    <th class="px-4 py-3 font-semibold text-gray-700">Type</th>
                    <th class="px-4 py-3 font-semibold text-gray-700">Description</th>
                    <th class="px-4 py-3 font-semibold text-gray-700">Date</th>
                </tr>
            </thead>
            <tbody>
                <g:each var="s" in="${signalementList}">
                    <tr class="border-t border-gray-200 hover:bg-gray-50">
                        <td class="px-4 py-3 font-medium">${s.equipement?.type?.nom ?: s.infoEquipement} </td>
                        <td class="px-4 py-3"><span class="text-xs font-semibold px-2 py-1 bg-red-100 text-red-800">${s.type?.label}</span></td>
                        <td class="px-4 py-3 text-gray-600">${s.description?.encodeAsHTML()}</td>
                        <td class="px-4 py-3 text-gray-500"><g:formatDate format="dd/MM/yyyy" date="${s.dateCreated}" /></td>
                    </tr>
                </g:each>
                <g:if test="${!signalementList}">
                    <tr><td colspan="4" class="px-4 py-8 text-center text-gray-400">Aucun signalement</td></tr>
                </g:if>
            </tbody>
        </table>
    </div>
    <g:render template="/shared/pagination" model="[total: total, max: max, offset: offset]"/>
</body>
</html>
