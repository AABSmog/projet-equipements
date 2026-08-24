/* Formulaire equipement (creation / modification) */
window.PAGE_INIT = async function () {
  const params = new URLSearchParams(window.location.search);
  const id = params.get('id');
  const isEdit = !!id;
  const form = document.getElementById('equipement-form');
  const errorBox = document.getElementById('form-error');
  const typeSel = document.getElementById('type');
  const etatSel = document.getElementById('etat');
  const nouveauWrap = document.getElementById('nouveau-type-wrap');

  let types = [];
  try {
    types = await Api.get('/api/admin/types');
  } catch (e) { /* ignore */ }

  typeSel.innerHTML = '<option value="">-- Selectionner --</option>' +
    (types || []).map(t => `<option value="${t.id}">${esc(t.nom)}</option>`).join('') +
    `<option value="autre">Autre...</option>`;

  typeSel.addEventListener('change', () => {
    nouveauWrap.classList.toggle('hidden', typeSel.value !== 'autre');
  });

  if (isEdit) {
    const data = await Api.get(`/api/admin/equipements/${id}`);
    const e = data;
    typeSel.value = e.type ? String(e.type.id) : '';
    document.getElementById('numero-serie-display').textContent = e.numeroSerie || '';
    document.getElementById('description').value = e.description || '';
    const etats = await Api.get('/api/meta');
    etatSel.innerHTML = (etats.etatsEquipement || []).map(s =>
      `<option value="${esc(s.value)}" ${s.value === e.etat ? 'selected' : ''}>${esc(s.label)}</option>`).join('');
  }

  form.addEventListener('submit', async (ev) => {
    ev.preventDefault();
    hideError(errorBox);
    const payload = {
      typeId: typeSel.value,
      description: document.getElementById('description').value.trim()
    };
    if (typeSel.value === 'autre') {
      payload.nouveauType = document.getElementById('nouveauType').value.trim();
    }
    if (isEdit) {
      payload.etat = etatSel.value;
    }
    form.querySelector('button[type=submit]').disabled = true;
    try {
      if (isEdit) {
        await Api.put(`/api/admin/equipements/${id}`, payload);
        Flash.set('Equipement mis a jour', 'success');
      } else {
        await Api.post('/api/admin/equipements', payload);
        Flash.set('Equipement cree', 'success');
      }
      window.location = '/admin/equipements.html';
    } catch (err) {
      showError(errorBox, err.message);
      form.querySelector('button[type=submit]').disabled = false;
    }
  });
};