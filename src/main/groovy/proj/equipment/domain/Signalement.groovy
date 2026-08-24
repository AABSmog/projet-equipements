package proj.equipment.domain

import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp

@Entity
@Table(name = 'signalement')
class Signalement {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = 'hibernate_sequence')
    @SequenceGenerator(name = 'hibernate_sequence', sequenceName = 'hibernate_sequence', allocationSize = 1)
    Long id

    @Version
    Long version

    @Column(name = 'date_created', nullable = false)
    @CreationTimestamp
    java.util.Date dateCreated

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = 'equipement_id')
    Equipement equipement

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = 'personnel_id', nullable = false)
    Personnel personnel

    @Enumerated(EnumType.STRING)
    @Column(name = 'type', nullable = false, length = 255)
    TypeSignalement type

    @Column(name = 'description', nullable = false, length = 1000)
    String description

    @Column(name = 'info_equipement', length = 255)
    String infoEquipement

    String toString() { "Signalement: ${type?.label} - ${equipement ?: infoEquipement}" }
}