<!doctype html>
<html>
<head>
    <meta name="layout" content="admin"/>
    <title>Journal d'audit</title>
</head>
<body>
    <h1 class="text-xl font-bold text-gray-900 mb-1">Journal d'audit</h1>
    <p class="text-sm text-gray-500 mb-4">Trace des actions sensibles (connexions, creations, modifications, suppressions, attributions).</p>

    <div class="bg-white border border-gray-200 px-2 py-1.5 mb-4">
        <form method="get" class="flex items-center gap-1.5">
            <input type="text" name="q" value="${params.q}" placeholder="Utilisateur, action, cible, details..." class="flex-1 px-2 py-1.5 border border-gray-300 text-xs focus:outline-none focus:border-gray-600"/>
            <button type="submit" class="px-3 py-1.5 bg-gray-800 text-white text-xs font-semibold hover:bg-gray-700 transition-colors">Chercher</button>
            <g:if test="${params.q}"><a href="/admin/audit/list" class="px-3 py-1.5 text-xs text-gray-600 border border-gray-300 hover:border-gray-400 transition-colors">Effacer</a></g:if>
        </form>
    </div>

    <div class="bg-white border border-gray-200 overflow-hidden">
        <table class="w-full text-sm">
            <thead>
                <tr class="bg-gray-100 text-left">
                    <th class="px-4 py-3 font-semibold text-gray-700">Date</th>
                    <th class="px-4 py-3 font-semibold text-gray-700">Utilisateur</th>
                    <th class="px-4 py-3 font-semibold text-gray-700">Action</th>
                    <th class="px-4 py-3 font-semibold text-gray-700">Cible</th>
                    <th class="px-4 py-3 font-semibold text-gray-700">Details</th>
                </tr>
            </thead>
            <tbody>
                <g:each var="a" in="${auditList}">
                    <tr class="border-t border-gray-200 hover:bg-gray-50">
                        <td class="px-4 py-3 text-gray-500"><g:formatDate format="dd/MM/yyyy HH:mm:ss" date="${a.dateCreated}" /></td>
                        <td class="px-4 py-3">${a.utilisateur}</td>
                        <td class="px-4 py-3"><span class="text-xs font-semibold px-2 py-1 bg-gray-100 text-gray-700">${a.action}</span></td>
                        <td class="px-4 py-3">${a.cible} ${a.cibleId ? "#${a.cibleId}" : ''}</td>
                        <td class="px-4 py-3 text-gray-600 max-w-md truncate">${a.details?.encodeAsHTML()}</td>
                    </tr>
                </g:each>
                <g:if test="${!auditList}">
                    <tr><td colspan="5" class="px-4 py-8 text-center text-gray-400">Aucune trace d'audit</td></tr>
                </g:if>
            </tbody>
        </table>
    </div>
    <g:render template="/shared/pagination" model="[total: total, max: max, offset: offset]"/>
</body>
</html>