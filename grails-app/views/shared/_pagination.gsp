<%--
    Pagination partagee.
    Variables attendues : total (int), max (int), offset (int).
    Conserve les parametres de recherche (q, etat) dans les liens.
--%>
<g:set var="totalPages" value="${total ? Math.ceil((total as double) / max) as long : 1}"/>
<g:set var="currentPage" value="${(offset / max).intValue() + 1}"/>
<g:if test="${totalPages > 1}">
    <div class="flex items-center justify-between mt-4 px-1">
        <span class="text-xs text-gray-500">${total} resultat(s) — Page ${currentPage}/${totalPages}</span>
        <div class="flex gap-1">
            <g:if test="${currentPage > 1}">
                <g:link controller="${controllerName}" action="${actionName ?: 'list'}" namespace="${params.namespace}"
                        params="${[offset: Math.max(offset - max, 0), q: params.q, etat: params.etat].findAll { it.value != null }}"
                        class="px-2 py-1 border border-gray-300 text-gray-700 text-xs hover:bg-gray-100">Precedent</g:link>
            </g:if>
            <g:if test="${currentPage < totalPages}">
                <g:link controller="${controllerName}" action="${actionName ?: 'list'}" namespace="${params.namespace}"
                        params="${[offset: offset + max, q: params.q, etat: params.etat].findAll { it.value != null }}"
                        class="px-2 py-1 border border-gray-300 text-gray-700 text-xs hover:bg-gray-100">Suivant</g:link>
            </g:if>
        </div>
    </div>
</g:if>