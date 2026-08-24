/* Liste des signalements (administration) */
window.PAGE_INIT = function () {
  const params = new URLSearchParams(window.location.search);
  let q = params.get('q') || '';
  const max = '10';
  let offset = params.get('offset') || '0';

  const qInput = document.getElementById('q');
  qInput.value = q;

  const clearLink = document.getElementById('clear-filters');
  if (clearLink) clearLink.classList.toggle('hidden', !q);

  function baseUrl() {
    const p = new URLSearchParams();
    if (q) p.set('q', q);
    return '/admin/signalements.html?' + p.toString();
  }

  async function load() {
    const p = new URLSearchParams();
    if (q) p.set('q', q);
    p.set('max', max);
    p.set('offset', offset);
    const data = await Api.get('/api/admin/signalements?' + p.toString());
    const rows = document.getElementById('rows');
    if (!data.items || !data.items.length) {
      rows.innerHTML = emptyRow(6, 'Aucun signalement');
    } else {
      rows.innerHTML = data.items.map((s) => {
        const eq = (s.equipement && s.equipement.type && s.equipement.type.nom) || s.infoEquipement || '-';
        return `<tr class="border-t border-gray-200 hover:bg-gray-50">
              <td class="px-4 py-3">${esc(eq)}</td>
              <td class="px-4 py-3">${esc(s.personnel ? s.personnel.prenom + ' ' + s.personnel.nom : '')}</td>
              <td class="px-4 py-3"><span class="text-xs font-semibold px-2 py-1 bg-red-100 text-red-800">${esc(s.typeLabel || s.type)}</span></td>
              <td class="px-4 py-3 text-gray-600 max-w-xs truncate">${esc(s.description)}</td>
              <td class="px-4 py-3 text-gray-500">${fmtDateJour(s.dateCreated)}</td>
              <td class="px-4 py-3">
                <button data-act="delete" data-id="${s.id}" class="text-red-600 hover:text-red-800 text-sm bg-transparent border-0 p-0 cursor-pointer">Supprimer</button>
              </td>
            </tr>`;
      }).join('');
    }
    document.getElementById('pagination').innerHTML = paginationNav(data.total, data.max, data.offset, baseUrl());
  }

  document.getElementById('search-form').addEventListener('submit', (e) => {
    e.preventDefault();
    q = qInput.value.trim();
    offset = '0';
    window.location = baseUrl() + `&max=${max}`;
  });

  document.getElementById('rows').addEventListener('click', async (e) => {
    const btn = e.target.closest('[data-act="delete"]');
    if (!btn) return;
    if (!confirm('Supprimer ce signalement ?')) return;
    await Api.del(`/api/admin/signalements/${btn.dataset.id}`);
    Flash.set('Signalement supprime', 'success');
    window.location.reload();
  });

  load().catch((err) => {
    document.getElementById('rows').innerHTML = emptyRow(6, err.message);
  });
};