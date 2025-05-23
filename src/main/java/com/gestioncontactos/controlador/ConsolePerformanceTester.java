package com.gestioncontactos.controlador;

import java.nio.file.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import com.gestioncontactos.modelo.Persona;
import com.gestioncontactos.modelo.PersonaDAO;

//Clase para pruebas de rendimiento en consola. Permite comparar el tiempo de ejecución entre versiones concurrentes y secuenciales
public class ConsolePerformanceTester {
	private List<Persona> contactos = new ArrayList<>();
	private static final int TEST_SIZE = 10000; // número de contactos para prueba
	private static final int TEST_ITERATIONS = 5; // Número de iteraciones para promediar resultados

	// Método estático de entrada para ejecutar todas las pruebas
	public static void runAllTests() {
		new ConsolePerformanceTester().runTestsInstance();
	}

	// Método de instancia que ejecuta la lógica de pruebas y muestra resultados
	private void runTestsInstance() {
		System.out.println("=== INICIANDO PRUEBAS DE RENDIMIENTO ===");
		System.out.println("Tamaño de prueba: " + TEST_SIZE + " contactos");
		System.out.println("Iteraciones: " + TEST_ITERATIONS + "\n");

		testLoadContacts();
		testExportContacts();

		System.out.println("=== PRUEBAS COMPLETADAS ===");
	}

	// Prueba el tiempo de carga de contactos, comparando versiones concurrente y
	// secuencial
	private void testLoadContacts() {
		System.out.println("Preparando prueba de carga...");
		List<Persona> testContacts = generateTestContacts(TEST_SIZE);

		long totalConcurrent = 0;
		long totalSequential = 0;

		for (int i = 0; i < TEST_ITERATIONS; i++) {
			contactos.clear();
			contactos.addAll(testContacts);

			// Versión concurrente
			long start = System.currentTimeMillis();
			cargarContactosRegistradosConcurrente();
			long concurrentTime = System.currentTimeMillis() - start;
			totalConcurrent += concurrentTime;

			// Versión secuencial
			start = System.currentTimeMillis();
			cargarContactosRegistradosSecuencial();
			long sequentialTime = System.currentTimeMillis() - start;
			totalSequential += sequentialTime;

			System.out.printf("Iteración %d - Concurrente: %d ms | Secuencial: %d ms%n", i + 1, concurrentTime,
					sequentialTime);
		}

		printResults("CARGA DE CONTACTOS (Promedio)", totalConcurrent / TEST_ITERATIONS,
				totalSequential / TEST_ITERATIONS);
	}

	// Prueba el tiempo de exportación de contactos a CSV, comparando versiones
	// concurrente y secuencial
	private void testExportContacts() {
		System.out.println("\nPreparando prueba de exportación...");
		contactos.clear();
		contactos.addAll(generateTestContacts(TEST_SIZE));

		long totalConcurrent = 0;
		long totalSequential = 0;

		for (int i = 0; i < TEST_ITERATIONS; i++) {
			// Versión concurrente
			long start = System.currentTimeMillis();
			exportarContactosCSVConcurrente(Paths.get("test_concurrent_" + i + ".csv"));
			long concurrentTime = System.currentTimeMillis() - start;
			totalConcurrent += concurrentTime;

			// Versión secuencial
			start = System.currentTimeMillis();
			exportarContactosCSVSecuencial(Paths.get("test_sequential_" + i + ".csv"));
			long sequentialTime = System.currentTimeMillis() - start;
			totalSequential += sequentialTime;

			System.out.printf("Iteración %d - Concurrente: %d ms | Secuencial: %d ms%n", i + 1, concurrentTime,
					sequentialTime);
		}

		printResults("EXPORTACIÓN DE CONTACTOS (Promedio)", totalConcurrent / TEST_ITERATIONS,
				totalSequential / TEST_ITERATIONS);
	}

	// Procesa la lista de contactos en paralelo usando 4 hilos
	private void cargarContactosRegistradosConcurrente() {
		ExecutorService executor = Executors.newFixedThreadPool(4);
		try {
			List<Future<?>> futures = new ArrayList<>();

			// Dividir el trabajo en 4 partes
			for (int i = 0; i < 4; i++) {
				final int segment = i;
				futures.add(executor.submit(() -> {
					int start = segment * (contactos.size() / 4);
					int end = (segment == 3) ? contactos.size() : (segment + 1) * (contactos.size() / 4);

					// Simular procesamiento costoso
					for (int j = start; j < end; j++) {
						Persona p = contactos.get(j);
						// Operación "costosa" simulada
						p.setNombre(p.getNombre().toUpperCase());
						try {
							Thread.sleep(1);
						} catch (InterruptedException e) {
						}
					}
				}));
			}

			// Esperar a que terminen todos los hilos
			for (Future<?> future : futures) {
				future.get();
			}
		} catch (Exception e) {
			System.err.println("Error en carga concurrente: " + e.getMessage());
		} finally {
			executor.shutdown();
		}
	}

	// Procesa la lista de contactos de forma secuencial, simulando una operación
	// costosa
	private void cargarContactosRegistradosSecuencial() {
		try {
			// Simular procesamiento
			for (Persona p : contactos) {
				p.setNombre(p.getNombre().toUpperCase());
				try {
					Thread.sleep(1);
				} catch (InterruptedException e) {
				}
			}
		} catch (Exception e) {
			System.err.println("Error en carga secuencial: " + e.getMessage());
		}
	}

	// Exporta los contactos a un archivo CSV procesando cada línea en paralelo
	private void exportarContactosCSVConcurrente(Path archivo) {
		ExecutorService executor = Executors.newFixedThreadPool(2);
		try {
			// Escritor principal
			BufferedWriter writer = Files.newBufferedWriter(archivo);
			writer.write("Nombre,Telefono,Email,Categoria,Favorito\n");

			// Procesamiento en paralelo
			List<Future<String>> futures = new ArrayList<>();
			for (Persona contacto : contactos) {
				futures.add(executor.submit(() -> processContactLine(contacto)));
			}

			// Escribir resultados
			for (Future<String> future : futures) {
				writer.write(future.get() + "\n");
			}

			writer.close();
		} catch (Exception e) {
			System.err.println("Error en exportación concurrente: " + e.getMessage());
		} finally {
			executor.shutdown();
		}
	}

	// Exporta los contactos a un archivo CSV de manera secuencial
	private void exportarContactosCSVSecuencial(Path archivo) {
		try (BufferedWriter writer = Files.newBufferedWriter(archivo)) {
			writer.write("Nombre,Telefono,Email,Categoria,Favorito\n");
			for (Persona contacto : contactos) {
				writer.write(processContactLine(contacto) + "\n");
			}
		} catch (Exception e) {
			System.err.println("Error en exportación secuencial: " + e.getMessage());
		}
	}

	// Simula el procesamiento costos de una línea de contacto para exportar
	private String processContactLine(Persona contacto) {
		// Simular procesamiento costoso
		try {
			Thread.sleep(1);
		} catch (InterruptedException e) {
		}
		return String.join(",", contacto.getNombre(), contacto.getTelefono(), contacto.getEmail(),
				contacto.getCategoria(), String.valueOf(contacto.isFavorito()));
	}

	// Genera una lista de contactos de prueba con datos aleatorios
	private List<Persona> generateTestContacts(int count) {
		List<Persona> contacts = new ArrayList<>();
		Random random = new Random();

		for (int i = 0; i < count; i++) {
			contacts.add(new Persona("Contacto_" + i, "600" + (100000 + random.nextInt(900000)),
					"contacto" + i + "@test.com",
					random.nextBoolean() ? "family" : (random.nextBoolean() ? "friends" : "work"),
					random.nextBoolean()));
		}
		return contacts;
	}

	// Imprime los resultados promedio de las pruebas y calcula la mejora porcentual
	private void printResults(String testName, long concurrentTime, long sequentialTime) {
		System.out.println("\n" + testName);
		System.out.println("---------------------------------");
		System.out.printf("Versión concurrente: %d ms%n", concurrentTime);
		System.out.printf("Versión secuencial:  %d ms%n", sequentialTime);
		System.out.printf("Diferencia:          %d ms%n", (sequentialTime - concurrentTime));
		System.out.printf("Mejora:              %.2f%%%n",
				((double) (sequentialTime - concurrentTime) / sequentialTime) * 100);

		if (concurrentTime < sequentialTime) {
			System.out.println("✓ La versión concurrente fue más rápida");
		} else {
			System.out.println("× La versión secuencial fue más rápida");
		}
		System.out.println("---------------------------------");
	}

	// Método estático para preguntar al usuario si desea ejecutar las pruebas de
	// rendimiento
	public static void runPerformanceTests() {
		Scanner scanner = new Scanner(System.in);
		System.out.println("\n¿Desea ejecutar pruebas de rendimiento? (s/n)");
		String respuesta = scanner.nextLine().trim().toLowerCase();

		if (respuesta.equals("s")) {
			runAllTests(); // Llama al método estático
		}
		scanner.close();
	}
}