/* Journal d'audit */
window.PAGE_INIT = function () {
  const params = new URLSearchParams(window.location.search);
  let q = params.get('q') || '';
  const max = '20';
  let offset = params.get('offset') || '0';

  const qInput = document.getElementById('q');
  qInput.value = q;

  const clearLink = document.getElementById('clear-filters');
  if (clearLink) clearLink.classList.toggle('hidden', !q);

  function baseUrl() {
    const p = new URLSearchParams();
    if (q) p.set('q', q);
    return '/admin/audit.html?' + p.toString();
  }

  async function load() {
    const p = new URLSearchParams();
    if (q) p.set('q', q);
    p.set('max', max);
    p.set('offset', offset);
    const data = await Api.get('/api/admin/audit?' + p.toString());
    const rows = document.getElementById('rows');
    if (!data.items || !data.items.length) {
      rows.innerHTML = emptyRow(5, 'Aucune trace d\'audit');
    } else {
      rows.innerHTML = data.items.map((a) => `
        <tr class="border-t border-gray-200 hover:bg-gray-50">
          <td class="px-4 py-3 text-gray-500">${fmtDateTime(a.dateCreated)}</td>
          <td class="px-4 py-3">${esc(a.utilisateur)}</td>
          <td class="px-4 py-3"><span class="text-xs font-semibold px-2 py-1 bg-gray-100 text-gray-700">${esc(a.action)}</span></td>
          <td class="px-4 py-3">${esc(a.cible)}${a.cibleId ? ' #' + a.cibleId : ''}</td>
          <td class="px-4 py-3 text-gray-600 max-w-md truncate">${esc(a.details)}</td>
        </tr>`).join('');
    }
    document.getElementById('pagination').innerHTML = paginationNav(data.total, data.max, data.offset, baseUrl());
  }

  document.getElementById('search-form').addEventListener('submit', (e) => {
    e.preventDefault();
    q = qInput.value.trim();
    offset = '0';
    window.location = baseUrl() + `&max=${max}`;
  });

  load().catch((err) => {
    document.getElementById('rows').innerHTML = emptyRow(5, err.message);
  });
};