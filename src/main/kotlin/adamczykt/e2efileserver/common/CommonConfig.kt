package adamczykt.e2efileserver.common

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.time.Clock

@Configuration
internal class CommonConfig {
    @Bean
    protected fun clock(): Clock = Clock.systemUTC()
}
