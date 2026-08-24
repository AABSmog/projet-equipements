package proj.equipment.domain

import jakarta.persistence.*

@Entity
@Table(name = 'affectation')
class Affectation {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = 'hibernate_sequence')
    @SequenceGenerator(name = 'hibernate_sequence', sequenceName = 'hibernate_sequence', allocationSize = 1)
    Long id

    @Version
    Long version

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = 'equipement_id')
    Equipement equipement

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = 'personnel_id', nullable = false)
    Personnel personnel

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = 'attribue_par_id')
    Personnel attribuePar

    @Column(name = 'date_affectation', nullable = false)
    java.util.Date dateAffectation

    @Column(name = 'date_retour')
    java.util.Date dateRetour

    @Column(name = 'raison_retour', length = 255)
    String raisonRetour

    @Column(name = 'info_equipement', length = 255)
    String infoEquipement

    String toString() { "Affectation: ${equipement ?: infoEquipement} -> ${personnel}" }
}