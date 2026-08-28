package proj.equipment.domain

import jakarta.persistence.*

@Entity
@Table(name = 'regle_gestion', uniqueConstraints = @UniqueConstraint(columnNames = ['etablissement_id', 'cle']))
class RegleGestion {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = 'hibernate_sequence')
    @SequenceGenerator(name = 'hibernate_sequence', sequenceName = 'hibernate_sequence', allocationSize = 1)
    Long id

    @Version
    Long version

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = 'etablissement_id', nullable = false)
    Etablissement etablissement

    @Column(name = 'cle', nullable = false, length = 80)
    String cle // ex: email.pattern, password.minLength

    @Column(name = 'valeur', nullable = false, length = 500)
    String valeur

    @Column(name = 'date_modif', nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    Date dateModif = new Date()

    static Map<String, String> defaults(String domaineEmail = 'example.com') {
        [
            'email.pattern'          : '{prenom}.{nom}@' + domaineEmail,
            'email.domaine'          : domaineEmail,
            'password.minLength'     : '8',
            'password.requireMaj'    : 'true',
            'password.requireMin'    : 'true',
            'password.requireChiffre': 'true',
            'password.requireSymbole': 'false',
            'password.expirationJours': '90',
            'affectation.dureeMaxJours': '365'
        ]
    }
}
