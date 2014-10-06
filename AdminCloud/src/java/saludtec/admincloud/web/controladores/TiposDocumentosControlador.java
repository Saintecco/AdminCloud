/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package saludtec.admincloud.web.controladores;

import java.util.List;
import javax.ejb.EJB;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import saludtec.admincloud.ejb.crud.TiposDocumentosEjb;
import saludtec.admincloud.ejb.entidades.TiposDeDocumentos;

/**
 *
 * @author saintec
 */
public class TiposDocumentosControlador {

    @EJB
    TiposDocumentosEjb ejbTipoDocumento;

    public JSONArray listar(Integer idClinica) {
        List<TiposDeDocumentos> tiposDocumentos = ejbTipoDocumento.listar(idClinica);
        JSONArray array = new JSONArray();
        JSONObject obj = null;
        if (tiposDocumentos != null) {
            for (TiposDeDocumentos tipoDocumento : tiposDocumentos) {
                obj = new JSONObject();
                obj.put("idTipoDocumento", tipoDocumento.getIdTipoDeDocumento());
                obj.put("tipoDocumento", tipoDocumento.getTipoDeDocumento());
                array.add(obj);
            }
        }
        return array;
    }

    public String litarr() {
        return ejbTipoDocumento.listarr();
    }
}
