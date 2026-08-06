<!doctype html>
<html>
<head>
    <meta name="layout" content="admin"/>
    <title>Equipements</title>
</head>
<body>
    <div class="flex items-center justify-between mb-4">
        <h1 class="text-xl font-bold text-gray-900">Equipements</h1>
        <a href="/admin/equipement/create" class="bg-maroon text-white px-4 py-2 text-sm font-semibold hover:bg-red-900 transition-colors">Nouvel equipement</a>
    </div>
    <div class="bg-white border border-gray-200 px-2 py-1.5 mb-4">
        <form method="get" class="flex items-center gap-1.5">
            <input type="text" name="q" value="${params.q}" placeholder="N° serie, description, type..." class="flex-1 px-2 py-1.5 border border-gray-300 text-xs focus:outline-none focus:border-gray-600"/>
            <select name="etat" class="px-2 py-1.5 border border-gray-300 text-xs focus:outline-none focus:border-gray-600">
                <option value="">Tous</option>
                <g:each var="e" in="${proj.equipment.EtatEquipement.values()}">
                    <option value="${e.name()}" ${params.etat == e.name() ? 'selected' : ''}>${e.label}</option>
                </g:each>
            </select>
            <button type="submit" class="px-3 py-1.5 bg-gray-800 text-white text-xs font-semibold hover:bg-gray-700 transition-colors">Chercher</button>
            <g:if test="${params.q || params.etat}"><a href="/admin/equipement/list" class="px-3 py-1.5 text-xs text-gray-600 border border-gray-300 hover:border-gray-400 transition-colors">Effacer</a></g:if>
        </form>
    </div>
    <div class="bg-white border border-gray-200 overflow-hidden">
        <table class="w-full text-sm">
            <thead>
                <tr class="bg-gray-100 text-left">
                    
                    <th class="px-4 py-3 font-semibold text-gray-700">Type</th>
                    <th class="px-4 py-3 font-semibold text-gray-700">N° Serie</th>
                    <th class="px-4 py-3 font-semibold text-gray-700">Description</th>
                    <th class="px-4 py-3 font-semibold text-gray-700">Etat</th>
                    <th class="px-4 py-3 font-semibold text-gray-700">Affecte a</th>
                    <th class="px-4 py-3 font-semibold text-gray-700">Actions</th>
                </tr>
            </thead>
            <tbody>
                <g:each var="eq" in="${equipementList}">
                    <tr class="border-t border-gray-200 hover:bg-gray-50">
                        
                        <td class="px-4 py-3 font-medium">${eq.type?.nom}</td>
                        <td class="px-4 py-3 text-gray-500">${eq.numeroSerie}</td>
                        <td class="px-4 py-3 text-gray-600">${eq.description?.encodeAsHTML()}</td>
                        <td class="px-4 py-3">
                            <span class="text-xs font-semibold px-2 py-1
                                ${eq.etat == proj.equipment.EtatEquipement.DISPONIBLE ? 'bg-green-100 text-green-800' :
                                  eq.etat == proj.equipment.EtatEquipement.AFFECTE ? 'bg-blue-100 text-blue-800' :
                                  eq.etat == proj.equipment.EtatEquipement.EN_PANNE ? 'bg-red-100 text-red-800' :
                                  eq.etat == proj.equipment.EtatEquipement.REPARE ? 'bg-yellow-100 text-yellow-800' :
                                  'bg-gray-100 text-gray-800'}">${eq.etat?.label}</span>
                        </td>
                        <td class="px-4 py-3 text-gray-600">
                            <g:if test="${eq.etat == proj.equipment.EtatEquipement.AFFECTE}">
                                ${proj.equipment.Affectation.findByEquipementAndDateRetourIsNull(eq)?.personnel?.prenom} ${proj.equipment.Affectation.findByEquipementAndDateRetourIsNull(eq)?.personnel?.nom}
                            </g:if>
                            <g:else><span class="text-gray-400">-</span></g:else>
                        </td>
                        <td class="px-4 py-3">
                            <a href="/admin/equipement/edit/${eq.id}" class="text-gray-600 hover:text-gray-900 mr-3">Modifier</a>
                            <g:if test="${eq.etat == proj.equipment.EtatEquipement.AFFECTE}">
                                <a href="/admin/equipement/desaffecter/${eq.id}" class="text-orange-600 hover:text-orange-800 mr-3">Desaffecter</a>
                            </g:if>
                            <a href="/admin/equipement/declasser/${eq.id}" class="text-red-600 hover:text-red-800">Declasser</a>
                        </td>
                    </tr>
                </g:each>
                <g:if test="${!equipementList}">
                    <tr><td colspan="7" class="px-4 py-8 text-center text-gray-400">Aucun equipement</td></tr>
                </g:if>
            </tbody>
        </table>
    </div>
</body>
</html>