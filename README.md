# Gestión de Contactos - Aplicación Java  
## Descripción del Proyecto ##  
Este proyecto es una aplicación de Gestión de Contactos desarrollada en Java, migrada y evolucionada para usar Maven como sistema de construcción y gestión de dependencias. La aplicación permite administrar contactos con funcionalidades de agregar, modificar, eliminar, importar y exportar contactos en formatos JSON y CSV. Además, cuenta con una interfaz gráfica moderna y personalizable gracias a FlatLaf, y presenta estadísticas visuales mediante gráficos generados con JFreeChart.  
El objetivo principal es ofrecer una aplicación robusta, eficiente y segura que aproveche la programación concurrente para mejorar la experiencia del usuario y la gestión avanzada de dependencias para garantizar estabilidad y seguridad.  
## Funcionalidades principales ##  
- Gestión completa de contactos: alta, baja, modificación.  
- Importación y exportación de contactos en formato JSON y CSV.  
- Interfaz gráfica moderna con soporte para múltiples temas (claro, oscuro, Intellij, Darcula).  
- Visualización de estadísticas mediante gráficos.
- Busqueda y filtrado eficiente con programación concurrente para evitar bloqueos en la UI.
- Internacionalización con soporte para varios idiomas (español, inglés y francés).  
- Auditoría y gestión de dependencias para mantener la seguridad y estabilidad del proyecto.
## Tecnologías y bibliotecas utilizadas ##  
- **Java 17:** Lenguaje de programación.
- **Maven:** Gestión de proyecto y dependencias.
- **FlatLaf:** Temas modernos para interfaz gráfica.
- **Jackson:** Serialización y deserialización JSON.
- **JFreeChart:** Generación de gráficos estadísticos.
- **OWASP Dependency-Check:** Auditoría de seguridad de dependencias.
- **SwingWorker y ExecutorService:** Programación concurrente para tareas en background.
## Gestión avanzada de dependencias ##  
- Las dependencias están definidas con versiones estables en el archivo pom.xml.
- Se excluyeron dependencias transitivas innecesarias, como jackson.module-jaxb-annotations.
- Se realizó auditoría de seguridad con OWASP Dependency-Check, detectando y mitigando vulnerabilidades en jackson-databind actualizando a la versión 2.16.0.
- No se encontraron dependencias transitivas innecesarias adicionales tras analizar el árbol de dependencias (mvn dependency:tree).
## Auditoría de dependencias ##  
Se utilizó la herramienta OWASP Dependency-Check para analizar vulnerabilidades conocidas en las bibliotecas usadas. El reporte identificó:
|Dependencia      |Versión | Vulnerabilidad CVE        | Severidad| Acción Tomada          |  
|-----------------|--------|---------------------------|----------|------------------------|  
|jackson-databind |2.15.2  |CVE-2023-35116             |Media     |Actualizada a 2.16.0    |  
|jfreechart       |1.5.4   |CVE-2023-52070 (disputada) |Alta      |Documentada, riesgo bajo|  

El reporte completo se encuentra en el anexo dependency-check-report.html.

## Instrucciones para Clonar y Ejecutar el Proyecto Maven ##
**1.** Asegurate de tener Git instalado en tu sistema. Puedes verificarlo abriendo una terminal y ejecutando el siguiente comando:
     
	 git -- version
Si git está instalado, verás la versión correspondiente. Si no está instalado, descárgalo e instálalo desde https://git-scm.com/.    
**2.** Navega a la carpeta donde deseas clonar el proyecto.  
**3.** Haz clic derecho en la carpeta y selecciona "Open Git Bash Here". Esto abrirá una terminal de Git Bash en la ubicación seleccionada.  
**4.** Ejecuta el siguiente comando:
    
	git clone https://github.com/DarwinMendoza7/GestionDeContactos.git
**5.** Para importar el proyecto a Eclipse haz lo siguiente:
- Abre Eclipse.
- Selecciona File luego Import y luego Existing Projects Into Workspace y pulsa en Next.
- Navega hasta la carpeta donde clonaste el proyecto (debe contener el archivo pom.xml) y haz clic en Finish.  

**6.** En la vista de Proyecto, encuentra la clase Principal que está en el paquete main, haz clic derecho sobre la clase y selecciona Run As y luego Java Application.

**Para clonar directamente desde Eclipse sigue estos pasos:**

**1.** Inicia Eclipse.  
**2.** Ve al menú File y selecciona la opción Import. Luego expande la carpeta Git y selecciona Projects from Git. A continuación, elige Clone URI y haz clic en Next.  
**3.** En el campo URI pega el enlace del repositorio que vas a clonar: https://github.com/DarwinMendoza7/GestionDeContactos.git y completa el campo de Authentication con tus datos y haz clic en Next.  
**4.** En la selección de ramas (Branch Selection), marca la rama main o master según corresponda y haz click en Next.  
**5.** En local Destination, selecciona la carpeta donde deseas clonar el proyecto y asigna un nombre al proyecto en el campo correspondiente. Luego haz clic en Next.  
**6.** En la ventana Select a wizard to use for importing projects, selecciona Import using the New Project wizard y haz clic en Finish.  
**7.** Se abrirá una ventana donde debes seleccionar el tipo de proyecto, elige Java Project en la carpeta de Java.  
**8.** Aparecerá una ventana para crear el nuevo proyecto. Ingresa el nombre del proyecto que desees, desmarca la opción que dice Use default location, y escoge la carpeta donde se clonó el proyecto. Luego haz clic en Finish.  
**9.** Por último, navega a la clase Principal que está en el paquete main, haz clic derecho sobre ella, selecciona Run As, y luego elige Java Application.  
