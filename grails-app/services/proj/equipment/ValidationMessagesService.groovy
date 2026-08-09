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
                if (c.contains('maxsize')) return "L'adresse email est trop longue."
                return null
            case 'nom':
                if (c.contains('existe')) return "Ce type d'equipement existe deja."
                if (c.contains('blank') || c.contains('null')) return 'Le nom est obligatoire.'
                if (c.contains('maxsize')) return 'Le nom est trop long.'
                return null
            case 'prenom':
                if (c.contains('blank') || c.contains('null')) return 'Le prenom est obligatoire.'
                return null
            case 'motDePasse':
                if (c.contains('blank') || c.contains('null')) return 'Le mot de passe est obligatoire.'
                if (c.contains('minsize')) return 'Le mot de passe doit contenir au moins 6 caracteres.'
                if (c.contains('maxsize')) return 'Le mot de passe est trop long (72 caracteres max).'
                return null
            case 'etat':
                if (c.contains('creation')) return 'Un nouvel equipement doit etre cree a l\'etat disponible.'
                if (c.contains('affecte')) return 'Cet equipement ne peut pas etre marque affecte sans affectation active.'
                if (c.contains('disponible')) return 'Impossible de rendre disponible un equipement encore affecte.'
                if (c.contains('horsservice')) return 'Impossible de classer hors service un equipement encore affecte.'
                return null
            case 'dateRetour':
                if (c.contains('anterieur')) return "La date de retour ne peut pas etre anterieure a la date d'affectation."
                return null
            case 'type':
                return (c.contains('null') || c.contains('blank')) ? 'Le type de signalement est obligatoire.' : null
            case 'personnel':
                return (c.contains('null') || c.contains('blank')) ? 'Le personnel est obligatoire.' : null
            case 'dateAffectation':
                return (c.contains('null') || c.contains('blank')) ? "La date d'affectation est obligatoire." : null
            default:
                return null
        }
    }
}
