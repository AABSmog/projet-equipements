package proj.equipment.domain

import jakarta.persistence.*
import org.mindrot.jbcrypt.BCrypt

@Entity
@Table(name = 'equipement', uniqueConstraints = @UniqueConstraint(columnNames = ['numero_serie']))
class Equipement {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = 'hibernate_sequence')
    @SequenceGenerator(name = 'hibernate_sequence', sequenceName = 'hibernate_sequence', allocationSize = 1)
    Long id

    @Version
    Long version

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = 'type_id', nullable = false)
    TypeEquipement type = null

    @Column(name = 'numero_serie', length = 64)
    String numeroSerie

    @Column(name = 'description', nullable = false, length = 500)
    String description

    @Enumerated(EnumType.STRING)
    @Column(name = 'etat', nullable = false, length = 255)
    EtatEquipement etat = EtatEquipement.DISPONIBLE

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = 'etablissement_id')
    Etablissement etablissement

    @PrePersist
    protected void genererNumeroSerieSiAbsent() {
        if (!numeroSerie) {
            numeroSerie = 'SN-' + UUID.randomUUID().toString().take(8).toUpperCase()
        }
    }

    String toString() { "${type?.nom} #$id ($numeroSerie)" }
}