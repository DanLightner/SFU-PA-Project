package edu.francis.my.sfupa;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.error.ErrorMvcAutoConfiguration;

@SpringBootApplication(exclude = {
	WebMvcAutoConfiguration.class,
	ErrorMvcAutoConfiguration.class
})
public class SfuPaProjectApplication {
	//James Test
	//nates test
	//mina test
	//test-danbranch2
	public static void main(String[] args) {
		SpringApplication.run(SfuPaProjectApplication.class, args);
	}

}
