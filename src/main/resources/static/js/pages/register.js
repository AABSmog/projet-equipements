/* Page d'inscription */
window.PAGE_INIT = function () {
  const form = document.getElementById('register-form');
  const errorBox = document.getElementById('reg-error');
  if (!form) return;

  form.addEventListener('submit', async (e) => {
    e.preventDefault();
    hideError(errorBox);
    const payload = {
      prenom: document.getElementById('prenom').value.trim(),
      nom: document.getElementById('nom').value.trim(),
      email: document.getElementById('email').value.trim(),
      motDePasse: document.getElementById('password').value
    };
    form.querySelector('button[type=submit]').disabled = true;
    try {
      await Api.post('/api/auth/register', payload);
      Flash.set('Compte cree avec succes. Connectez-vous avec votre email: ' + payload.email, 'success');
      window.location = '/index.html';
    } catch (err) {
      showError(errorBox, err.message);
      form.querySelector('button[type=submit]').disabled = false;
    }
  });
};