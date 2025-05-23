package com.gestioncontactos.controlador;

import java.util.List;
import javax.swing.SwingUtilities;
import com.gestioncontactos.modelo.Persona;

//Clase que valida si un contacto ya existe en la lista, comparando nombre, teléfono o email
public class ValidadorContactos extends Thread {

	private List<Persona> contactos; //Lista de contactos existentes
	private String nombre; //Nombre a validar
	private String telefono; //Teléfono a validar
	private String email; //Email a validar
	private ValidadorCallback callback; //Callback para notificar el resultado
	//Interfaz de callback para noificación asíncrona del resultado de la validación
	public interface ValidadorCallback {
		
		void onValidacionCompletada(boolean existeDuplicado);
	}
	
	public ValidadorContactos(List<Persona> contactos, String nombre, String telefono, String email, ValidadorCallback callback) {
		this.contactos = contactos;
		this.nombre = nombre;
		this.telefono = telefono;
		this.email = email;
		this.callback = callback;		
	}
	//Método principal del hilo
	@Override
	public void run() {
		boolean existeDuplicado = false;
		
		//Validar si el contacto ya existe (comparando nombre, teléfono y email)
		for(Persona contacto: contactos) {
			if(contacto.getNombre().equalsIgnoreCase(nombre) || contacto.getTelefono().equals(telefono) || contacto.getEmail().equalsIgnoreCase(email)) {
				existeDuplicado = true;
				break;
			}
		}
		
		final boolean resultado = existeDuplicado;
		
		//Ejecutar el calback en el hilo de EDT (Event Dispatch Thread) para actualizar la interfaz de forma segura
		SwingUtilities.invokeLater(() -> {
			callback.onValidacionCompletada(resultado);
		});
	}
}
