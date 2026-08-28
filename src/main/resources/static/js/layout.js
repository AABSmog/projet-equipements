/* Navigation latérale (même présentation que l'application Grails d'origine) */
function renderNavigation(user, active) {
  const el = document.getElementById('app-nav');
  if (!el) return;
  const isAdmin = user && user.role === 'ADMIN';

  const li = (href, label, activePrefix) =>
    `<a href="${href}" class="flex items-center px-4 py-2.5 text-sm ${active && active.startsWith(activePrefix) ? 'bg-maroon text-white font-medium' : 'text-gray-300 hover:bg-gray-800 hover:text-white transition-colors'}">${label}</a>`;

  let nav = '';
  if (isAdmin) {
    nav += `
      <div class="px-3 py-2 text-xs font-semibold text-gray-500 uppercase tracking-wider">Administration</div>
      ${li('/admin/index.html', 'Dashboard', '/admin/ind')}
      ${li('/admin/etablissement-wizard.html', 'Etablissements', '/admin/etab')}
      <div class="mt-4 px-3 py-2 text-xs font-semibold text-gray-500 uppercase tracking-wider">Gestion</div>
      ${li('/admin/equipements.html', 'Equipements', '/admin/equip')}
      ${li('/admin/affectations.html', 'Affectations', '/admin/affect')}
      ${li('/admin/affectation-historique.html', 'Historique des attributions', '/admin/hist')}
      ${li('/admin/signalements.html', 'Signalements', '/admin/sign')}
      ${li('/admin/personnels.html', 'Personnel', '/admin/pers')}
      ${li('/admin/regles.html', 'Regles de gestion', '/admin/regles')}
      <div class="mt-4 px-3 py-2 text-xs font-semibold text-gray-500 uppercase tracking-wider">Suivi</div>
      ${li('/admin/audit.html', 'Journal d\'audit', '/admin/aud')}`;
  } else {
    nav += `
      <div class="px-3 py-2 text-xs font-semibold text-gray-500 uppercase tracking-wider">Menu</div>
      ${li('/app/equipements.html', 'Mes equipements', '/app/equip')}
      ${li('/app/signalements.html', 'Mes signalements', '/app/sign')}`;
  }

  /* #app-nav est le panneau lateral : en enfant direct du conteneur flex,
     il s'etire automatiquement sur toute la hauteur (comme le <aside> Grails). */
  el.className = 'w-56 bg-gray-900 flex-shrink-0 flex flex-col';
  el.innerHTML = `
    <div class="h-14 flex items-center px-4 border-b border-gray-700">
      <span class="text-white font-bold text-sm tracking-wide">${isAdmin ? 'GESTION' : 'EQUIPEMENTS'}</span>
    </div>
    <nav class="flex-1 overflow-y-auto py-2">${nav}</nav>
    <div class="border-t border-gray-700 p-3">
      <div class="text-xs text-gray-400 mb-1">${(user.prenom || '')} ${(user.nom || '')}</div>
      <button type="button" data-logout class="text-xs text-gray-500 hover:text-white transition-colors bg-transparent border-0 p-0 cursor-pointer">Deconnexion</button>
    </div>`;

  el.querySelector('[data-logout]').addEventListener('click', () => {
    Api.post('/api/auth/logout').then(() => window.location = '/index.html').catch(() => window.location = '/index.html');
  });
}