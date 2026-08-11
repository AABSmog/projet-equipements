<%--
    Messages flash partages (succes / erreur).
    Affiche flash.success et flash.error avec icone, bouton de fermeture et
    disparition automatique pour les succes. A utiliser via :
      <g:render template="/shared/flash"/>
--%>
<g:if test="${flash.success}">
    <div class="flash-alert flash-success mb-4 flex items-start gap-3 rounded-lg border-l-4 border-green-600 bg-green-50 px-4 py-3 text-sm text-green-800 shadow-sm" role="alert">
        <svg class="h-5 w-5 flex-shrink-0 text-green-600" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2" aria-hidden="true">
            <path stroke-linecap="round" stroke-linejoin="round" d="M9 12.75L11.25 15 15 9.75M21 12a9 9 0 11-18 0 9 9 0 0118 0z"/>
        </svg>
        <span class="flex-1 leading-snug">${flash.success}</span>
        <button type="button" class="flash-close text-green-700 opacity-60 transition-opacity hover:opacity-100" aria-label="Fermer le message">
            <svg class="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2" aria-hidden="true">
                <path stroke-linecap="round" stroke-linejoin="round" d="M6 18L18 6M6 6l12 12"/>
            </svg>
        </button>
    </div>
</g:if>
<g:if test="${flash.error}">
    <div class="flash-alert flash-error mb-4 flex items-start gap-3 rounded-lg border-l-4 border-red-600 bg-red-50 px-4 py-3 text-sm text-red-800 shadow-sm" role="alert">
        <svg class="h-5 w-5 flex-shrink-0 text-red-600" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2" aria-hidden="true">
            <path stroke-linecap="round" stroke-linejoin="round" d="M12 9v3.75m-9.303 3.376c-.866 1.5.217 3.374 1.948 3.374h14.71c1.73 0 2.813-1.874 1.948-3.374L13.949 3.378c-.866-1.5-3.032-1.5-3.898 0L2.697 16.126zM12 15.75h.007v.008H12v-.008z"/>
        </svg>
        <span class="flex-1 leading-snug">${flash.error}</span>
        <button type="button" class="flash-close text-red-700 opacity-60 transition-opacity hover:opacity-100" aria-label="Fermer le message">
            <svg class="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2" aria-hidden="true">
                <path stroke-linecap="round" stroke-linejoin="round" d="M6 18L18 6M6 6l12 12"/>
            </svg>
        </button>
    </div>
</g:if>