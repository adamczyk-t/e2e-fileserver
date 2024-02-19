package adamczykt.e2efileserver.user.entity

import jakarta.persistence.*
import java.util.UUID

@Entity
@Table(name="`users`")
class User(
    @Column(nullable = false, unique = true)
    val username: String,

    @Column(nullable = false)
    val password: String,

    @Id
    val id: UUID = UUID.randomUUID(),
)