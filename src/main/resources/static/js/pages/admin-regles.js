/* Règles de gestion par entreprise */
window.PAGE_INIT = function () {
  const sel = document.getElementById('etab-select');
  const fields = document.getElementById('regles-fields');
  const errorBox = document.getElementById('form-error');
  const status = document.getElementById('save-status');
  const previewPrenom = document.getElementById('preview-prenom');
  const previewNom = document.getElementById('preview-nom');
  const previewEmail = document.getElementById('preview-email');
  let currentId = null;

  async function loadEtabs() {
    const list = await Api.get('/api/admin/etablissements');
    sel.innerHTML = list.map(e => `<option value="${e.id}">${esc(e.nom)} (${esc(e.slug)})</option>`).join('');
    if (list.length) {
      currentId = list[0].id;
      sel.value = currentId;
      await loadRegles();
    }
  }

  async function loadRegles() {
    hideError(errorBox);
    fields.innerHTML = '<div class="text-xs text-gray-400">Chargement...</div>';
    try {
      const data = await Api.get(`/api/admin/etablissements/${currentId}/regles`);
      const regles = data.regles || [];
      fields.innerHTML = regles.map(r => {
        const isBool = r.type === 'boolean';
        const input = isBool
          ? `<input type="checkbox" data-cle="${r.cle}" ${r.valeur==='true'?'checked':''} class="ml-auto"/>`
          : `<input data-cle="${r.cle}" value="${esc(r.valeur)}" class="ml-auto w-48 px-2 py-1 border border-gray-300 text-sm"/>`;
        return `<label class="flex items-center gap-2 text-sm">${esc(r.label)} <span class="text-gray-400 text-xs">(${esc(r.cle)})</span> ${input}</label>`;
      }).join('');
      fields.querySelectorAll('input').forEach(i => i.addEventListener('input', updatePreview));
      updatePreview();
    } catch (err) {
      showError(errorBox, err.message);
    }
  }

  async function updatePreview() {
    const p = previewPrenom.value.trim() || 'Jean';
    const n = previewNom.value.trim() || 'Dupont';
    if (!currentId) return;
    try {
      const data = await Api.get(`/api/admin/etablissements/${currentId}/regles/preview-email?prenom=${encodeURIComponent(p)}&nom=${encodeURIComponent(n)}`);
      previewEmail.textContent = data.email || '—';
    } catch (e) {
      previewEmail.textContent = p.toLowerCase() + '.' + n.toLowerCase() + '@...';
    }
  }
  previewPrenom.addEventListener('input', updatePreview);
  previewNom.addEventListener('input', updatePreview);

  sel.addEventListener('change', () => { currentId = sel.value; loadRegles(); });

  document.getElementById('regles-form').addEventListener('submit', async (e) => {
    e.preventDefault();
    hideError(errorBox);
    status.textContent = 'Enregistrement...';
    const regles = {};
    fields.querySelectorAll('[data-cle]').forEach(i => {
      regles[i.dataset.cle] = i.type === 'checkbox' ? String(i.checked) : i.value;
    });
    try {
      await Api.put(`/api/admin/etablissements/${currentId}/regles`, { regles });
      status.textContent = 'Enregistre ✓';
      mountAlert(document.getElementById('flash-container'), 'Regles mises a jour', 'success');
      setTimeout(() => status.textContent = '', 2000);
    } catch (err) {
      showError(errorBox, err.message);
      status.textContent = '';
    }
  });

  loadEtabs().catch(err => showError(errorBox, err.message));
};
