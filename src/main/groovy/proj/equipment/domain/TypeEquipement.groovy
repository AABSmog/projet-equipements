package proj.equipment.domain

import jakarta.persistence.*

@Entity
@Table(name = 'type_equipement')
class TypeEquipement {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = 'hibernate_sequence')
    @SequenceGenerator(name = 'hibernate_sequence', sequenceName = 'hibernate_sequence', allocationSize = 1)
    Long id

    @Version
    Long version

    @Column(name = 'nom', nullable = false, length = 255)
    String nom

    String toString() { nom }
}