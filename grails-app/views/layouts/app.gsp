<!doctype html>
<html lang="fr">
<head>
    <meta charset="utf-8"/>
    <meta name="viewport" content="width=device-width, initial-scale=1"/>
    <title><g:layoutTitle default="Equipements"/></title>
    <asset:stylesheet src="tailwind.css"/>
    <asset:javascript src="flash.js"/>
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
        <div class="px-6 pt-5">
            <g:render template="/shared/flash"/>
        </div>
        <div class="p-6">
            <g:layoutBody/>
        </div>
    </main>
</div>

</body>
</html>