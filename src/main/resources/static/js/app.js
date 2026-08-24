/* Initialisation commune de chaque page.
   Lecture des regles via les attributs data-* du <body> :
     - data-guest :  page publique (pas d'authentification requise)
     - data-admin :  1 = page admin, 0 = page app (defaut)
     - data-active :  prefixe de navigation a surligner */
(function () {
  const body = document.body;
  const isGuest = body.dataset.guest === '1';
  const isAdminPage = body.dataset.admin === '1';
  const active = body.dataset.active || '';

  async function boot() {
    await Api.initSession();
    const user = Api.user();

    if (isGuest) {
      if (user) {
        window.location = user.role === 'ADMIN' ? '/admin/index.html' : '/app/equipements.html';
        return;
      }
      Flash.show();
      document.querySelector('#loading')?.remove();
      if (window.PAGE_INIT) { await window.PAGE_INIT(); }
      return;
    }

    if (!user) {
      window.location = '/index.html';
      return;
    }
    if (isAdminPage && user.role !== 'ADMIN') {
      window.location = '/app/equipements.html';
      return;
    }

    renderNavigation(user, active);
    Flash.show();
    const root = document.querySelector('#layout-root');
    if (root) root.style.display = 'flex';
    document.querySelector('#loading')?.remove();

    if (window.PAGE_INIT) { await window.PAGE_INIT(); }
  }

  boot().catch((e) => console.error('boot error', e));
})();