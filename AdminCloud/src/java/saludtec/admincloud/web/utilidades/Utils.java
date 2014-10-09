/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package saludtec.admincloud.web.utilidades;

import java.sql.Timestamp;
import java.util.Date;

/**
 *
 * @author saintec
 */
public class Utils {
    
    public static Date fecha(){
        Date calendario = new Date();
        Date fecha = new Timestamp(calendario.getTime());
        return fecha;
    }
    
}
