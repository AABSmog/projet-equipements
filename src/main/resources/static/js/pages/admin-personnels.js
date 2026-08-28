/* Liste du personnel (administration) */
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
    return '/admin/personnels.html?' + p.toString();
  }

  function roleBadge(role) {
    const isAdmin = role === 'ADMIN';
    const badge = isAdmin ? 'bg-maroon text-white' : 'bg-gray-200 text-gray-700';
    const label = isAdmin ? 'Administrateur' : 'Utilisateur';
    return `<span class="text-xs font-semibold px-2 py-1 ${badge}">${label}</span>`;
  }

  async function load() {
    const p = new URLSearchParams();
    if (q) p.set('q', q);
    p.set('max', max);
    p.set('offset', offset);
    const data = await Api.get('/api/admin/personnels?' + p.toString());
    const rows = document.getElementById('rows');
    if (!data.items || !data.items.length) {
      rows.innerHTML = emptyRow(5, 'Aucun personnel');
    } else {
      rows.innerHTML = data.items.map((p) => `
        <tr class="border-t border-gray-200 hover:bg-gray-50">
          <td class="px-4 py-3 font-medium">${esc(p.nom)}</td>
          <td class="px-4 py-3">${esc(p.prenom)}</td>
          <td class="px-4 py-3 text-gray-600">${esc(p.email)}</td>
          <td class="px-4 py-3">${roleBadge(p.role)}</td>
          <td class="px-4 py-3 whitespace-nowrap">
            <a href="/admin/personnel-edit.html?id=${p.id}" class="text-gray-600 hover:text-gray-900 mr-3">Modifier</a>
            <button data-act="delete" data-id="${p.id}" data-name="${esc(p.prenom)} ${esc(p.nom)}" class="text-red-600 hover:text-red-800 text-sm bg-transparent border-0 p-0 cursor-pointer">Supprimer</button>
          </td>
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

  document.getElementById('rows').addEventListener('click', async (e) => {
    const btn = e.target.closest('[data-act="delete"]');
    if (!btn) return;
    if (!confirm('Supprimer ce personnel ?')) return;
    try {
      await Api.del(`/api/admin/personnels/${btn.dataset.id}`);
      Flash.set('Membre supprime', 'success');
      window.location.reload();
    } catch (err) {
      const msg = 'Erreur lors de la suppression : ' + (err.message || 'Erreur inconnue');
      const flash = document.getElementById('flash-container');
      if (flash && typeof mountAlert === 'function') {
        mountAlert(flash, msg, 'error');
        flash.scrollIntoView({ behavior: 'smooth', block: 'start' });
      } else {
        alert(msg);
      }
    }
  });

  load().catch((err) => {
    const msg = 'Erreur lors du chargement du personnel : ' + (err.message || 'Erreur inconnue');
    const flash = document.getElementById('flash-container');
    if (flash && typeof mountAlert === 'function') mountAlert(flash, msg, 'error');
    document.getElementById('rows').innerHTML = emptyRow(5, msg);
  });
};