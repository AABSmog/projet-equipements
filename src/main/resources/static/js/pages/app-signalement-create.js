/* Creation d'un signalement (espace utilisateur) */
window.PAGE_INIT = async function () {
  const params = new URLSearchParams(window.location.search);
  const equipementId = params.get('equipementId');
  const form = document.getElementById('signalement-form');
  const errorBox = document.getElementById('form-error');
  const typeSel = document.getElementById('type');
  const showBack = `/app/equipement-show.html?id=${equipementId}`;
  document.getElementById('back-link').href = showBack;
  document.getElementById('cancel-link').href = showBack;

  const data = await Api.get(`/api/app/signalements/creation?equipementId=${equipementId}`);
  const e = data.equipement || {};
  document.getElementById('equipement-info').innerHTML =
    `<div class="text-sm text-gray-600"><span class="font-semibold text-gray-900">${esc(e.type ? e.type.nom : '')}</span>${e.description ? ' - ' + esc(e.description) : ''}</div>`;

  typeSel.innerHTML = (data.types || []).map(t =>
    `<option value="${esc(t.value)}">${esc(t.label)}</option>`).join('');

  form.addEventListener('submit', async (ev) => {
    ev.preventDefault();
    hideError(errorBox);
    const payload = {
      equipementId: equipementId,
      type: typeSel.value,
      description: document.getElementById('description').value.trim()
    };
    form.querySelector('button[type=submit]').disabled = true;
    try {
      await Api.post('/api/app/signalements', payload);
      Flash.set('Signalement envoye', 'success');
      window.location = '/app/signalements.html';
    } catch (err) {
      showError(errorBox, err.message);
      form.querySelector('button[type=submit]').disabled = false;
    }
  });
};