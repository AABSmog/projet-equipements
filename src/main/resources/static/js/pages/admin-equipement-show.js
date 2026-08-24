/* Detail equipement (administration) */
window.PAGE_INIT = async function () {
  const params = new URLSearchParams(window.location.search);
  const id = params.get('id');

  const data = await Api.get(`/api/admin/equipements/${id}`);
  document.getElementById('title').textContent =
    `${data.type ? data.type.nom : ''} ${data.numeroSerie ? ' - ' + data.numeroSerie : ''}`.trim();

  document.getElementById('info').innerHTML =
    `<div><dt class="text-gray-500">Numero de serie</dt><dd class="font-mono">${esc(data.numeroSerie)}</dd></div>
     <div><dt class="text-gray-500">Type</dt><dd>${esc(data.type && data.type.nom)}</dd></div>
     <div><dt class="text-gray-500">Etat</dt><dd>${badgeEtat(data.etat)}</dd></div>
     <div><dt class="text-gray-500">Description</dt><dd>${esc(data.description)}</dd></div>`;

  const aff = document.getElementById('affectation');
  if (data.affecte) {
    const a = data.affecte;
    aff.innerHTML =
      `<div><dt class="text-gray-500">Affecte a</dt><dd>${esc(a.personnel ? a.personnel.prenom + ' ' + a.personnel.nom : '—')}</dd></div>
       <div><dt class="text-gray-500">Email</dt><dd>${esc(a.personnel && a.personnel.email)}</dd></div>
       <div><dt class="text-gray-500">Date d'affectation</dt><dd>${fmtDateJour(a.dateAffectation)}</dd></div>`;
  } else {
    aff.innerHTML = '<p class="text-gray-500">Aucune affectation en cours.</p>';
  }

  const sig = document.getElementById('signalements');
  if (!data.signalements || !data.signalements.length) {
    sig.innerHTML = '<p class="text-gray-500">Aucun signalement.</p>';
  } else {
    sig.innerHTML = data.signalements.map((s) =>
      `<div class="border border-gray-100 p-3 mb-2">
        <div class="flex items-center justify-between">
          <span class="font-semibold">${esc(s.typeLabel || s.type)}</span>
          <span class="text-xs text-gray-500">${fmtDate(s.dateCreated)}</span>
        </div>
        <p class="text-gray-700 mt-1">${esc(s.description)}</p>
        <p class="text-xs text-gray-500 mt-1">Par ${esc(s.personnel ? s.personnel.prenom + ' ' + s.personnel.nom : '—')}</p>
       </div>`).join('');
  }

  const actions = document.getElementById('actions');
  let html = `<a href="/admin/equipement-edit.html?id=${id}" class="px-3 py-1.5 border border-gray-300 text-sm hover:bg-gray-100">Modifier</a>`;
  if (data.etat === 'AFFECTE') {
    html += `<button data-act="desaffecter" class="px-3 py-1.5 border border-gray-300 text-sm hover:bg-gray-100 cursor-pointer bg-white">Desaffecter</button>`;
  }
  if (data.etat !== 'HORS_SERVICE') {
    html += `<button data-act="declasser" class="px-3 py-1.5 border border-gray-300 text-sm hover:bg-gray-100 cursor-pointer bg-white">Declasser</button>`;
  }
  html += `<button data-act="delete" class="px-3 py-1.5 border border-red-300 text-red-700 text-sm hover:bg-red-50 cursor-pointer bg-white">Supprimer</button>`;
  actions.innerHTML = html;

  actions.addEventListener('click', async (e) => {
    const btn = e.target.closest('[data-act]');
    if (!btn) return;
    const act = btn.dataset.act;
    if (act === 'delete') {
      if (!confirm('Supprimer cet equipement ?')) return;
      await Api.del(`/api/admin/equipements/${id}`);
      Flash.set('Equipement supprime', 'success');
      window.location = '/admin/equipements.html';
    } else if (act === 'desaffecter') {
      if (!confirm('Desaffecter cet equipement ?')) return;
      await Api.post(`/api/admin/equipements/${id}/desaffecter`);
      Flash.set('Equipement desaffecte', 'success');
      window.location.reload();
    } else if (act === 'declasser') {
      if (!confirm('Declarer hors service ?')) return;
      await Api.post(`/api/admin/equipements/${id}/declasser`);
      Flash.set('Equipement declare hors service', 'success');
      window.location.reload();
    }
  });
};