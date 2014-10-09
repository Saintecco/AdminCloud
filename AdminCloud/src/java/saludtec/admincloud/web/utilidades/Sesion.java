/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package saludtec.admincloud.web.utilidades;

import javax.servlet.http.HttpServletRequest;
import saludtec.admincloud.ejb.entidades.Clinicas;

/**
 *
 * @author saintec
 */
public class Sesion {
    
     public Clinicas clinica (HttpServletRequest r){
        Clinicas clinica = new Clinicas();
        clinica.setIdClinica(1);
        return clinica;
    }
    
}
