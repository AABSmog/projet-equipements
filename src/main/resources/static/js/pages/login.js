/* Page de connexion - avec selection d'entreprise */
window.PAGE_INIT = async function () {
  const form = document.getElementById('login-form');
  const errorBox = document.getElementById('login-error');
  const etabSelect = document.getElementById('etab-select');
  const etabInfo = document.getElementById('etab-info');
  const noEtabMsg = document.getElementById('no-etab-msg');
  const loginBtn = document.getElementById('login-btn');
  if (!form) return;

  // Init session avant tout POST (login)
  try { await Api.initSession(); } catch(e) { /* ignore, sera reessaye au login */ }

  // Charger entreprises
  let etablissements = [];
  try {
    etablissements = await fetch('/api/etablissements', { headers: { 'Accept': 'application/json' } }).then(r => r.json());
  } catch (e) {
    etablissements = [];
  }
  if (!Array.isArray(etablissements)) etablissements = [];

  function slugFromUrl() {
    const p = new URLSearchParams(window.location.search);
    return p.get('etablissement') || p.get('slug') || '';
  }

  // Remplir select
  if (!etablissements.length) {
    etabSelect.innerHTML = '<option value="">Aucune entreprise</option>';
    if (noEtabMsg) noEtabMsg.classList.remove('hidden');
    if (etabInfo) { etabInfo.textContent = 'Créez votre première entreprise pour commencer.'; etabInfo.classList.remove('hidden'); }
  } else {
    etabSelect.innerHTML = etablissements.map(e => `<option value="${e.id}" data-slug="${esc(e.slug)}">${esc(e.nom)}</option>`).join('');
    const slug = slugFromUrl();
    if (slug) {
      const found = etablissements.find(e => e.slug === slug);
      if (found) etabSelect.value = found.id;
    }
    // Auto-select Principal si un seul ou si etablissement en sessionStorage
    const saved = sessionStorage.getItem('etablissementId');
    if (saved && etablissements.find(e => String(e.id) === saved)) {
      etabSelect.value = saved;
    } else if (etablissements.length === 1) {
      etabSelect.value = etablissements[0].id;
    }
    if (etabInfo) {
      const updateInfo = () => {
        const sel = etablissements.find(e => String(e.id) === etabSelect.value);
        if (sel) {
          etabInfo.textContent = sel.nom + (sel.domaineEmail ? ' · ' + sel.domaineEmail : '');
          etabInfo.classList.remove('hidden');
        }
      };
      etabSelect.addEventListener('change', () => {
        sessionStorage.setItem('etablissementId', etabSelect.value);
        updateInfo();
      });
      updateInfo();
    }
    if (noEtabMsg) noEtabMsg.classList.add('hidden');
  }

  // Flash si vient du wizard
  if (typeof Flash !== 'undefined' && Flash.show) Flash.show();

  form.addEventListener('submit', async (e) => {
    e.preventDefault();
    hideError(errorBox);
    const etablissementId = etabSelect.value;
    if (!etablissementId) {
      showError(errorBox, 'Veuillez sélectionner une entreprise');
      return;
    }
    const email = document.getElementById('email').value.trim();
    const password = document.getElementById('password').value;
    const btn = form.querySelector('button[type=submit]');
    btn.disabled = true;
    sessionStorage.setItem('etablissementId', etablissementId);
    const slug = etabSelect.options[etabSelect.selectedIndex]?.dataset?.slug || '';
    try {
      await Api.post('/api/auth/login', { email, password, etablissementId, etablissementSlug: slug });
      const user = Api.user();
      // Stocker etablissement courant pour les appels suivants (header X-Tenant)
      if (user && user.etablissement) {
        sessionStorage.setItem('etablissementId', user.etablissement.id);
        sessionStorage.setItem('etablissementSlug', user.etablissement.slug);
      }
      window.location = user.role === 'ADMIN' ? '/admin/index.html' : '/app/equipements.html';
    } catch (err) {
      showError(errorBox, err.message);
      btn.disabled = false;
    }
  });
};
// Auto-init pour page login (data-guest sans app.js) - CSP: pas de inline script
if (document.readyState === 'loading') {
  document.addEventListener('DOMContentLoaded', () => { if (window.PAGE_INIT && !window._loginDone) { window._loginDone = true; window.PAGE_INIT(); } });
} else {
  if (!window._loginDone) { window._loginDone = true; window.PAGE_INIT(); }
}