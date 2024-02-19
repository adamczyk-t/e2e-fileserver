package adamczykt.e2efileserver.common

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class HelloController {
    @GetMapping("/api/hello")
    fun hello(): ResponseEntity<String> = ResponseEntity.ok("Hello")
}