<!doctype html>
<html lang="fr">
<head>
    <meta charset="utf-8"/>
    <meta name="viewport" content="width=device-width, initial-scale=1"/>
    <title><g:layoutTitle default="Equipements"/></title>
    <asset:stylesheet src="tailwind.css"/>
    <g:layoutHead/>
</head>
<body class="bg-gray-50 font-sans antialiased">

<div class="flex h-screen overflow-hidden">
    <aside class="w-56 bg-gray-900 flex-shrink-0 flex flex-col">
        <div class="h-14 flex items-center px-4 border-b border-gray-700">
            <span class="text-white font-bold text-sm tracking-wide">EQUIPEMENTS</span>
        </div>
        <nav class="flex-1 overflow-y-auto py-2">
            <div class="px-3 py-2 text-xs font-semibold text-gray-500 uppercase tracking-wider">Menu</div>
            <a href="/app/equipement/list" class="flex items-center px-4 py-2.5 text-sm ${request.forwardURI?.startsWith('/app/equipement') ? 'bg-maroon text-white font-medium' : 'text-gray-300 hover:bg-gray-800 hover:text-white transition-colors'}">Mes equipements</a>
            <a href="/app/signalement/list" class="flex items-center px-4 py-2.5 text-sm ${request.forwardURI?.startsWith('/app/signalement') ? 'bg-maroon text-white font-medium' : 'text-gray-300 hover:bg-gray-800 hover:text-white transition-colors'}">Mes signalements</a>
        </nav>
        <div class="border-t border-gray-700 p-3">
            <div class="text-xs text-gray-400 mb-1">${session.user?.prenom} ${session.user?.nom}</div>
            <a href="/logout" class="text-xs text-gray-500 hover:text-white transition-colors">Deconnexion</a>
        </div>
    </aside>

    <main class="flex-1 overflow-y-auto">
        <g:if test="${flash.success}">
            <div class="bg-green-100 border-b border-green-200 px-6 py-3 text-sm text-green-800">${flash.success}</div>
        </g:if>
        <g:if test="${flash.error}">
            <div class="bg-red-100 border-b border-red-200 px-6 py-3 text-sm text-red-800">${flash.error}</div>
        </g:if>
        <div class="p-6">
            <g:layoutBody/>
        </div>
    </main>
</div>

</body>
</html>