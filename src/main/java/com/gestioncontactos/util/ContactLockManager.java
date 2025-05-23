package com.gestioncontactos.util;

import java.io.File;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.io.PrintStream;

/**
 * Gestor de bloqueos para contactos usando bloqueos de archivos
 * que funcionan entre diferentes instancias de la aplicación
 */
public class ContactLockManager {
    private static ContactLockManager instance;
    
    // Stream para logging (por defecto usa System.err)
    private static PrintStream logStream = System.err;
    
    // Directorio donde se guardarán los archivos de bloqueo
    private static final String LOCK_DIR = System.getProperty("user.home") + File.separator + ".contactapp_locks";
    
    // Mapa para mantener referencia a los canales y bloqueos activos
    private final Map<String, LockInfo> activeLocks = new HashMap<>();
    
    private ContactLockManager() {
        // Crear directorio de bloqueos si no existe
        try {
            Files.createDirectories(Paths.get(LOCK_DIR));
        } catch (Exception e) {
            logError("No se pudo crear el directorio de bloqueos", e);
        }
    }
    
    public static synchronized ContactLockManager getInstance() {
        if (instance == null) {
            instance = new ContactLockManager();
        }
        return instance;
    }
    
    /**
     * Establece un stream personalizado para logging
     */
    public static void setLogStream(PrintStream stream) {
        if (stream != null) {
            logStream = stream;
        }
    }
    
    /**
     * Método simple para logging de errores
     */
    private void logError(String message, Exception e) {
        logStream.println("[ERROR] " + message);
        if (e != null) {
            e.printStackTrace(logStream);
        }
    }
    
    /**
     * Método simple para logging de advertencias
     */
    private void logWarning(String message, Exception e) {
        logStream.println("[WARNING] " + message);
        if (e != null) {
            e.printStackTrace(logStream);
        }
    }
    
    /**
     * Intenta adquirir un bloqueo para el contacto especificado
     * @param contactName Nombre del contacto a bloquear
     * @return true si se adquirió el bloqueo, false en caso contrario
     */
    public boolean tryLock(String contactName) {
        if (contactName == null || contactName.isEmpty()) {
            return false;
        }
        
        // Si ya tenemos el bloqueo, retornar verdadero
        if (hasLock(contactName)) {
            return true;
        }
        
        // Nombre de archivo seguro para el bloqueo
        String safeName = contactName.replaceAll("[^a-zA-Z0-9]", "_");
        Path lockFile = Paths.get(LOCK_DIR, safeName + ".lock");
        
        try {
            // Asegurar que el archivo de bloqueo existe
            if (!Files.exists(lockFile)) {
                Files.createFile(lockFile);
            }
            
            // Abrir archivo para bloqueo
            RandomAccessFile raf = new RandomAccessFile(lockFile.toFile(), "rw");
            FileChannel channel = raf.getChannel();
            
            // Intentar adquirir bloqueo no exclusivo
            FileLock lock = channel.tryLock();
            
            if (lock != null) {
                // Guardar referencia al bloqueo y canal
                activeLocks.put(contactName, new LockInfo(channel, lock, raf));
                return true;
            }
            
            // Si no se pudo obtener el bloqueo, cerrar el canal
            channel.close();
            raf.close();
            return false;
            
        } catch (Exception e) {
            logWarning("Error al intentar bloquear contacto: " + contactName, e);
            return false;
        }
    }
    
    /**
     * Verifica si ya tenemos un bloqueo para el contacto
     */
    public boolean hasLock(String contactName) {
        return activeLocks.containsKey(contactName);
    }
    
    /**
     * Libera el bloqueo para el contacto especificado
     */
    public void unlock(String contactName) {
        LockInfo lockInfo = activeLocks.remove(contactName);
        
        if (lockInfo != null) {
            try {
                // Liberar bloqueo y cerrar recursos
                if (lockInfo.lock != null && lockInfo.lock.isValid()) {
                    lockInfo.lock.release();
                }
                if (lockInfo.channel != null && lockInfo.channel.isOpen()) {
                    lockInfo.channel.close();
                }
                if (lockInfo.raf != null) {
                    lockInfo.raf.close();
                }
            } catch (Exception e) {
                logWarning("Error al liberar bloqueo para: " + contactName, e);
            }
        }
    }
    
    /**
     * Libera todos los bloqueos al cerrar la aplicación
     */
    public void releaseAllLocks() {
        for (String contactName : activeLocks.keySet().toArray(new String[0])) {
            unlock(contactName);
        }
    }
    
    /**
     * Clase auxiliar para mantener información de bloqueo
     */
    private static class LockInfo {
        final FileChannel channel;
        final FileLock lock;
        final RandomAccessFile raf;
        
        LockInfo(FileChannel channel, FileLock lock, RandomAccessFile raf) {
            this.channel = channel;
            this.lock = lock;
            this.raf = raf;
        }
    }
}
