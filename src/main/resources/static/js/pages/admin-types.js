/* Types d'equipement : liste en lecture seule (les types se creent via "Autre..." dans le formulaire equipement) */
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
    return '/admin/types.html?' + p.toString();
  }

  async function load() {
    const p = new URLSearchParams();
    if (q) p.set('q', q);
    p.set('max', max);
    p.set('offset', offset);
    const data = await Api.get('/api/admin/types/list?' + p.toString());
    const rows = document.getElementById('rows');
    if (!data.items || !data.items.length) {
      rows.innerHTML = emptyRow(2, 'Aucun type');
    } else {
      rows.innerHTML = data.items.map((t) => `
        <tr class="border-t border-gray-200 hover:bg-gray-50">
          <td class="px-4 py-3 font-medium">${esc(t.nom)}</td>
          <td class="px-4 py-3 text-gray-400 text-xs">(cree via equipement)</td>
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
    const msg = 'Erreur lors du chargement des types : ' + (err.message || 'Erreur inconnue');
    const flash = document.getElementById('flash-container');
    if (flash && typeof mountAlert === 'function') mountAlert(flash, msg, 'error');
    document.getElementById('rows').innerHTML = emptyRow(2, msg);
  });
};