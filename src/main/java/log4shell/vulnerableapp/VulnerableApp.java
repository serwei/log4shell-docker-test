package log4shell.vulnerableapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class VulnerableApp {

	public static void main(String[] args) {
		SpringApplication.run(VulnerableApp.class, args);
	}

}
