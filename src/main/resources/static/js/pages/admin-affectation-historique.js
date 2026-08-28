/* Historique des attributions */
window.PAGE_INIT = function () {
  const params = new URLSearchParams(window.location.search);
  let q = params.get('q') || '';
  let typeId = params.get('typeId') || '';
  const max = '10';
  let offset = params.get('offset') || '0';

  const qInput = document.getElementById('q');
  const typeSel = document.getElementById('typeId');
  qInput.value = q;

  const clearLink = document.getElementById('clear-filters');
  if (clearLink) clearLink.classList.toggle('hidden', !q && !typeId);

  function baseUrl() {
    const p = new URLSearchParams();
    if (q) p.set('q', q);
    if (typeId) p.set('typeId', typeId);
    return '/admin/affectation-historique.html?' + p.toString();
  }

  async function load() {
    const p = new URLSearchParams();
    if (q) p.set('q', q);
    if (typeId) p.set('typeId', typeId);
    p.set('max', max);
    p.set('offset', offset);
    const data = await Api.get('/api/admin/affectations/historique?' + p.toString());
    typeSel.innerHTML = '<option value="">Tous les types</option>' +
      (data.types || []).map(t => `<option value="${t.id}" ${String(t.id) === typeId ? 'selected' : ''}>${esc(t.nom)}</option>`).join('');
    const rows = document.getElementById('rows');
    if (!data.items || !data.items.length) {
      rows.innerHTML = emptyRow(9, 'Aucune attribution dans l\'historique');
    } else {
      rows.innerHTML = data.items.map((a) => {
        const eq = (a.equipement && a.equipement.type && a.equipement.type.nom) || a.infoEquipement || '-';
        const ns = (a.equipement && a.equipement.numeroSerie) || '-';
        const attribuePar = a.attribuePar ? esc(a.attribuePar.prenom + ' ' + a.attribuePar.nom) : '-';
        const dateRetour = a.dateRetour ? fmtDate(a.dateRetour) : '<span class="text-gray-400">-</span>';
        const raison = a.raisonRetour ? esc(a.raisonRetour) : '-';
        const statut = a.dateRetour
          ? '<span class="text-xs font-semibold px-2 py-1 bg-green-100 text-green-800">Cloturee</span>'
          : '<span class="text-xs font-semibold px-2 py-1 bg-blue-100 text-blue-800">En cours</span>';
        let actions = '<span class="text-gray-300">-</span>';
        if (!a.dateRetour) {
          actions = `<form data-act="retour" data-id="${a.id}" class="flex gap-1 items-center">
                        <input type="text" name="raisonRetour" placeholder="Motif (optionnel)" class="w-36 px-2 py-1 border border-gray-300 text-xs focus:outline-none focus:border-gray-600"/>
                        <button type="submit" class="px-2 py-1 bg-amber-600 text-white text-xs font-semibold hover:bg-amber-700 transition-colors">Retour</button>
                      </form>`;
        }
        return `<tr class="border-t border-gray-200 hover:bg-gray-50">
              <td class="px-4 py-3">${esc(eq)}</td>
              <td class="px-4 py-3 text-gray-500">${esc(ns)}</td>
              <td class="px-4 py-3">${esc(a.personnel ? a.personnel.prenom + ' ' + a.personnel.nom : '')}</td>
              <td class="px-4 py-3 text-gray-600">${attribuePar}</td>
              <td class="px-4 py-3 text-gray-600">${fmtDate(a.dateAffectation)}</td>
              <td class="px-4 py-3 text-gray-600">${dateRetour}</td>
              <td class="px-4 py-3 text-gray-500 max-w-xs truncate">${raison}</td>
              <td class="px-4 py-3">${statut}</td>
              <td class="px-4 py-3">${actions}</td>
            </tr>`;
      }).join('');
    }
    document.getElementById('pagination').innerHTML = paginationNav(data.total, data.max, data.offset, baseUrl());
  }

  document.getElementById('search-form').addEventListener('submit', (e) => {
    e.preventDefault();
    q = qInput.value.trim();
    typeId = typeSel.value;
    offset = '0';
    window.location = baseUrl() + `&max=${max}`;
  });

  document.getElementById('rows').addEventListener('submit', async (e) => {
    const form = e.target.closest('[data-act="retour"]');
    if (!form) return;
    e.preventDefault();
    if (form.dataset.submitting === '1') return;
    form.dataset.submitting = '1';
    const button = form.querySelector('button[type=submit]');
    if (button) button.disabled = true;
    const raison = form.querySelector('input[name=raisonRetour]').value;
    try {
      await Api.post(`/api/admin/affectations/${form.dataset.id}/retour`, { raisonRetour: raison });
      Flash.set('Retour enregistre', 'success');
      window.location.reload();
    } catch (err) {
      const msg = 'Erreur lors du retour : ' + (err.message || 'Erreur inconnue');
      const flash = document.getElementById('flash-container');
      if (flash && typeof mountAlert === 'function') {
        mountAlert(flash, msg, 'error');
        flash.scrollIntoView({ behavior: 'smooth', block: 'start' });
      } else {
        alert(msg);
      }
      delete form.dataset.submitting;
      if (button) button.disabled = false;
    }
  });

  load().catch((err) => {
    const msg = 'Erreur lors du chargement de l\'historique : ' + (err.message || 'Erreur inconnue');
    const flash = document.getElementById('flash-container');
    if (flash && typeof mountAlert === 'function') mountAlert(flash, msg, 'error');
    document.getElementById('rows').innerHTML = emptyRow(9, msg);
  });
};