<!doctype html>
<html>
<head>
    <meta name="layout" content="admin"/>
    <title>Historique des attributions</title>
</head>
<body>
    <h1 class="text-xl font-bold text-gray-900 mb-1">Historique des attributions</h1>
    <p class="text-sm text-gray-500 mb-4">Qui a attribue quel equipement, a qui et quand.</p>

    <div class="bg-white border border-gray-200 px-2 py-1.5 mb-4">
        <form method="get" class="flex items-center gap-1.5">
            <input type="text" name="q" value="${params.q}" placeholder="N° serie, equipement, personnel ou administrateur..." class="flex-1 px-2 py-1.5 border border-gray-300 text-xs focus:outline-none focus:border-gray-600"/>
            <button type="submit" class="px-3 py-1.5 bg-gray-800 text-white text-xs font-semibold hover:bg-gray-700 transition-colors">Chercher</button>
            <g:if test="${params.q}"><a href="/admin/affectation/historique" class="px-3 py-1.5 text-xs text-gray-600 border border-gray-300 hover:border-gray-400 transition-colors">Effacer</a></g:if>
        </form>
    </div>

    <div class="bg-white border border-gray-200 overflow-hidden">
        <table class="w-full text-sm">
            <thead>
                <tr class="bg-gray-100 text-left">
                    <th class="px-4 py-3 font-semibold text-gray-700">Equipement</th>
                    <th class="px-4 py-3 font-semibold text-gray-700">N° Serie</th>
                    <th class="px-4 py-3 font-semibold text-gray-700">Attribue a</th>
                    <th class="px-4 py-3 font-semibold text-gray-700">Attribue par</th>
                    <th class="px-4 py-3 font-semibold text-gray-700">Date affectation</th>
                    <th class="px-4 py-3 font-semibold text-gray-700">Date retour</th>
                    <th class="px-4 py-3 font-semibold text-gray-700">Raison du retour</th>
                    <th class="px-4 py-3 font-semibold text-gray-700">Statut</th>
                </tr>
            </thead>
            <tbody>
                <g:each var="a" in="${affectationList}">
                    <tr class="border-t border-gray-200 hover:bg-gray-50">
                        <td class="px-4 py-3">${a.equipement?.type?.nom ?: a.infoEquipement ?: '-'}</td>
                        <td class="px-4 py-3 text-gray-500">${a.equipement?.numeroSerie ?: '-'}</td>
                        <td class="px-4 py-3">${a.personnel?.prenom} ${a.personnel?.nom}</td>
                        <td class="px-4 py-3 text-gray-600">${a.attribuePar ? "${a.attribuePar.prenom} ${a.attribuePar.nom}" : '-'}</td>
                        <td class="px-4 py-3 text-gray-600"><g:formatDate format="dd/MM/yyyy HH:mm" date="${a.dateAffectation}" /></td>
                        <td class="px-4 py-3 text-gray-600">
                            <g:if test="${a.dateRetour}">
                                <g:formatDate format="dd/MM/yyyy HH:mm" date="${a.dateRetour}" />
                            </g:if>
                            <g:else><span class="text-gray-400">-</span></g:else>
                        </td>
                        <td class="px-4 py-3 text-gray-500 max-w-xs truncate">${a.raisonRetour?.encodeAsHTML() ?: '-'}</td>
                        <td class="px-4 py-3">
                            <g:if test="${a.dateRetour}">
                                <span class="text-xs font-semibold px-2 py-1 bg-green-100 text-green-800">Cloturee</span>
                            </g:if>
                            <g:else>
                                <span class="text-xs font-semibold px-2 py-1 bg-blue-100 text-blue-800">En cours</span>
                            </g:else>
                        </td>
                    </tr>
                </g:each>
                <g:if test="${!affectationList}">
                    <tr><td colspan="8" class="px-4 py-8 text-center text-gray-400">Aucune attribution dans l'historique</td></tr>
                </g:if>
            </tbody>
        </table>
    </div>
</body>
</html>
