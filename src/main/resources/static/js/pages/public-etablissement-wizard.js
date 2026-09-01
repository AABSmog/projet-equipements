/* Wizard creation etablissement - 4 etapes */
window.PAGE_INIT = function () {
  let step = 1;
  const total = 4;
  const errorBox = document.getElementById('form-error');

  const etabNom = document.getElementById('etab-nom');
  const etabSlug = document.getElementById('etab-slug');
  const etabDomaine = document.getElementById('etab-domaine');
  const emailPreview = document.getElementById('email-preview');

  function slugify(s) {
    return s.toLowerCase().normalize('NFD').replace(/[\u0300-\u036f]/g, '').replace(/[^a-z0-9]+/g, '-').replace(/^-|-$/g, '');
  }

  etabNom.addEventListener('input', () => {
    if (!etabSlug.dataset.touched) etabSlug.value = slugify(etabNom.value);
    updatePreview();
  });
  etabSlug.addEventListener('input', () => { etabSlug.dataset.touched = '1'; });
  etabDomaine.addEventListener('input', updatePreview);
  function updatePreview() {
    const d = etabDomaine.value.trim() || 'exemple.com';
    emailPreview.textContent = 'prenom.nom@' + d;
  }

  // Admins
  const adminsList = document.getElementById('admins-list');
  function adminRow(data = {}) {
    const div = document.createElement('div');
    div.className = 'grid grid-cols-4 gap-2 items-end bg-gray-50 p-3 border border-gray-200';
    div.innerHTML = `
      <label class="text-xs">Nom *<input data-k="nom" value="${esc(data.nom||'')}" class="mt-1 w-full px-2 py-1 border border-gray-300 text-sm" placeholder="Diop"/></label>
      <label class="text-xs">Prenom *<input data-k="prenom" value="${esc(data.prenom||'')}" class="mt-1 w-full px-2 py-1 border border-gray-300 text-sm" placeholder="Mamadou"/></label>
      <label class="text-xs">Email (auto)<input data-k="email" value="${esc(data.email||'')}" class="mt-1 w-full px-2 py-1 border border-gray-300 text-sm" placeholder="auto"/></label>
      <div class="flex gap-1">
        <label class="text-xs flex-1">MDP *<input data-k="motDePasse" type="password" value="${esc(data.motDePasse||'')}" class="mt-1 w-full px-2 py-1 border border-gray-300 text-sm" placeholder="Admin123"/></label>
        <button type="button" data-act="remove" class="mb-1 px-2 text-red-600 text-xs">x</button>
      </div>
      <div class="col-span-4 text-xs text-gray-500 font-mono" data-preview></div>
    `;
    const update = () => {
      const p = div.querySelector('[data-k=prenom]').value.trim();
      const n = div.querySelector('[data-k=nom]').value.trim();
      const d = etabDomaine.value.trim() || 'exemple.com';
      const emailInput = div.querySelector('[data-k=email]');
      if (!emailInput.value.trim() && p && n) {
        const email = p.toLowerCase().replace(/[^a-z0-9]/g,'') + '.' + n.toLowerCase().replace(/[^a-z0-9]/g,'') + '@' + d;
        div.querySelector('[data-preview]').textContent = 'Apercu : ' + email.toLowerCase();
      } else {
        div.querySelector('[data-preview]').textContent = emailInput.value ? 'Email : ' + emailInput.value : '';
      }
    };
    div.querySelectorAll('input').forEach(i => i.addEventListener('input', update));
    div.querySelector('[data-act=remove]').addEventListener('click', () => div.remove());
    return div;
  }
  document.getElementById('add-admin').addEventListener('click', () => adminsList.appendChild(adminRow()));
  adminsList.appendChild(adminRow({nom:'Diop', prenom:'Mamadou', motDePasse:'Admin123'}))

  // Employes
  const empsList = document.getElementById('employes-list');
  function empRow(data={}) {
    const div = document.createElement('div');
    div.className = 'grid grid-cols-4 gap-2 items-end bg-gray-50 p-3 border border-gray-200';
    div.innerHTML = `
      <label class="text-xs">Nom<input data-k="nom" value="${esc(data.nom||'')}" class="mt-1 w-full px-2 py-1 border border-gray-300 text-sm"/></label>
      <label class="text-xs">Prenom<input data-k="prenom" value="${esc(data.prenom||'')}" class="mt-1 w-full px-2 py-1 border border-gray-300 text-sm"/></label>
      <label class="text-xs">Email (auto)<input data-k="email" value="${esc(data.email||'')}" class="mt-1 w-full px-2 py-1 border border-gray-300 text-sm"/></label>
      <div class="flex gap-1">
        <label class="text-xs flex-1">MDP<input data-k="motDePasse" type="password" value="${esc(data.motDePasse||'')}" class="mt-1 w-full px-2 py-1 border border-gray-300 text-sm" placeholder="User1234"/></label>
        <button type="button" data-act="remove" class="mb-1 px-2 text-red-600 text-xs">x</button>
      </div>
    `;
    div.querySelector('[data-act=remove]').addEventListener('click', () => div.remove());
    return div;
  }
  document.getElementById('add-emp').addEventListener('click', () => empsList.appendChild(empRow()));
  empsList.appendChild(empRow());

  // Regles par defaut
  const reglesPanel = document.getElementById('regles-panel');
  const defaultRegles = {
    'email.pattern': '{prenom}.{nom}@' + (etabDomaine.value || 'exemple.com'),
    'email.domaine': etabDomaine.value || 'exemple.com',
    'password.minLength': '8',
    'password.requireMaj': 'true',
    'password.requireMin': 'true',
    'password.requireChiffre': 'true',
    'password.requireSymbole': 'false'
  };
  function renderRegles() {
    reglesPanel.innerHTML = Object.entries(defaultRegles).map(([k,v]) => {
      const isBool = k.startsWith('password.require');
      if (isBool) {
        return `<label class="flex items-center gap-2"><input type="checkbox" data-regle="${k}" ${v==='true'?'checked':''}/> <span>${k}</span> <span class="text-gray-400 text-xs">(${v})</span></label>`;
      }
      return `<label class="flex items-center gap-2">${k} <input data-regle="${k}" value="${esc(v)}" class="ml-auto w-48 px-2 py-1 border border-gray-300 text-sm"/></label>`;
    }).join('');
  }
  renderRegles();
  etabDomaine.addEventListener('input', () => {
    defaultRegles['email.domaine'] = etabDomaine.value.trim() || 'exemple.com';
    defaultRegles['email.pattern'] = '{prenom}.{nom}@' + defaultRegles['email.domaine'];
    renderRegles();
  });

  // Navigation
  const btnPrev = document.getElementById('btn-prev');
  const btnNext = document.getElementById('btn-next');
  const btnCreate = document.getElementById('btn-create');

  function showStep(n) {
    step = n;
    document.querySelectorAll('[data-panel]').forEach(p => p.classList.toggle('hidden', p.dataset.panel !== String(n)));
    document.querySelectorAll('.step-dot').forEach(d => {
      const s = parseInt(d.dataset.step);
      d.className = s <= n ? 'step-dot w-8 h-8 rounded-full bg-gray-800 text-white flex items-center justify-center text-sm font-bold'
                          : 'step-dot w-8 h-8 rounded-full bg-gray-200 text-gray-600 flex items-center justify-center text-sm font-bold';
    });
    document.querySelectorAll('[data-progress]').forEach(p => {
      const s = parseInt(p.dataset.progress);
      p.style.width = s < n ? '100%' : '0%';
    });
    if (btnPrev) { btnPrev.classList.toggle('hidden', n === 1); btnPrev.style.display = (n === 1) ? 'none' : ''; }
    if (btnNext) { btnNext.classList.toggle('hidden', n === total); btnNext.style.display = (n === total) ? 'none' : ''; }
    if (btnCreate) { btnCreate.classList.toggle('hidden', n !== total); btnCreate.style.display = (n === total) ? 'inline-block' : 'none'; }
    if (n === 4) {
      try { updateRecap(); } catch(e) { console.error('updateRecap', e); }
    }
    hideError(errorBox);
  }

  function updateRecap() {
    const admins = [...adminsList.children].map(div => ({
      nom: div.querySelector('[data-k=nom]').value.trim(),
      prenom: div.querySelector('[data-k=prenom]').value.trim(),
      email: div.querySelector('[data-k=email]').value.trim() || '(auto)'
    }));
    const emps = [...empsList.children].map(div => ({
      nom: div.querySelector('[data-k=nom]').value.trim(),
      prenom: div.querySelector('[data-k=prenom]').value.trim()
    })).filter(e => e.nom || e.prenom);
    const regles = [...reglesPanel.querySelectorAll('[data-regle]')].map(i => `${i.dataset.regle}=${i.type==='checkbox'?i.checked:i.value}`).join(', ');
    document.getElementById('recap').innerHTML = `
      <div><b>Etablissement :</b> ${esc(etabNom.value)} (${esc(etabSlug.value)}) — domaine ${esc(etabDomaine.value||'exemple.com')}</div>
      <div><b>Admins :</b> ${admins.map(a=>esc(a.prenom+' '+a.nom+' <'+a.email+'>')).join(', ')}</div>
      <div><b>Employes :</b> ${emps.length ? emps.map(e=>esc(e.prenom+' '+e.nom)).join(', ') : 'aucun'}</div>
      <div><b>Regles :</b> ${esc(regles)}</div>
    `;
  }

  btnPrev.addEventListener('click', () => showStep(step - 1));
  btnNext.addEventListener('click', () => {
    hideError(errorBox);
    if (step === 1) {
      if (!etabNom.value.trim()) return showError(errorBox, 'Le nom de l\'etablissement est obligatoire');
      if (!etabSlug.value.trim()) return showError(errorBox, 'Le slug est obligatoire');
    }
    if (step === 2) {
      const admins = [...adminsList.children];
      if (!admins.length) return showError(errorBox, 'Ajoutez au moins un administrateur');
      for (const div of admins) {
        if (!div.querySelector('[data-k=nom]').value.trim() || !div.querySelector('[data-k=prenom]').value.trim()) {
          return showError(errorBox, 'Chaque admin doit avoir nom et prenom');
        }
        if (!div.querySelector('[data-k=motDePasse]').value.trim()) return showError(errorBox, 'Mot de passe admin obligatoire');
      }
    }
    showStep(step + 1);
  });

  async function doCreate(btn) {
    hideError(errorBox);
    const target = btn || btnCreate;
    target.disabled = true;
    const origText = target.textContent;
    target.textContent = 'Creation...';
    const altBtn = document.getElementById('btn-create-alt');
    const altOrig = altBtn ? altBtn.textContent : '';
    if (altBtn) { altBtn.disabled = true; altBtn.textContent = 'Creation...'; }
    const admins = [...adminsList.children].map(div => ({
      nom: div.querySelector('[data-k=nom]').value.trim(),
      prenom: div.querySelector('[data-k=prenom]').value.trim(),
      email: div.querySelector('[data-k=email]').value.trim(),
      motDePasse: div.querySelector('[data-k=motDePasse]').value
    }));
    const employes = [...empsList.children].map(div => ({
      nom: div.querySelector('[data-k=nom]').value.trim(),
      prenom: div.querySelector('[data-k=prenom]').value.trim(),
      email: div.querySelector('[data-k=email]').value.trim(),
      motDePasse: div.querySelector('[data-k=motDePasse]').value
    })).filter(e => e.nom || e.prenom);
    const regles = {};
    reglesPanel.querySelectorAll('[data-regle]').forEach(i => regles[i.dataset.regle] = i.type==='checkbox' ? String(i.checked) : i.value);
    try {
      const res = await Api.post('/api/etablissements/wizard', {
        etablissement: { nom: etabNom.value.trim(), slug: etabSlug.value.trim(), domaineEmail: etabDomaine.value.trim() },
        admins, employes, regles
      });
      Flash.set('Etablissement cree — connectez-vous', 'success');
      const slug = res.etablissement ? res.etablissement.slug : etabSlug.value.trim();
      window.location = '/index.html?etablissement=' + encodeURIComponent(slug);
    } catch (err) {
      showError(errorBox, err.message);
    } finally {
      target.disabled = false;
      target.textContent = origText;
      if (altBtn) { altBtn.disabled = false; altBtn.textContent = altOrig; }
    }
  }
  btnCreate.addEventListener('click', () => doCreate(btnCreate));
  const btnAlt = document.getElementById('btn-create-alt');
  if (btnAlt) btnAlt.addEventListener('click', () => doCreate(btnAlt));

  showStep(1);
};
// Auto-init pour la page publique sans app.js
if (document.readyState === 'loading') {
  document.addEventListener('DOMContentLoaded', () => { if (window.PAGE_INIT && !window._wizardDone) { window._wizardDone = true; window.PAGE_INIT(); } });
} else {
  if (!window._wizardDone) { window._wizardDone = true; window.PAGE_INIT(); }
}
