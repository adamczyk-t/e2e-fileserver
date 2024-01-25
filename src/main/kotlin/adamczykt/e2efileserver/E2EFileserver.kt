package adamczykt.e2efileserver

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class E2EFileserver

fun main(args: Array<String>) {
	runApplication<E2EFileserver>(*args)
}
