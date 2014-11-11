/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package saludtec.admincloud.web.utilidades;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author saintec
 */
public class Calendario {

    public static Date fechaCompleta() {
        Date calendario = new Date();
        Date fecha = new Timestamp(calendario.getTime());
        return fecha;
    }

    public static Date fechaActual(Date fecha) {
        try {
            DateFormat df = new SimpleDateFormat("yyyy-mm-dd");
            String horaString = df.format(fecha);
            SimpleDateFormat sdfh = new SimpleDateFormat("yyyy-mm-dd");
            fecha = sdfh.parse(horaString);
        } catch (ParseException ex) {
            Logger.getLogger(Calendario.class.getName()).log(Level.SEVERE, null, ex);
        }
        return fecha;
    }

    public static Date horaActual(Date hora) {
        try {
            DateFormat df = new SimpleDateFormat("hh:mm:ss");
            String horaString = df.format(hora);
            SimpleDateFormat sdfh = new SimpleDateFormat("hh:mm:ss");
            hora = sdfh.parse(horaString);
        } catch (ParseException ex) {
            Logger.getLogger(Calendario.class.getName()).log(Level.SEVERE, null, ex);
        }
        return hora;
    }

    public static Date stringFecha(String fechaString) {
        Date fecha = new Date();
        try {
            fecha = new SimpleDateFormat("yyyy-mm-dd").parse(fechaString);
        } catch (ParseException ex) {
            Logger.getLogger(Calendario.class.getName()).log(Level.SEVERE, null, ex);
        }
        return fecha;
    }
    
    public static Date stringHora(String horaString) {
        Date hora = new Date();
        try {
            hora = new SimpleDateFormat("hh:mm:ss").parse(horaString);
        } catch (ParseException ex) {
            Logger.getLogger(Calendario.class.getName()).log(Level.SEVERE, null, ex);
        }
        return hora;
    }

}
