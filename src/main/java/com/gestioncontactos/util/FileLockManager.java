package com.gestioncontactos.util;

import java.io.*;
import java.nio.channels.FileLock;
import java.nio.file.*;
import java.util.*;
/*Clase utilitaria para gestionar bloqueos (locks) de contactos a través de  archivos de lock en el sistema de archivos.
Esto permite que múltiples procesos o instancias de la aplicación puedan coordinar el acceso exclusivo a un recurso (contacto) específico*/
public class FileLockManager {
    private static final Map<String, FileLock> locks = new HashMap<>(); //MApa que asocia el nombre del contacto con su FileLock activo
    private static final Path LOCK_DIR = Paths.get(System.getProperty("user.home"), "contact_locks"); //Directorio donde se almacenan los archivos de lock

    static {
        // Limpiar locks antiguos al iniciar
        try {
            Files.createDirectories(LOCK_DIR);
            Files.list(LOCK_DIR)
                 .filter(p -> p.toString().endsWith(".lock"))
                 .forEach(p -> {
                     try { Files.delete(p); } 
                     catch (IOException ignored) {}
                 });
        } catch (IOException e) {
            System.err.println("Error limpiando locks antiguos: " + e.getMessage());
        }
    }
    //Intenta adquirir un lock de archivo para el contacto especificado
    public static synchronized boolean tryLock(String contactName) {
        try {
            Path lockFile = LOCK_DIR.resolve(contactName + ".lock");
            RandomAccessFile file = new RandomAccessFile(lockFile.toFile(), "rw");
            FileLock lock = file.getChannel().tryLock();
            
            if (lock != null) {
                locks.put(contactName, lock);
                return true;
            }
            return false;
        } catch (IOException e) {
            return false;
        }
    }
    //Libera el lock de archivo asociado al contacto
    public static synchronized void unlock(String contactName) {
        FileLock lock = locks.remove(contactName);
        if (lock != null) {
            try {
                lock.release();
                lock.channel().close();
            } catch (IOException ignored) {}
        }
    }
}
