<!doctype html>
<html lang="fr">
<head>
    <meta charset="utf-8"/>
    <meta name="viewport" content="width=device-width, initial-scale=1"/>
    <title>Connexion - Gestion Equipements</title>
    <asset:stylesheet src="tailwind.css"/>
</head>
<body class="bg-gray-900 min-h-screen flex items-center justify-center font-sans antialiased">
    <div class="w-full max-w-sm">
        <div class="mb-8 text-center">
            <h1 class="text-2xl font-bold text-white tracking-wide">GESTION EQUIPEMENTS</h1>
            <p class="text-gray-400 text-sm mt-1">Connectez-vous pour continuer</p>
        </div>
        <div class="bg-white">
            <form action="/login/attempt" method="post" class="p-6">
                <input type="hidden" name="_csrf" value="${session.csrfToken}"/>
                <g:if test="${flash.error}">
                    <div class="bg-red-100 border border-red-200 px-4 py-3 mb-4 text-sm text-red-800">${flash.error}</div>
                </g:if>
                <div class="mb-4">
                    <label class="block text-sm font-semibold text-gray-700 mb-1" for="email">Email</label>
                    <input type="email" name="email" id="email" required
                        class="w-full px-3 py-2 border border-gray-300 text-sm focus:outline-none focus:border-gray-600"/>
                </div>
                <div class="mb-6">
                    <label class="block text-sm font-semibold text-gray-700 mb-1" for="password">Mot de passe</label>
                    <input type="password" name="password" id="password" required
                        class="w-full px-3 py-2 border border-gray-300 text-sm focus:outline-none focus:border-gray-600"/>
                </div>
                <button type="submit"
                    class="w-full py-2.5 bg-maroon text-white text-sm font-semibold hover:bg-red-900 transition-colors">
                    Se connecter
                </button>
            </form>
            <div class="border-t border-gray-200 mt-4 pt-4 text-center">
                <a href="/register" class="text-sm text-gray-600 hover:text-gray-900">Creer un compte</a>
            </div>
        </div>
        <p class="text-center text-xs text-gray-500 mt-4">Projet Equipements &copy; 2026</p>
    </div>
</body>
</html>
