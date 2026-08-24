/* Mes equipements (espace utilisateur) */
window.PAGE_INIT = function () {
  const params = new URLSearchParams(window.location.search);
  let max = params.get('max') || '10';
  let offset = params.get('offset') || '0';

  function baseUrl() {
    return '/app/equipements.html?';
  }

  async function load() {
    const p = new URLSearchParams();
    p.set('max', max);
    p.set('offset', offset);
    const data = await Api.get('/api/app/equipements?' + p.toString());
    const rows = document.getElementById('rows');
    if (!data.items || !data.items.length) {
      rows.innerHTML = emptyRow(6, 'Aucun equipement ne vous est affecte');
    } else {
      rows.innerHTML = data.items.filter(a => a.equipement).map((a) => {
        const e = a.equipement;
        return `<tr class="border-t border-gray-200 hover:bg-gray-50">
              <td class="px-4 py-3 font-medium">${esc(e.type ? e.type.nom : '')}</td>
              <td class="px-4 py-3 text-gray-500">${esc(e.numeroSerie)}</td>
              <td class="px-4 py-3 text-gray-600 max-w-xs truncate">${esc(e.description)}</td>
              <td class="px-4 py-3 text-gray-600">${fmtDateJour(a.dateAffectation)}</td>
              <td class="px-4 py-3">${badgeEtat(e.etat)}</td>
              <td class="px-4 py-3">
                <a href="/app/equipement-show.html?id=${e.id}" class="text-blue-600 hover:text-blue-800 text-sm font-semibold">Voir</a>
                <a href="/app/signalement-create.html?equipementId=${e.id}" class="ml-3 text-red-600 hover:text-red-800 text-sm font-semibold">Signaler</a>
                <button data-act="retour" data-id="${e.id}" class="ml-3 text-green-600 hover:text-green-800 text-sm font-semibold bg-transparent border-0 p-0 cursor-pointer">Retourner</button>
              </td>
            </tr>`;
      }).join('');
    }
    document.getElementById('pagination').innerHTML = paginationNav(data.total, data.max, data.offset, baseUrl());
  }

  document.getElementById('rows').addEventListener('click', async (e) => {
    const btn = e.target.closest('[data-act="retour"]');
    if (!btn) return;
    if (!confirm('Retourner cet equipement ?')) return;
    try {
      await Api.post(`/api/app/equipements/${btn.dataset.id}/retour`, { raisonRetour: "Retourne par l'employe" });
      Flash.set('Retour enregistre', 'success');
      window.location.reload();
    } catch (err) {
      alert(err.message);
    }
  });

  load().catch((err) => {
    document.getElementById('rows').innerHTML = emptyRow(6, err.message);
  });
};