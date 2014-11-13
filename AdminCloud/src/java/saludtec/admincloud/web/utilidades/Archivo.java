/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package saludtec.admincloud.web.utilidades;

import java.io.File;

/**
 *
 * @author saintec
 */
public class Archivo {

    private final File rutaArchivos;
    private final File rutaClinicas;
    private final File rutaTemporal;
    private final File clinica;
    private final File rutaPacientes;
    private final File paciente;
    private final File perfilPaciente;
    private final File galeriaPaciente;
    private final File anexosPaciente;
    private final String carpetaClinica;
    private final String carpetaPaciente;

    public Archivo(String idClinica, String idPaciente) {
        carpetaClinica = idClinica;
        carpetaPaciente = idPaciente;
        rutaArchivos = new File("/var/www/AdminCloudData");
        rutaClinicas = new File((rutaArchivos.getPath()) + "/clinicas");
        rutaTemporal = new File((rutaArchivos.getPath()) + "/temp");
        clinica = new File(rutaClinicas.getPath() + "/" + carpetaClinica);
        rutaPacientes = new File(clinica.getPath() + "/pacientes");
        paciente = new File(rutaPacientes.getPath() + "/" + carpetaPaciente);
        perfilPaciente = new File(paciente.getPath() + "/perfil");
        galeriaPaciente = new File(paciente.getPath() + "/galeria");
        anexosPaciente = new File(paciente.getPath() + "/anexos");
    }

    public Boolean crearDirectorioClinica() {
        Boolean ok = false;
        if (carpetaClinica != null) {
            while (!ok) {
                if (rutaArchivos.exists()) {
                    if (!rutaTemporal.exists()) {
                        rutaTemporal.mkdir();
                    }
                    if (rutaClinicas.exists()) {
                        if (clinica.exists()) {
                            if (!rutaPacientes.exists()) {
                                if (rutaPacientes.mkdir()) {
                                    ok = true;
                                    System.out.println("Directorio de la clinica " + clinica.getName() + " creado");
                                }
                            }
                        } else {
                            clinica.mkdir();
                        }
                    } else {
                        rutaClinicas.mkdir();
                    }
                } else {
                    rutaArchivos.mkdir();
                }
            }
        } else {
            System.out.println("El nombre de la clinica no puede ser null");
        }
        return ok;
    }

    public Boolean crearDirectorioPaciente() {
        Boolean ok = false;
        if (carpetaPaciente != null || carpetaPaciente != null) {
            while (!ok) {
                if (rutaArchivos.exists()) {
                    if (rutaClinicas.exists()) {
                        if (clinica.exists()) {
                            if (rutaPacientes.exists()) {
                                if (paciente.exists()) {
                                    if (!perfilPaciente.exists()) {
                                        perfilPaciente.mkdir();
                                    }
                                    if (!galeriaPaciente.exists()) {
                                        galeriaPaciente.mkdir();
                                    }
                                    if (!anexosPaciente.exists()) {
                                        anexosPaciente.mkdir();
                                    }
                                    ok = true;
                                    System.out.println("Directorio del paciente " + paciente.getName() + " creado");

                                } else {
                                    paciente.mkdir();
                                }
                            } else {
                                rutaPacientes.mkdir();
                            }
                        } else {
                            clinica.mkdir();
                        }
                    } else {
                        rutaClinicas.mkdir();
                    }
                } else {
                    rutaArchivos.mkdir();
                }
            }
        } else {
            System.out.println("El nombre de la clinica o del paciente no pueden ser null");
        }
        return ok;
    }

}
