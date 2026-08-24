/* Module API : session, jeton CSRF, requetes JSON */
const Api = (() => {
  let csrfToken = null;
  let currentUser = null;

  /* Journalisation : active uniquement en local, secrets masques, groupe repliable par appel */
  const ApiLog = {
    enabled: /^(localhost|127\.0\.0\.1)$/.test(window.location.hostname),
    secretPattern: /(password|motdepasse|token|csrf)/i,
    sanitize(data) {
      if (Array.isArray(data)) return data.map((v) => this.sanitize(v));
      if (data && typeof data === 'object') {
        return Object.fromEntries(Object.entries(data).map(([k, v]) =>
          [k, this.secretPattern.test(k) ? '***' : this.sanitize(v)]));
      }
      return data;
    },
    begin(method, url, body) {
      if (!this.enabled) return null;
      console.groupCollapsed(`%c[API] ${method} ${url}`, 'color:#6b7280');
      if (body) console.log('body :', this.sanitize(body));
      return performance.now();
    },
    end(startedAt, status) {
      if (startedAt === null) return;
      console.log(`status : ${status} (${Math.round(performance.now() - startedAt)} ms)`);
      console.groupEnd();
    },
    fail(startedAt, method, url, err) {
      if (startedAt === null) return;
      console.error(`echec reseau : ${method} ${url}`, err);
      console.groupEnd();
    }
  };

  async function initSession() {
    const startedAt = ApiLog.begin('GET', '/api/auth/session');
    const res = await fetch('/api/auth/session', { headers: { 'Accept': 'application/json' } });
    const data = await res.json();
    csrfToken = data.csrfToken;
    currentUser = data.user;
    ApiLog.end(startedAt, res.status);
    return { user: currentUser, csrfToken };
  }

  async function request(method, url, body) {
    const startedAt = ApiLog.begin(method, url, body);
    const headers = { 'Accept': 'application/json', 'X-CSRF-Token': csrfToken || '' };
    if (body) headers['Content-Type'] = 'application/json';
    try {
      const res = await fetch(url, { method, headers, body: body ? JSON.stringify(body) : undefined });
      let data = null;
      try { data = await res.json(); } catch (e) { /* reponse sans corps JSON */ }
      ApiLog.end(startedAt, res.status);
      if (!res.ok) {
        const err = new Error((data && data.error) || ('Erreur ' + res.status));
        err.status = res.status;
        err.data = data;
        if (res.status === 401 && !url.startsWith('/api/auth/')) {
          Flash.set('Session expiree, reconnectez-vous', 'error');
          window.location = '/index.html';
        }
        throw err;
      }
      if (data && Object.prototype.hasOwnProperty.call(data, 'user')) {
        currentUser = data.user;
      }
      return data;
    } catch (err) {
      if (!(err instanceof Error) || err.status === undefined) {
        ApiLog.fail(startedAt, method, url, err);
      }
      throw err;
    }
  }

  return {
    initSession,
    user: () => currentUser,
    csrf: () => csrfToken,
    get: (u) => request('GET', u),
    post: (u, b) => request('POST', u, b || {}),
    put: (u, b) => request('PUT', u, b || {}),
    del: (u) => request('DELETE', u, {})
  };
})();

/* Messages flash (indirects entre pages) */
const Flash = {
  set(message, type) { sessionStorage.setItem('flash', JSON.stringify({ message, type })); },
  get() {
    const raw = sessionStorage.getItem('flash');
    if (!raw) return null;
    sessionStorage.removeItem('flash');
    try { return JSON.parse(raw); } catch (e) { return null; }
  },
  show() {
    const flash = Flash.get();
    const el = document.getElementById('flash-container');
    if (!el || !flash) return;
    mountAlert(el, flash.message, flash.type === 'success' ? 'success' : 'error');
  }
};

function escapeHtml(s) {
  return String(s == null ? '' : s).replace(/[&<>"']/g, (c) => ({
    '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;'
  }[c]));
}

function svgAlertIcon(color) {
  const path = color === 'green'
    ? 'M9 12.75L11.25 15 15 9.75M21 12a9 9 0 11-18 0 9 9 0 0118 0z'
    : 'M12 9v3.75m-9.303 3.376c-.866 1.5.217 3.374 1.948 3.374h14.71c1.73 0 2.813-1.874 1.948-3.374L13.949 3.378c-.866-1.5-3.032-1.5-3.898 0L2.697 16.126zM12 15.75h.007v.008H12v-.008z';
  return `<svg class="h-5 w-5 flex-shrink-0 text-${color}-600" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2" aria-hidden="true"><path stroke-linecap="round" stroke-linejoin="round" d="${path}"/></svg>`;
}

function svgAlertClose(color) {
  return `<button type="button" class="flash-close text-${color}-700 opacity-60 transition-opacity hover:opacity-100" aria-label="Fermer le message">
      <svg class="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2" aria-hidden="true"><path stroke-linecap="round" stroke-linejoin="round" d="M6 18L18 6M6 6l12 12"/></svg>
    </button>`;
}

function dismissAlert(node) {
  node.style.transition = 'opacity .4s ease, transform .4s ease';
  node.style.opacity = '0';
  node.style.transform = 'translateY(-6px)';
  setTimeout(() => node.remove(), 400);
}

function mountAlert(container, message, type) {
  const success = type === 'success';
  const color = success ? 'green' : 'red';
  const node = document.createElement('div');
  node.className = `flash-alert ${success ? 'flash-success' : 'flash-error'} mb-4 flex items-start gap-3 rounded-lg border-l-4 border-${color}-600 bg-${color}-50 px-4 py-3 text-sm text-${color}-800 shadow-sm`;
  node.setAttribute('role', 'alert');
  node.innerHTML = svgAlertIcon(color) +
    `<span class="flex-1 leading-snug">${escapeHtml(message)}</span>` +
    svgAlertClose(color);
  node.querySelector('.flash-close').addEventListener('click', () => dismissAlert(node));
  container.appendChild(node);
  if (success) setTimeout(() => dismissAlert(node), 8000);
}

/* Utilitaire d'affichage d'erreur dans la page (meme presentation que les alerts Grails) */
function showError(el, message) {
  if (!el) return;
  el.className = 'flash-alert flash-error mb-4 flex items-start gap-3 rounded-lg border-l-4 border-red-600 bg-red-50 px-4 py-3 text-sm text-red-800 shadow-sm';
  el.setAttribute('role', 'alert');
  el.innerHTML = svgAlertIcon('red') +
    `<span class="flex-1 leading-snug">${escapeHtml(message)}</span>` +
    svgAlertClose('red');
  el.querySelector('.flash-close').addEventListener('click', () => hideError(el));
  el.scrollIntoView({ behavior: 'smooth', block: 'center' });
}

function hideError(el) {
  if (!el) return;
  el.className = 'hidden';
  el.innerHTML = '';
}