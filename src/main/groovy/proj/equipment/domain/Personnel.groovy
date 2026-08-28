package proj.equipment.domain

import jakarta.persistence.*
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes

@Entity
@Table(name = 'personnel', uniqueConstraints = @UniqueConstraint(columnNames = ['email']))
class Personnel {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = 'hibernate_sequence')
    @SequenceGenerator(name = 'hibernate_sequence', sequenceName = 'hibernate_sequence', allocationSize = 1)
    Long id

    @Version
    Long version

    @Column(name = 'nom', nullable = false, length = 100)
    String nom

    @Column(name = 'prenom', nullable = false, length = 100)
    String prenom

    @Column(name = 'email', nullable = false, length = 254)
    String email

    @Column(name = 'mot_de_passe', nullable = false, length = 72)
    String motDePasse

    @Enumerated(EnumType.STRING)
    @Column(name = 'role', nullable = false, length = 255)
    RolePersonnel role = RolePersonnel.USER

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = 'etablissement_id')
    Etablissement etablissement

    @PrePersist
    @PreUpdate
    protected void hashPasswordSiNecessaire() {
        if (motDePasse && !isBcrypt(motDePasse)) {
            motDePasse = org.mindrot.jbcrypt.BCrypt.hashpw(motDePasse, org.mindrot.jbcrypt.BCrypt.gensalt())
        }
    }

    private static boolean isBcrypt(String value) {
        value.startsWith('$2')
    }

    String toString() { "$prenom $nom ($email)" }
}