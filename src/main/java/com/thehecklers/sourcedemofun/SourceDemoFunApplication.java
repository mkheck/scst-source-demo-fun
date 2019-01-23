package com.thehecklers.sourcedemofun;

import lombok.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.messaging.Source;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import javax.annotation.PostConstruct;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@EnableBinding(Source.class)
@SpringBootApplication
public class SourceDemoFunApplication {

	public static void main(String[] args) {
		SpringApplication.run(SourceDemoFunApplication.class, args);
	}

}

@Component
class PingMachine {
	private final Source source;
	private Long prefix = 0L;

	PingMachine(Source source) {
		this.source = source;
	}

	@PostConstruct
	private void streamPings() {
		Flux.interval(Duration.ofSeconds(1))
				.onBackpressureDrop()
				.map(l -> {
					Ping ping = new Ping(prefix++ % 2 == 0 ? "A" : "B", UUID.randomUUID().toString(), Instant.now().toString());
					source.output().send(MessageBuilder.withPayload(ping).build());
					return ping;
				})
				.subscribe(System.out::println);
	}
}

@Value
class Ping {
	private final String group, id, message;
}