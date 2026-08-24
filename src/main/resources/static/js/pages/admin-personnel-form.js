/* Formulaire personnel (creation / modification) */
window.PAGE_INIT = async function () {
  const params = new URLSearchParams(window.location.search);
  const id = params.get('id');
  const isEdit = !!id;
  const form = document.getElementById('personnel-form');
  const errorBox = document.getElementById('form-error');

  const meta = await Api.get('/api/meta');
  const roleSel = document.getElementById('role');
  roleSel.innerHTML = (meta.roles || []).map(r =>
    `<option value="${esc(r.value)}">${esc(r.label)}</option>`).join('');

  if (isEdit) {
    const p = await Api.get(`/api/admin/personnels/${id}`);
    document.getElementById('prenom').value = p.prenom || '';
    document.getElementById('nom').value = p.nom || '';
    document.getElementById('email').value = p.email || '';
    roleSel.value = p.role;
  }

  form.addEventListener('submit', async (ev) => {
    ev.preventDefault();
    hideError(errorBox);
    const payload = {
      prenom: document.getElementById('prenom').value.trim(),
      nom: document.getElementById('nom').value.trim(),
      email: document.getElementById('email').value.trim(),
      role: roleSel.value
    };
    const pw = document.getElementById('motDePasse').value;
    if (pw) payload.motDePasse = pw;
    form.querySelector('button[type=submit]').disabled = true;
    try {
      if (isEdit) {
        await Api.put(`/api/admin/personnels/${id}`, payload);
        Flash.set('Membre modifie', 'success');
      } else {
        await Api.post('/api/admin/personnels', payload);
        Flash.set('Membre cree', 'success');
      }
      window.location = '/admin/personnels.html';
    } catch (err) {
      showError(errorBox, err.message);
      form.querySelector('button[type=submit]').disabled = false;
    }
  });
};