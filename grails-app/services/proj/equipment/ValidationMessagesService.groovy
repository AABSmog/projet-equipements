package proj.equipment

import org.springframework.validation.FieldError

class ValidationMessagesService {

    String message(def entity) {
        if (!entity?.errors?.hasErrors()) return null
        def erreurs = entity.errors.allErrors
        def message = erreurs.collect { err ->
            def champ = (err instanceof FieldError) ? err.field : err.objectName
            def code = err.code ?: ''
            traduction(champ, code)
        }.find { it }
        message ?: 'Une ou plusieurs saisies sont invalides.'
    }

    private static String traduction(String champ, String code) {
        def c = code.toLowerCase()
        switch (champ) {
            case 'numeroSerie':
                return c.contains('unique') ? 'Ce numero de serie est deja utilise.' : null
            case 'description':
                return (c.contains('blank') || c.contains('null')) ? 'La description est obligatoire.' : null
            case 'email':
                if (c.contains('unique')) return 'Cet email est deja utilise.'
                if (c.contains('blank') || c.contains('null')) return "L'adresse email est obligatoire."
                if (c.contains('email') || c.contains('invalid') || c.contains('matches')) return "L'adresse email n'est pas valide."
                return null
            case 'nom':
                return (c.contains('blank') || c.contains('null')) ? 'Le nom est obligatoire.' : null
            case 'prenom':
                return (c.contains('blank') || c.contains('null')) ? 'Le prenom est obligatoire.' : null
            case 'motDePasse':
                return (c.contains('blank') || c.contains('null')) ? 'Le mot de passe est obligatoire.' : null
            default:
                return null
        }
    }
}
