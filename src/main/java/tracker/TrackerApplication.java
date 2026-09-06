package tracker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class TrackerApplication {

	public static void main(String[] args) {

		SpringApplication.run(TrackerApplication.class, args);
	}

}
