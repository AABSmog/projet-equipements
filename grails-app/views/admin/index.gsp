<%@ page import="proj.equipment.RolePersonnel" %>
<!doctype html>
<html>
<head>
    <meta name="layout" content="admin"/>
    <title>Dashboard</title>
</head>
<body>
    <h1 class="text-xl font-bold text-gray-900 mb-6">Dashboard</h1>
    <div class="grid grid-cols-4 gap-4">
        <div class="bg-white border border-gray-200 p-5">
            <div class="text-3xl font-bold text-gray-900">${totalEquipements}</div>
            <div class="text-sm text-gray-500 mt-1">Equipements</div>
        </div>
        <div class="bg-white border border-gray-200 p-5">
            <div class="text-3xl font-bold text-gray-900">${affectationsEnCours}</div>
            <div class="text-sm text-gray-500 mt-1">Affectations en cours</div>
        </div>
        <div class="bg-white border border-gray-200 p-5">
            <div class="text-3xl font-bold text-gray-900">${signalementsOuverts}</div>
            <div class="text-sm text-gray-500 mt-1">Signalements</div>
        </div>
        <div class="bg-white border border-gray-200 p-5">
            <div class="text-3xl font-bold text-gray-900">${totalPersonnel}</div>
            <div class="text-sm text-gray-500 mt-1">Personnel</div>
        </div>
    </div>
    <div class="mt-8 grid grid-cols-2 gap-4">
        <a href="/admin/equipement/list" class="bg-white border border-gray-200 p-5 hover:border-gray-400 transition-colors">
            <div class="text-sm font-semibold text-gray-900">Gerer les equipements</div>
            <div class="text-xs text-gray-500 mt-1">Ajouter, modifier, supprimer</div>
        </a>
        <a href="/admin/affectation/list" class="bg-white border border-gray-200 p-5 hover:border-gray-400 transition-colors">
            <div class="text-sm font-semibold text-gray-900">Voir les affectations</div>
            <div class="text-xs text-gray-500 mt-1">Suivi des equipements affectes</div>
        </a>
        <a href="/admin/signalement/list" class="bg-white border border-gray-200 p-5 hover:border-gray-400 transition-colors">
            <div class="text-sm font-semibold text-gray-900">Voir les signalements</div>
            <div class="text-xs text-gray-500 mt-1">Problemes rapportes</div>
        </a>
        <a href="/admin/personnel/list" class="bg-white border border-gray-200 p-5 hover:border-gray-400 transition-colors">
            <div class="text-sm font-semibold text-gray-900">Gerer le personnel</div>
            <div class="text-xs text-gray-500 mt-1">Ajouter, modifier les utilisateurs</div>
        </a>
    </div>
</body>
</html>
