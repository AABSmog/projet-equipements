/* Detail equipement (espace utilisateur) */
window.PAGE_INIT = async function () {
  const params = new URLSearchParams(window.location.search);
  const id = params.get('id');

  const data = await Api.get(`/api/app/equipements/${id}`);
  const e = data;
  document.getElementById('title').textContent =
    `${e.type ? e.type.nom : ''}${e.numeroSerie ? ' - ' + e.numeroSerie : ''}`;

  document.getElementById('info').innerHTML =
    `<div><dt class="text-gray-500 font-medium">Type</dt><dd class="text-gray-900 mb-2">${esc(e.type && e.type.nom)}</dd></div>
     <div><dt class="text-gray-500 font-medium">N° Serie</dt><dd class="text-gray-900 mb-2">${esc(e.numeroSerie)}</dd></div>
     <div><dt class="text-gray-500 font-medium">Description</dt><dd class="text-gray-900 mb-2">${esc(e.description)}</dd></div>
     <div><dt class="text-gray-500 font-medium">Etat</dt><dd class="mb-2">${badgeEtat(e.etat)}</dd></div>`;

  document.getElementById('signaler-btn').href = `/app/signalement-create.html?equipementId=${id}`;

  const sig = document.getElementById('signalements');
  if (!data.signalements || !data.signalements.length) {
    sig.innerHTML = `<tr><td colspan="3" class="px-4 py-8 text-center text-gray-400">Aucun signalement pour cet equipement</td></tr>`;
  } else {
    sig.innerHTML = data.signalements.map((s) => `
      <tr class="border-t border-gray-200 hover:bg-gray-50">
        <td class="px-4 py-3 text-gray-500">${fmtDateJour(s.dateCreated)}</td>
        <td class="px-4 py-3"><span class="text-xs font-semibold px-2 py-1 bg-red-100 text-red-800">${esc(s.typeLabel || s.type)}</span></td>
        <td class="px-4 py-3 text-gray-600">${esc(s.description)}</td>
      </tr>`).join('');
  }
};