package proj.equipment.domain

import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes

@Entity
@Table(name = 'audit_log')
class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = 'hibernate_sequence')
    @SequenceGenerator(name = 'hibernate_sequence', sequenceName = 'hibernate_sequence', allocationSize = 1)
    Long id

    @Version
    Long version

    @Column(name = 'date_creation', nullable = false)
    @CreationTimestamp
    java.util.Date dateCreated

    @Column(name = 'utilisateur', length = 254)
    String utilisateur

    @Column(name = 'action', nullable = false, length = 50)
    String action

    @Column(name = 'cible', length = 100)
    String cible

    @Column(name = 'cible_id')
    Long cibleId

    @Column(name = 'details', length = 1000)
    String details

    String toString() { "Audit ${action} ${cible}#${cibleId} par ${utilisateur}" }
}