package com.example.day4_week8.security;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 * Primo livello della protezione: ragiona su metodo e percorso.
 *
 * Finche' non esiste un login tutto e' aperto: e' l'unica configurazione onesta di un
 * progetto senza utenti. Quando il login arriva, i permessi si stringono qui dentro,
 * un requestMatchers alla volta, e i ruoli sulle singole operazioni li dice @PreAuthorize
 * sul metodo - viva grazie a @EnableMethodSecurity, senza il quale resterebbe un commento.
 */
@Configuration
@EnableMethodSecurity
public class SecurityConfig {

	private final JwtAuthFilter jwtAuthFilter;
	private final String[] origini;

	public SecurityConfig(JwtAuthFilter jwtAuthFilter, @Value("${app.cors.allowed-origins}") String[] origini) {
		this.jwtAuthFilter = jwtAuthFilter;
		this.origini = origini;
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http
			// Nessun cookie di sessione: le credenziali viaggiano nell'header Authorization,
			// che un sito terzo non puo' far allegare al browser. Viene a mancare il
			// presupposto del CSRF, non la difesa.
			.csrf(csrf -> csrf.disable())
			.cors(cors -> cors.configurationSource(corsConfigurationSource()))
			.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
			.authorizeHttpRequests(auth -> auth
				// L'health check di Render non ha un token da mostrare.
				.requestMatchers("/actuator/health", "/actuator/health/**").permitAll()

				// Entrare e registrarsi resta aperto anche dopo: e' da li' che si
				// ottiene il token.
				.requestMatchers("/api/auth/**").permitAll()

				// Nessun login ancora: il resto e' aperto. Da stringere qui quando c'e'.
				.anyRequest().permitAll()
			)
			.exceptionHandling(ex -> ex.authenticationEntryPoint(jsonAuthenticationEntryPoint()))
			.addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

		return http.build();
	}

	/** Chi non ha fatto l'accesso merita un 401, non il 403 di chi e' entrato e non puo'. */
	@Bean
	public AuthenticationEntryPoint jsonAuthenticationEntryPoint() {
		return (request, response, authException) -> {
			response.setStatus(HttpMethod.OPTIONS.name().equals(request.getMethod()) ? 200 : 401);
			response.setContentType(MediaType.APPLICATION_JSON_VALUE);
			response.getWriter().write("{\"message\":\"Accesso richiesto\"}");
		};
	}

	/**
	 * In produzione FE e BE stanno su due domini diversi: senza CORS il browser blocca
	 * ogni fetch. Le origini ammesse arrivano da ALLOWED_ORIGIN - un solo posto, qui.
	 */
	@Bean
	public CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration config = new CorsConfiguration();
		config.setAllowedOrigins(List.of(origini));
		config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
		config.setAllowedHeaders(List.of("*"));
		config.setAllowCredentials(true);
		config.setMaxAge(3600L);

		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", config);
		return source;
	}

}
