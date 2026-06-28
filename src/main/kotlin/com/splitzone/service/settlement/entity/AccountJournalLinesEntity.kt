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
@Table(name = "account_journal_lines")
@EntityListeners(AuditingEntityListener::class)
class AccountJournalLinesEntity(
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
        
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "expense_id", nullable = false)
    var expense: ExpenseEntity,
        
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "settlement_id", nullable = false)
    var settlement: SettlementEntity,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    var user: UserEntity,
        
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    var group: GroupEntity,
        
    @Column(nullable = false, precision = 10, scale = 2)
    var netAmount: BigDecimal,
        
    @Column(length = 3, nullable = false)
    var currency: String,

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
        other as AccountJournalLinesEntity
        return id != null && id == other.id
    }

    override fun hashCode(): Int = javaClass.hashCode()

    override fun toString(): String {
        return "AccountJournalLinesEntity(id=$id, netAmount='$netAmount')"
    }
}