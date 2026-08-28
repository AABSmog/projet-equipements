package proj.equipment.domain

import jakarta.persistence.*

@Entity
@Table(name = 'etablissement', uniqueConstraints = @UniqueConstraint(columnNames = ['slug']))
class Etablissement {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = 'hibernate_sequence')
    @SequenceGenerator(name = 'hibernate_sequence', sequenceName = 'hibernate_sequence', allocationSize = 1)
    Long id

    @Version
    Long version

    @Column(name = 'nom', nullable = false, length = 150)
    String nom

    @Column(name = 'slug', nullable = false, length = 80)
    String slug // ex: acme-corp, utilisé pour DB `equipments_<slug>` et sous-domaine

    @Column(name = 'domaine_email', length = 120)
    String domaineEmail // ex: acme.com pour auto-génération {prenom}.{nom}@domaine

    @Column(name = 'db_url', length = 300)
    String dbUrl // jdbc:postgresql://.../equipments_<slug> (null = single-DB mode avec etablissement_id)

    @Column(name = 'db_user', length = 80)
    String dbUser

    @Column(name = 'db_password_chiffre', length = 500)
    String dbPasswordChiffre // AES/GCM, jamais en clair

    @Column(name = 'statut', nullable = false, length = 20)
    String statut = 'ACTIF' // ACTIF, ARCHIVE

    @Column(name = 'date_creation', nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    Date dateCreation = new Date()

    String toString() { "$nom ($slug)" }
}
