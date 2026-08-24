/* Affectations : formulaire d'affectation (meme comportement que l'autocomplete Grails) */
window.PAGE_INIT = function () {
  const errorBox = document.getElementById('affect-error');
  const eqType = document.getElementById('eqType');
  const eqInput = document.getElementById('eqInput');
  const eqMenu = document.getElementById('eqMenu');
  const eqId = document.getElementById('eqId');
  const persInput = document.getElementById('persInput');
  const persMenu = document.getElementById('persMenu');
  const persId = document.getElementById('persId');
  let eqTimer = null;
  let persTimer = null;

  function showDrop(inp, m) {
    const r = inp.getBoundingClientRect();
    m.style.position = 'fixed';
    m.style.left = r.left + 'px';
    m.style.width = r.width + 'px';
    m.style.top = r.bottom + 'px';
    m.style.zIndex = '9999';
    m.classList.remove('hidden');
  }

  function hideDrop(m) {
    m.classList.add('hidden');
    m.style.position = '';
  }

  function renderItems(m, items, pickFn) {
    m.innerHTML = '';
    if (!items || items.length === 0) {
      const empty = document.createElement('div');
      empty.className = 'px-2 py-1.5 text-gray-400';
      empty.textContent = 'Aucun resultat';
      m.appendChild(empty);
      return;
    }
    items.forEach((it) => {
      const d = document.createElement('div');
      d.className = 'px-2 py-1.5 cursor-pointer hover:bg-gray-100 border-b border-gray-100 last:border-b-0';
      d.setAttribute('data-id', it.id);
      d.textContent = it.label;
      d.addEventListener('click', () => pickFn(it));
      m.appendChild(d);
    });
  }

  function pickItem(it, inp, hid, m) {
    inp.value = it.label;
    hid.value = it.id;
    hideDrop(m);
  }

  function eqSearch() {
    showDrop(eqInput, eqMenu);
    const q = eqInput.value.trim();
    const typeId = eqType.value;
    clearTimeout(eqTimer);
    eqTimer = setTimeout(async () => {
      try {
        const p = new URLSearchParams();
        if (q) p.set('q', q);
        if (typeId) p.set('typeId', typeId);
        const items = await Api.get('/api/admin/recherche/equipements?' + p.toString());
        renderItems(eqMenu, items, (it) => pickItem(it, eqInput, eqId, eqMenu));
      } catch (err) { /* ignore */ }
    }, 200);
  }

  function persSearch() {
    showDrop(persInput, persMenu);
    const q = persInput.value.trim();
    clearTimeout(persTimer);
    persTimer = setTimeout(async () => {
      try {
        const p = new URLSearchParams();
        if (q) p.set('q', q);
        const items = await Api.get('/api/admin/recherche/personnels?' + p.toString());
        renderItems(persMenu, items, (it) => pickItem(it, persInput, persId, persMenu));
      } catch (err) { /* ignore */ }
    }, 200);
  }

  eqInput.addEventListener('focus', eqSearch);
  eqInput.addEventListener('input', eqSearch);
  eqType.addEventListener('change', eqSearch);
  persInput.addEventListener('focus', persSearch);
  persInput.addEventListener('input', persSearch);

  document.addEventListener('click', (e) => {
    if (!e.target.closest('#eqInput, #eqMenu')) hideDrop(eqMenu);
    if (!e.target.closest('#persInput, #persMenu')) hideDrop(persMenu);
  });

  submitGuard(document.getElementById('affecter-form'), async () => {
    hideError(errorBox);
    if (!eqId.value || !persId.value) {
      showError(errorBox, 'Selectionnez un equipement et un personnel dans les listes proposees.');
      return;
    }
    try {
      await Api.post('/api/admin/affectations', { equipementId: eqId.value, personnelId: persId.value });
      Flash.set('Equipement affecte', 'success');
      window.location.reload();
    } catch (err) {
      showError(errorBox, err.message);
    }
  });

  (async () => {
    const types = await Api.get('/api/admin/types');
    eqType.innerHTML = '<option value="">Tous les types</option>' + (types || []).map(t => `<option value="${t.id}">${esc(t.nom)}</option>`).join('');
  })().catch((err) => {
    showError(errorBox, err.message);
  });
};