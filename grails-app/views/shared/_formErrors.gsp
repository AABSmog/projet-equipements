<%--
    Resume des erreurs de validation d'un domaine.
    Usage : <g:render template="/shared/formErrors" model="[bean: equipement]"/>
--%>
<g:hasErrors bean="${bean}">
    <div class="mb-4 flex items-start gap-3 rounded-lg border-l-4 border-red-600 bg-red-50 px-4 py-3 text-sm text-red-800 shadow-sm" role="alert">
        <svg class="h-5 w-5 flex-shrink-0 text-red-600" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2" aria-hidden="true">
            <path stroke-linecap="round" stroke-linejoin="round" d="M12 9v3.75m-9.303 3.376c-.866 1.5.217 3.374 1.948 3.374h14.71c1.73 0 2.813-1.874 1.948-3.374L13.949 3.378c-.866-1.5-3.032-1.5-3.898 0L2.697 16.126zM12 15.75h.007v.008H12v-.008z"/>
        </svg>
        <div class="flex-1">
            <p class="font-semibold">Le formulaire contient des erreurs</p>
            <ul class="mt-1 space-y-1">
                <g:eachError bean="${bean}"><li><g:message error="${it}"/></li></g:eachError>
            </ul>
        </div>
    </div>
</g:hasErrors>