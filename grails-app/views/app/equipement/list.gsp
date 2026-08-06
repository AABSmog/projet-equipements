<!doctype html>
<html>
<head>
    <meta name="layout" content="app"/>
    <title>Mes equipements</title>
</head>
<body>
    <h1 class="text-xl font-bold text-gray-900 mb-6">Mes equipements</h1>
    <div class="bg-white border border-gray-200 overflow-hidden">
        <table class="w-full text-sm">
            <thead>
                <tr class="bg-gray-100 text-left">
                    <th class="px-4 py-3 font-semibold text-gray-700">Equipement</th>
                    <th class="px-4 py-3 font-semibold text-gray-700">N° Serie</th>
                    <th class="px-4 py-3 font-semibold text-gray-700">Description</th>
                    <th class="px-4 py-3 font-semibold text-gray-700">Affecte le</th>
                    <th class="px-4 py-3 font-semibold text-gray-700">Etat</th>
                    <th class="px-4 py-3 font-semibold text-gray-700">Actions</th>
                </tr>
            </thead>
            <tbody>
                <g:each var="a" in="${affectationList}">
                    <tr class="border-t border-gray-200 hover:bg-gray-50">
                        <td class="px-4 py-3 font-medium">${a.equipement?.type?.nom ?: a.infoEquipement} </td>
                        <td class="px-4 py-3 text-gray-500">${a.equipement?.numeroSerie}</td>
                        <td class="px-4 py-3 text-gray-600 max-w-xs truncate">${a.equipement?.description?.encodeAsHTML()}</td>
                        <td class="px-4 py-3 text-gray-600"><g:formatDate format="dd/MM/yyyy" date="${a.dateAffectation}" /></td>
                        <td class="px-4 py-3">
                            <span class="text-xs font-semibold px-2 py-1 ${a.equipement?.etat == proj.equipment.EtatEquipement.DISPONIBLE ? 'bg-green-100 text-green-800' : a.equipement?.etat == proj.equipment.EtatEquipement.AFFECTE ? 'bg-blue-100 text-blue-800' : a.equipement?.etat == proj.equipment.EtatEquipement.EN_PANNE ? 'bg-red-100 text-red-800' : 'bg-yellow-100 text-yellow-800'}">${a.equipement?.etat?.label}</span>
                        </td>
                        <td class="px-4 py-3">
                            <g:if test="${a.equipement}">
                                <a href="/app/equipement/show/${a.equipement.id}" class="text-blue-600 hover:text-blue-800 text-sm font-semibold">Voir</a>
                                <a href="/app/signalement/create/${a.equipement.id}" class="ml-3 text-red-600 hover:text-red-800 text-sm font-semibold">Signaler</a>
                                <form action="/app/equipement/retour/${a.equipement.id}" method="post" class="inline ml-3" onsubmit="return confirm('Retourner cet equipement ?')">
                                    <input type="hidden" name="raisonRetour" value="Retourne par l'employe"/>
                                    <button type="submit" class="text-green-600 hover:text-green-800 text-sm font-semibold bg-transparent border-0 p-0 cursor-pointer">Retourner</button>
                                </form>
                            </g:if>
                        </td>
                    </tr>
                </g:each>
                <g:if test="${!affectationList}">
                    <tr><td colspan="6" class="px-4 py-8 text-center text-gray-400">Aucun equipement ne vous est affecte</td></tr>
                </g:if>
            </tbody>
        </table>
    </div>
</body>
</html>