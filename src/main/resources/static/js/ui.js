/* Utilitaires d'interface partages */
function esc(s) {
  return String(s == null ? '' : s).replace(/[&<>"']/g, (c) => ({
    '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;'
  }[c]));
}

function fmtDate(d) {
  if (!d) return '—';
  const dt = new Date(d);
  if (isNaN(dt.getTime())) return '—';
  return dt.toLocaleDateString('fr-FR', { day: '2-digit', month: '2-digit', year: 'numeric' }) + ' ' +
    dt.toLocaleTimeString('fr-FR', { hour: '2-digit', minute: '2-digit' });
}

function fmtDateJour(d) {
  if (!d) return '—';
  const dt = new Date(d);
  if (isNaN(dt.getTime())) return '—';
  return dt.toLocaleDateString('fr-FR', { day: '2-digit', month: '2-digit', year: 'numeric' });
}

function fmtDateTime(d) {
  if (!d) return '—';
  const dt = new Date(d);
  if (isNaN(dt.getTime())) return '—';
  return dt.toLocaleDateString('fr-FR', { day: '2-digit', month: '2-digit', year: 'numeric' }) + ' ' +
    dt.toLocaleTimeString('fr-FR', { hour: '2-digit', minute: '2-digit', second: '2-digit' });
}

function badgeEtat(etat) {
  const map = {
    'DISPONIBLE': ['bg-green-100 text-green-800', 'Disponible'],
    'AFFECTE': ['bg-blue-100 text-blue-800', 'Affecte'],
    'EN_PANNE': ['bg-red-100 text-red-800', 'En panne'],
    'HORS_SERVICE': ['bg-gray-100 text-gray-800', 'Hors service'],
    'REPARE': ['bg-yellow-100 text-yellow-800', 'Repare']
  };
  const e = map[etat] || ['bg-gray-100 text-gray-800', etat || ''];
  return `<span class="text-xs font-semibold px-2 py-1 ${e[0]}">${esc(e[1])}</span>`;
}

function paginationNav(total, max, offset, base) {
  const pages = Math.max(1, Math.ceil(total / max));
  const current = Math.floor(offset / max) + 1;
  if (pages <= 1) return '';
  const url = (o) => `${base}${base.includes('?') ? '&' : '?'}max=${max}&offset=${o}`;
  let html = '<div class="flex items-center justify-between mt-4 px-1">';
  html += `<span class="text-xs text-gray-500">${total} resultat(s) &mdash; Page ${current}/${pages}</span>`;
  html += '<div class="flex gap-1">';
  if (current > 1) html += `<a href="${url(offset - max)}" class="px-2 py-1 border border-gray-300 text-gray-700 text-xs hover:bg-gray-100">Precedent</a>`;
  if (current < pages) html += `<a href="${url(offset + max)}" class="px-2 py-1 border border-gray-300 text-gray-700 text-xs hover:bg-gray-100">Suivant</a>`;
  html += '</div></div>';
  return html;
}

function emptyRow(colspan, message) {
  return `<tr><td colspan="${colspan}" class="px-4 py-6 text-center text-sm text-gray-500">${esc(message)}</td></tr>`;
}

/* Empeche la double soumission d'un formulaire : bouton desactive pendant le traitement */
async function submitGuard(form, handler) {
  form.addEventListener('submit', async (e) => {
    e.preventDefault();
    if (form.dataset.submitting === '1') return;
    const button = form.querySelector('button[type=submit]');
    form.dataset.submitting = '1';
    if (button) button.disabled = true;
    try {
      await handler(e);
    } finally {
      delete form.dataset.submitting;
      if (button) button.disabled = false;
    }
  });
}