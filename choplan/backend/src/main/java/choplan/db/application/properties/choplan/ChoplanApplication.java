package choplan.db.application.properties.choplan;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class ChoplanApplication {

	public static void main(String[] args) {
		SpringApplication.run(ChoplanApplication.class, args);
	}

}
