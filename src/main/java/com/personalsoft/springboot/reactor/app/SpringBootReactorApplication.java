package com.personalsoft.springboot.reactor.app;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CountDownLatch;

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.personalsoft.springboot.reactor.app.models.Comentarios;
import com.personalsoft.springboot.reactor.app.models.Usuario;
import com.personalsoft.springboot.reactor.app.models.UsuarioConComentarios;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@SpringBootApplication
public class SpringBootReactorApplication implements CommandLineRunner {

	// llamamos la clase logger
	private static final Logger log = LoggerFactory.getLogger(SpringBootReactorApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(SpringBootReactorApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		// ejemploIteracion();
		// ejemploFlatMap();
		// ejemploToString();
		// ejemploCollectList();
		// ejemploUsuarioComentarioFlatMap();
		// ejemploUsuarioComentarioZipWith();
		// ejemploUsuarioComentarioZipWithForma2();
		// ejemploZipWithRangos();
		// ejemploInterval();
		// ejemploDelayElements();
		// ejemploIntervalInfinito();
		//ejemploIntervalDesdeCreate();
		ejemploContraPresion();

	}

	public void ejemploContraPresion() throws Exception{
		
		Flux.range(1, 10)
		.log()
		//.limitRate(2)
		.subscribe(new Subscriber<Integer>() {
			
			
			private Subscription s;
			private Integer limite = 2;
			private Integer consumidos = 0;
			
			
			@Override
			public void onSubscribe(Subscription s) {
				this.s = s;
				s.request(limite);
				
			}

			@Override
			public void onNext(Integer t) {
				log.info(t.toString());
				consumidos++;
				if(consumidos == limite) {
					consumidos = 0;
					s.request(limite);					
				}
				
			}

			@Override
			public void onError(Throwable t) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onComplete() {
				// TODO Auto-generated method stub
				
			}
		});
		
		
	}
	
	
	
	public void ejemploIntervalDesdeCreate() throws Exception {

		Flux.create(emitter -> {
			Timer timer = new Timer();
			timer.schedule(new TimerTask() {

				private Integer contador = 0;

				@Override
				public void run() {
					emitter.next(++contador);

					if (contador == 10) {
						timer.cancel();
						emitter.complete();
					}
					if(contador == 5) {
						timer.cancel();
						emitter.error(new InterruptedException("error, se dtuvo el flujo"));
					}

				}
			}, 1000, 1000);
		}).subscribe(next -> log.info(next.toString()), error -> log.error(error.getMessage()),
				() -> log.info("Fnalizado"));

	}

	public void ejemploIntervalInfinito() throws Exception {
		CountDownLatch latch = new CountDownLatch(1);

		Flux.interval(Duration.ofSeconds(1)).doOnTerminate(() -> latch.countDown()).flatMap(x -> {
			if (x >= 5) {
				return Flux.error(new InterruptedException("Solo hasta 5"));
			}
			return Flux.just(x);
		}).map(i -> "Hola " + i).subscribe(s -> log.info(s.toString()), error -> log.error(error.getMessage()));

		latch.await();

	}

	public void ejemploDelayElements() throws Exception {
		Flux<Integer> rango = Flux.range(1, 12).delayElements(Duration.ofSeconds(1))
				.doOnNext(i -> log.info(i.toString()));

		rango.blockLast();

	}

	public void ejemploInterval() throws Exception {
		Flux<Integer> rango = Flux.range(1, 12);
		Flux<Long> delay = Flux.interval(Duration.ofSeconds(1));

		rango.zipWith(delay, (r, d) -> r).doOnNext(i -> log.info(i.toString())).blockLast();
	}

	public void ejemploZipWithRangos() throws Exception {
		Flux.just(1, 2, 3, 4).map(i -> i * 2)
				.zipWith(Flux.range(0, 4), (uno, dos) -> String.format("Primer: %d - Segundo: %d", uno, dos))
				.subscribe(result -> log.info(result));
	}

	public void ejemploUsuarioComentarioZipWithForma2() throws Exception {

		Mono<Usuario> usuarioMono = Mono.fromCallable(() -> new Usuario("Luis", "Angulo"));

		Mono<Comentarios> comentariosUsuarioMono = Mono.fromCallable(() -> {
			Comentarios comentarios = new Comentarios();
			comentarios.addComentario("que mas mijo");
			comentarios.addComentario("commo vamos");
			comentarios.addComentario("como va la causa");
			comentarios.addComentario("todo bien");
			return comentarios;

		});

		Mono<UsuarioConComentarios> usuarioConComentarios = usuarioMono.zipWith(comentariosUsuarioMono).map(tuple -> {
			Usuario u = tuple.getT1();
			Comentarios c = tuple.getT2();
			return new UsuarioConComentarios(u, c);

		});

		usuarioConComentarios.subscribe(uc -> log.info(uc.toString()));
	}

	public void ejemploIteracion() throws Exception {

		List<String> listaUsuarios = new ArrayList<>();
		listaUsuarios.add("Luis Velasquez");
		listaUsuarios.add("Antonio Toro");
		listaUsuarios.add("Jose Puerto");
		listaUsuarios.add("Pedro Gonzalez");
		listaUsuarios.add("Esteban Quiroz");
		listaUsuarios.add("Juan Rodriguez");
		listaUsuarios.add("Carlos Tobon");
		listaUsuarios.add("Bruce Lee");
		listaUsuarios.add("Bruce Willis");

		/*
		 * Flux<String> nombres = Flux .just("Luis Velasquez", "Antonio Toro",
		 * "Jose Puerto", "Pedro Gonzalez", "Esteban Quiroz", "Juan Rodriguez",
		 * "Carlos Tobon", "Bruce Lee", "Bruce Willis");
		 */
		Flux<String> nombres = Flux.fromIterable(listaUsuarios);

		Flux<Usuario> usuarios = nombres
				.map(nombreCompleto -> new Usuario(nombreCompleto.split(" ")[0], nombreCompleto.split(" ")[1]))
				.filter(usuario -> {
					return !usuario.getNombre().equalsIgnoreCase("Bruce");

				}).doOnNext(usuario -> {
					if (usuario == null) {
						throw new RuntimeException("el valor enviado esta vacio");
					}
					System.out.println(usuario.getNombre().concat(" ").concat(usuario.getApellido()));

				});

		usuarios.subscribe(e -> log.info(e.toString()), error -> log.error(error.getMessage()), // captura la excepcion
																								// lanzada
				// desde el observable
				new Runnable() {// esto se ejecuta al final del flujo (doOnComplete)

					@Override
					public void run() {
						log.info("ha finalizado el flujo");

					}
				});

	}

	public void ejemploFlatMap() throws Exception {

		List<String> listaUsuarios = new ArrayList<>();
		listaUsuarios.add("Luis Velasquez");
		listaUsuarios.add("Antonio Toro");
		listaUsuarios.add("Jose Puerto");
		listaUsuarios.add("Pedro Gonzalez");
		listaUsuarios.add("Esteban Quiroz");
		listaUsuarios.add("Juan Rodriguez");
		listaUsuarios.add("Carlos Tobon");
		listaUsuarios.add("Bruce Lee");
		listaUsuarios.add("Bruce Willis");

		Flux.fromIterable(listaUsuarios)
				.map(nombreCompleto -> new Usuario(nombreCompleto.split(" ")[0], nombreCompleto.split(" ")[1]))
				.flatMap(usuario -> {// convierte a un tipo de dato observable, retorna un observable(Flux o Mono)
					if (!usuario.getNombre().equalsIgnoreCase("Bruce")) {
						return Mono.just(usuario);

					} else {
						return Mono.empty();
					}

				}).subscribe(u -> log.info(u.toString()));

	}

	public void ejemploToString() throws Exception {

		List<Usuario> listaUsuarios = new ArrayList<>();
		listaUsuarios.add(new Usuario("Luis", "Velasquez"));
		listaUsuarios.add(new Usuario("Antonio", "Toro"));
		listaUsuarios.add(new Usuario("Jose", "Puerto"));
		listaUsuarios.add(new Usuario("Pedro", "Gonzalez"));
		listaUsuarios.add(new Usuario("Esteban", "Quiroz"));
		listaUsuarios.add(new Usuario("Juan", "Rodriguez"));
		listaUsuarios.add(new Usuario("Carlos", "Tobon"));
		listaUsuarios.add(new Usuario("Bruce", "Lee"));
		listaUsuarios.add(new Usuario("Bruce", "Willis"));

		Flux.fromIterable(listaUsuarios).map(usuario -> usuario.getNombre() + " " + usuario.getApellido())
				.flatMap(usuario -> {// convierte a un tipo de dato observable, retorna un observable(Flux o Mono)
					if (!usuario.contains("Bruce")) {
						return Mono.just(usuario);

					} else {
						return Mono.empty();
					}

				}).subscribe(u -> log.info(u.toString()));

	}

	public void ejemploCollectList() throws Exception {

		List<Usuario> listaUsuarios = new ArrayList<>();
		listaUsuarios.add(new Usuario("Luis", "Velasquez"));
		listaUsuarios.add(new Usuario("Antonio", "Toro"));
		listaUsuarios.add(new Usuario("Jose", "Puerto"));
		listaUsuarios.add(new Usuario("Pedro", "Gonzalez"));
		listaUsuarios.add(new Usuario("Esteban", "Quiroz"));
		listaUsuarios.add(new Usuario("Juan", "Rodriguez"));
		listaUsuarios.add(new Usuario("Carlos", "Tobon"));
		listaUsuarios.add(new Usuario("Bruce", "Lee"));
		listaUsuarios.add(new Usuario("Bruce", "Willis"));

		Flux.fromIterable(listaUsuarios).collectList().subscribe(lista -> {
			lista.forEach(item -> log.info(item.toString()));
		});
	}

	public void ejemploUsuarioComentarioFlatMap() throws Exception {

		Mono<Usuario> usuarioMono = Mono.fromCallable(() -> new Usuario("Luis", "Angulo"));

		Mono<Comentarios> comentariosUsuarioMono = Mono.fromCallable(() -> {
			Comentarios comentarios = new Comentarios();
			comentarios.addComentario("que mas mijo");
			comentarios.addComentario("commo vamos");
			comentarios.addComentario("como va la causa");
			comentarios.addComentario("todo bien");
			return comentarios;

		});

		usuarioMono.flatMap(u -> comentariosUsuarioMono.map(c -> new UsuarioConComentarios(u, c)))
				.subscribe(uc -> log.info(uc.toString()));// junta los flujos de usuario y comentario creando uno de
															// usuariosConComentarios

	}

	public void ejemploUsuarioComentarioZipWith() throws Exception {

		Mono<Usuario> usuarioMono = Mono.fromCallable(() -> new Usuario("Luis", "Angulo"));

		Mono<Comentarios> comentariosUsuarioMono = Mono.fromCallable(() -> {
			Comentarios comentarios = new Comentarios();
			comentarios.addComentario("que mas mijo");
			comentarios.addComentario("commo vamos");
			comentarios.addComentario("como va la causa");
			comentarios.addComentario("todo bien");
			return comentarios;

		});

		Mono<UsuarioConComentarios> usuarioConComentarios = usuarioMono.zipWith(comentariosUsuarioMono,
				(usuario, comentarios) -> new UsuarioConComentarios(usuario, comentarios));// junta los flujos de
																							// usuario y comentario
																							// creando uno de
																							// usuariosConComentarios

		usuarioConComentarios.subscribe(uc -> log.info(uc.toString()));
	}

//	@Override
//	public void run(String... args) throws Exception {
	// Stream de datos, flujo, observable,publicador
	/*
	 * Flux<String> nombres = Flux.just("Luis", "Antonio",
	 * "Jose","Pedro","Esteban","Juan","Carlos") .doOnNext(elemento -> {
	 * 
	 * System.out.println(elemento); System.out.println(elemento + "15");
	 * 
	 * 
	 * });//esto seria una tarea
	 */

//		Flux<String> nombres = Flux.just("Luis", "Antonio", "Jose", "Pedro", "Esteban", "Juan", "Carlos")
//				.doOnNext(System.out::println);
//
//		// hay que suscribirnos
//		nombres.subscribe(log::info);//realiza la tarea dentro del parentesis justo despues de que llegue(emita) el dato (doOnNext)
//		

//		Flux<String> nombres = Flux.just("Luis", "Antonio", "Jose", "Pedro", "Esteban", "Juan", "Carlos")
//				.doOnNext(e -> {
//					if (e.isEmpty()) {
//						throw new RuntimeException("el valor enviado esta vacio");
//					}
//					System.out.println(e);
//
//				}).map(nombre -> {
//					return nombre.toUpperCase();
//				});
//		
//		Flux<Usuario> nombres = Flux.just("Luis", "Antonio", "Jose", "Pedro", "Esteban", "Juan", "Carlos")
//				.map(nombre -> new Usuario(nombre, null))
//				.doOnNext(usuario -> {
//					if (usuario == null) {
//						throw new RuntimeException("el valor enviado esta vacio");
//					}
//					System.out.println(usuario.getNombre());
//
//				});
//
//		nombres.subscribe(e -> log.info(e.toString()), error -> log.error(error.getMessage()), // captura la excepcion lanzada
//																					// desde el observable
//				new Runnable() {// esto se ejecuta al final del flujo (doOnComplete)
//
//					@Override
//					public void run() {
//						log.info("ha finalizado el flujo");
//
//					}
//				});
	/*
	 * Flux<Usuario> nombres = Flux .just("Luis Velasquez", "Antonio Toro",
	 * "Jose Puerto", "Pedro Gonzalez", "Esteban Quiroz", "Juan Rodriguez",
	 * "Carlos Tobon", "Bruce Lee", "Bruce Willis") .map(nombreCompleto -> new
	 * Usuario(nombreCompleto.split(" ")[0], nombreCompleto.split(" ")[1]))
	 * .filter(usuario -> { return !usuario.getNombre().equalsIgnoreCase("Bruce");
	 * 
	 * }).doOnNext(usuario -> { if (usuario == null) { throw new
	 * RuntimeException("el valor enviado esta vacio"); }
	 * System.out.println(usuario.getNombre().concat(" ").concat(usuario.getApellido
	 * ()));
	 * 
	 * });
	 * 
	 * nombres.subscribe(e -> log.info(e.toString()), error ->
	 * log.error(error.getMessage()), // captura la excepcion // lanzada // desde el
	 * observable new Runnable() {// esto se ejecuta al final del flujo
	 * (doOnComplete)
	 * 
	 * @Override public void run() { log.info("ha finalizado el flujo");
	 * 
	 * } });
	 */

	/*
	 * List<String> listaUsuarios = new ArrayList<>();
	 * listaUsuarios.add("Luis Velasquez"); listaUsuarios.add("Antonio Toro");
	 * listaUsuarios.add("Jose Puerto"); listaUsuarios.add("Pedro Gonzalez");
	 * listaUsuarios.add("Esteban Quiroz"); listaUsuarios.add("Juan Rodriguez");
	 * listaUsuarios.add("Carlos Tobon"); listaUsuarios.add("Bruce Lee");
	 * listaUsuarios.add("Bruce Willis");
	 * 
	 * 
	 */

	/*
	 * Flux<String> nombres = Flux .just("Luis Velasquez", "Antonio Toro",
	 * "Jose Puerto", "Pedro Gonzalez", "Esteban Quiroz", "Juan Rodriguez",
	 * "Carlos Tobon", "Bruce Lee", "Bruce Willis");
	 */
	/*
	 * Flux<String> nombres = Flux.fromIterable(listaUsuarios);
	 * 
	 * Flux<Usuario> usuarios = nombres.map(nombreCompleto -> new
	 * Usuario(nombreCompleto.split(" ")[0], nombreCompleto.split(" ")[1]))
	 * .filter(usuario -> { return !usuario.getNombre().equalsIgnoreCase("Bruce");
	 * 
	 * }).doOnNext(usuario -> { if (usuario == null) { throw new
	 * RuntimeException("el valor enviado esta vacio"); }
	 * System.out.println(usuario.getNombre().concat(" ").concat(usuario.getApellido
	 * ()));
	 * 
	 * });
	 * 
	 * usuarios.subscribe(e -> log.info(e.toString()), error ->
	 * log.error(error.getMessage()), // captura la excepcion // lanzada // desde el
	 * observable new Runnable() {// esto se ejecuta al final del flujo
	 * (doOnComplete)
	 * 
	 * @Override public void run() { log.info("ha finalizado el flujo");
	 * 
	 * } });
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * }
	 */

}
