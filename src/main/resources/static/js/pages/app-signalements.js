/* Mes signalements (espace utilisateur) */
window.PAGE_INIT = function () {
  const params = new URLSearchParams(window.location.search);
  let max = params.get('max') || '10';
  let offset = params.get('offset') || '0';

  function baseUrl() {
    return '/app/signalements.html?';
  }

  async function load() {
    const p = new URLSearchParams();
    p.set('max', max);
    p.set('offset', offset);
    const data = await Api.get('/api/app/signalements?' + p.toString());
    const rows = document.getElementById('rows');
    if (!data.items || !data.items.length) {
      rows.innerHTML = emptyRow(4, 'Aucun signalement');
    } else {
      rows.innerHTML = data.items.map((s) => {
        const e = s.equipement || {};
        const label = e.id ? ((e.type ? e.type.nom : '') + (e.numeroSerie ? ' - ' + e.numeroSerie : '')) : (s.infoEquipement || '');
        return `<tr class="border-t border-gray-200 hover:bg-gray-50">
              <td class="px-4 py-3 font-medium">${esc(label)}</td>
              <td class="px-4 py-3"><span class="text-xs font-semibold px-2 py-1 bg-red-100 text-red-800">${esc(s.typeLabel || s.type)}</span></td>
              <td class="px-4 py-3 text-gray-600">${esc(s.description)}</td>
              <td class="px-4 py-3 text-gray-500">${fmtDateJour(s.dateCreated)}</td>
            </tr>`;
      }).join('');
    }
    document.getElementById('pagination').innerHTML = paginationNav(data.total, data.max, data.offset, baseUrl());
  }

  load().catch((err) => {
    document.getElementById('rows').innerHTML = emptyRow(4, err.message);
  });
};