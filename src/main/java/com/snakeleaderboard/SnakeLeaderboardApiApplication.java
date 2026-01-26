package com.snakeleaderboard;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Spring Boot entrypoint for the Snake Leaderboard API.
 */
@SpringBootApplication
public class SnakeLeaderboardApiApplication {

	/**
	 * Boots the Spring application.
	 *
	 * @param args standard Spring Boot arguments
	 */
	public static void main(String[] args) {
		SpringApplication.run(SnakeLeaderboardApiApplication.class, args);
	}

}
