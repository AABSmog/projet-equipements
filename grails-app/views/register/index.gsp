<!doctype html>
<html lang="fr">
<head>
    <meta charset="utf-8"/>
    <meta name="viewport" content="width=device-width, initial-scale=1"/>
    <title>Inscription - Gestion Equipements</title>
    <asset:stylesheet src="tailwind.css"/>
</head>
<body class="bg-gray-900 min-h-screen flex items-center justify-center font-sans antialiased">
    <div class="w-full max-w-sm">
        <div class="mb-8 text-center">
            <h1 class="text-2xl font-bold text-white tracking-wide">GESTION EQUIPEMENTS</h1>
            <p class="text-gray-400 text-sm mt-1">Creez votre compte</p>
        </div>
        <div class="bg-white">
            <g:hasErrors bean="${personnel}">
                <div class="bg-red-100 border border-red-200 px-4 py-3 mb-0 text-sm text-red-800">
                    <span class="font-semibold">Erreurs :</span><br/>
                    <g:eachError bean="${personnel}"><g:message error="${it}"/><br/></g:eachError>
                </div>
            </g:hasErrors>
            <g:if test="${flash.error}">
                <div class="bg-red-100 border border-red-200 px-4 py-3 mb-0 text-sm text-red-800">${flash.error}</div>
            </g:if>
            <form action="/register/save" method="post" class="p-6">
                <input type="hidden" name="_csrf" value="${session.csrfToken}"/>
                <div class="grid grid-cols-2 gap-3 mb-3">
                    <div>
                        <label class="block text-sm font-semibold text-gray-700 mb-1" for="nom">Nom <span class="text-red-500">*</span></label>
                        <input type="text" name="nom" id="nom" value="${personnel?.nom}" required
                            class="w-full px-3 py-2 border ${personnel?.errors?.getFieldErrors('nom') ? 'border-red-500' : 'border-gray-300'} text-sm focus:outline-none focus:border-gray-600"/>
                        <g:eachError bean="${personnel}" field="nom"><p class="text-xs text-red-600 mt-1"><g:message error="${it}"/></p></g:eachError>
                    </div>
                    <div>
                        <label class="block text-sm font-semibold text-gray-700 mb-1" for="prenom">Prenom <span class="text-red-500">*</span></label>
                        <input type="text" name="prenom" id="prenom" value="${personnel?.prenom}" required
                            class="w-full px-3 py-2 border ${personnel?.errors?.getFieldErrors('prenom') ? 'border-red-500' : 'border-gray-300'} text-sm focus:outline-none focus:border-gray-600"/>
                        <g:eachError bean="${personnel}" field="prenom"><p class="text-xs text-red-600 mt-1"><g:message error="${it}"/></p></g:eachError>
                    </div>
                </div>
                <div class="mb-3">
                    <label class="block text-sm font-semibold text-gray-700 mb-1" for="email">Email <span class="text-red-500">*</span></label>
                    <input type="email" name="email" id="email" value="${personnel?.email}" required
                        class="w-full px-3 py-2 border ${personnel?.errors?.getFieldErrors('email') ? 'border-red-500' : 'border-gray-300'} text-sm focus:outline-none focus:border-gray-600"/>
                    <g:eachError bean="${personnel}" field="email"><p class="text-xs text-red-600 mt-1"><g:message error="${it}"/></p></g:eachError>
                </div>
                <div class="mb-4">
                    <label class="block text-sm font-semibold text-gray-700 mb-1" for="motDePasse">Mot de passe <span class="text-red-500">*</span></label>
                    <input type="password" name="motDePasse" id="motDePasse" required
                        class="w-full px-3 py-2 border ${personnel?.errors?.getFieldErrors('motDePasse') ? 'border-red-500' : 'border-gray-300'} text-sm focus:outline-none focus:border-gray-600"/>
                    <p class="text-xs text-gray-500 mt-1">8 caracteres minimum, avec une minuscule, une majuscule et un chiffre.</p>
                    <g:eachError bean="${personnel}" field="motDePasse"><p class="text-xs text-red-600 mt-1"><g:message error="${it}"/></p></g:eachError>
                </div>
                <button type="submit"
                    class="w-full py-2.5 bg-maroon text-white text-sm font-semibold hover:bg-red-900 transition-colors">
                    Creer mon compte
                </button>
            </form>
            <div class="border-t border-gray-200 px-6 py-4 text-center">
                <a href="/login" class="text-sm text-gray-600 hover:text-gray-900">Deja un compte ? Connectez-vous</a>
            </div>
        </div>
        <p class="text-center text-xs text-gray-500 mt-4">Projet Equipements &copy; 2026</p>
    </div>
</body>
</html>
