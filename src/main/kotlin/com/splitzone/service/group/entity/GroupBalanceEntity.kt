import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EntityListeners
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToMany
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.math.BigDecimal
import java.time.ZonedDateTime

@Entity
@EntityListeners(AuditingEntityListener::class)
@Table(
    name = "group_balances",
    uniqueConstraints = [
        UniqueConstraint(columnNames = ["group_id", "user_from_id", "user_to_id"])
    ]
)
class GroupBalanceEntity(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id :Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    var group :GroupEntity,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_from_id", nullable = false)
    var userFrom :UserEntity,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_to_id", nullable = false)
    var userTo :UserEntity,

    @Column(nullable = false, precision = 10, scale = 2)
    var balance :BigDecimal,

    @Column(nullable = false, length = 3)
    var currency :String,

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
        other as GroupBalanceEntity
        return id != null && id == other.id
    }

    override fun hashCode(): Int = javaClass.hashCode()

    override fun toString(): String {
        return "GroupBalanceEntity(id=$id)"
    }
}