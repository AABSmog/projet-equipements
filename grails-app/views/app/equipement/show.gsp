<!doctype html>
<html>
<head>
    <meta name="layout" content="app"/>
    <title>Equipement</title>
</head>
<body>
    <div class="mb-4">
        <a href="/app/equipement/list" class="text-sm text-gray-500 hover:text-gray-700">&larr; Retour a mes equipements</a>
    </div>
    <h1 class="text-xl font-bold text-gray-900 mb-6">${equipement?.type?.nom} - ${equipement?.numeroSerie}</h1>

    <div class="grid grid-cols-1 md:grid-cols-2 gap-6 mb-8">
        <div class="bg-white border border-gray-200 p-4">
            <h2 class="text-sm font-semibold text-gray-700 mb-3">Informations</h2>
            <dl class="text-sm">
                <dt class="text-gray-500 font-medium">Type</dt>
                <dd class="text-gray-900 mb-2">${equipement?.type?.nom}</dd>
                <dt class="text-gray-500 font-medium">N° Serie</dt>
                <dd class="text-gray-900 mb-2">${equipement?.numeroSerie}</dd>
                <dt class="text-gray-500 font-medium">Description</dt>
                <dd class="text-gray-900 mb-2">${equipement?.description?.encodeAsHTML()}</dd>
                <dt class="text-gray-500 font-medium">Etat</dt>
                <dd class="mb-2">
                    <span class="text-xs font-semibold px-2 py-1 ${equipement?.etat == proj.equipment.EtatEquipement.DISPONIBLE ? 'bg-green-100 text-green-800' : equipement?.etat == proj.equipment.EtatEquipement.AFFECTE ? 'bg-blue-100 text-blue-800' : equipement?.etat == proj.equipment.EtatEquipement.EN_PANNE ? 'bg-red-100 text-red-800' : 'bg-yellow-100 text-yellow-800'}">${equipement?.etat?.label}</span>
                </dd>
            </dl>
            <a href="/app/signalement/create/${equipement.id}" class="inline-block mt-3 px-4 py-2 bg-red-600 text-white text-sm font-semibold hover:bg-red-700 rounded-none">Signaler un probleme</a>
        </div>
    </div>

    <h2 class="text-lg font-bold text-gray-900 mb-4">Signalements</h2>
    <div class="bg-white border border-gray-200 overflow-hidden">
        <table class="w-full text-sm">
            <thead>
                <tr class="bg-gray-100 text-left">
                    <th class="px-4 py-3 font-semibold text-gray-700">Date</th>
                    <th class="px-4 py-3 font-semibold text-gray-700">Type</th>
                    <th class="px-4 py-3 font-semibold text-gray-700">Description</th>
                </tr>
            </thead>
            <tbody>
                <g:each var="s" in="${signalementList}">
                    <tr class="border-t border-gray-200 hover:bg-gray-50">
                        <td class="px-4 py-3 text-gray-500"><g:formatDate format="dd/MM/yyyy" date="${s.dateCreated}" /></td>
                        <td class="px-4 py-3"><span class="text-xs font-semibold px-2 py-1 bg-red-100 text-red-800">${s.type?.label}</span></td>
                        <td class="px-4 py-3 text-gray-600">${s.description?.encodeAsHTML()}</td>
                    </tr>
                </g:each>
                <g:if test="${!signalementList}">
                    <tr><td colspan="3" class="px-4 py-8 text-center text-gray-400">Aucun signalement pour cet equipement</td></tr>
                </g:if>
            </tbody>
        </table>
    </div>
</body>
</html>