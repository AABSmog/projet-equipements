<!doctype html>
<html>
<head>
    <meta name="layout" content="admin"/>
    <title>Modifier personnel</title>
</head>
<body>
    <h1 class="text-xl font-bold text-gray-900 mb-6">Modifier personnel</h1>
    <div class="bg-white border border-gray-200 p-6 max-w-lg">
        <g:render template="/shared/formErrors" model="[bean: personnel]"/>
        <form action="/admin/personnel/update" method="post">
            <input type="hidden" name="_csrf" value="${session.csrfToken}"/>
            <input type="hidden" name="id" value="${personnel.id}"/>
            <div class="grid grid-cols-2 gap-4 mb-4">
                <div><label class="block text-sm font-semibold text-gray-700 mb-1">Nom <span class="text-red-500">*</span></label><input type="text" name="nom" value="${personnel.nom}" required class="w-full px-3 py-2 border ${personnel?.errors?.getFieldErrors('nom') ? 'border-red-500' : 'border-gray-300'} text-sm focus:outline-none focus:border-gray-600"/><g:eachError bean="${personnel}" field="nom"><p class="text-xs text-red-600 mt-1"><g:message error="${it}"/></p></g:eachError></div>
                <div><label class="block text-sm font-semibold text-gray-700 mb-1">Prenom <span class="text-red-500">*</span></label><input type="text" name="prenom" value="${personnel.prenom}" required class="w-full px-3 py-2 border ${personnel?.errors?.getFieldErrors('prenom') ? 'border-red-500' : 'border-gray-300'} text-sm focus:outline-none focus:border-gray-600"/><g:eachError bean="${personnel}" field="prenom"><p class="text-xs text-red-600 mt-1"><g:message error="${it}"/></p></g:eachError></div>
            </div>
            <div class="mb-4"><label class="block text-sm font-semibold text-gray-700 mb-1">Email <span class="text-red-500">*</span></label><input type="email" name="email" value="${personnel.email}" required class="w-full px-3 py-2 border ${personnel?.errors?.getFieldErrors('email') ? 'border-red-500' : 'border-gray-300'} text-sm focus:outline-none focus:border-gray-600"/><g:eachError bean="${personnel}" field="email"><p class="text-xs text-red-600 mt-1"><g:message error="${it}"/></p></g:eachError></div>
            <div class="mb-4"><label class="block text-sm font-semibold text-gray-700 mb-1">Mot de passe (laisser vide pour conserver)</label><input type="password" name="motDePasse" class="w-full px-3 py-2 border border-gray-300 text-sm focus:outline-none focus:border-gray-600"/>
                <p class="text-xs text-gray-500 mt-1">8 caracteres minimum, avec une minuscule, une majuscule et un chiffre.</p></div>
            <div class="mb-4"><label class="block text-sm font-semibold text-gray-700 mb-1">Role <span class="text-red-500">*</span></label>
                <select name="role" class="w-full px-3 py-2 border border-gray-300 text-sm focus:outline-none focus:border-gray-600">
                    <option value="USER" ${personnel.role == proj.equipment.RolePersonnel.USER ? 'selected' : ''}>Utilisateur</option>
                    <option value="ADMIN" ${personnel.role == proj.equipment.RolePersonnel.ADMIN ? 'selected' : ''}>Administrateur</option>
                </select>
            </div>
            <div class="flex gap-3">
                <button type="submit" class="bg-maroon text-white px-4 py-2 text-sm font-semibold hover:bg-red-900 transition-colors">Enregistrer</button>
                <a href="/admin/personnel/list" class="px-4 py-2 text-sm text-gray-600 hover:text-gray-900 border border-gray-300 hover:border-gray-400 transition-colors">Annuler</a>
            </div>
        </form>
    </div>
</body>
</html>
