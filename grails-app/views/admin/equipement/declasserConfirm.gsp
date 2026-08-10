<!doctype html>
<html>
<head>
    <meta name="layout" content="admin"/>
    <title>Declasser equipement</title>
</head>
<body>
    <h1 class="text-xl font-bold text-gray-900 mb-6">Declasser un equipement</h1>
    <div class="bg-white border border-gray-200 p-6 max-w-lg">
        <div class="mb-4 p-4 bg-yellow-50 border border-yellow-200 text-sm text-yellow-800">
            <span class="font-semibold">Attention :</span> Vous allez declasser l'equipement ci-dessous. Cette action est irreversible.
        </div>
        <dl class="text-sm mb-4">
            <dt class="text-gray-500 font-medium">Type</dt>
            <dd class="text-gray-900 mb-2">${equipement.type?.nom}</dd>
            <dt class="text-gray-500 font-medium">N° Serie</dt>
            <dd class="text-gray-900 mb-2">${equipement.numeroSerie}</dd>
            <dt class="text-gray-500 font-medium">Description</dt>
            <dd class="text-gray-900 mb-2">${equipement.description?.encodeAsHTML()}</dd>
            <dt class="text-gray-500 font-medium">Etat actuel</dt>
            <dd class="mb-2"><span class="text-xs font-semibold px-2 py-1
                ${equipement.etat == proj.equipment.EtatEquipement.AFFECTE ? 'bg-blue-100 text-blue-800' :
                  equipement.etat == proj.equipment.EtatEquipement.DISPONIBLE ? 'bg-green-100 text-green-800' :
                  'bg-gray-100 text-gray-800'}">${equipement.etat?.label}</span></dd>
            <g:if test="${affectation}">
                <dt class="text-gray-500 font-medium">Actuellement affecte a</dt>
                <dd class="text-gray-900 mb-2">${affectation.personnel?.prenom} ${affectation.personnel?.nom}</dd>
            </g:if>
        </dl>
        <form action="/admin/equipement/declasser/${equipement.id}" method="post">
            <input type="hidden" name="_csrf" value="${session.csrfToken}"/>
            <div class="flex gap-3">
                <button type="submit" class="bg-red-600 text-white px-4 py-2 text-sm font-semibold hover:bg-red-700 transition-colors">Confirmer le declassement</button>
                <a href="/admin/equipement/list" class="px-4 py-2 text-sm text-gray-600 hover:text-gray-900 border border-gray-300 hover:border-gray-400 transition-colors">Annuler</a>
            </div>
        </form>
    </div>
</body>
</html>
