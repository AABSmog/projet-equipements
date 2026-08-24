/* Page de connexion */
window.PAGE_INIT = function () {
  const form = document.getElementById('login-form');
  const errorBox = document.getElementById('login-error');
  if (!form) return;

  form.addEventListener('submit', async (e) => {
    e.preventDefault();
    hideError(errorBox);
    const email = document.getElementById('email').value.trim();
    const password = document.getElementById('password').value;
    form.querySelector('button[type=submit]').disabled = true;
    try {
      await Api.post('/api/auth/login', { email, password });
      const user = Api.user();
      window.location = user.role === 'ADMIN' ? '/admin/index.html' : '/app/equipements.html';
    } catch (err) {
      showError(errorBox, err.message);
      form.querySelector('button[type=submit]').disabled = false;
    }
  });
};