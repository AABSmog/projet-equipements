<!doctype html>
<html>
<head>
    <meta name="layout" content="admin"/>
    <title>Nouveau personnel</title>
</head>
<body>
    <h1 class="text-xl font-bold text-gray-900 mb-6">Nouveau personnel</h1>
    <div class="bg-white border border-gray-200 p-6 max-w-lg">
        <g:hasErrors bean="${personnel}">
            <div class="bg-red-100 border border-red-200 px-4 py-3 mb-4 text-sm text-red-800">
                <g:eachError bean="${personnel}"><g:message error="${it}"/><br/></g:eachError>
            </div>
        </g:hasErrors>
        <form action="/admin/personnel/save" method="post">
            <div class="grid grid-cols-2 gap-4 mb-4">
                <div><label class="block text-sm font-semibold text-gray-700 mb-1">Nom</label><input type="text" name="nom" value="${personnel?.nom}" required class="w-full px-3 py-2 border border-gray-300 text-sm focus:outline-none focus:border-gray-600"/></div>
                <div><label class="block text-sm font-semibold text-gray-700 mb-1">Prenom</label><input type="text" name="prenom" value="${personnel?.prenom}" required class="w-full px-3 py-2 border border-gray-300 text-sm focus:outline-none focus:border-gray-600"/></div>
            </div>
            <div class="mb-4"><label class="block text-sm font-semibold text-gray-700 mb-1">Email</label><input type="email" name="email" value="${personnel?.email}" required class="w-full px-3 py-2 border border-gray-300 text-sm focus:outline-none focus:border-gray-600"/></div>
            <div class="mb-4"><label class="block text-sm font-semibold text-gray-700 mb-1">Mot de passe</label><input type="password" name="motDePasse" required class="w-full px-3 py-2 border border-gray-300 text-sm focus:outline-none focus:border-gray-600"/></div>
            <div class="mb-4"><label class="block text-sm font-semibold text-gray-700 mb-1">Role</label>
                <select name="role" class="w-full px-3 py-2 border border-gray-300 text-sm focus:outline-none focus:border-gray-600">
                    <option value="USER">Utilisateur</option>
                    <option value="ADMIN">Administrateur</option>
                </select>
            </div>
            <div class="flex gap-3">
                <button type="submit" class="bg-maroon text-white px-4 py-2 text-sm font-semibold hover:bg-red-900 transition-colors">Creer</button>
                <a href="/admin/personnel/list" class="px-4 py-2 text-sm text-gray-600 hover:text-gray-900 border border-gray-300 hover:border-gray-400 transition-colors">Annuler</a>
            </div>
        </form>
    </div>
</body>
</html>
