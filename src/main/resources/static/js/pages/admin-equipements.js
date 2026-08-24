/* Liste des equipements (administration) */
window.PAGE_INIT = function () {
  const params = new URLSearchParams(window.location.search);
  let q = params.get('q') || '';
  let etat = params.get('etat') || '';
  const max = '10';
  let offset = params.get('offset') || '0';

  const qInput = document.getElementById('q');
  const etatSel = document.getElementById('etat');
  qInput.value = q;

  const clearLink = document.getElementById('clear-filters');
  if (clearLink) clearLink.classList.toggle('hidden', !(q || etat));

  function baseUrl() {
    const p = new URLSearchParams();
    if (q) p.set('q', q);
    if (etat) p.set('etat', etat);
    return '/admin/equipements.html?' + p.toString();
  }

  async function load() {
    const p = new URLSearchParams();
    if (q) p.set('q', q);
    if (etat) p.set('etat', etat);
    p.set('max', max);
    p.set('offset', offset);
    const data = await Api.get('/api/admin/equipements?' + p.toString());
    etatSel.innerHTML = '<option value="">Tous</option>' + (data.etats || []).map(e =>
      `<option value="${esc(e.value)}" ${e.value === etat ? 'selected' : ''}>${esc(e.label)}</option>`).join('');

    const rows = document.getElementById('rows');
    if (!data.items || !data.items.length) {
      rows.innerHTML = emptyRow(6, 'Aucun equipement');
    } else {
      rows.innerHTML = data.items.map((e) => {
        let actions = `<a href="/admin/equipement-edit.html?id=${e.id}" class="text-gray-600 hover:text-gray-900 mr-3">Modifier</a>`;
        if (e.etat === 'AFFECTE') {
          actions += `<form data-act="desaffecter" data-id="${e.id}" class="inline mr-3"><button type="submit" class="text-orange-600 hover:text-orange-800 text-sm bg-transparent border-0 p-0 cursor-pointer">Desaffecter</button></form>`;
        }
        actions += `<form data-act="declasser" data-id="${e.id}" class="inline"><button type="submit" class="text-red-600 hover:text-red-800 text-sm bg-transparent border-0 p-0 cursor-pointer">Declasser</button></form>`;
        return `<tr class="border-t border-gray-200 hover:bg-gray-50">
              <td class="px-4 py-3 font-medium">${esc(e.type && e.type.nom)}</td>
              <td class="px-4 py-3 text-gray-500">${esc(e.numeroSerie)}</td>
              <td class="px-4 py-3 text-gray-600">${esc(e.description)}</td>
              <td class="px-4 py-3">${badgeEtat(e.etat)}</td>
              <td class="px-4 py-3 text-gray-600">${e.affecte ? esc(e.affecte.prenom + ' ' + e.affecte.nom) : '<span class="text-gray-400">-</span>'}</td>
              <td class="px-4 py-3 whitespace-nowrap">${actions}</td>
            </tr>`;
      }).join('');
    }
    document.getElementById('pagination').innerHTML = paginationNav(data.total, data.max, data.offset, baseUrl());
  }

  document.getElementById('search-form').addEventListener('submit', (e) => {
    e.preventDefault();
    q = qInput.value.trim();
    etat = etatSel.value;
    offset = '0';
    window.location = baseUrl() + `&max=${max}`;
  });

  document.getElementById('rows').addEventListener('submit', async (e) => {
    const form = e.target.closest('[data-act]');
    if (!form) return;
    e.preventDefault();
    const id = form.dataset.id;
    const act = form.dataset.act;
    if (act === 'desaffecter') {
      if (!confirm('Desaffecter cet equipement ?')) return;
      await Api.post(`/api/admin/equipements/${id}/desaffecter`);
      Flash.set('Equipement desaffecte', 'success');
    } else if (act === 'declasser') {
      if (!confirm('Declasser cet equipement ?')) return;
      await Api.post(`/api/admin/equipements/${id}/declasser`);
      Flash.set('Equipement declasse hors service', 'success');
    }
    window.location.reload();
  });

  load().catch((err) => {
    document.getElementById('rows').innerHTML = emptyRow(6, err.message);
  });
};