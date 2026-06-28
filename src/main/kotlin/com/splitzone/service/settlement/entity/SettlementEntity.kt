import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EntityListeners
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.math.BigDecimal
import java.time.ZonedDateTime

@Entity
@Table(name = "settlements")
@EntityListeners(AuditingEntityListener::class)
class SettlementEntity(
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    var group: GroupEntity,
        
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payer_id", nullable = false)
    var payer: UserEntity,
        
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_id", nullable = false)
    var receiver: UserEntity,

    @Column(nullable = false, precision = 10, scale = 2)
    var amount: BigDecimal,
        
    @Column(length = 3, nullable = false)
    var currency: String,
        
    @Column(nullable = false)
    var settledAt: ZonedDateTime,

    @Column(nullable = false)
    @CreatedDate
    var createdAt: ZonedDateTime? = null,

    @Column(nullable = false, updatable = true)
    @LastModifiedDate
    var updatedAt: ZonedDateTime? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as SettlementEntity
        return id != null && id == other.id
    }

    override fun hashCode(): Int = javaClass.hashCode()

    override fun toString(): String {
        return "SettlementEntity(id=$id, amount='$amount')"
    }
}