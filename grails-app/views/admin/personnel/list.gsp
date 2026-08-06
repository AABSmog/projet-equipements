<!doctype html>
<html>
<head>
    <meta name="layout" content="admin"/>
    <title>Personnel</title>
</head>
<body>
    <div class="flex items-center justify-between mb-4">
        <h1 class="text-xl font-bold text-gray-900">Personnel</h1>
        <a href="/admin/personnel/create" class="bg-maroon text-white px-4 py-2 text-sm font-semibold hover:bg-red-900 transition-colors">Nouveau</a>
    </div>
    <div class="bg-white border border-gray-200 px-2 py-1.5 mb-4">
        <form method="get" class="flex items-center gap-1.5">
            <input type="text" name="q" value="${params.q}" placeholder="Nom, prenom ou email..." class="flex-1 px-2 py-1.5 border border-gray-300 text-xs focus:outline-none focus:border-gray-600"/>
            <button type="submit" class="px-3 py-1.5 bg-gray-800 text-white text-xs font-semibold hover:bg-gray-700 transition-colors">Chercher</button>
            <g:if test="${params.q}"><a href="/admin/personnel/list" class="px-3 py-1.5 text-xs text-gray-600 border border-gray-300 hover:border-gray-400 transition-colors">Effacer</a></g:if>
        </form>
    </div>
    <div class="bg-white border border-gray-200 overflow-hidden">
        <table class="w-full text-sm">
            <thead>
                <tr class="bg-gray-100 text-left">
                    
                    <th class="px-4 py-3 font-semibold text-gray-700">Nom</th>
                    <th class="px-4 py-3 font-semibold text-gray-700">Prenom</th>
                    <th class="px-4 py-3 font-semibold text-gray-700">Email</th>
                    <th class="px-4 py-3 font-semibold text-gray-700">Role</th>
                    <th class="px-4 py-3 font-semibold text-gray-700">Actions</th>
                </tr>
            </thead>
            <tbody>
                <g:each var="p" in="${personnelList}">
                    <tr class="border-t border-gray-200 hover:bg-gray-50">
                        
                        <td class="px-4 py-3 font-medium">${p.nom}</td>
                        <td class="px-4 py-3">${p.prenom}</td>
                        <td class="px-4 py-3 text-gray-600">${p.email}</td>
                        <td class="px-4 py-3"><span class="text-xs font-semibold px-2 py-1 ${p.role == proj.equipment.RolePersonnel.ADMIN ? 'bg-maroon text-white' : 'bg-gray-200 text-gray-700'}">${p.role?.label}</span></td>
                        <td class="px-4 py-3">
                            <a href="/admin/personnel/edit/${p.id}" class="text-gray-600 hover:text-gray-900 mr-3">Modifier</a>
                            <a href="/admin/personnel/delete/${p.id}" class="text-red-600 hover:text-red-800" onclick="return confirm('Supprimer ?')">Supprimer</a>
                        </td>
                    </tr>
                </g:each>
                <g:if test="${!personnelList}">
                    <tr><td colspan="6" class="px-4 py-8 text-center text-gray-400">Aucun personnel</td></tr>
                </g:if>
            </tbody>
        </table>
    </div>
</body>
</html>
