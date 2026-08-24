package proj.equipment

import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject
import proj.equipment.domain.Affectation
import proj.equipment.domain.Equipement
import proj.equipment.domain.Personnel
import proj.equipment.domain.TypeEquipement
import proj.equipment.service.ValidationMessagesService
import spock.lang.Specification

@MicronautTest
class AffectationSpec extends Specification {

    @Inject ValidationMessagesService validation

    void "une affectation valide est sauvegarde"() {
        given:
        def aff = new Affectation(
                personnel: new Personnel(nom: "Diop", prenom: "Ali", email: "a@example.com", motDePasse: "Secret123"),
                dateAffectation: new Date()
        )

        expect:
        validation.validateAffectation(aff) == null
    }

    void "le personnel et la date d affectation sont obligatoires"() {
        expect:
        validation.validateAffectation(new Affectation(personnel: null, dateAffectation: new Date())).contains("personnel")
        validation.validateAffectation(new Affectation(personnel: new Personnel(nom: "A", prenom: "B", email: "a@b.com", motDePasse: "Secret123"), dateAffectation: null)).contains("date")
    }

    void "la date de retour ne peut pas etre anterieure a la date d affectation"() {
        given:
        def now = new Date()
        def aff = new Affectation(
                personnel: new Personnel(nom: "Diop", prenom: "Ali", email: "a@example.com", motDePasse: "Secret123"),
                dateAffectation: now,
                dateRetour: new Date(now.time - 86400000)
        )

        expect:
        validation.validateAffectation(aff).contains("anterieure")
    }
}
